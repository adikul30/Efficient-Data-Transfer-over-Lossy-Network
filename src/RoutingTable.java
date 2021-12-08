import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import static java.lang.Math.min;

/**
 * @author Aditya Kulkarni [ak8650]
 */

public class RoutingTable {

    private static final String LOG = RoutingTable.class.getSimpleName();

    private static List<TableEntry> tableEntries;
    private static RoutingTable INSTANCE = new RoutingTable();
    private static String localhostAddress;
    private static String ownIpAddress;
    private static String routerName;
    private final Object lock = new Object();
    private Map<String, TimerTask> timerTaskMap;
    private static Timer timer;
    private static String NODE_TYPE;

    /**
     * Singleton class as there only needs to be one instance of the routing table.
     */
    private RoutingTable() {
        tableEntries = new ArrayList<>();
        timerTaskMap = new HashMap<>();
        timer = new Timer();
    }

    public static RoutingTable getInstance() {
        return INSTANCE;
    }

    /**
     * Takes in a type of packet (REQUEST, RESPONSE, TRIGGERED) and constructs a RIPPacket based on that.
     *
     * @param type: Constants.REQUEST | Constants.RESPONSE | Constants.TRIGGERED
     * @return RIPPacket
     */
    public RIPPacket tableToPacket(int type) {
        synchronized (lock) {
            List<RIPEntry> ripEntries = new ArrayList<>();
            switch (type) {
                case Constants.RESPONSE:
                    for (TableEntry entries : getTableEntries()) {
                        ripEntries.add(new RIPEntry(2, entries.getAddress(), "255.255.255.0", entries.getNextHop(), entries.getCost()));
                    }
                    return new RIPPacket(Constants.RESPONSE, 2, ripEntries);
                case Constants.TRIGGERED:
                    for (TableEntry entries : getTableEntries()) {
                        if (entries.isRouteChanged())
                            ripEntries.add(new RIPEntry(2, entries.getAddress(), "255.255.255.0", entries.getNextHop(), entries.getCost()));
                    }
                    return new RIPPacket(Constants.RESPONSE, 2, ripEntries);
                default:
                    ripEntries.add(new RIPEntry(0, ownIpAddress, "255.255.255.0", localhostAddress, 16));
                    return new RIPPacket(Constants.REQUEST, 2, ripEntries);
            }
        }
    }

    /**
     * Processes an incoming packet based on RFC 2453 Section: 3.9.2 Response Messages.
     * <p>
     * Poisoned Reverse is implemented by the receiver. If the nextHop of an entry is the receiver itself, that entry is ignored.
     *
     * @param ripPacket: RIPPacket received from a router.
     * @param address:   address of the router.
     * @throws IOException
     */
    public void packetToTable(RIPPacket ripPacket, InetAddress address) throws IOException {
        synchronized (lock) {
            String nextHop = address.toString().substring(1);
            boolean trigger = false;
            if (!nextHop.equals(localhostAddress)) {
//                System.out.println(LOG + " Someone else's packet, finally! " + ripPacket.getRouteEntries().size());
                for (RIPEntry entry : ripPacket.getRouteEntries()) {
                    String destAddress = entry.getIpAddress();
//                    System.out.println(LOG + " destAddress = " + destAddress);
                    boolean routeExists = false;
                    TableEntry matchedEntry = null;

                    if (entry.getNextHop().equals(localhostAddress) || entry.getIpAddress().equals(ownIpAddress)) {
//                        System.out.println(LOG + " route passing through me! Ignoring");
                        continue;
                    }

                    for (TableEntry tableEntry : getTableEntries()) {
                        if (tableEntry.getAddress().equals(destAddress)) {
                            matchedEntry = tableEntry;
//                            System.out.println(LOG + " Route exists");
                            routeExists = true;
                            break;
                        }
                    }
                    int newMetric = min(1 + entry.getMetric(), 16);
                    if (routeExists) {
                        // conditions from RFC page 27-28
////                        System.out.println(LOG + " matchedEntry.getNextHop() = " + matchedEntry.getNextHop());
////                        System.out.println(LOG + " nextHop = " + nextHop);
////                        System.out.println(LOG + " matchedEntry.getCost() = " + matchedEntry.getCost());
////                        System.out.println(LOG + " New metric = " + newMetric);
                        if (matchedEntry.getNextHop().equals(nextHop) && matchedEntry.getCost() != newMetric) {
                            // Update existing entry
                            trigger = true;
//                            System.out.println(LOG + " change in metric, updating entry");
                            matchedEntry.setCost(newMetric);
                            resetTimer(matchedEntry);
                        } else if (!matchedEntry.getNextHop().equals(nextHop) && newMetric < matchedEntry.getCost()) {
//                            System.out.println(LOG + " route with less cost from another router found");
                            // Remove old entry, put in new entry
                            trigger = true;
                            TableEntry newEntry = new TableEntry(entry.getIpAddress(), nextHop, newMetric, true);
                            tableEntries.remove(matchedEntry);
                            tableEntries.add(newEntry);
                            startTimer(newEntry);
                        } else if (matchedEntry.getNextHop().equals(nextHop) && newMetric == matchedEntry.getCost()) {
                            resetTimer(matchedEntry);
//                            System.out.println(LOG + " No change");
                        }
                    } else {
                        // TODO: 3/2/20 Check if metric is infinity(16) before adding
//                        System.out.println(LOG + " New route encountered");
                        trigger = true;
                        TableEntry newEntry = new TableEntry(entry.getIpAddress(), nextHop, newMetric, true);
                        tableEntries.add(newEntry);
                        startTimer(newEntry);
                    }
                }
                if (trigger) {
//                    System.out.println(LOG + " Sending Triggered update");
                    new Thread(new UdpMulticastSender(Constants.TRIGGERED)).start();
                }
            } else {
//                System.out.println(LOG + " Own packet ignored");
            }
        }
        printTable();
    }

    /**
     * A router's own entry is added at start-up.
     *
     * @param localAddress: self ip address of the form (172.18.0.x)
     * @param ipAddress:    address of the internal pod network of the form (10.0.x.0/24)
     * @param name:         name of the router (an integer) given as an argument to the docker run command.
     */
    public void addOwnEntry(String localAddress, String ipAddress, String name) {
        localhostAddress = localAddress;
        ownIpAddress = ipAddress;
        routerName = name;
        tableEntries.add(new TableEntry(ownIpAddress, localhostAddress, 0, true));
        printTable();
    }

    /**
     * Resets flags after a triggered update has been sent.
     */
    public void resetFlags() {
        for (TableEntry entry : getTableEntries()) {
            entry.setRouteChanged(false);
        }
    }

    /**
     * Prints the table after a packet is processed.
     */
    private void printTable() {
//        System.out.println(LOG + "Printing table for Router : " + routerName);
        System.out.println("\n************************************************************\n");
//        System.out.println("Address\t Next Hop\t Cost\n");
        for (TableEntry entry : getTableEntries()) {
            System.out.println(entry.getAddress() + "\t " + entry.getNextHop() + "\t " + entry.getCost());
        }
    }

    /**
     * Starts a timer for an entry in the RoutingTable.
     * Schedules the task after an delay of 10 seconds.
     * The task itself only sets the cost of the entry as Infinite.
     *
     * @param entry: TableEntry
     */
    private void startTimer(TableEntry entry) {
        TimeoutTask timeoutTask = new TimeoutTask(entry);
        timerTaskMap.put(entry.getAddress(), timeoutTask);
        timer.schedule(timeoutTask, 10 * 1000);
    }

    /**
     * Resets the timer for an entry in the RoutingTable.
     *
     * @param entry: TableEntry
     */
    private void resetTimer(TableEntry entry) {
        TimerTask timerTask = timerTaskMap.get(entry.getAddress());
        timerTask.cancel();
        startTimer(entry);
    }

    // getters and setters

    public List<TableEntry> getTableEntries() {
        return tableEntries;
    }

    public String getOwnIpAddress() {
        return ownIpAddress;
    }

    public String getLocalhostAddress() {
        return localhostAddress;
    }

    public String getNodeType() {
        return NODE_TYPE;
    }

    public void setNodeType(String nodeType) {
        NODE_TYPE = nodeType;
    }

}
