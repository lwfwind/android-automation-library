package com.qa;

import android.app.Application;
import android.util.Log;

import com.alipay.euler.andfix.AndFix;
import com.qa.android.hook.AndFixHookManager;
import com.qa.android.hook.LogHook;

import java.lang.reflect.Method;

/**
 * The type My application.
 */
public class AndFixApplication extends Application {
    private static final String TAG = "AndFixApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            boolean result = AndFix.setup();
            if (!result) {
                Log.e(TAG, "AndFix not support !");
                return;
            }
            AndFixHookManager.getGlobalInstance().applyHooks(LogHook.class);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
        Log.e(TAG, "Execute: AndFix original method.");
    }

}
