/**
 * @author Aditya Kulkarni [ak8650]
 * POJO for an entry in the table.
 */
public class TableEntry {

    private String address;
    private String nextHop;
    private int cost;
    private boolean routeChanged;

    public TableEntry(String address, String nextHop, int cost, boolean routeChanged) {
        this.address = address;
        this.nextHop = nextHop;
        this.cost = cost;
        this.routeChanged = routeChanged;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public boolean isRouteChanged() {
        return routeChanged;
    }

    public void setRouteChanged(boolean routeChanged) {
        this.routeChanged = routeChanged;
    }
}
