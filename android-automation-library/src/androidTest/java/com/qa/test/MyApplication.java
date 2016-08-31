package com.qa.test;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.alipay.euler.andfix.AndFix;
import com.lody.legend.HookManager;
import com.qa.automation.android.hook.AndFixHookManager;
import com.qa.automation.android.hook.LogHook;
import com.qa.automation.android.util.log.LogQueueGlobal;
import com.qa.test.legend.ToastTest;

import junit.framework.Assert;

/**
 * The type My application.
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * And fix.
     */
    public void AndFix() {
        try {
            boolean result = AndFix.setup();
            if (!result) {
                Log.e(TAG, "AndFix not support !");
                return;
            }
            AndFixHookManager.getGlobalInstance().applyHooks(LogHook.class);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        Log.e(TAG, "AndFix");
        Assert.assertTrue(LogQueueGlobal.getInstance().getLogQueue().toString().contains("AndFix"));
    }

    /**
     * Legend.
     */
    public void Legend() {
        HookManager.getDefault().applyHooks(ToastTest.class);
        Toast.makeText(getApplicationContext(),"LegendTest",Toast.LENGTH_SHORT).show();
        Assert.assertTrue(LogQueueGlobal.getInstance().getLogQueue().toString().contains("Toast_show"));
    }


}
