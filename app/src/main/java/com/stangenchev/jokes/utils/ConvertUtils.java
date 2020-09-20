package com.stangenchev.jokes.utils;

public class ConvertUtils {
    public int byteArrayToInt(byte[] intByte) {
        return ((intByte[0] & 255) ^ 170) + (((intByte[1] & 255) ^ 170) << 8) + (((intByte[2] & 255) ^ 170) << 16) + (((intByte[3] & 255) ^ 170) << 24);
    }
}
