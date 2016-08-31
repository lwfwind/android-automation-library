package com.qa.automation.android.hook;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.qa.automation.android.find.Sleeper;
import com.qa.automation.android.highlight.HighlightView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The type Proxy wm invocation handler.
 */
public class ProxyWMInvocationHandler implements InvocationHandler {
    private static final String TAG = "ProxyWMInvHandler";
    private Object target;
    private Activity activity;

    /**
     * Instantiates a new Proxy wm invocation handler.
     *
     * @param target   the target
     * @param activity the activity
     */
    public ProxyWMInvocationHandler(Object target, Activity activity) {
        this.target = target;
        this.activity = activity;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = null;
        try {
            if (method.getName().equalsIgnoreCase("addview")) {
                final View decorView = (View) args[0];
                Log.w(TAG, method.getName() + " " + getActivity(decorView));
                ret = method.invoke(target, args);
                new Thread() {
                    public void run() {
                        Sleeper sleeper = new Sleeper();
                        long endTime = SystemClock.uptimeMillis() + 2000;
                        while (SystemClock.uptimeMillis() <= endTime) {
                            HighlightView.highlight(activity, decorView);
                            sleeper.sleep();
                        }
                    }

                }.start();
                try {
                    Field instanceField = Activity.class.getDeclaredField("mWindowManager");
                    instanceField.setAccessible(true);
                    instanceField.set(this.activity, this.target);
                    Log.w(TAG, "revert mWindowManager to " + this.target);
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage(), e);
                }
            }
        } catch (InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return ret;
    }

    private Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
