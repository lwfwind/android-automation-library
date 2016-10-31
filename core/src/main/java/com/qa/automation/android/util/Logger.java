package com.qa.automation.android.util;

import android.util.Log;

/**
 * The type Logger.
 */
public class Logger {

    private static final String TAG = "Logger";

    /**
     * The constant OPEN_LOG.
     */
    public static boolean OPEN_LOG = true;

    /**
     * .
     *
     * @param msg the msg
     */
    public static void i(String msg) {
        if (OPEN_LOG) {
            Log.i(TAG, msg);
        }
    }

    /**
     * .
     *
     * @param format the format
     * @param args   the args
     */
    public static void i(String format, Object... args) {
        i(String.format(format, args));
    }

    /**
     * D.
     *
     * @param msg the msg
     */
    public static void d(String msg) {
        if (OPEN_LOG) {
            Log.d(TAG, msg);
        }
    }

    /**
     * D.
     *
     * @param format the format
     * @param args   the args
     */
    public static void d(String format, Object... args) {
        d(String.format(format, args));
    }

    /**
     * W.
     *
     * @param msg the msg
     */
    public static void w(String msg) {
        if (OPEN_LOG) {
            Log.w(TAG, msg);
        }
    }

    /**
     * W.
     *
     * @param format the format
     * @param args   the args
     */
    public static void w(String format, Object... args) {
        w(String.format(format, args));
    }

    /**
     * E.
     *
     * @param msg the msg
     */
    public static void e(String msg) {
        if (OPEN_LOG) {
            Log.e(TAG, msg);
        }
    }

    /**
     * E.
     *
     * @param format the format
     * @param args   the args
     */
    public static void e(String format, Object... args) {
        e(String.format(format, args));
    }
}
