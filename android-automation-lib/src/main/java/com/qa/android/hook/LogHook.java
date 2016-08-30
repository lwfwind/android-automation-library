package com.qa.android.hook;

import android.annotation.SuppressLint;
import android.util.Log;

import com.qa.android.util.log.LogQueueGlobal;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogHook {
    public static int v(String tag, String msg) {
        println(Log.VERBOSE, tag, msg);
        return 0;
    }

    public static int d(String tag, String msg) {
        println(Log.DEBUG, tag, msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        println(Log.INFO, tag, msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        println(Log.WARN, tag, msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        println(Log.ERROR, tag, msg);
        return 0;
    }

    public static int v(String tag, String msg, Throwable tr) {
        println(Log.VERBOSE, tag, msg, tr);
        return 0;
    }

    public static int d(String tag, String msg, Throwable tr) {
        println(Log.DEBUG, tag, msg, tr);
        return 0;
    }

    public static int i(String tag, String msg, Throwable tr) {
        println(Log.INFO, tag, msg, tr);
        return 0;
    }

    public static int w(String tag, String msg, Throwable tr) {
        println(Log.WARN, tag, msg, tr);
        return 0;
    }

    public static int e(String tag, String msg, Throwable tr) {
        println(Log.ERROR, tag, msg, tr);
        return 0;
    }

    public static void println(int priority, String tag, String msg, Throwable... tr) {
        String level = "";
        switch (priority) {
            case 2:
                level = "V";
                break;
            case 3:
                level = "D";
                break;
            case 4:
                level = "I";
                break;
            case 5:
                level = "W";
                break;
            case 6:
                level = "E";
                break;
            case 7:
                level = "A";
                break;
        }
        String log = "";
        if (tr.length > 0) {
            log = getLongStandardString(new Date()) + " " + level + "/ TAG:" + tag + ",msg:" + msg + ",StackTrace:" + Log.getStackTraceString(tr[0]);
        } else {
            log = getLongStandardString(new Date()) + " " + level + "/ TAG:" + tag + ",msg:" + msg;
        }
        LogQueueGlobal.getInstance().add(log);
        Log.println(priority, tag, msg);
        //System.out.println(log);
    }

    /**
     * Gets long standard string.
     *
     * @param date the date
     * @return the long standard string
     */
    public static String getLongStandardString(Date date) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat standardFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return standardFormat.format(date);
    }
}
