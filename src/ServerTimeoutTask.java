import java.io.IOException;
import java.util.TimerTask;
/**
 * @author Aditya Kulkarni [ak8650]
 */

/**
 * Custom TimerTask class used for waiting a period of 10 secs before sending ACKs or EACKs.
 * Once the timer ends,
 *
 * 1. The server writes the contiguous packets available from the left edge of the window.
 * 2. Creates a RDPPacket with ACK NO = left edge of its window = next anticipated packet (Cumulative ACK)
 * 3. Includes a list of EACKs, which are the non-cumulative packets received.
 */
public class ServerTimeoutTask extends TimerTask {

    private static final String LOG = ServerTimeoutTask.class.getSimpleName();
    private String destIp;

    public ServerTimeoutTask() {

    }

    public ServerTimeoutTask(String destIpAddress) {
        this.destIp = destIpAddress;
    }

    /**
     * If the run method is executed, it means that 10 seconds have passed.
     * So, retransmit the packet.
     */
    @Override
    public void run() {
//        System.out.println(LOG + " Timer ran out");
        try {
            Server server = Server.getInstance();
            ServerController serverController = server.getController(destIp);
            serverController.writeToFile();
            serverController.sendACK();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
