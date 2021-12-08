import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Aditya Kulkarni [ak8650]
 *
 * Util Thread to send a RDPPacket to an ipAddress : port
 */
public class PacketSenderThread implements Runnable {
    private static final String LOG = PacketSenderThread.class.getSimpleName();
    private RDPPacket rdpPacket;
    private int mPort;
    private String sentFrom, ipAddress;

    public PacketSenderThread(RDPPacket packet, String address, int port, String from) {
        rdpPacket = packet;
        mPort = port;
        sentFrom = from;
        ipAddress = address;
    }

    @Override
    public void run() {
        System.out.println(LOG + " packet with ack.no = " + rdpPacket.getRdpHeader().getAckNo() + " sent from " + sentFrom + ", sent to " + ipAddress);
        try {
            byte[] bytes = PacketIO.encodeRDPPacket(rdpPacket);
            DatagramSocket socket = new DatagramSocket();

            InetAddress group = InetAddress.getLocalHost();

            // Packet setup
            DatagramPacket rdpPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(ipAddress), mPort);

            // let 'er rip
            socket.send(rdpPacket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
