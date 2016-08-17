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
import android.view.View;
import android.view.ViewDebug;
import android.widget.TextView;
import com.qa.automation.android.view.Getter;
import com.qa.automation.android.window.WindowListener;
import com.qa.automation.android.window.WindowManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class can be used to enable the use of HierarchyViewer inside an
 * application. HierarchyViewer is an Android SDK tool that can be used
 * to inspect and debug the user interface of running applications. For
 * security reasons, HierarchyViewer does not work on production builds
 * (for instance phones bought in store.) By using this class, you can
 * make HierarchyViewer work on any device. You must be very careful
 * however to only enable HierarchyViewer when debugging your
 * application.
 * To use this view server, your application must require the INTERNET
 * permission.
 * The recommended way to use this API is to register activities when
 * they are created, and to unregister them when they get destroyed:
 * <pre>
 * public class MyActivity extends Activity {
 *     public void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         // Set content view, etc.
 *         AutomationServer.setCurrentContext(this).addWindow(this);
 *     }
 *
 *     public void onDestroy() {
 *         super.onDestroy();
 *         AutomationServer.setCurrentContext(this).removeWindow(this);
 *     }
 *
 *     public void onResume() {
 *         super.onResume();
 *         AutomationServer.setCurrentContext(this).setFocusedWindow(this);
 *     }
 * }
 * </pre>
 * In a similar fashion, you can use this API with an InputMethodService:
 * <pre>
 * public class MyInputMethodService extends InputMethodService {
 *     public void onCreate() {
 *         super.onCreate();
 *         View decorView = getWindow().getWindow().getDecorView();
 *         String name = "MyInputMethodService";
 *         AutomationServer.setCurrentContext(this).addWindow(decorView, name);
 *     }
 *
 *     public void onDestroy() {
 *         super.onDestroy();
 *         View decorView = getWindow().getWindow().getDecorView();
 *         AutomationServer.setCurrentContext(this).removeWindow(decorView);
 *     }
 *
 *     public void onStartInput(EditorInfo attribute, boolean restarting) {
 *         super.onStartInput(attribute, restarting);
 *         View decorView = getWindow().getWindow().getDecorView();
 *         AutomationServer.setCurrentContext(this).setFocusedWindow(decorView);
 *     }
 * }
 * </pre>
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
    private static Context mContext;
    private final int mPort;
    private static boolean mHighlightFlag = false;
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
     * Returns a unique instance of the AutomationServer. This method should only be
     * called from the main thread of your application. The server will have
     * the same lifetime as your process.
     *
     * @return the automation server
     */
    public static AutomationServer startListening() {
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

        return sServer;
    }

    /**
     * Starts the server.
     *
     * @return True if the server was successfully created, or false if it already exists.
     * @throws IOException If the server cannot be created.
     * @see #stop() #stop()#stop()#stop()
     * @see #isRunning() #isRunning()#isRunning()#isRunning()
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
     * @see #start() #start()#start()#start()
     * @see #isRunning() #isRunning()#isRunning()#isRunning()
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
     * @see #start() #start()#start()#start()
     * @see #stop() #stop()#stop()#stop()
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

    public static WindowManager getWindowManagerInstance(){
        return windowManager;
    }

    public static boolean getHighlightFlag(){
        return mHighlightFlag;
    }

    public static void setHighlightFlag(boolean flag){
        mHighlightFlag=flag;
    }

    /**
     * Gets last toast.
     *
     * @param timeout the timeout
     * @return the last toast
     */
    public static String getLastToast(int timeout) {
        Getter getter = new Getter(mContext, timeout);
        TextView toastTextView = (TextView) getter.getView("message", 0);
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
        Getter getter = new Getter(mContext, timeout);
        TextView toastTextView = (TextView) getter.getTextView("message", excludeText, 0);
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
        final AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            Log.d(LOG_TAG, "isMusicActive: couldn't get AudioManager reference");
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
        mContext = context;
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




}
