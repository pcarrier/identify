package identify;

public class Bytes {
    static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static final String toHex(byte[] in) {
        final int length = in.length;
        final char[] out = new char[2 * length];
        for (int i = length - 1; i >= 0; i--) {
            final byte inByte = in[i];
            out[2 * i] = hexChars[(inByte & 0xf0) >> 4];
            out[2 * i + 1] = hexChars[inByte & 0xf];
        }
        return new String(out);
    }
}
