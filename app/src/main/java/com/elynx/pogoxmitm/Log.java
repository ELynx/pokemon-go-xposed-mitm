package com.elynx.pogoxmitm;

public class Log {
    public static final String TAG = "PoGo MitM";

    public static void d(String message) {
        android.util.Log.d(TAG, message);
    }

    public static void i(String message) {
        android.util.Log.i(TAG, message);
    }

    public static void e(String message) {
        android.util.Log.e(TAG, message);
    }

    public static void e(Throwable t) {
        android.util.Log.e(TAG, android.util.Log.getStackTraceString(t));
    }
}
