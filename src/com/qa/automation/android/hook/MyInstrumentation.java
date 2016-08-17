package com.qa.automation.android.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;

import com.qa.automation.android.AutomationServer;
import com.qa.automation.android.highlight.HighlightView;

public class MyInstrumentation extends Instrumentation{
    private static final String TAG = "MyInstrumentation";

    // ActivityThread中原始的对象, 保存起来
    Instrumentation mBase;

    public MyInstrumentation(Instrumentation base) {
        mBase = base;
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
        mBase.callActivityOnStart(activity);
        afterOnStart(activity);
    }

    private void afterOnStart(Activity activity){
        Log.d(TAG,"afterOnStart:"+activity.getClass().getSimpleName());
        HighlightView.highlight(activity);
    }

    private void beforeOnStart(Activity activity){
        Log.d(TAG,"beforeOnStart:"+activity.getClass().getSimpleName());
    }


    /**
     * Perform calling of an activity's {@link Activity#onCreate}
     * method.  The default implementation simply calls through to that method.
     *
     * @param activity The activity being created.
     * @param icicle The previously frozen state (or null) to pass through to onCreate().
     */
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        beforeOnCreate(activity);
        mBase.callActivityOnCreate(activity,icicle);
        afterOnCreate(activity);
    }

    private void afterOnCreate(Activity activity){
        Log.d(TAG,"afterOnCreate:"+activity.getClass().getSimpleName());
        AutomationServer.setCurrentContext(activity.getApplicationContext()).addWindow(activity);
    }

    private void beforeOnCreate(Activity activity){
        Log.d(TAG,"beforeOnCreate:"+activity.getClass().getSimpleName());
    }

    /**
     * Perform calling of an activity's {@link Activity#onResume} method.  The
     * default implementation simply calls through to that method.
     *
     * @param activity The activity being resumed.
     */
    public void callActivityOnResume(Activity activity) {
        beforeOnResume(activity);
        mBase.callActivityOnResume(activity);
        afterOnResume(activity);
    }

    private void afterOnResume(Activity activity){
        Log.d(TAG,"afterOnResume:"+activity.getClass().getSimpleName());
        AutomationServer.setFocusedWindow(activity);
    }

    private void beforeOnResume(Activity activity){
        Log.d(TAG,"beforeOnResume:"+activity.getClass().getSimpleName());
    }

    public void callActivityOnDestroy(Activity activity) {
        beforeOnDestroy(activity);
        mBase.callActivityOnDestroy(activity);
        afterOnDestroy(activity);
    }

    private void afterOnDestroy(Activity activity){
        Log.d(TAG,"afterOnDestroy:"+activity.getClass().getSimpleName());
        AutomationServer.removeWindow(activity);
    }

    private void beforeOnDestroy(Activity activity){
        Log.d(TAG,"beforeOnDestroy:"+activity.getClass().getSimpleName());
    }
}
