package com.qa.automation.android.hook;


import com.alipay.euler.andfix.AndFix;
import com.qa.automation.android.GlobalVariables;

public class AndFixHook {

    public static void init(){
        if (AndFix.setup()) {
            AndFixHookManager.getGlobalInstance().applyHooks(LogHook.class);
            GlobalVariables.ENABLE_ANDFIX_MODE=true;
        }
    }
}
