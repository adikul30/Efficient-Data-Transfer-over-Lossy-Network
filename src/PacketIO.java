import java.io.*;
import java.util.*;
/**
 * @author Aditya Kulkarni [ak8650]
 */

/**
 * Utility class to serialize and deserialize packets.
 */
public class PacketIO {

    private static StringBuilder sb = new StringBuilder();

    /**
     * Takes in a RDPPacket and encodes it into a byte array acc. to the format given in 'RDP Packet structure.pdf'
     * @param rdpPacket: Type = RDPPacket
     * @return byte array
     * @throws IOException
     */
    public static byte[] encodeRDPPacket(RDPPacket rdpPacket) throws IOException {
        byte[] buffer;
        BitSet flagsBitSet = new BitSet();
        RDPHeader rdpHeader = rdpPacket.getRdpHeader();
        flagsBitSet.set(0, rdpHeader.isSYN());
        flagsBitSet.set(1, rdpHeader.isACK());
        flagsBitSet.set(2, rdpHeader.isEAK());
        flagsBitSet.set(3, rdpHeader.isRST());
        flagsBitSet.set(4, rdpHeader.isNUL());
        flagsBitSet.set(5, rdpHeader.isFIN());
        flagsBitSet.set(6, false);
        flagsBitSet.set(7, false);
        byte[] flags = flagsBitSet.toByteArray();

        byte headerLen = 24;

        byte[] srcPort = new byte[2];
        byte[] destPort = new byte[2];
        byte[] dataLen = new byte[2];
        byte[] seqNo = new byte[4];
        byte[] ackNo = new byte[4];
        byte[] WINDOW_SIZE = new byte[2];
        byte[] SEGMENT_SIZE = new byte[2];
        byte[] EACKs = new byte[0];
        byte[] data = new byte[rdpHeader.getDataLen()];
        byte[] srcIp = new byte[4];
        byte[] destIp = new byte[4];

        srcPort[0] = (byte) (rdpHeader.getSrcPort() >> 8);
        srcPort[1] = (byte) (rdpHeader.getSrcPort() >> 0);

        destPort[0] = (byte) (rdpHeader.getDestPort() >> 8);
        destPort[1] = (byte) (rdpHeader.getDestPort() >> 0);

        dataLen[0] = (byte) (rdpHeader.getDataLen() >> 8);
        dataLen[1] = (byte) (rdpHeader.getDataLen() >> 0);

        seqNo[0] = (byte) (rdpHeader.getSeqNo() >> 24);
        seqNo[1] = (byte) (rdpHeader.getSeqNo() >> 16);
        seqNo[2] = (byte) (rdpHeader.getSeqNo() >> 8);
        seqNo[3] = (byte) (rdpHeader.getSeqNo() >> 0);

        ackNo[0] = (byte) (rdpHeader.getAckNo() >> 24);
        ackNo[1] = (byte) (rdpHeader.getAckNo() >> 16);
        ackNo[2] = (byte) (rdpHeader.getAckNo() >> 8);
        ackNo[3] = (byte) (rdpHeader.getAckNo() >> 0);

        String subnetSrcIp = rdpHeader.getSrcIp();
//        System.out.println(subnetSrcIp);
        String[] srcIpArray = subnetSrcIp.substring(0, subnetSrcIp.indexOf("/")).split("\\.");
        srcIp[0] = (byte) (Integer.parseInt(srcIpArray[0]));
        srcIp[1] = (byte) (Integer.parseInt(srcIpArray[1]));
        srcIp[2] = (byte) (Integer.parseInt(srcIpArray[2]));
        srcIp[3] = (byte) (Integer.parseInt(srcIpArray[3]));

        String subnetDestIp = rdpHeader.getDestIp();
//        System.out.println(subnetDestIp);
        String[] destIpArray = subnetDestIp.substring(0, subnetDestIp.indexOf("/")).split("\\.");
        destIp[0] = (byte) (Integer.parseInt(destIpArray[0]));
        destIp[1] = (byte) (Integer.parseInt(destIpArray[1]));
        destIp[2] = (byte) (Integer.parseInt(destIpArray[2]));
        destIp[3] = (byte) (Integer.parseInt(destIpArray[3]));

        if (rdpHeader.isSYN()) {
            // SYN + VARIABLE HEADER
            int maxNoOutstandingSegments = rdpHeader.getMaxNoOutstandingSegments();
            WINDOW_SIZE[0] = (byte) (maxNoOutstandingSegments >> 8);
            WINDOW_SIZE[1] = (byte) (maxNoOutstandingSegments >> 0);
            int maxSegmentSize = rdpHeader.getMaxSegmentSize();
            SEGMENT_SIZE[0] = (byte) (maxSegmentSize >> 8);
            SEGMENT_SIZE[1] = (byte) (maxSegmentSize >> 0);
            headerLen += 4;
        } else if (rdpHeader.isACK() && rdpHeader.isEAK()) {
            // ACK + EAK
            List<Integer> eacKs = rdpHeader.getEACKs();
            EACKs = new byte[eacKs.size() * 2];
            Iterator<Integer> iterator = eacKs.iterator();
            int count = 0;
            while (iterator.hasNext()) {
                int i = iterator.next();
                EACKs[count++] = (byte) (i >> 8);
                EACKs[count++] = (byte) (i >> 0);
            }
            headerLen += EACKs.length;
        } else if (rdpHeader.isACK()) {
            // ACK + DATA
            data = rdpPacket.getData();
        }

        try (ByteArrayOutputStream ops = new ByteArrayOutputStream()) {
            ops.write(flags);
            ops.write(headerLen);
            ops.write(srcPort);
            ops.write(destPort);
            ops.write(dataLen);
            ops.write(seqNo);
            ops.write(ackNo);
            ops.write(srcIp);
            ops.write(destIp);

            if (rdpHeader.isSYN()) {
                ops.write(WINDOW_SIZE);
                ops.write(SEGMENT_SIZE);
            }
            if (rdpHeader.isACK() && rdpHeader.isEAK()) {
                ops.write(EACKs);
            } else if (rdpHeader.isACK() && rdpHeader.getDataLen() > 0) {
                ops.write(data);
            }

            buffer = ops.toByteArray();
        }
        return buffer;
    }

    /**
     * Takes in a byte array and decodes it into a RDPPacket structure acc. to the format given in 'RDP Packet structure.pdf'
     * @param buffer: byte array
     * @return RDPPacket
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static RDPPacket decodeRDPPacket(byte[] buffer) {
//        System.out.println(buffer.length);
        int defaultHeaderLength = 24;

        byte[] flagArray = Arrays.copyOfRange(buffer, 0, 1);
        BitSet flagsBitSet = BitSet.valueOf(flagArray);
        boolean SYN = flagsBitSet.get(0);
        boolean ACK = flagsBitSet.get(1);
        boolean EAK = flagsBitSet.get(2);
        boolean RST = flagsBitSet.get(3);
        boolean NUL = flagsBitSet.get(4);
        boolean FIN = flagsBitSet.get(5);

        int headerLen = byteArrayToInt(buffer, 1, 2, 0, 0);
        int srcPort = byteArrayToInt(buffer, 2, 4, 0, 1);
        int desPort = byteArrayToInt(buffer, 4, 6, 0, 1);
        int dataLen = byteArrayToInt(buffer, 6, 8, 0, 1);
        int seqNo = byteArrayToInt(buffer, 8, 12, 0, 3);
        int ackNo = byteArrayToInt(buffer, 12, 16, 0, 3);

        byte[] srcIpArray = Arrays.copyOfRange(buffer, 16, 20);
        String srcIp = "";
        for (int i = 0; i < srcIpArray.length; i++) {
            srcIp = srcIp.concat(String.valueOf(Integer.parseInt(String.valueOf(srcIpArray[i])))).concat(".");
        }
        srcIp = srcIp.substring(0, srcIp.length() - 1).concat("/24");

        byte[] destIpArray = Arrays.copyOfRange(buffer, 20, 24);
        String destIp = "";
        for (int i = 0; i < srcIpArray.length; i++) {
            destIp = destIp.concat(String.valueOf(Integer.parseInt(String.valueOf(destIpArray[i])))).concat(".");
        }
        destIp = destIp.substring(0, destIp.length() - 1).concat("/24");

        byte[] data = new byte[dataLen];
        int maxSegSize, windowSize;

        RDPHeader rdpHeader = new RDPHeader(
                SYN, ACK, EAK, RST, NUL, FIN,
                srcPort, desPort,
                dataLen,
                seqNo, ackNo,
                srcIp, destIp,
                null
        );

        if (SYN) {
            // SYN + VARIABLE HEADER(window_size, max_segment_size)
            int varLen = headerLen - defaultHeaderLength;
            windowSize = byteArrayToInt(buffer, defaultHeaderLength, defaultHeaderLength + (varLen / 2), 0, 1);
            maxSegSize = byteArrayToInt(buffer, defaultHeaderLength + 2, defaultHeaderLength + 2 + (varLen / 2), 0, 1);
            rdpHeader.setMaxNoOutstandingSegments(windowSize);
            rdpHeader.setMaxSegmentSize(maxSegSize);
        } else if (ACK && EAK) {
            // ACK + VARIABLE HEADER(EAK)
            int varLen = headerLen - defaultHeaderLength;
//            System.out.println(varLen);
            byte[] varArgs = Arrays.copyOfRange(buffer, defaultHeaderLength, defaultHeaderLength + varLen);
//            System.out.println(varArgs.length);
            List<Integer> EACKs = new ArrayList<>();
            for (int i = 0; i < varLen / 2; i++) {
                EACKs.add(Integer.parseInt(hexToString(varArgs, 2 * i, (2 * i) + 1), 16));
            }
            rdpHeader.setEACKs(EACKs);
        } else if (ACK && dataLen > 0) {
            // ACK + DATA
            data = Arrays.copyOfRange(buffer, defaultHeaderLength, defaultHeaderLength + dataLen);
        }
        else if (FIN) {

        }

        RDPPacket rdpPacket = new RDPPacket(rdpHeader, data);
        return rdpPacket;
    }

    /**
     * Util method to convert portion of byte array to integer.
     * @param buffer
     * @param from
     * @param to
     * @param start
     * @param end
     * @return
     */
    private static int byteArrayToInt(byte[] buffer, int from, int to, int start, int end) {
        return Integer.parseInt(hexToString(Arrays.copyOfRange(buffer, from, to), start, end), 16);
    }

    /**
     * Util method to convert hexadecimal values in byte array to String
     * @param allBytes
     * @param start
     * @param end
     * @return
     */
    private static String hexToString(byte[] allBytes, int start, int end) {
        for (int i = start; i <= end; i++) {
            sb.append(String.format("%02x", allBytes[i]).replace(" ", ""));
        }
        String res = sb.toString();
        sb.setLength(0);
        return res;
    }

    /**
     * Takes in a RIPPacket and returns a byte array.
     * @param packet: Type = RIPPacket
     * @return byte array
     * @throws IOException
     */
    public static byte[] serializeRIPPacket(RIPPacket packet) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(packet);
            objectOutputStream.flush();
            byte[] buf = byteArrayOutputStream.toByteArray();
//            System.arraycopy(buf, 0, buffer, 0, buf.length);
            return buf;
        }
    }

    /**
     * Takes in a byte array and constructs a RIPPacket.
     * @param data: byte array
     * @return RIPPacket
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static RIPPacket deserializeRIPPacket(byte[] data) throws IOException, ClassNotFoundException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (RIPPacket) objectInputStream.readObject();
        }
    }

}
