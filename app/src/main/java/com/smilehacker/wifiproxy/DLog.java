package com.smilehacker.wifiproxy;

import android.util.Log;


/**
 * 用户Debug模式下Logcat
 * @author quan,zhou
 * @create 2017/4/20.
 */
public class DLog {
    public static void i(String msg) {
        if (isDebug()) {
            Log.i(getCallerName(), msg);
        }
    }

    public static void d(String msg) {
        if (isDebug()) {
            Log.d(getCallerName(), msg);
        }
    }

    public static void v(String msg) {
        if (isDebug()) {
            Log.v(getCallerName(), msg);
        }
    }

    public static void e(String msg) {
        if (isDebug()) {
            Log.e(getCallerName(), msg);
        }
    }

    public static void e(Throwable e) {
        if (isDebug()) {
            Log.e(getCallerName(), "error", e);
        }
    }

    public static void e(String msg, Throwable e) {
        if (isDebug()) {
            Log.e(getCallerName(), msg, e);
        }
    }

    public static void w(String msg) {
        if (isDebug()) {
            Log.w(getCallerName(), msg);
        }
    }

    private static String getCallerName() {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        return elements[2].getClassName();
    }

    private static Boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
