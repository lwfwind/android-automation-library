package com.qa.automation.android.exception;

import android.content.Context;
import android.util.Log;

import com.qa.automation.android.GlobalVariables;
import com.qa.automation.android.util.AppInfoUtil;
import com.qa.automation.android.util.DeviceUtil;
import com.qa.automation.android.util.email.MailSender;
import com.qa.automation.android.util.log.LogCat;
import com.qa.automation.android.util.log.LogQueueGlobal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.mail.MessagingException;

/**
 * The type Crash handler.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();

    private static CrashHandler instance;
    private Context mContext;
    private Thread.UncaughtExceptionHandler defaultHandler;

    private String sUserInfo = "";


    private CrashHandler() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }

    /**
     * Init.
     *
     * @param context the context
     */
    public void init(Context context) {
        this.mContext = context;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        boolean res = handleException(ex);
        if (!res && defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.w(TAG, "error : ", e);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }

        new Thread() {
            @Override
            public void run() {
                Log.w(TAG, "error : ", ex);
                ;
                try {
                    sUserInfo = getUserInfo();
                    String[] emails = GlobalVariables.EMAIL_TO.split(" ");
                    MailSender.sendHTMLMail("android_automation@126.com", "Automation123", "smtp.126.com",
                            "android-automation-library uncatched exception", sUserInfo + getErrorTrace(ex) + getRecentLogs(),
                            null, emails);
                } catch (MessagingException e) {
                    Log.w(TAG, "send mail error : ", e);
                }
            }
        }.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Gets error trace.
     *
     * @param ex the ex
     * @return the error trace
     */
    public String getErrorTrace(Throwable ex) {
        StringWriter stringWrite = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWrite);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        return stringWrite.toString();
    }

    /**
     * Gets user info.
     *
     * @return the user info
     */
    public String getUserInfo() {
        String newline = "\n<br>";
        return "time:" + new Date().toString() + newline +
                "processName:" + AppInfoUtil.getCurProcessName() + newline +
                "app version:" + AppInfoUtil.getAPPVersion() + newline +
                "app channel:" + AppInfoUtil.getChannelName() + newline +
                "device model:" + DeviceUtil.getDeviceMode() + newline +
                "device id:" + DeviceUtil.getDeviceId() + newline +
                "device version code:" + DeviceUtil.getDeviceVersionCode() + newline +
                "device net state:" + DeviceUtil.getNetworkType() + newline +
                newline +
                "====================" +
                "error message:" +
                newline;
    }

    /**
     * Gets recent logs.
     *
     * @return the recent logs
     */
    public String getRecentLogs() {
        StringBuilder sb = new StringBuilder();
        String newline = "\n<br>";
        sb.append(newline).append(newline).append("=============================Logs:").append(newline);
        if(!GlobalVariables.ENABLE_ANDFIX_MODE) {
            LogCat.getRecentLogs();
        }
        for (Object log : LogQueueGlobal.getInstance().getLogQueue()) {
            sb.append(log).append(newline);
        }
        return sb.toString();
    }
}