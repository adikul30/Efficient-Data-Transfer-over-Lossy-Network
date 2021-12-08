import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// Code originally from:
//https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html
//
// edited by Sam Fryer.

/**
 * Class for broadcasting RIP packets.
 */
public class UdpMulticastSender implements Runnable {

    private static int port = 63001; // port to send on
    private static String broadcastAddress = "230.230.230.230"; // multicast address to send on
    private int node = 0; // the arbitrary node number of this executable
    private static int type;
    private static final String LOG = UdpMulticastSender.class.getSimpleName();
    // standard constructor
    public UdpMulticastSender(int typ) {
        type = typ;
    }

    // Send the UDP Multicast message
    public static void sendUdpMessage() throws IOException {
        // Socket setup
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(broadcastAddress);

        // Packet setup
        RIPPacket ripPacket = RoutingTable.getInstance().tableToPacket(type);

        byte[] bytes = PacketIO.serializeRIPPacket(ripPacket);
        DatagramPacket udpPacket = new DatagramPacket(bytes, bytes.length, group, port);

        // let 'er rip
        socket.send(udpPacket);
        if (type == Constants.TRIGGERED){
            RoutingTable.getInstance().resetFlags();
        }
        socket.close();
    }

    // the thread runnable.  Starts sending packets every 500ms.
    @Override
    public void run() {
//        while (true) {
            try {
//                // set our message as "Node 1" (or applicable number)
                sendUdpMessage();
//                Thread.sleep(5000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
//        }
    }
}
