import java.util.HashMap;
import java.util.Map;

/**
 * @author Aditya Kulkarni [ak8650]
 * Used for handling multiple concurrent connections.
 * Sends the packet to the appropriate ServerController aka. demultiplexer
 */
public class Server {
    private static final String LOG = Server.class.getSimpleName();
    private final Object lock = new Object();
    private String serverOwnAddress;
    // src IP to controller map
    private Map<String, ServerController> serverControllerMap;
    private static Server INSTANCE = new Server();

    private Server () {
        serverControllerMap = new HashMap<>();
    }

    public static Server getInstance() {
        return INSTANCE;
    }

    /**
     * Check the source IP for the packet and pass it to the appropriate ServerController.
     * @param srcIp
     * @param rdpPacket
     */
    public void demux(String srcIp, RDPPacket rdpPacket) {
        synchronized (lock) {
            if (serverControllerMap.containsKey(srcIp)) {
                serverControllerMap.get(srcIp).processPacket(rdpPacket);
            } else {
                ServerController serverController = new ServerController(srcIp);
                serverControllerMap.put(srcIp, serverController);
                serverControllerMap.get(srcIp).processPacket(rdpPacket);
            }
        }
    }

    // getters and setters

    public String getServerOwnAddress() {
        return serverOwnAddress;
    }

    public void setServerOwnAddress(String serverOwnAddress) {
        this.serverOwnAddress = serverOwnAddress;
    }

    public ServerController getController(String destIp) {
        return serverControllerMap.get(destIp);
    }
}
