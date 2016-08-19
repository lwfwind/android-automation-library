package com.qa.automation.android.exception;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.qa.automation.android.util.email.MailSender;

import javax.mail.MessagingException;

/**
 * The type Crash handler.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();

    private static CrashHandler instance; // 单例模式
    private Context context; // 程序Context对象
    private Thread.UncaughtExceptionHandler defaultHandler; // 系统默认的UncaughtException处理类
    private String emailTo = "wind@abc360.com";

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

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
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
                Looper.prepare();
                Log.w(TAG, "error : ", ex);
                String err = "[" + ex.getMessage() + "]";
                Toast.makeText(context, "程序出现异常." + err, Toast.LENGTH_LONG)
                        .show();
                Looper.loop();
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(ex.getMessage());
                    sb.append(System.lineSeparator());
                    for (StackTraceElement e : ex.getStackTrace()) {
                        sb.append(e.toString());
                        sb.append(System.lineSeparator());
                    }
                    String[] emails = emailTo.split(" ");
                    MailSender.sendTextMail("EmmageePlus@126.com", "Lwfwind789", "smtp.126.com",
                            "android-automation-library uncatched exception", sb.toString(),
                            null, emails);
                } catch (MessagingException e) {
                    Log.w(TAG, "send mail error : ", e);
                }
            }
        }.start();

        return true;
    }
}