import java.util.TimerTask;
/**
 * @author Aditya Kulkarni [ak8650]
 */

/**
 * Custom TimerTask class used for ttl of an entry in the routing table.
 */
public class TimeoutTask extends TimerTask {

    private TableEntry entry;
    private static final String LOG = TimeoutTask.class.getSimpleName();

    /**
     * Takes a TableEntry as input.
     * @param tableEntry
     */
    public TimeoutTask(TableEntry tableEntry) {
        entry = tableEntry;
    }

    /**
     * If the run method is executed, it means that 10 seconds have passed and no update for this entry is received.
     * So, set the cost to Infinite (16).
     * And, send a TRIGGERED update.
     */
    @Override
    public void run() {
//        System.out.println(LOG + ": " + entry.getAddress() + " is timed out!");
        entry.setCost(16);
        entry.setRouteChanged(true);
        new Thread(new UdpMulticastSender(Constants.TRIGGERED)).start();
    }
}
