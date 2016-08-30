package com.qa;

import android.app.Application;
import android.util.Log;
import com.lody.legend.HookManager;
import com.qa.android.hook.LogHook;


/**
 * The type My application.
 */
public class LegendApplication extends Application {
    private static final String TAG = "LegendApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        HookManager.getDefault().applyHooks(LogHook.class);
        Log.e(TAG, "LegendApplication");
    }

}
