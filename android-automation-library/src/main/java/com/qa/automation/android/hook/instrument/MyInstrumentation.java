package com.qa.automation.android.hook.instrument;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;

import com.qa.automation.android.AutomationServer;
import com.qa.automation.android.GlobalVariables;
import com.qa.automation.android.highlight.HighlightView;
import com.qa.automation.android.hook.ProxyWMInvocationHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * The type My instrumentation.
 */
public class MyInstrumentation extends Instrumentation {
    private static final String TAG = "MyInstrumentation";
    private static HashMap<String, Integer> onDuration;
    private static boolean isFirstLaunch = true;

    /**
     * ActivityThread中原始的对象, 保存起来
     */
    Instrumentation mBase;

    /**
     * Instantiates a new My instrumentation.
     *
     * @param base the base
     */
    public MyInstrumentation(Instrumentation base) {
        mBase = base;
    }

    public void callApplicationOnCreate(Application app) {
        beforeCallApplicationOnCreate(app);
        long onAppCreateStartTime = SystemClock.uptimeMillis();
        mBase.callApplicationOnCreate(app);
        long onAppCreateEndTime = SystemClock.uptimeMillis();
        int duration = (int) (onAppCreateEndTime - onAppCreateStartTime);
        GlobalVariables.APP_LAUNCH_DURATION_MAP.put("AppName", app.getClass().getName());
        GlobalVariables.APP_LAUNCH_DURATION_MAP.put("OnCreate", "" + duration);
        Log.w(TAG, "OnAppCreate Duration:" + duration);
        afterCallApplicationOnCreate(app);
    }

    /**
     * Before call application on create.
     *
     * @param app the app
     */
    public void beforeCallApplicationOnCreate(Application app) {
    }

    /**
     * After call application on create.
     *
     * @param app the app
     */
    public void afterCallApplicationOnCreate(Application app) {
    }

    /**
     * Perform calling of an activity's {@link Activity#onCreate}
     * method.  The default implementation simply calls through to that method.
     *
     * @param activity The activity being created.
     * @param icicle   The previously frozen state (or null) to pass through to onCreate().
     */
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        beforeOnCreate(activity);
        long onCreateStartTime = SystemClock.uptimeMillis();
        mBase.callActivityOnCreate(activity, icicle);
        long onCreateEndTime = SystemClock.uptimeMillis();
        if (GlobalVariables.ACTIVITY_DURATION_MAP.get(activity.getClass().getName()) == null) {
            onDuration = new HashMap<>();
            int onCreateDuration = (int) (onCreateEndTime - onCreateStartTime);
            onDuration.put("OnCreate", onCreateDuration);
        }
        afterOnCreate(activity);
    }

    private void beforeOnCreate(Activity activity) {
        Log.w(TAG, "beforeOnCreate:" + activity.getClass().getSimpleName());
    }

    private void afterOnCreate(Activity activity) {
        Log.w(TAG, "afterOnCreate:" + activity.getClass().getSimpleName());
        AutomationServer.setActivity(activity);
        AutomationServer.setCurrentContext(activity).addWindow(activity);
    }


    /**
     * Perform calling of an activity's {@link Activity#onStart}
     * method.  The default implementation simply calls through to that method.
     *
     * @param activity The activity being started.
     */
    @Override
    public void callActivityOnStart(Activity activity) {
        beforeOnStart(activity);
        long onStartStartTime = SystemClock.uptimeMillis();
        mBase.callActivityOnStart(activity);
        long onStartEndTime = SystemClock.uptimeMillis();
        if (GlobalVariables.ACTIVITY_DURATION_MAP.get(activity.getClass().getName()) == null) {
            onDuration.put("OnStart", (int) (onStartEndTime - onStartStartTime));
        }
        afterOnStart(activity);
    }

    private void beforeOnStart(Activity activity) {
        Log.w(TAG, "beforeOnStart:" + activity.getClass().getSimpleName());
    }

    private void afterOnStart(Activity activity) {
        Log.w(TAG, "afterOnStart:" + activity.getClass().getSimpleName());
        if (GlobalVariables.ENABLE_HIGHLIGHT) {
            //proxy WindowManagerImp
            WindowManager windowManager = activity.getWindowManager();
            InvocationHandler handler = new ProxyWMInvocationHandler(windowManager, activity);
            WindowManager proxyWindowManager = WindowManager.class.cast(Proxy.newProxyInstance(windowManager.getClass().getClassLoader(), windowManager.getClass().getInterfaces(), handler));
            try {
                Field instanceField = Activity.class.getDeclaredField("mWindowManager");
                instanceField.setAccessible(true);
                instanceField.set(activity, proxyWindowManager);
                Log.w(TAG, "proxy mWindowManager from " + windowManager);
            } catch (Exception e) {
                Log.w(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * Perform calling of an activity's {@link Activity#onResume} method.  The
     * default implementation simply calls through to that method.
     *
     * @param activity The activity being resumed.
     */
    public void callActivityOnResume(Activity activity) {
        beforeOnResume(activity);
        long onResumeStartTime = SystemClock.uptimeMillis();
        mBase.callActivityOnResume(activity);
        long onResumeEndTime = SystemClock.uptimeMillis();
        if (GlobalVariables.ACTIVITY_DURATION_MAP.get(activity.getClass().getName()) == null) {
            onDuration.put("OnResume", (int) (onResumeStartTime - onResumeEndTime));
            int total = onDuration.get("OnCreate") + onDuration.get("OnStart") + onDuration.get("OnResume");
            onDuration.put("TotalDuration", total);
            Log.w(TAG, activity.getClass().getName() + " TotalDuration:" + total);
            GlobalVariables.ACTIVITY_DURATION_MAP.put(activity.getClass().getName(), onDuration);
            if (isFirstLaunch) {
                isFirstLaunch = false;
                if (total + Integer.parseInt(GlobalVariables.APP_LAUNCH_DURATION_MAP.get("OnCreate")) > 400) {
                    AutomationServer.sendActivityDuration(activity.getClass().getName(), true);
                }
            } else {
                if (total > 400) {
                    AutomationServer.sendActivityDuration(activity.getClass().getName(), false);
                }
            }
        }
        afterOnResume(activity);
    }

    private void afterOnResume(Activity activity) {
        Log.w(TAG, "afterOnResume:" + activity.getClass().getSimpleName());
        AutomationServer.setFocusedWindow(activity);
        if (GlobalVariables.ENABLE_HIGHLIGHT) {
            HighlightView.removeHighlightedActivity(activity.getClass().getName());
        }
    }

    private void beforeOnResume(Activity activity) {
        Log.w(TAG, "beforeOnResume:" + activity.getClass().getSimpleName());
    }

    public void callActivityOnDestroy(Activity activity) {
        beforeOnDestroy(activity);
        mBase.callActivityOnDestroy(activity);
        afterOnDestroy(activity);
    }

    private void afterOnDestroy(Activity activity) {
        Log.w(TAG, "afterOnDestroy:" + activity.getClass().getSimpleName());
        AutomationServer.removeWindow(activity);
    }

    private void beforeOnDestroy(Activity activity) {
        Log.w(TAG, "beforeOnDestroy:" + activity.getClass().getSimpleName());
    }
}
