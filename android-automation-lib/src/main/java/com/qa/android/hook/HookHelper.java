package com.qa.android.hook;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.util.Log;

import com.alipay.euler.andfix.AndFix;
import com.qa.android.util.log.LogQueueGlobal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The type Hook helper.
 */
public class HookHelper {
    private static final String TAG = "HookHelper";

    /**
     * Start.
     */
    public static void start() {
        try {
            // 先获取到当前的ActivityThread对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            //currentActivityThread是一个static函数所以可以直接invoke，不需要带实例参数
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            // 拿到原始的 mInstrumentation字段
            Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);
            Instrumentation myInstrumentation = new MyInstrumentation(mInstrumentation);
            mInstrumentationField.set(currentActivityThread, myInstrumentation);

            boolean result = AndFix.setup();
            if (!result) {
                Log.e(TAG, "AndFix not support !");
                return;
            }
            Method original = Log.class.getDeclaredMethod("println", int.class, String.class, String.class);
            Method target = HookHelper.class.getDeclaredMethod("println", int.class, String.class, String.class);
            AndFix.addReplaceMethod(original, target);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage(), e);
        }
    }

    /**
     * Println int.
     *
     * @param priority the priority
     * @param tag      the tag
     * @param msg      the msg
     * @return the int
     */
    public static int println(int priority, String tag, String msg) {
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
        String log = getLongStandardString(new Date()) + " " + level + "/ TAG:" + tag + ",msg:" + msg;
        LogQueueGlobal.getInstance().add(log);
        System.out.println(log);
        return Log.println(priority, tag, msg);
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
