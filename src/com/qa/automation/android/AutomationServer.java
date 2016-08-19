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
import android.media.AudioManager;
import android.util.Log;
import android.widget.TextView;
import com.qa.automation.android.exception.CrashHandler;
import com.qa.automation.android.find.Finder;
import com.qa.automation.android.hook.HookHelper;
import com.qa.automation.android.popupwindow.PopupWindow;
import com.qa.automation.android.util.AppInfoUtil;
import com.qa.automation.android.util.DeviceUtil;
import com.qa.automation.android.window.WindowManager;
import com.qa.serializable.Point;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Automation server.
 */
public class AutomationServer implements Runnable {
    /**
     * The default port used to start view servers.
     */
    private static final int VIEW_SERVER_DEFAULT_PORT = 4939;
    private static final int VIEW_SERVER_MAX_CONNECTIONS = 10;

    // Debug facility
    private static final String LOG_TAG = "AutomationServer";

    private static AutomationServer sServer;
    private static WindowManager windowManager = new WindowManager();
    private static Context currContext;
    private static Activity currActivity;
    private static boolean mHighlightFlag = false;
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
     * Gets activity.
     *
     * @return the activity
     */
    public static Activity getActivity() {
        return currActivity;
    }

    /**
     * Sets activity.
     *
     * @param activity the activity
     */
    public static void setActivity(Activity activity) {
        AutomationServer.currActivity = activity;
    }

    /**
     * Returns a unique instance of the AutomationServer. This method should only be
     * called from the main thread of your application. The server will have
     * the same lifetime as your process.
     *
     * @return the automation server
     */
    public static AutomationServer startListening(Context context) {
        if (sServer == null) {
            sServer = new AutomationServer(AutomationServer.VIEW_SERVER_DEFAULT_PORT);
        }

        if (!sServer.isRunning()) {
            try {
                sServer.start();
            } catch (IOException e) {
                Log.w(LOG_TAG, "Error:", e);
            }
        }

        currContext = context;
        init();
        return sServer;
    }

    public static void init(){
        HookHelper.start();
        AppInfoUtil.init(currContext);
        DeviceUtil.init(currContext);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(currContext);
    }

    /**
     * Gets window manager instance.
     *
     * @return the window manager instance
     */
    public static WindowManager getWindowManagerInstance() {
        return windowManager;
    }

    /**
     * Gets highlight flag.
     *
     * @return the highlight flag
     */
    public static boolean getHighlightFlag() {
        return mHighlightFlag;
    }

    /**
     * Sets highlight flag.
     *
     * @param flag the flag
     */
    public static void setHighlightFlag(boolean flag) {
        mHighlightFlag = flag;
    }

    /**
     * Gets view center.
     *
     * @param text  the text
     * @param index the index
     * @return the view center
     */
    public static Point getViewCenter(String text, int index) {
        return PopupWindow.getElementCenterByText(text, index);
    }

    /**
     * Gets view center.
     *
     * @param id the id
     * @return the view center
     */
    public static Point getViewCenter(String id) {
        return PopupWindow.getElementCenterById(id);
    }

    /**
     * Gets last toast.
     *
     * @param timeout the timeout
     * @return the last toast
     */
    public static String getLastToast(int timeout) {
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
    public static String getLastToast(int timeout, String excludeText) {
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
    public static boolean isMusicActive() {
        final AudioManager am = (AudioManager) currContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            Log.w(LOG_TAG, "isMusicActive: couldn't get AudioManager reference");
            return false;
        }
        return am.isMusicActive();
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
     * @see #stop() #stop()#stop()#stop()#stop()#stop()#stop()
     * @see #isRunning() #isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()
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
     * @see #start() #start()#start()#start()#start()#start()#start()
     * @see #isRunning() #isRunning()#isRunning()#isRunning()#isRunning()#isRunning()#isRunning()
     */
    public boolean stop() {
        if (mThread != null) {
            mThread.interrupt();
            if (mThreadPool != null) {
                try {
                    mThreadPool.shutdownNow();
                } catch (SecurityException e) {
                    Log.w(LOG_TAG, "Could not stop all view server threads");
                }
            }
            mThreadPool = null;
            mThread = null;

            try {
                mServer.close();
                mServer = null;
                return true;
            } catch (IOException e) {
                Log.w(LOG_TAG, "Could not close the view server");
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
     * @see #start() #start()#start()#start()#start()#start()#start()
     * @see #stop() #stop()#stop()#stop()#stop()#stop()#stop()
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
            Log.w(LOG_TAG, "Starting ServerSocket error: ", e);
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
                Log.w(LOG_TAG, "Connection error: ", e);
            }
        }
    }


}
