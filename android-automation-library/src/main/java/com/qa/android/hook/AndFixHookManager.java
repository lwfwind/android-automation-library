package com.qa.android.hook;

import com.alipay.euler.andfix.AndFix;
import com.qa.android.util.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * The type And fix hook manager.
 */
public class AndFixHookManager {

    /**
     * Singleton
     */
    private static final AndFixHookManager sGlobal = new AndFixHookManager();

    /**
     * Gets global instance.
     *
     * @return the global instance
     */
    public static AndFixHookManager getGlobalInstance() {
        return sGlobal;
    }


    /**
     * Apply hooks.
     *
     * @param holdClass the hold class
     */
    public void applyHooks(Class<?> holdClass) {
        for (Method hookMethod : holdClass.getDeclaredMethods()) {
            Hook hook = hookMethod.getAnnotation(Hook.class);
            if (hook != null) {
                String statement = hook.value();
                String[] splitValues = statement.split("::");
                if (splitValues.length == 2) {
                    String className = splitValues[0];
                    String[] methodNameWithSignature = splitValues[1].split("@");
                    if (methodNameWithSignature.length <= 2) {
                        String methodName = methodNameWithSignature[0];
                        String signature = methodNameWithSignature.length == 2 ? methodNameWithSignature[1] : "";
                        String[] paramList = signature.split("#");
                        if (paramList[0].equals("")) {
                            paramList = new String[0];
                        }
                        try {
                            Class<?> clazz = Class.forName(className);
                            boolean isResolve = false;
                            for (Method method : clazz.getDeclaredMethods()) {
                                if (method.getName().equals(methodName)) {
                                    Class<?>[] types = method.getParameterTypes();
                                    if (paramList.length == types.length) {
                                        boolean isMatch = true;
                                        for (int N = 0; N < types.length; N++) {
                                            if (!types[N].getName().equals(paramList[N])) {
                                                isMatch = false;
                                                break;
                                            }
                                        }
                                        if (isMatch) {
                                            AndFix.addReplaceMethod(method, hookMethod);
                                            isResolve = true;
                                            Logger.d("[+++] %s have hooked.", method.getName());
                                        }
                                    }
                                }
                                if (isResolve) {
                                    break;
                                }
                            }
                            if (!isResolve) {
                                Logger.e("[---] Cannot resolve Method : %s.", Arrays.toString(methodNameWithSignature));
                            }
                        } catch (Throwable e) {
                            Logger.e("[---] Error to Load Hook Method From : %s.", hookMethod.getName());
                            e.printStackTrace();
                        }

                    } else {
                        Logger.e("[---] Can't split method and signature : %s.", Arrays.toString(methodNameWithSignature));
                    }
                } else {
                    Logger.e("[---] Can't understand your statement : [%s].", statement);
                }
            }
        }
    }
}
