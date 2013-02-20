package me.hellfire212.MineralManager.utils;

public class DecoderRing {
    public static boolean decodeBool(Object obj, boolean defaultValue) {
        if (obj != null && obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }
        return defaultValue;
    }

    public static String decodeString(Object obj, String defaultValue) {
        if (obj != null && obj instanceof String) {
            return (String) obj;
        }
        return defaultValue;
    }

    public static int decodeInt(Object obj, int defaultValue) {
        if (obj != null && obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return defaultValue;
    }
}
