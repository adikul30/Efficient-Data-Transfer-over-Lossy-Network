/**
 * @author Aditya Kulkarni [ak8650]
 *
 * Data class for maintaining connection records for a single connection.
 */
public class ConnectionRecord {
    private String STATE;
    private int SND_NXT, SND_UNA, SND_MAX,
            RCV_CUR, RCV_MAX,
            SEG_SEQ, SEG_ACK, SEG_MAX, SEG_BMAX;
    private int leftEdgeSeqNo;
    private boolean isTimerScheduled = false;
    private int packetsSent;
    private String ipAddress;
    private boolean isFINSet;
    private int lastSeqNo;

    public ConnectionRecord() {

    }

    public boolean isFINSet() {
        return isFINSet;
    }

    public int getLastSeqNo() {
        return lastSeqNo;
    }

    public void setLastSeqNo(int lastSeqNo) {
        this.lastSeqNo = lastSeqNo;
    }

    public void setFINSet(boolean FINSet) {
        isFINSet = FINSet;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPacketsSent() {
        return packetsSent;
    }

    public void setPacketsSent(int packetsSent) {
        this.packetsSent = packetsSent;
    }

    public void incrPacketsSent() {
        this.packetsSent = this.packetsSent + 1;
    }

    public int getLeftEdgeSeqNo() {
        return leftEdgeSeqNo;
    }

    public void setLeftEdgeSeqNo(int leftEdgeSeqNo) {
        this.leftEdgeSeqNo = leftEdgeSeqNo;
    }

    public void incrLeftEdgeSeqNo() {
        this.leftEdgeSeqNo = this.leftEdgeSeqNo + 1;
    }

    public boolean isTimerScheduled() {
        return isTimerScheduled;
    }

    public void setTimerScheduled(boolean timerScheduled) {
        isTimerScheduled = timerScheduled;
    }

    public String getSTATE() {
        return STATE;
    }

    public void setSTATE(String STATE) {
        this.STATE = STATE;
    }

    public int getSND_NXT() {
        return SND_NXT;
    }

    public void setSND_NXT(int SND_NXT) {
        this.SND_NXT = SND_NXT;
    }

    public void incrSND_NXT() {
        this.SND_NXT = this.SND_NXT + 1;
    }

    public int getSND_UNA() {
        return SND_UNA;
    }

    public void setSND_UNA(int SND_UNA) {
        this.SND_UNA = SND_UNA;
    }

    public int getSND_MAX() {
        return SND_MAX;
    }

    public void setSND_MAX(int SND_MAX) {
        this.SND_MAX = SND_MAX;
    }

    public int getRCV_CUR() {
        return RCV_CUR;
    }

    public void setRCV_CUR(int RCV_CUR) {
        this.RCV_CUR = RCV_CUR;
    }

    public int getRCV_MAX() {
        return RCV_MAX;
    }

    public void setRCV_MAX(int RCV_MAX) {
        this.RCV_MAX = RCV_MAX;
    }

    public int getSEG_SEQ() {
        return SEG_SEQ;
    }

    public void setSEG_SEQ(int SEG_SEQ) {
        this.SEG_SEQ = SEG_SEQ;
    }

    public int getSEG_ACK() {
        return SEG_ACK;
    }

    public void setSEG_ACK(int SEG_ACK) {
        this.SEG_ACK = SEG_ACK;
    }

    public int getSEG_MAX() {
        return SEG_MAX;
    }

    public void setSEG_MAX(int SEG_MAX) {
        this.SEG_MAX = SEG_MAX;
    }

    public int getSEG_BMAX() {
        return SEG_BMAX;
    }

    public void setSEG_BMAX(int SEG_BMAX) {
        this.SEG_BMAX = SEG_BMAX;
    }
}
