import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Aditya Kulkarni [ak8650]
 * Class for handling transfer for a single connection.
 */
public class ClientController {

    private static final String LOG = ClientController.class.getSimpleName();
    private static final int WINDOW_SIZE = 10;
    private static Timer timer;
    private static ClientTimeoutTask clientTimeoutTask;
    private static ClientController INSTANCE = new ClientController();
    private String fileName, destinationIpAddress, ownIpAddress;
    ConnectionRecord state;
    private LinkedList<RDPPacket> sendingWindow;
    private boolean isSYNACKReceived;

    private ClientController() {
        this.sendingWindow = new LinkedList<>();
        for (int i = 0; i < WINDOW_SIZE; i++) {
            sendingWindow.add(null);
        }
        timer = new Timer();
        state = new ConnectionRecord();
    }

    public static ClientController getInstance() {
        return INSTANCE;
    }

    // window management

    /**
     * Process incoming packet from server.
     * @param rdpPacket: Packet could be a SYN-ACK or ACK / EACKs packet.
     * @throws IOException
     */
    public void processPacket(RDPPacket rdpPacket) throws IOException {
        printWindow("processPacket");
        RDPHeader rdpHeader = rdpPacket.getRdpHeader();
        if (rdpHeader.isSYN() && rdpHeader.isACK()) {
            System.out.println(LOG + " SYN-ACK received. Sending ACK. ");
            setSYNACKReceived(true);
            state.setRCV_CUR(rdpPacket.getRdpHeader().getSeqNo());
            createNewPackets();
            sendNewPackets();
            startTimer();
        } else if (rdpHeader.isACK()) {
            // TODO: 4/26/20 Latest change : Not stopping timer here

//            if (state.isTimerScheduled()) {
//                stopTimer();
//            }
            System.out.println(LOG + " ACK No." + rdpHeader.getAckNo() + " received from server. Processing... ");
            processACKs(rdpPacket);
/*            if (state.isFINSet() && isWindowEmpty()){
                System.out.println(LOG + "FIN in thread");
                sendFINPacket();
            }
            createNewPackets();
            sendNewPackets();*/
            // TODO: 4/26/20 Latest change : Not starting timer here
//            startTimer();

        }
    }

    /**
     * Process the ACK received from the server.
     * Shifts the left edge of the window.
     * Fills in the EACKs.
     * @param rdpPacket
     */
    private void processACKs(RDPPacket rdpPacket) {
        RDPHeader rdpHeader = rdpPacket.getRdpHeader();
        int receivedUpto = rdpHeader.getAckNo();
        List<Integer> EACKs = rdpHeader.getEACKs();
        for (Integer eak : EACKs) {
            System.out.println(eak);
        }
//        System.out.println("state.getLeftEdgeSeqNo() = " + state.getLeftEdgeSeqNo());
//        System.out.println("state.getSND_NXT() = " + state.getSND_NXT());
        ListIterator it = sendingWindow.listIterator();
        int count = 0;
        while (it.hasNext()) {
            Object next = it.next();
            if (state.getLeftEdgeSeqNo() < receivedUpto) {
                it.remove();
                state.incrLeftEdgeSeqNo();
                count++;
            } else if (next != null) {
                RDPPacket packet = (RDPPacket) next;
                int currSeqNo = packet.getRdpHeader().getSeqNo();
//                if (state.getLeftEdgeSeqNo() <= currSeqNo && currSeqNo < receivedUpto) {
//                System.out.println("getLeftEdgeSeqNo = " + state.getLeftEdgeSeqNo() + ", receivedUpto = " + receivedUpto);
                if (state.getLeftEdgeSeqNo() < receivedUpto) {
                    it.remove();
                    state.incrLeftEdgeSeqNo();
                    count++;
                } else if (EACKs.contains(currSeqNo)) {
                    try {
                        sendingWindow.set(currSeqNo - state.getLeftEdgeSeqNo(), null);
                    } catch (Exception e) {

                    }
                }
            }
        }
        while (count != 0) {
            sendingWindow.add(null);
            count--;
        }
        state.setLeftEdgeSeqNo(receivedUpto);
        printWindow("processACKs");
    }

    void initState(RDPPacket rdpPacket) {
        RDPHeader rdpHeader = rdpPacket.getRdpHeader();
        state.setRCV_MAX(rdpHeader.getMaxSegmentSize());
        state.setRCV_CUR(rdpHeader.getSeqNo());
        state.setSEG_MAX(rdpHeader.getMaxNoOutstandingSegments());
        state.setSEG_BMAX(rdpHeader.getMaxSegmentSize());
    }

    // construct packets

    /**
     * Creates new packets to be sent after receiving the latest ACKs / EACKs.
     */
    void createNewPackets() {
        int lastRem = 0;
        for (int i = 0; i < sendingWindow.size(); i++) {
            if (sendingWindow.get(i) != null) {
                lastRem = i;
            }
        }
        int leftEdge = state.getLeftEdgeSeqNo();
        for (int i = 0; i < sendingWindow.size(); i++) {
            if (sendingWindow.get(i) == null && (i > lastRem || isWindowEmpty()) && (i == state.getSND_NXT() - leftEdge)) {
                RDPPacket packet = createPacket();
                if (packet != null) {
                    state.incrPacketsSent();
                    sendingWindow.set(i, packet);
                    state.incrSND_NXT();
                } else {
                    state.setFINSet(true);
                    break;
                }
            }
        }
        printWindow("createNewPackets");
    }

    RDPPacket createPacket() {
        FileIO fileIO = readBytes();
        RDPHeader ackHeader = new RDPHeader(
                false, true, false, false, false, false,
                8080, 8080, fileIO.getLength(), state.getSND_NXT(), state.getRCV_CUR(),
                ownIpAddress, destinationIpAddress,
                null
        );
        if (fileIO.getLength() == 0) {
            return null;
        } else {
            RDPPacket ackPacket = new RDPPacket(ackHeader, fileIO.getData());
            return ackPacket;
        }
    }

    RDPPacket constructSYNPacket() {
        int clientSeqNo = new Random(3).nextInt(10);
        System.out.println(LOG + " clientSeqNo = " + clientSeqNo);
        state.setSND_NXT(clientSeqNo + 1);
        state.setLeftEdgeSeqNo(clientSeqNo + 1);
        RDPHeader rdpHeader = new RDPHeader(
                true, false, false, false, false, false,
                8080, 8080,0,
                clientSeqNo, 0,
                ownIpAddress, destinationIpAddress,
                null
        );
        rdpHeader.setMaxNoOutstandingSegments(10);
        rdpHeader.setMaxSegmentSize(50000);
        RDPPacket rdpPacket = new RDPPacket(rdpHeader, null);

        return rdpPacket;
    }

    RDPPacket constructFINPacket() {
        // TODO: 4/26/20 Sequence number 0 for FIN ?
        RDPHeader rdpHeader = new RDPHeader(
                false, false, false, false, false, true,
                8080, 8080,
                0, state.getSND_NXT(), 0,
                ownIpAddress, destinationIpAddress,
                null
        );
        RDPPacket rdpPacket = new RDPPacket(rdpHeader, null);

        return rdpPacket;
    }

    // send packets

    void sendFINPacket() {
        RDPPacket rdpPacket = constructFINPacket();
        String sendTo = getNextHop(destinationIpAddress);
        new Thread(new PacketSenderThread(rdpPacket, sendTo, 8080, "client")).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            System.exit(0);
            stopTimer();
        }
    }

    /**
     * Goes through the current window and sends each filled packet with a new thread.
     */
    void sendNewPackets() {
        System.out.println(LOG + " Sending new packets");
        for (int i = 0; i < sendingWindow.size(); i++) {
            RDPPacket rdpPacket = sendingWindow.get(i);
            if (rdpPacket != null) {
                String sendTo = getNextHop(destinationIpAddress);
                new Thread(new PacketSenderThread(rdpPacket, sendTo, 8080, "client")).start();
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Goes through the routing table and finds the next hop address for the destination.
     * @param destIpAddress
     * @return
     */
    private String getNextHop(String destIpAddress) {
        List<TableEntry> tableEntries = RoutingTable.getInstance().getTableEntries();
        System.out.println(LOG + "checking next hop from " + tableEntries.size() + " entries");
        for (int i = 0; i < tableEntries.size(); i++) {
            if (tableEntries.get(i).getAddress().equals(destIpAddress)) {
                return tableEntries.get(i).getNextHop();
            }
        }
        return null;
    }

    // file

    /**
     * Queries the state to check the number of bytes already sent.
     * @return
     */
    private long getBytesToSkip() {
        return state.getPacketsSent() * state.getSEG_BMAX();
    }

    /**
     * Reads the data to be sent in a single packet.
     * @return
     */
    private FileIO readBytes() {
        int read = -1;
        long bytesToSkip = getBytesToSkip();
        byte[] buffer = new byte[50000];
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            long skipped = fileInputStream.skip(bytesToSkip);
            read = fileInputStream.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (read == -1)
            return new FileIO(new byte[0], 0);
        return new FileIO(buffer, read);
    }

    // timer

    void startTimer() {
        clientTimeoutTask = new ClientTimeoutTask();
        timer.schedule(clientTimeoutTask, 8 * 1000);
        state.setTimerScheduled(true);
    }

    private void stopTimer() {
        clientTimeoutTask.cancel();
    }

    // util

    /**
     * Output the current state of the client window.
     * @param at: stage at which this method is called
     */
    private void printWindow(String at) {
        System.out.println("\nPrinting window " + at);
        for (int i = 0; i < sendingWindow.size(); i++) {
            RDPPacket rdpPacket = sendingWindow.get(i);
            if (rdpPacket != null) {
                System.out.print(rdpPacket.getRdpHeader().getSeqNo() + " ; ");
            } else {
                System.out.print("null ; ");
            }
        }
        System.out.println("\n");
    }

    /**
     * Checks if window is empty.
     * @return
     */
    boolean isWindowEmpty() {
        for (int i = 0; i < sendingWindow.size(); i++) {
            if (sendingWindow.get(i) != null) {
                return false;
            }
        }
        return true;
    }

    // getters and setters

    public boolean isSYNACKReceived() {
        return isSYNACKReceived;
    }

    public void setSYNACKReceived(boolean SYNACKReceived) {
        isSYNACKReceived = SYNACKReceived;
    }

    public String getOwnIpAddress() {
        return ownIpAddress;
    }

    public void setOwnIpAddress(String ownIpAddress) {
        this.ownIpAddress = ownIpAddress;
    }

    public String getDestinationIpAddress() {
        return destinationIpAddress;
    }

    public void setDestinationIpAddress(String destinationIpAddress) {
        this.destinationIpAddress = destinationIpAddress;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
