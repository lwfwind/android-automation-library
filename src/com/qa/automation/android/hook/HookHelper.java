package com.qa.automation.android.hook;

import android.app.Instrumentation;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

            // 创建代理对象
            Instrumentation myInstrumentation = new MyInstrumentation(mInstrumentation);

            // 偷梁换柱
            mInstrumentationField.set(currentActivityThread, myInstrumentation);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage(), e);
        }
    }
}
