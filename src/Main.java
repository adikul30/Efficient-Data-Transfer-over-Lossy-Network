import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Entry point for the program.
 * Initializes different state variables according to the type of node, CLIENT or SERVER / ROUTER.
 */
class Main {
    private static final String LOG = Main.class.getSimpleName();
    static int nodeNum;

    public static void main(String[] args) throws UnknownHostException {
        String localAddress = InetAddress.getLocalHost().getHostAddress().trim();
        String fileName, ownAddress;

        try {
            nodeNum = Integer.parseInt(args[0]);
            System.out.println("I'm node " + nodeNum);
            RoutingTable instance = RoutingTable.getInstance();
            ownAddress = "10.0." + args[0] + ".0/24";
            instance.addOwnEntry(localAddress, ("10.0." + args[0] + ".0/24"), args[0]);

            // Starting Multicast Receiver
            System.out.println("Starting Multicast Receiver...");
            Thread client = new Thread(new UdpMulticastClient(63001, "230.230.230.230"));
            client.start();

            // Sending the initial REQUEST
            System.out.println("Broadcasting from: " + localAddress);
            new Thread(new UdpMulticastSender(Constants.REQUEST)).start();

            // Starting Unicast Data Receiver
            System.out.println("Starting Unicast Receiver...");
            Thread dataReceiver = new Thread(new UdpUnicastReceiver());
            dataReceiver.start();

            switch (args.length) {
                case 3:
                    System.out.println(LOG + " : I'm a CLIENT");
                    RoutingTable.getInstance().setNodeType(Constants.CLIENT);
                    String destIpAddress = args[1];
                    fileName = args[2];
                    System.out.println(LOG + " destIP in args = " + destIpAddress);
                    ClientController clientController = ClientController.getInstance();
                    clientController.setFileName(fileName);
                    clientController.setDestinationIpAddress(destIpAddress);
                    clientController.setOwnIpAddress(ownAddress);
                    new Thread(new SYNThread()).start();
                    break;
                case 1:
                    System.out.println(LOG + " : I'm a NODE");
                    RoutingTable.getInstance().setNodeType(Constants.SERVER);
                    Server server = Server.getInstance();
                    server.setServerOwnAddress(ownAddress);
                    break;
                default:
                    printArgs();
                    System.exit(0);
                    break;
            }

            // Starting Multicast Sender
            System.out.println("Starting Multicast Sender...");
            sendPeriodicUpdates();

            while (true) {
                Thread.sleep(1000);
            }
        }
        catch (ArrayIndexOutOfBoundsException e1){
            printArgs();
            System.exit(0);
        }
        catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    /**
     * Used for starting a new thread to send the periodic update. Interval is 5 seconds.
     *
     * @throws InterruptedException
     */
    private static void sendPeriodicUpdates() throws InterruptedException {
        while (true) {
            Thread sender = new Thread(new UdpMulticastSender(Constants.RESPONSE));
            sender.start();
//            System.out.println("Sending broadcast from " + nodeNum);
            Thread.sleep(5000);
        }
    }

    /**
     * Method to guide
     */
    private static void printArgs() {
        System.out.println("WRONG ARGS !!");
        System.out.println("CLIENT args : unique number, destination ip, filename.jpg");
        System.out.println("NODE args : unique number");
    }

}
