package com.qa.automation.android.hook;

import android.annotation.SuppressLint;
import android.util.Log;

import com.qa.automation.android.util.log.LogQueueGlobal;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The type Log hook.
 */
public class LogHook {

    /**
     * V int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    @Hook("android.util.Log::v@java.lang.String#java.lang.String")
    public static int v(String tag, String msg) {
        println(Log.VERBOSE, tag, msg);
        return 0;
    }

    /**
     * D int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    @Hook("android.util.Log::d@java.lang.String#java.lang.String")
    public static int d(String tag, String msg) {
        println(Log.DEBUG, tag, msg);
        return 0;
    }

    /**
     * int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    @Hook("android.util.Log::i@java.lang.String#java.lang.String")
    public static int i(String tag, String msg) {
        println(Log.INFO, tag, msg);
        return 0;
    }

    /**
     * W int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    @Hook("android.util.Log::w@java.lang.String#java.lang.String")
    public static int w(String tag, String msg) {
        println(Log.WARN, tag, msg);
        return 0;
    }

    /**
     * E int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    @Hook("android.util.Log::e@java.lang.String#java.lang.String")
    public static int e(String tag, String msg) {
        println(Log.ERROR, tag, msg);
        return 0;
    }

    /**
     * V int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    @Hook("android.util.Log::v@java.lang.String#java.lang.String#java.lang.Throwable")
    public static int v(String tag, String msg, Throwable tr) {
        println(Log.VERBOSE, tag, msg, tr);
        return 0;
    }

    /**
     * D int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    @Hook("android.util.Log::d@java.lang.String#java.lang.String#java.lang.Throwable")
    public static int d(String tag, String msg, Throwable tr) {
        println(Log.DEBUG, tag, msg, tr);
        return 0;
    }

    /**
     * int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    @Hook("android.util.Log::i@java.lang.String#java.lang.String#java.lang.Throwable")
    public static int i(String tag, String msg, Throwable tr) {
        println(Log.INFO, tag, msg, tr);
        return 0;
    }

    /**
     * W int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    @Hook("android.util.Log::w@java.lang.String#java.lang.String#java.lang.Throwable")
    public static int w(String tag, String msg, Throwable tr) {
        println(Log.WARN, tag, msg, tr);
        return 0;
    }

    /**
     * E int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    @Hook("android.util.Log::e@java.lang.String#java.lang.String#java.lang.Throwable")
    public static int e(String tag, String msg, Throwable tr) {
        println(Log.ERROR, tag, msg, tr);
        return 0;
    }

    /**
     * Println.
     *
     * @param priority the priority
     * @param tag      the tag
     * @param msg      the msg
     * @param tr       the tr
     */
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
