import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Aditya Kulkarni [ak8650]
 *
 * Class for handling transfer for a single connection.
 * Multiple instances hanndle multiple simultaneous connections.
 */
public class ServerController {

    private static final String LOG = ServerController.class.getSimpleName();
    private static final int WINDOW_SIZE = 10;
    private Timer timer;
    private ServerTimeoutTask serverTimeoutTask;
    private File file;
    private String fileName, destIpAddress, ownAddress;
    private LinkedList<RDPPacket> receivingWindow;
    private ConnectionRecord state;
    private boolean isSYNReceived;

    public ServerController(String srcIp) {
        this.destIpAddress = srcIp;
        this.receivingWindow = new LinkedList<>();
        for (int i = 0; i < WINDOW_SIZE; i++) {
            receivingWindow.add(null);
        }
        timer = new Timer();
        state = new ConnectionRecord();
        this.ownAddress = Server.getInstance().getServerOwnAddress();
    }

    /**
     * Process incoming packet from client.
     * @param rdpPacket: Packet could be a SYN, DATA or FIN .
     * @throws IOException
     */
    public void processPacket(RDPPacket rdpPacket) {
        System.out.println(LOG + " new packet with seq no = " + rdpPacket.getRdpHeader().getSeqNo());
        printWindow("processPacket");
        RDPHeader rdpHeader = rdpPacket.getRdpHeader();
        if (rdpHeader.isSYN()) {
            System.out.println(LOG + " SYN received. Sending SYN-ACK. ");
            setSYNReceived(true);
            initState(rdpPacket);
            sendSYNACK(rdpPacket);
            state.setLeftEdgeSeqNo(rdpHeader.getSeqNo() + 1);
        } else if (rdpHeader.isACK()) {
//            System.out.println(LOG + " ACK + DATA");
            fillWindow(rdpPacket);
            if (!state.isTimerScheduled()) {
//                    state.setLeftEdgeSeqNo(rdpHeader.getSeqNo() + 1);
                System.out.println(LOG + " Timer started for the first time. ");
                startTimer();
                state.setTimerScheduled(true);
            }
        } else if (rdpHeader.isFIN()) {
            state.setFINSet(true);
            state.setLastSeqNo(rdpHeader.getSeqNo());
//            stopTimer();
//            System.exit(0);
        }
    }

    // state management

    private void initState(RDPPacket rdpPacket) {
        RDPHeader rdpHeader = rdpPacket.getRdpHeader();
        state.setRCV_MAX(rdpHeader.getMaxSegmentSize());
        state.setRCV_CUR(rdpHeader.getSeqNo());
        state.setSEG_MAX(rdpHeader.getMaxNoOutstandingSegments());
        state.setSEG_BMAX(rdpHeader.getMaxSegmentSize());
    }

    /**
     * The server buffers packets in the window as they arrive.
     * The server may receive packets out of order.
     * @param rdpPacket
     */
    private void fillWindow(RDPPacket rdpPacket) {
        int seqNo = rdpPacket.getRdpHeader().getSeqNo();
        int first = state.getLeftEdgeSeqNo();
        int last = first + state.getRCV_MAX() - 1;
        if (first <= seqNo && seqNo <= last) {
            try {
                receivingWindow.set(seqNo - first, rdpPacket);
            } catch (Exception e) {

            } finally {
//                System.out.println(LOG + " Filling window at " + (seqNo - first));
            }
        } else {
//            System.out.println(LOG + " Packet out of window range!");
        }
        printWindow("fillWindow");
    }

    // sending packets

    /**
     * Goes through the routing table and finds the next hop address for the destination.
     * @param destIpAddress
     * @return
     */
    private String getNextHop(String destIpAddress) {
        List<TableEntry> tableEntries = RoutingTable.getInstance().getTableEntries();
//        System.out.println(LOG + "checking next hop from " + tableEntries.size() + " entries");
        for (int i = 0; i < tableEntries.size(); i++) {
            if (tableEntries.get(i).getAddress().equals(destIpAddress)) {
                return tableEntries.get(i).getNextHop();
            }
        }
        return null;
    }

    /**
     * SYN/ACK from server to client.
     * @param rdpPacket
     */
    private void sendSYNACK(RDPPacket rdpPacket) {
        RDPHeader head = rdpPacket.getRdpHeader();
        int serverSeqNo = new Random(3).nextInt(10) + 10;
        System.out.println(LOG + " serverSeqNo = " + serverSeqNo);
        state.setSND_NXT(serverSeqNo + 1);
        RDPHeader ackHeader = new RDPHeader(
                true, true, false, false, false, false,
                head.getDestPort(), head.getSrcPort(), 0,
                serverSeqNo, head.getSeqNo(),
                ownAddress, destIpAddress,
                null
        );

        RDPPacket ackPacket = new RDPPacket(ackHeader, null);
        String nextHop = getNextHop(getDestIpAddress());
        System.out.println("Send to = " + nextHop);
        new Thread(new PacketSenderThread(ackPacket, nextHop, 8080, "server")).start();
    }

    /**
     * Creates a RDPPacket with ACK NO = left edge of its window = next anticipated packet (Cumulative ACK)
     * Includes a list of EACKs, which are the non-cumulative packets received.
     */
    void sendACK() {
        System.out.println(LOG + " Sending ACKs and / or EACKs. ");
        boolean isContiguous = true;
        int ackNo = state.getLeftEdgeSeqNo();
//        System.out.println(LOG + " left edge before sending ACK = " + ackNo);
        if (!state.isFINSet() || (state.isFINSet() && ackNo < state.getLastSeqNo())) {
            List<Integer> EACKs = new ArrayList<>();
            for (int i = 0; i < receivingWindow.size(); i++) {
                RDPPacket packet = receivingWindow.get(i);
                if (packet == null) {
                    isContiguous = false;
                } else {
                    if (isContiguous) {
                        ackNo = packet.getRdpHeader().getSeqNo();
                    } else {
                        EACKs.add(packet.getRdpHeader().getSeqNo());
                    }
                }
            }
//        System.out.println(LOG + " left edge before sending ACK = " + ackNo);
            RDPHeader ackHeader = new RDPHeader(
                    false, true, true, false, false, false,
                    8080, 8080, 0,
                    state.getSND_NXT(), ackNo,
                    ownAddress, destIpAddress,
                    null
            );
            ackHeader.setEACKs(EACKs);
            RDPPacket ackPacket = new RDPPacket(ackHeader, null);
            String nextHop = getNextHop(getDestIpAddress());
//            System.out.println("Send to = " + nextHop);
            new Thread(new PacketSenderThread(ackPacket, nextHop, 8080, "server")).start();
            resetTimer();
        }
        else if (state.isFINSet() && ackNo == state.getLastSeqNo()){
            stopTimer();
        }
    }

    // timer management

    private void startTimer() {
        serverTimeoutTask = new ServerTimeoutTask(destIpAddress);
        timer.schedule(serverTimeoutTask, 5 * 1000);
    }

    private void resetTimer() {
        try {
            serverTimeoutTask.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startTimer();
    }

    private void stopTimer() {
        try {
            serverTimeoutTask.cancel();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    // util methods

    /**
     * The server writes the contiguous packets available from the left edge of the window.
     * @throws IOException
     */
    void writeToFile() throws IOException {
        System.out.println(LOG + " Writing to file. ");
        // while entries in window are not null, write out
        int count = 0;
        ListIterator it = receivingWindow.listIterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next != null) {
                RDPPacket packet = (RDPPacket) next;
                String fileName = "/usr/src/myapp/" + destIpAddress.substring(0, destIpAddress.indexOf("/")) + ".jpeg";
//                System.out.println(fileName);
                try (FileOutputStream fos = new FileOutputStream(new File(fileName), true)) {
                    fos.write(packet.getData());
                }
                it.remove();
                state.incrLeftEdgeSeqNo();
//                System.out.println(state.getLeftEdgeSeqNo());
                count++;
            } else {
                break;
            }
        }
        // dequeue those entries, enqueue same amount of new null entries
        while (count != 0) {
            receivingWindow.add(null);
            count--;
        }
        printWindow("writeToFile");
    }

    /**
     * Output the current state of the client window.
     * @param at: stage at which this method is called
     */
    private void printWindow(String at) {
//        System.out.println("\nPrinting window " + at);
        for (int i = 0; i < receivingWindow.size(); i++) {
            RDPPacket rdpPacket = receivingWindow.get(i);
            if (rdpPacket != null) {
                System.out.print(rdpPacket.getRdpHeader().getSeqNo() + " ; ");
            } else {
                System.out.print("null ; ");
            }
        }
        System.out.println("\n");
    }

    // getters and setters

    public boolean isSYNReceived() {
        return isSYNReceived;
    }

    public void setSYNReceived(boolean SYNReceived) {
        isSYNReceived = SYNReceived;
    }

    public String getOwnAddress() {
        return ownAddress;
    }

    public void setOwnAddress(String ownAddress) {
        this.ownAddress = ownAddress;
    }

    public String getDestIpAddress() {
        return destIpAddress;
    }

    public void setDestIpAddress(String destIpAddress) {
        this.destIpAddress = destIpAddress;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        file = new File(fileName);
    }
}
