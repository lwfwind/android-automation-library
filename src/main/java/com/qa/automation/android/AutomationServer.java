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
import com.qa.automation.android.View.Getter;
import com.qa.automation.android.Window.WindowListener;
import com.qa.automation.android.Window.WindowManager;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>This class can be used to enable the use of HierarchyViewer inside an
 * application. HierarchyViewer is an Android SDK tool that can be used
 * to inspect and debug the user interface of running applications. For
 * security reasons, HierarchyViewer does not work on production builds
 * (for instance phones bought in store.) By using this class, you can
 * make HierarchyViewer work on any device. You must be very careful
 * however to only enable HierarchyViewer when debugging your
 * application.</p>
 * <p>
 * <p>To use this view server, your application must require the INTERNET
 * permission.</p>
 * <p>
 * <p>The recommended way to use this API is to register activities when
 * they are created, and to unregister them when they get destroyed:</p>
 * <p>
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
 * <p>
 * <p>
 * In a similar fashion, you can use this API with an InputMethodService:
 * </p>
 * <p>
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

    private static final String VALUE_PROTOCOL_VERSION = "4";
    private static final String VALUE_SERVER_VERSION = "4";

    // Protocol commands
    // Returns the protocol version
    private static final String COMMAND_PROTOCOL_VERSION = "PROTOCOL";
    // Returns the server version
    private static final String COMMAND_SERVER_VERSION = "SERVER";
    // Lists all of the available windows in the system
    private static final String COMMAND_WINDOW_MANAGER_LIST = "LIST";
    // Keeps a connection open and notifies when the list of windows changes
    private static final String COMMAND_WINDOW_MANAGER_AUTOLIST = "AUTOLIST";
    // Returns the focused window
    private static final String COMMAND_WINDOW_MANAGER_GET_FOCUS = "GET_FOCUS";

    private static final String COMMAND_IS_MUSIC_ACTIVE = "isMusicActive";
    private static final String COMMAND_GET_TOAST = "toast";

    private static AutomationServer sServer;
    private static WindowManager windowManager = new WindowManager();
    private static Context mContext;
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

    private static boolean writeValue(Socket client, String value) {
        boolean result;
        BufferedWriter out = null;
        try {
            OutputStream clientStream = client.getOutputStream();
            out = new BufferedWriter(new OutputStreamWriter(clientStream), 8 * 1024);
            out.write(value);
            out.write("\n");
            out.flush();
            result = true;
        } catch (Exception e) {
            result = false;
            Log.w(LOG_TAG, "Error:", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Starts the server.
     *
     * @return True if the server was successfully created, or false if it already exists.
     * @throws IOException If the server cannot be created.
     * @see #stop() #stop()
     * @see #isRunning() #isRunning()
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
     * @see #start() #start()
     * @see #isRunning() #isRunning()
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
     * @see #start() #start()
     * @see #stop() #stop()
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
                    mThreadPool.submit(new ViewServerWorker(client));
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

    private static class UncloseableOutputStream extends OutputStream {
        private final OutputStream mStream;

        /**
         * Instantiates a new Uncloseable output stream.
         *
         * @param stream the stream
         */
        UncloseableOutputStream(OutputStream stream) {
            mStream = stream;
        }

        public void close() throws IOException {
            // Don't close the stream
        }

        public boolean equals(Object o) {
            return mStream.equals(o);
        }

        public void flush() throws IOException {
            mStream.flush();
        }

        public int hashCode() {
            return mStream.hashCode();
        }

        public String toString() {
            return mStream.toString();
        }

        public void write(byte[] buffer, int offset, int count)
                throws IOException {
            mStream.write(buffer, offset, count);
        }

        public void write(byte[] buffer) throws IOException {
            mStream.write(buffer);
        }

        public void write(int oneByte) throws IOException {
            mStream.write(oneByte);
        }
    }

    private static class NoopViewServer extends AutomationServer {
        private NoopViewServer() {
        }

        @Override
        public boolean start() throws IOException {
            return false;
        }

        @Override
        public boolean stop() {
            return false;
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public void run() {
        }
    }

    private class ViewServerWorker implements Runnable, WindowListener {
        private final Object[] mLock = new Object[0];
        private Socket mClient;
        private boolean mNeedWindowListUpdate;
        private boolean mNeedFocusedWindowUpdate;

        /**
         * Instantiates a new View server worker.
         *
         * @param client the client
         */
        public ViewServerWorker(Socket client) {
            mClient = client;
            mNeedWindowListUpdate = false;
            mNeedFocusedWindowUpdate = false;
        }

        public void run() {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(mClient.getInputStream()), 1024);

                final String request = in.readLine();

                String command;
                String parameters;

                int index = request.indexOf(' ');
                if (index == -1) {
                    command = request;
                    parameters = "";
                } else {
                    command = request.substring(0, index);
                    parameters = request.substring(index + 1);
                }

                boolean result;
                if (COMMAND_PROTOCOL_VERSION.equalsIgnoreCase(command)) {
                    result = writeValue(mClient, VALUE_PROTOCOL_VERSION);
                } else if (COMMAND_SERVER_VERSION.equalsIgnoreCase(command)) {
                    result = writeValue(mClient, VALUE_SERVER_VERSION);
                } else if (COMMAND_WINDOW_MANAGER_LIST.equalsIgnoreCase(command)) {
                    result = windowManager.listWindows(mClient);
                } else if (COMMAND_WINDOW_MANAGER_GET_FOCUS.equalsIgnoreCase(command)) {
                    result = windowManager.getFocusedWindow(mClient);
                } else if (COMMAND_WINDOW_MANAGER_AUTOLIST.equalsIgnoreCase(command)) {
                    result = windowManagerAutolistLoop();
                } else if (COMMAND_GET_TOAST.equalsIgnoreCase(command)) {
                    int timeout = Integer.parseInt(parameters);
                    result = writeValue(mClient, getLastToast(timeout));
                } else if (COMMAND_IS_MUSIC_ACTIVE.equalsIgnoreCase(command)) {
                    result = writeValue(mClient, isMusicActive(mContext) ? "true" : "false");

                } else {
                    result = windowCommand(mClient, command, parameters);
                }
                Log.w(LOG_TAG, "execute command: " + command);
                if (!result) {
                    Log.w(LOG_TAG, "An error occurred with the command: " + command);
                }
            } catch (IOException e) {
                Log.w(LOG_TAG, "Connection error: ", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (mClient != null) {
                    try {
                        mClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private String getLastToast(int timeout) {
            Getter getter = new Getter(mContext,timeout);
            TextView toastTextView = (TextView) getter.getView("message", 0);
            if (null != toastTextView) {
                return toastTextView.getText().toString();
            }
            return "";
        }

        /**
         * Checks whether any music is active
         *
         * @param context the context
         * @return the boolean
         */
        public boolean isMusicActive(Context context) {
            final AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am == null) {
                Log.d(LOG_TAG, "isMusicActive: couldn't get AudioManager reference");
                return false;
            }
            return am.isMusicActive();
        }

        private boolean windowCommand(Socket client, String command, String parameters) {
            boolean success = true;
            BufferedWriter out = null;

            try {
                // Find the hash code of the window
                int index = parameters.indexOf(' ');
                if (index == -1) {
                    index = parameters.length();
                }
                final String code = parameters.substring(0, index);
                int hashCode = (int) Long.parseLong(code, 16);

                // Extract the command's parameter after the window description
                if (index < parameters.length()) {
                    parameters = parameters.substring(index + 1);
                } else {
                    parameters = "";
                }

                final View window = windowManager.findWindow(hashCode);
                if (window == null) {
                    return false;
                }

                // call stuff
                final Method dispatch = ViewDebug.class.getDeclaredMethod("dispatchCommand",
                        View.class, String.class, String.class, OutputStream.class);
                dispatch.setAccessible(true);
                dispatch.invoke(null, window, command, parameters,
                        new UncloseableOutputStream(client.getOutputStream()));

                if (!client.isOutputShutdown()) {
                    out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                    out.write("DONE\n");
                    out.flush();
                }

            } catch (Exception e) {
                Log.w(LOG_TAG, "Could not send command " + command +
                        " with parameters " + parameters, e);
                success = false;
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        success = false;
                    }
                }
            }
            return success;
        }

        public void windowsChanged() {
            synchronized (mLock) {
                mNeedWindowListUpdate = true;
                mLock.notifyAll();
            }
        }

        public void focusChanged() {
            synchronized (mLock) {
                mNeedFocusedWindowUpdate = true;
                mLock.notifyAll();
            }
        }

        private boolean windowManagerAutolistLoop() {
            windowManager.addWindowListener(this);
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(mClient.getOutputStream()));
                while (!Thread.interrupted()) {
                    boolean needWindowListUpdate = false;
                    boolean needFocusedWindowUpdate = false;
                    synchronized (mLock) {
                        while (!mNeedWindowListUpdate && !mNeedFocusedWindowUpdate) {
                            mLock.wait();
                        }
                        if (mNeedWindowListUpdate) {
                            mNeedWindowListUpdate = false;
                            needWindowListUpdate = true;
                        }
                        if (mNeedFocusedWindowUpdate) {
                            mNeedFocusedWindowUpdate = false;
                            needFocusedWindowUpdate = true;
                        }
                    }
                    if (needWindowListUpdate) {
                        out.write("LIST UPDATE\n");
                        out.flush();
                    }
                    if (needFocusedWindowUpdate) {
                        out.write("FOCUS UPDATE\n");
                        out.flush();
                    }
                }
            } catch (Exception e) {
                Log.w(LOG_TAG, "Connection error: ", e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
                windowManager.removeWindowListener(this);
            }
            return true;
        }
    }
}
