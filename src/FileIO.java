/**
 * @author Aditya Kulkarni [ak8650]
 */
public class FileIO {
    private byte[] data;
    private int length;

    public FileIO(byte[] data, int length) {
        this.data = data;
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
