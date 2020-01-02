package com.bisoft.minipg.service.pgwireprotocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Util
 */
public final class Util {
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final String EMPTY_STRING = "";

    public static byte[] concatByteArray(byte[] a, byte[] b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static int readInt32(byte[] buffer, int start) {
        byte[] bytes = readByteArray(buffer, start, 4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static int readInt16(byte[] buffer, int start) {
        byte[] bytes = readByteArray(buffer, start, 2);
        bytes = concatByteArray(new byte[] { 0, 0 }, bytes);
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte readInt8(byte[] buffer, int start) {
        return buffer[start];
    }

    public static byte[] readByteArray(byte[] buffer, int start, int length) {
        byte[] bytes = Arrays.copyOfRange(buffer, start, start + length);
        return bytes;
    }

    public static String readCString(byte[] buffer, int start) {
        byte[] bytes = bytesBefore(buffer, start, (byte) 0);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] bytesBefore(byte[] buffer, int start, byte searchCharacter) {
        int end = -1;
        int index = start;
        while (index < buffer.length && end < 0) {
            if (buffer[index] == searchCharacter) {
                end = index;
            }
            index++;
        }
        if (end > -1) {
            return readByteArray(buffer, start, end - start);
        }
        return new byte[] {};
    }

    public static int[] readInt32Array(byte[] buffer, int start, int lengthParameterDataTypes) {
        int[] result = new int[lengthParameterDataTypes];
        for (int i = 0; i < lengthParameterDataTypes; i++) {
            result[i] = Util.readInt32(buffer, start + i * 4);
        }
        return result;
    }

    public static byte[] int32ByteArray(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    public static byte[] int16ByteArray(int value) {
        return new byte[] { (byte) (value >>> 8), (byte) value };
    }

    public static byte[] int8ByteArray(int value) {
        return new byte[] { (byte) value };
    }

    public static byte[] toCString(String commandStr) {
        byte[] bytes = {};
        if (commandStr != null) {
            bytes = commandStr.getBytes(StandardCharsets.UTF_8);
        }
        return concatByteArray(bytes, int8ByteArray(0));
    }

    public static boolean caseInsensitiveContains(String string, String pattern) {
        if (pattern == null || string == null || string.equals("")) {
            return false;
        }
        return string.toLowerCase().contains(pattern.toLowerCase());
    }

    public static String byteArrayToString(byte[] buffer) {
        if (buffer == null) {
            return "";
        }
        return new String(buffer);
    }
    public static byte[] byteZero() {
        return new byte[] { 0 };
    }

}