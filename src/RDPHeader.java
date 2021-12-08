/**
 * @author Aditya Kulkarni [ak8650]
 */

import java.util.List;

/**
 * @author Aditya Kulkarni [ak8650]
 * RDPHeader POJO class
 */
public class RDPHeader {
    private boolean SYN, ACK, EAK, RST, NUL, FIN;
    private int srcPort, destPort, seqNo, ackNo;
    private int dataLen;
    private String srcIp, destIp;
    private byte[] variableHeader;
    private List<Integer> EACKs;
    private int maxNoOutstandingSegments, maxSegmentSize;

    public RDPHeader(boolean SYN, boolean ACK, boolean EAK, boolean RST, boolean NUL, boolean FIN, int srcPort, int destPort, int dataLen, int seqNo, int ackNo, String srcIp, String destIp, byte[] variableHeader) {
        this.SYN = SYN;
        this.ACK = ACK;
        this.EAK = EAK;
        this.RST = RST;
        this.NUL = NUL;
        this.FIN = FIN;
        this.srcPort = srcPort;
        this.destPort = destPort;
        this.dataLen = dataLen;
        this.seqNo = seqNo;
        this.ackNo = ackNo;
        this.srcIp = srcIp;
        this.destIp = destIp;
        this.variableHeader = variableHeader;
    }

    public byte[] getVariableHeader() {
        return variableHeader;
    }

    public void setVariableHeader(byte[] variableHeader) {
        this.variableHeader = variableHeader;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public boolean isFIN() {
        return FIN;
    }

    public void setFIN(boolean FIN) {
        this.FIN = FIN;
    }

    public boolean isSYN() {
        return SYN;
    }

    public void setSYN(boolean SYN) {
        this.SYN = SYN;
    }

    public boolean isACK() {
        return ACK;
    }

    public void setACK(boolean ACK) {
        this.ACK = ACK;
    }

    public boolean isEAK() {
        return EAK;
    }

    public void setEAK(boolean EAK) {
        this.EAK = EAK;
    }

    public boolean isRST() {
        return RST;
    }

    public void setRST(boolean RST) {
        this.RST = RST;
    }

    public boolean isNUL() {
        return NUL;
    }

    public void setNUL(boolean NUL) {
        this.NUL = NUL;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public int getDestPort() {
        return destPort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public int getAckNo() {
        return ackNo;
    }

    public void setAckNo(int ackNo) {
        this.ackNo = ackNo;
    }

    public List<Integer> getEACKs() {
        return EACKs;
    }

    public void setEACKs(List<Integer> EACKs) {
        this.EACKs = EACKs;
    }

    public int getMaxNoOutstandingSegments() {
        return maxNoOutstandingSegments;
    }

    public void setMaxNoOutstandingSegments(int maxNoOutstandingSegments) {
        this.maxNoOutstandingSegments = maxNoOutstandingSegments;
    }

    public int getMaxSegmentSize() {
        return maxSegmentSize;
    }

    public void setMaxSegmentSize(int maxSegmentSize) {
        this.maxSegmentSize = maxSegmentSize;
    }
}
