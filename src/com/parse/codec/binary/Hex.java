package com.parse.codec.binary;

import android.support.v4.view.MotionEventCompat;
import com.parse.codec.BinaryDecoder;
import com.parse.codec.BinaryEncoder;
import com.parse.codec.DecoderException;
import com.parse.codec.EncoderException;
import java.io.UnsupportedEncodingException;

public class Hex implements BinaryEncoder, BinaryDecoder {
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private static final char[] DIGITS_LOWER;
    private static final char[] DIGITS_UPPER;
    private final String charsetName;

    static {
        DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        DIGITS_UPPER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    }

    public static byte[] decodeHex(char[] data) throws DecoderException {
        int len = data.length;
        if ((len & 1) != 0) {
            throw new DecoderException("Odd number of characters.");
        }
        byte[] out = new byte[(len >> 1)];
        int i = 0;
        int j = 0;
        while (j < len) {
            j++;
            j++;
            out[i] = (byte) (((toDigit(data[j], j) << 4) | toDigit(data[j], j)) & MotionEventCompat.ACTION_MASK);
            i++;
        }
        return out;
    }

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[(l << 1)];
        int j = 0;
        for (byte aData : data) {
            int i2 = j + 1;
            out[j] = toDigits[(aData & 240) >>> 4];
            j = i2 + 1;
            out[i2] = toDigits[aData & 15];
        }
        return out;
    }

    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    protected static int toDigit(char ch, int index) throws DecoderException {
        int digit = Character.digit(ch, 16);
        if (digit != -1) {
            return digit;
        }
        throw new DecoderException("Illegal hexadecimal character " + ch + " at index " + index);
    }

    public Hex() {
        this.charsetName = DEFAULT_CHARSET_NAME;
    }

    public Hex(String csName) {
        this.charsetName = csName;
    }

    public byte[] decode(byte[] array) throws DecoderException {
        try {
            return decodeHex(new String(array, getCharsetName()).toCharArray());
        } catch (UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    public Object decode(Object object) throws DecoderException {
        try {
            return decodeHex(object instanceof String ? ((String) object).toCharArray() : (char[]) object);
        } catch (ClassCastException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    public byte[] encode(byte[] array) {
        return StringUtils.getBytesUnchecked(encodeHexString(array), getCharsetName());
    }

    public Object encode(Object object) throws EncoderException {
        try {
            return encodeHex(object instanceof String ? ((String) object).getBytes(getCharsetName()) : (byte[]) object);
        } catch (ClassCastException | UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage(), e);
        }
    }

    public String getCharsetName() {
        return this.charsetName;
    }

    public String toString() {
        return super.toString() + "[charsetName=" + this.charsetName + "]";
    }
}
