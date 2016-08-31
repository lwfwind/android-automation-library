package com.qa.test.legend;


import android.util.Log;
import android.widget.Toast;

import com.lody.legend.Hook;
import com.lody.legend.HookManager;

/**
 * The type Log hook.
 */
public class ToastTest {
    private static final String TAG = "ToastTest";

    @Hook("android.widget.Toast::show")
    public static void Toast_show(Toast thiz) {
        Log.e(TAG, "Toast_show");
        //Call the origin method
        HookManager.getDefault().callSuper(thiz);
    }
}
