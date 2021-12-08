import java.io.Serializable;
/**
 * @author Aditya Kulkarni [ak8650]
 */

/**
 * POJO for a RIPEntry (RTE).
 */
public class RIPEntry implements Serializable {
    private int afi;
    private String ipAddress;
    private String subnetMask;
    private String nextHop;
    private int metric;

    public RIPEntry(int afi, String ipAddress, String subnetMask, String nextHop, int metric) {
        this.afi = afi;
        this.ipAddress = ipAddress;
        this.subnetMask = subnetMask;
        this.nextHop = nextHop;
        this.metric = metric;
    }

    public int getAfi() {
        return afi;
    }

    public void setAfi(int afi) {
        this.afi = afi;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    public int getMetric() {
        return metric;
    }

    public void setMetric(int metric) {
        this.metric = metric;
    }
}
