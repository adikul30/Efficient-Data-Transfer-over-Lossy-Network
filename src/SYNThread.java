import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

/**
 * The connection process is similar to TCP,
 *
 * 1. SYN from client to server
 * 2. SYN/ACK from server to client
 * 3. ACK (piggybacked with data) from client to server
 *
 * This thread handles lost SYN packets and retries until SYN-ACK is received.
 *
 * @author Aditya Kulkarni [ak8650]
 */
public class SYNThread implements Runnable {
    private static final String LOG = PacketSenderThread.class.getSimpleName();

    @Override
    public void run() {
        try {
            System.out.println("\n\nWaiting 15 secs for n/w to stabilise before sending SYN\n\n");
            Thread.sleep(15000);
            System.out.println("Hopefully the n/w is now stable!");
            ClientController clientController = ClientController.getInstance();
            while(!clientController.isSYNACKReceived()) {

                // Sending SYN
                RDPPacket rdpPacket = clientController.constructSYNPacket();
                clientController.initState(rdpPacket);

                byte[] bytes = PacketIO.encodeRDPPacket(rdpPacket);

                DatagramSocket socket = new DatagramSocket();
                String nextHopForDest = "";
                List<TableEntry> tableEntries = RoutingTable.getInstance().getTableEntries();
                for (int i = 0; i < tableEntries.size(); i++) {
                    if (tableEntries.get(i).getAddress().equals(clientController.getDestinationIpAddress())) {
                        nextHopForDest = tableEntries.get(i).getNextHop();
                    }
                }

                DatagramPacket udpPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(nextHopForDest), 8080);

                socket.send(udpPacket);
                System.out.println(LOG + " SYN sent");
                Thread.sleep(10000);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
