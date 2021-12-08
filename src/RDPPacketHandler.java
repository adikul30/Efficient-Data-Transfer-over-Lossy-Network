import java.io.IOException;
import java.util.List;

/**
 * @author Aditya Kulkarni [ak8650]
 *
 * Is the packet intended for this node ?
 *
 * if yes, it's either a client or server
 *
 * else, route it to the next router
 *
 */
public class RDPPacketHandler implements Runnable {
    private static final String LOG = RDPPacketHandler.class.getSimpleName();

    private byte[] buffer;

    public RDPPacketHandler(byte[] data) {
        buffer = data;
    }

    @Override
    public void run() {
        try {
            RoutingTable routingTable = RoutingTable.getInstance();
            RDPPacket rdpPacket = PacketIO.decodeRDPPacket(buffer);
            RDPHeader rdpHeader = rdpPacket.getRdpHeader();
            String destIp = rdpHeader.getDestIp();
            String ownIpAddress = routingTable.getOwnIpAddress();
            String localhostAddress = routingTable.getLocalhostAddress();

            if (destIp.equals(ownIpAddress)) {
                switch (routingTable.getNodeType()) {
                    case Constants.CLIENT:
                        System.out.println(LOG + " : For clients eyes only");
                        ClientController.getInstance().processPacket(rdpPacket);
                        break;
                    case Constants.SERVER:
                        System.out.println(LOG + " : For servers eyes only");
                        Server.getInstance().demux(rdpHeader.getSrcIp(), rdpPacket);
                        break;
                }
            } else {
                // Route Forrest Route
//                System.out.println(LOG + " : Route Forrest Route");
                List<TableEntry> tableEntries = routingTable.getTableEntries();
                for (int i = 0; i < tableEntries.size(); i++) {
                    if (tableEntries.get(i).getAddress().equals(destIp)){
                        String nextHop = tableEntries.get(i).getNextHop();
                        new Thread(new PacketSenderThread(rdpPacket, nextHop, 8080, localhostAddress)).start();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
