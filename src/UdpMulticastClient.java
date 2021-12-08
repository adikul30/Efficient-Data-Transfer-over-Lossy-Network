import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

// Code originally from:
//https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html
//
// edited by Sam Fryer.

/**
 * Class for receiving RIP packets.
 * For a REQUEST packet, send the entire routing table back.
 * For a RESPONSE packet, process the packet by passing it to the RoutingTable singleton instance.
 */
public class UdpMulticastClient implements Runnable {

    private static final String LOG = UdpMulticastClient.class.getSimpleName();

    public int port = 63001; // port to listen on
    public String broadcastAddress; // multicast address to listen on

    // standard constructor
    public UdpMulticastClient(int thePort, String broadcastIp) {
        port = thePort;
        broadcastAddress = broadcastIp;
    }

    // listens to the ipaddress and reports when a message arrived
    public void receiveUDPMessage() throws
            IOException {
        byte[] buffer = new byte[65535];

        // create and initialize the socket
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName(broadcastAddress);
        socket.joinGroup(group);

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // blocking call.... waits for next packet
                socket.receive(packet);

                RIPPacket ripPacket = PacketIO.deserializeRIPPacket(packet.getData());

                if (ripPacket.getCommand() == Constants.REQUEST) {
                    new Thread(new UdpMulticastSender(Constants.RESPONSE)).start();
                } else if (ripPacket.getCommand() == Constants.RESPONSE) {
                    RoutingTable.getInstance().packetToTable(ripPacket, packet.getAddress());
                }
                else {
                    System.out.println(LOG + " No more messages. Exiting : " + "msg");
                    break;
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        //close up ship
        socket.leaveGroup(group);
        socket.close();
    }

    // the thread runnable.  just starts listening.
    @Override
    public void run() {
        try {
            receiveUDPMessage();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
