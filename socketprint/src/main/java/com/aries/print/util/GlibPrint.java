package com.aries.print.util;

public class GlibPrint {
    private final static String TAG = "GlibPrint";

    private static GlibPrint instance;

    // Used to load the 'native_oboe_manager' library on application startup.
    static {
        System.loadLibrary("socket_print");
    }

    static {
        instance = new GlibPrint();
    }

    public static GlibPrint getInstance() {
        return instance;
    }

    private GlibPrint() {
    }

    public String getPrinterName(String ip, int port) {
        return GetPrinterName(ip, port);
    }

    public void printFile(String ip, int port, String filepath) {
        PrintFile(ip, port, filepath);
    }

    public native String stringFromJNI();
    public native String GetPrinterName(String ip, int port);
    public native void PrintFile(String ip, int port, String filePath);
}
