import java.io.Serializable;

/**
 * @author Aditya Kulkarni [ak8650]
 * RDPPacket POJO class
 */
public class RDPPacket {
    private RDPHeader rdpHeader;
    private byte[] data;

    public RDPPacket(RDPHeader rdpHeader, byte[] data) {
        this.rdpHeader = rdpHeader;
        this.data = data;
    }

    public RDPHeader getRdpHeader() {
        return rdpHeader;
    }

    public void setRdpHeader(RDPHeader rdpHeader) {
        this.rdpHeader = rdpHeader;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
