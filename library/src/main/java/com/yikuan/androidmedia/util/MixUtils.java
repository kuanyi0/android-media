package com.yikuan.androidmedia.util;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public class MixUtils {
    public static void mix(byte[] audio1, byte[] audio2, byte[] result) {
        short[] shorts1 = bytes2shorts(audio1);
        short[] shorts2 = bytes2shorts(audio2);
        short[] shorts = new short[shorts1.length];
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = (short) ((shorts1[i] + shorts2[i]) / 2);
        }
        for (int i = 0; i < shorts.length; i++) {
            result[2 * i] = (byte) (shorts[i] & 0x00ff);
            result[2 * i + 1] = (byte) ((shorts[i] & 0xff00) >> 8);
        }
    }

    private static short[] bytes2shorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = (short) ((bytes[i * 2] & 0xff) | (bytes[i * 2 + 1] & 0xff) << 8);
        }
        return shorts;
    }
}
