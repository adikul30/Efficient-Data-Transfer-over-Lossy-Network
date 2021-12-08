import java.util.TimerTask;

/**
 * @author Aditya Kulkarni [ak8650]
 *
 * Once the timer at the client expires,
 *
 * 1. It removes the contiguous packets available from the left edge of the window.
 * 2. Marks the non-cumulative packets from the list of EACKs received.
 * 3. Creates new packets to send along with unacknowledged packets.
 */
public class ClientTimeoutTask extends TimerTask {

    private static final String LOG = ClientTimeoutTask.class.getSimpleName();

    public ClientTimeoutTask() {

    }

    @Override
    public void run() {
//        System.out.println(LOG + " Timer ran out");
        ClientController instance = ClientController.getInstance();
        instance.createNewPackets();
        instance.sendNewPackets();
        if (instance.state.isFINSet() && instance.isWindowEmpty()) {
            System.out.println(LOG + "FIN in thread");
            instance.sendFINPacket();
            instance.startTimer();
        } else {
            instance.startTimer();
        }

    }
}
