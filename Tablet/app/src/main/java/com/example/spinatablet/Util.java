package com.example.spinatablet;

public class Util {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[(bytes.length * 3)];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 255;
            char[] cArr = hexArray;
            hexChars[i * 3] = cArr[v >>> 4];
            hexChars[(i * 3) + 1] = cArr[v & 15];
            hexChars[(i * 3) + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static int[] byteTodec(byte[] bytes) {
        int[] decData = new int[bytes.length];
        int i = 0;
        int length = bytes.length;
        int i2 = 0;
        while (i2 < length) {
            decData[i] = bytes[i2] & 255;
            i2++;
            i++;
        }
        return decData;
    }
}
