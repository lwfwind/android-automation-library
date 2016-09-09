package com.qa.automation.android.hook;


import com.alipay.euler.andfix.AndFix;
import com.qa.automation.android.GlobalVariables;

public class AndFixHook {

    public static void init(){
        if (GlobalVariables.ENABLE_ANDFIX_HOOK) {
            if (AndFix.setup()) {
                AndFixHookManager.getGlobalInstance().applyHooks(LogHook.class);
            }
        }
    }
}
