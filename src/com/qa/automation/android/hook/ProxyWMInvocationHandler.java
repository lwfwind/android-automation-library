package com.qa.automation.android.hook;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.View;
import com.qa.automation.android.highlight.HighlightView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * The type Proxy wm invocation handler.
 */
public class ProxyWMInvocationHandler implements InvocationHandler {
    private static final String TAG = "ProxyWMInvocationHandler";
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
        if (method.getName().equalsIgnoreCase("addview")) {
            View decorView = (View) args[0];
            Log.w(TAG, method.getName()+" "+getActivity(decorView));
            ret = method.invoke(target, args);
            HighlightView.highlight(this.activity, decorView);
            try {
                Field instanceField = Activity.class.getDeclaredField("mWindowManager");
                instanceField.setAccessible(true);
                instanceField.set(this.activity, this.target);
            } catch (Exception e) {
                Log.w(TAG, e.getMessage(), e);
            }
        }
        else
        {
            if(method.getName().contains("view")) {
                View decorView = (View) args[0];
                Log.w(TAG, method.getName()+" "+getActivity(decorView));
            }
        }
        return ret;
    }

    private Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
}
