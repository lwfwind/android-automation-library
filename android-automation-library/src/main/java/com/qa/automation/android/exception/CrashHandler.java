package com.qa.automation.android.exception;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.qa.automation.android.GlobalVariables;
import com.qa.automation.android.util.AppInfoUtil;
import com.qa.automation.android.util.DeviceUtil;
import com.qa.automation.android.util.email.MailSender;
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

    private static CrashHandler instance; // 单例模式
    private Context context; // 程序Context对象
    private Thread.UncaughtExceptionHandler defaultHandler; // 系统默认的UncaughtException处理类

    private String sUserInfo = "";


    private CrashHandler() {

    }

    /**
     * 获取CrashHandler实例
     *
     * @return CrashHandler instance
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
     * 异常处理初始化
     *
     * @param context the context
     */
    public void init(Context context) {
        this.context = context;
        // 获取系统默认的UncaughtException处理器
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        // 自定义错误处理
        boolean res = handleException(ex);
        if (!res && defaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            defaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.w(TAG, "error : ", e);
            }
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex exception
     * @return true:如果处理了该异常信息;否则返回false.
     */
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
        String newline = "\n";
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
        String newline = "\n";
        sb.append(newline).append(newline).append("=============================Logs:").append(newline);
        for (Object log : LogQueueGlobal.getInstance().getLogQueue()) {
            sb.append(log).append(newline);
        }
        return sb.toString();
    }
}