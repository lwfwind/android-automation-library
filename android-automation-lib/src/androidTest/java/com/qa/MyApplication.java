package com.qa;

import android.app.Application;
import android.util.Log;

import com.alipay.euler.andfix.AndFix;
import com.qa.android.hook.LogHook;

import java.lang.reflect.Method;

/**
 * The type My application.
 */
public class MyApplication extends Application {
    private static final String TAG = "AndFixTest";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            boolean result = AndFix.setup();
            if (!result) {
                Log.e(TAG, "AndFix not support !");
                return;
            }
            Method original = MyApplication.class.getDeclaredMethod("andFixOriginal");
            Method target = MyApplication.class.getDeclaredMethod("andFixReplaceMethod");
            AndFix.addReplaceMethod(original, target);

            Method log_original = Log.class.getDeclaredMethod("e",String.class,String.class);
            Method log_target = LogHook.class.getDeclaredMethod("e",String.class,String.class);
            AndFix.addReplaceMethod(log_original, log_target);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
        andFixOriginal();
    }

    private void andFixOriginal() {
        Log.e(TAG, "Execute: AndFix original method.");
    }

    private void andFixReplaceMethod() {
        Log.e(TAG, "Execute: AndFix replace method.");
    }

}
