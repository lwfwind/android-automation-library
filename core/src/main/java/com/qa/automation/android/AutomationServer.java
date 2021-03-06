/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qa.automation.android;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.util.Log;
import android.widget.TextView;

import com.qa.automation.android.exception.CrashHandler;
import com.qa.automation.android.find.Finder;
import com.qa.automation.android.hook.AndFixHook;
import com.qa.automation.android.hook.instrument.InstrumentationHook;
import com.qa.automation.android.popupwindow.PopupWindow;
import com.qa.automation.android.util.AppInfoUtil;
import com.qa.automation.android.util.DeviceUtil;
import com.qa.automation.android.util.email.MailSender;
import com.qa.automation.android.window.WindowManager;
import com.qa.serializable.Point;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;

/**
 * The type Automation server.
 */
public class AutomationServer implements Runnable {
    private static final String TAG = "AutomationServer";
    private static final int VIEW_SERVER_DEFAULT_PORT = 4939;
    private static final int VIEW_SERVER_MAX_CONNECTIONS = 10;
    private static AutomationServer sServer;
    private static WindowManager windowManager = new WindowManager();
    private static Context currContext;
    /**
     * The constant EMAIL_TO.
     */
    public static String EMAIL_TO = "lwfwind@126.com";

    /**
     * The constant ENABLE_HOOK.
     */
    public static boolean ENABLE_ANDFIX_MODE = false;

    /**
     * The constant ENABLE_HOOK.
     */
    public static boolean ENABLE_CRASH_CATCH = false;

    /**
     * The constant ENABLE_HOOK.
     */
    public static boolean ENABLE_STRICT_MODE = false;

    public static boolean ENABLE_COLLECT_DURATION = false;
    private final int mPort;
    private ServerSocket mServer;
    private Thread mThread;
    private ExecutorService mThreadPool;

    private AutomationServer() {
        mPort = -1;
    }

    /**
     * Creates a new AutomationServer associated with the specified window manager on the
     * specified local port. The server is not started by default.
     *
     * @param port The port for the server to listen to.
     * @see #start()
     */
    private AutomationServer(int port) {
        mPort = port;
    }

    /**
     * Gets curr context.
     *
     * @return the curr context
     */
    public static Context getCurrContext() {
        return currContext;
    }

    /**
     * Sets curr context.
     *
     * @param currContext the curr context
     */
    public static void setCurrContext(Context currContext) {
        AutomationServer.currContext = currContext;
    }

    /**
     * Returns a unique instance of the AutomationServer. This method should only be
     * called from the main thread of your application. The server will have
     * the same lifetime as your process.
     *
     * @param context the context
     * @return the automation server
     */
    public static AutomationServer install(Context context) {
        synchronized (AutomationServer.class) {
            if (sServer == null) {
                sServer = new AutomationServer(AutomationServer.VIEW_SERVER_DEFAULT_PORT);
            }
        }

        if (!sServer.isRunning()) {
            try {
                sServer.start();
            } catch (IOException e) {
                Log.w(TAG, "Error:", e);
            }
        }

        currContext = context;
        init();
        return sServer;
    }

    public AutomationServer setEmailTo(String emailTo) {
        EMAIL_TO = emailTo;
        return this;
    }

    public AutomationServer enableCrashCatch(boolean flag) {
        ENABLE_CRASH_CATCH = flag;
        return this;
    }

    public AutomationServer enableStrictMode(boolean flag) {
        ENABLE_STRICT_MODE = flag;
        return this;
    }

    public AutomationServer enableAndfixMode(boolean flag) {
        ENABLE_ANDFIX_MODE = flag;
        return this;
    }

    public AutomationServer enableCollectDuration(boolean flag){
        ENABLE_COLLECT_DURATION = flag;
        return this;
    }

    /**
     * Init.
     */
    private static void init() {
        InstrumentationHook.start();
        AppInfoUtil.init(currContext);
        DeviceUtil.init(currContext);
        if(ENABLE_ANDFIX_MODE) {
            AndFixHook.init();
        }
        if (ENABLE_STRICT_MODE) {
            initStrictMode();
        }
        if (ENABLE_CRASH_CATCH) {
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init(currContext);
        }
    }

    /**
     * Init strict mode.
     */
    @SuppressWarnings("unchecked")
    private static void initStrictMode() {
        int appFlags = currContext.getApplicationInfo().flags;
        if ((appFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            try {
                Class sMode = Class.forName("android.os.StrictMode");
                Method enableDefaults = sMode.getMethod("enableDefaults");
                enableDefaults.invoke(null);
                Log.w("StrictMode", "Start StrictMode");
            } catch (Exception e) {
                // StrictMode not supported on this device, punt
                Log.w("StrictMode", "... not supported. Skipping...");
            }
        }
    }

    /**
     * Gets window manager instance.
     *
     * @return the window manager instance
     */
    static WindowManager getWindowManagerInstance() {
        return windowManager;
    }

    /**
     * Gets view center.
     *
     * @param text  the text
     * @param index the index
     * @return the view center
     */
    static Point getViewCenter(String text, int index) {
        return PopupWindow.getElementCenterByText(text, index);
    }

    /**
     * Gets view center.
     *
     * @param id the id
     * @return the view center
     */
    static Point getViewCenter(String id) {
        return PopupWindow.getElementCenterById(id);
    }

    /**
     * Gets last toast.
     *
     * @param timeout the timeout
     * @return the last toast
     */
    static String getLastToast(int timeout) {
        Finder finder = new Finder(currContext, timeout);
        TextView toastTextView = (TextView) finder.getView("message", 0);
        if (null != toastTextView) {
            return toastTextView.getText().toString();
        }
        return "";
    }

    /**
     * Gets last toast.
     *
     * @param timeout     the timeout
     * @param excludeText the exclude text
     * @return the last toast
     */
    static String getLastToast(int timeout, String excludeText) {
        Finder finder = new Finder(currContext, timeout);
        TextView toastTextView = (TextView) finder.getTextView("message", excludeText, 0);
        if (null != toastTextView) {
            return toastTextView.getText().toString();
        }
        return "";
    }

    /**
     * Checks whether any music is active
     *
     * @return the boolean
     */
    static boolean isMusicActive() {
        final AudioManager am = (AudioManager) currContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            Log.w(TAG, "isMusicActive: couldn't get AudioManager reference");
            return false;
        }
        return am.isMusicActive();
    }

    /**
     * Report all activity duration.
     */
    public static void reportAllActivityDuration() {
        List<Map.Entry<String, HashMap<String, Integer>>> orderMapList = new ArrayList<Map.Entry<String, HashMap<String, Integer>>>(GlobalVariables.ACTIVITY_DURATION_MAP.entrySet());
        Collections.sort(orderMapList, new Comparator<Map.Entry<String, HashMap<String, Integer>>>() {
            public int compare(Map.Entry<String, HashMap<String, Integer>> o1, Map.Entry<String, HashMap<String, Integer>> o2) {
                return o1.getValue().get("TotalDuration").compareTo(o2.getValue().get("TotalDuration"));
            }
        });
        final StringBuilder durationInfo = new StringBuilder();
        String newline = "\n<br>";
        durationInfo.append("App Name:").append(GlobalVariables.APP_LAUNCH_DURATION_MAP.get("AppName")).append(newline).append(" OnCreate Duration:").append(GlobalVariables.APP_LAUNCH_DURATION_MAP.get("OnCreate")).append(newline);
        for (Map.Entry<String, HashMap<String, Integer>> map : orderMapList) {
            String activityName = map.getKey();
            durationInfo.append("Activity Name:").append(activityName).append(newline);
            if (GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("TotalDuration") > 800) {
                durationInfo.append(" Total Duration:").append("<font color=\"red\">").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("TotalDuration").toString()).append("</font>").append(newline);
            } else {
                durationInfo.append(" Total Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("TotalDuration").toString()).append(newline);
            }
            durationInfo.append(" OnCreate Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("OnCreate").toString()).append(newline);
            durationInfo.append(" OnStart Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("OnStart").toString()).append(newline);
            durationInfo.append(" OnResume Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("OnResume").toString()).append(newline);
            durationInfo.append(newline).append(newline);
        }
        new Thread() {
            @Override
            public void run() {
                Log.w(TAG, durationInfo.toString());
                try {
                    String[] emails = EMAIL_TO.split(" ");
                    MailSender.sendHTMLMail("android_automation@126.com", "Automation123", "smtp.126.com",
                            "Activity Duration Report", durationInfo.toString(),
                            null, emails);
                } catch (MessagingException e) {
                    Log.w(TAG, "send mail error : ", e);
                }
            }
        }.start();
    }

    /**
     * Send activity duration.
     *
     * @param activityName the activity name
     * @param isFirst      the is first
     */
    public static void sendActivityDuration(String activityName, boolean isFirst) {
        final StringBuilder durationInfo = new StringBuilder();
        String emailTitle = "";
        String newline = "\n<br>";
        if (isFirst) {
            emailTitle = "App First Launch Duration";
            durationInfo.append("App Name:").append(GlobalVariables.APP_LAUNCH_DURATION_MAP.get("AppName")).append(newline).append(" OnCreate Duration:").append(GlobalVariables.APP_LAUNCH_DURATION_MAP.get("OnCreate")).append(newline);
            durationInfo.append("Activity Name:").append(activityName).append(newline);
            durationInfo.append(" Activity Total Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("TotalDuration").toString()).append(newline);
            durationInfo.append(" OnCreate Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("OnCreate").toString()).append(newline);
            durationInfo.append(" OnStart Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("OnStart").toString()).append(newline);
            durationInfo.append(" OnResume Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("OnResume").toString()).append(newline);
            durationInfo.append(" App Launch Total Duration:").append("<font color=\"red\">").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("TotalDuration") + Integer.parseInt(GlobalVariables.APP_LAUNCH_DURATION_MAP.get("OnCreate")) + "").append("</font>");
        } else {
            emailTitle = "Activity Duration Report";
            durationInfo.append("Activity Name:").append(activityName);
            if (GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("TotalDuration") > 800) {
                durationInfo.append(" Total Duration:").append("<font color=\"red\">").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("TotalDuration").toString()).append("</font>").append(newline);
            } else {
                durationInfo.append(" Total Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("TotalDuration").toString()).append(newline);
            }
            durationInfo.append(" OnCreate Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("OnCreate").toString()).append(newline);
            durationInfo.append(" OnStart Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("OnStart").toString()).append(newline);
            durationInfo.append(" OnResume Duration:").append(GlobalVariables.ACTIVITY_DURATION_MAP.get(activityName).get("OnResume").toString()).append(newline);
        }
        final String finalEmailTitle = emailTitle;
        new Thread() {
            @Override
            public void run() {
                Log.w(TAG, durationInfo.toString());
                try {
                    String[] emails = EMAIL_TO.split(" ");
                    MailSender.sendHTMLMail("android_automation@126.com", "Automation123", "smtp.126.com",
                            finalEmailTitle, durationInfo.toString(),
                            null, emails);
                } catch (MessagingException e) {
                    Log.w(TAG, "send mail error : ", e);
                }
            }
        }.start();
    }

    /**
     * Sets current context.
     *
     * @param context the context
     * @return the current context
     */
    public static WindowManager setCurrentContext(Context context) {
        currContext = context;
        return windowManager;
    }

    /**
     * Add window.
     *
     * @param activity the activity
     */
    public static void addWindow(Activity activity) {
        windowManager.addWindow(activity);
    }

    /**
     * Remove window.
     *
     * @param activity the activity
     */
    public static void removeWindow(Activity activity) {
        windowManager.removeWindow(activity);
    }

    /**
     * Sets focused window.
     *
     * @param activity the activity
     */
    public static void setFocusedWindow(Activity activity) {
        windowManager.setFocusedWindow(activity);
    }

    /**
     * Starts the server.
     *
     * @return True if the server was successfully created, or false if it already exists.
     * @throws IOException If the server cannot be created.
     * @see #stop() #stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()
     * @see #isRunning() #isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()
     */
    public boolean start() throws IOException {
        if (mThread != null) {
            return false;
        }
        mThread = new Thread(this, "Local View Server [port=" + mPort + "]");
        mThreadPool = Executors.newFixedThreadPool(VIEW_SERVER_MAX_CONNECTIONS);
        mThread.start();
        return true;
    }

    /**
     * Stops the server.
     *
     * @return True if the server was stopped, false if an error occurred or if the server wasn't started.
     * @see #start() #start()#start()#start()#start()#start()#start()#start()#start()#start()#start()#start()#start()#start()#start()
     * @see #isRunning() #isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()
     */
    public boolean stop() {
        if (mThread != null) {
            mThread.interrupt();
            if (mThreadPool != null) {
                try {
                    mThreadPool.shutdownNow();
                } catch (SecurityException e) {
                    Log.w(TAG, "Could not stop all view server threads");
                }
            }
            mThreadPool = null;
            mThread = null;

            try {
                mServer.close();
                mServer = null;
                return true;
            } catch (IOException e) {
                Log.w(TAG, "Could not close the view server");
            }
        }
        windowManager.clearWindows();
        windowManager.clearFocusedWindow();
        return false;
    }

    /**
     * Indicates whether the server is currently running.
     *
     * @return True if the server is running, false otherwise.
     * @see #start() #start()#start()#start()#start()#start()#start()#start()#start()#start()#start()#start()#start()#start()#start()
     * @see #stop() #stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()#stop()
     */
    public boolean isRunning() {
        return mThread != null && mThread.isAlive();
    }

    /**
     * Main server loop.
     */
    public void run() {
        try {
            mServer = new ServerSocket(mPort, VIEW_SERVER_MAX_CONNECTIONS, InetAddress.getLocalHost());
        } catch (Exception e) {
            Log.w(TAG, "Starting ServerSocket error: ", e);
        }

        while (mServer != null && Thread.currentThread() == mThread) {
            // Any uncaught exception will crash the system process
            try {
                Socket client = mServer.accept();
                if (mThreadPool != null) {
                    mThreadPool.submit(new AutomationServerWorker(client));
                } else {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Connection error: ", e);
            }
        }
    }


}
