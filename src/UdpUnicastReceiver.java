import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


/**
 * @author Aditya Kulkarni [ak8650]
 * Receiver for RDPPacket.
 * Pass it to a new Handler thread.
 */
public class UdpUnicastReceiver implements Runnable {
    private static final String LOG = UdpUnicastReceiver.class.getSimpleName();
    @Override
    public void run() {
        System.out.println(LOG + " UdpUnicastReceiver started");
        DatagramSocket sock = null;
        try {
            sock = new DatagramSocket(8080);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while (true) {

            byte[] buffer = new byte[65535];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                sock.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            System.out.println("******************************************************************************");
//            System.out.println(LOG + " New data packet");
            RDPPacketHandler handler = new RDPPacketHandler(buffer);
            new Thread(handler).start();
        }
    }
}
