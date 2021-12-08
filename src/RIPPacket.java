import java.io.Serializable;
import java.util.List;
/**
 * @author Aditya Kulkarni [ak8650]
 */

/**
 * POJO for a RIPPacket.
 */
public class RIPPacket implements Serializable {
    private int command;
    private int version;

    private List<RIPEntry> routeEntries;

    public RIPPacket(int command, int version, List<RIPEntry> routeEntries) {
        this.command = command;
        this.version = version;
        this.routeEntries = routeEntries;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<RIPEntry> getRouteEntries() {
        return routeEntries;
    }

    public void setRouteEntries(List<RIPEntry> routeEntries) {
        this.routeEntries = routeEntries;
    }

    @Override
    public String toString() {
        return command + version + routeEntries.toString();
    }
}
