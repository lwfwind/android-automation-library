package com.qa.test;

import android.app.Application;
import android.util.Log;

/*import com.alipay.euler.andfix.AndFixHook;*/
import com.qa.automation.android.util.log.LogQueueGlobal;

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
    }


}
