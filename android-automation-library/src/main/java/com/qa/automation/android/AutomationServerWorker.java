package com.qa.automation.android;

import android.util.Log;
import android.view.View;
import android.view.ViewDebug;

import com.qa.automation.android.window.WindowListener;
import com.qa.automation.android.window.WindowManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * The type Automation server worker.
 */
public class AutomationServerWorker implements Runnable, WindowListener {
    private static final String LOG_TAG = "AutomationServerWorker";

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
    private static final String COMMAND_GET_CENTER = "center";
    private static final String COMMAND_GET_TOAST = "toast";
    private static final String COMMAND_HIGHLIGHT = "highlight";
    private static final String COMMAND_ACTIVITY_DURATION = "sendActivityDuration";
    private static WindowManager windowManager = AutomationServer.getWindowManagerInstance();
    private final Object[] mLock = new Object[0];
    private Socket mClient;
    private boolean mNeedWindowListUpdate;
    private boolean mNeedFocusedWindowUpdate;

    /**
     * Instantiates a new View server worker.
     *
     * @param client the client
     */
    public AutomationServerWorker(Socket client) {
        mClient = client;
        mNeedWindowListUpdate = false;
        mNeedFocusedWindowUpdate = false;
    }

    private static boolean writeValue(Socket client, String value) {
        boolean result;
        BufferedWriter out = null;
        try {
            OutputStream clientStream = client.getOutputStream();
            out = new BufferedWriter(new OutputStreamWriter(clientStream), 8 * 1024);
            out.write(value);
            out.newLine();
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
                    Log.w(LOG_TAG, "error: ", e);
                }
            }
        }
        return result;
    }

    private static boolean writeObject(Socket client, Object obj) {
        boolean result;
        ObjectOutputStream out = null;
        try {
            OutputStream clientStream = client.getOutputStream();
            out = new ObjectOutputStream(clientStream);
            out.writeObject(obj);
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
                    Log.w(LOG_TAG, "error: ", e);
                }
            }
        }
        return result;
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
            boolean result = false;
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
            } else if (COMMAND_HIGHLIGHT.equalsIgnoreCase(command)) {
                if (parameters.trim().equals("1")) {
                    AutomationServer.setHighlightFlag(true);
                } else {
                    AutomationServer.setHighlightFlag(false);
                }
                result = true;
            } else if (COMMAND_GET_TOAST.equalsIgnoreCase(command)) {
                Options options = new Options();
                options.addOption("t", true, "timeout");
                options.addOption("ex", true, "excludeText");
                DefaultParser parser = new DefaultParser();
                String[] args = parameters.split(" ");
                try {
                    CommandLine cl = parser.parse(options, args);
                    if (cl.hasOption("ex")) {
                        String timeout = cl.getOptionValue("t");
                        String excludeText = cl.getOptionValue("ex");
                        result = writeValue(mClient, AutomationServer.getLastToast(Integer.parseInt(timeout), excludeText));
                    } else {
                        String timeout = cl.getOptionValue("t");
                        result = writeValue(mClient, AutomationServer.getLastToast(Integer.parseInt(timeout)));
                    }
                } catch (ParseException e) {
                    Log.w(LOG_TAG, e.getCause());
                    result = false;
                }
            } else if (COMMAND_IS_MUSIC_ACTIVE.equalsIgnoreCase(command)) {
                result = writeValue(mClient, AutomationServer.isMusicActive() ? "true" : "false");
            } else if (COMMAND_ACTIVITY_DURATION.equalsIgnoreCase(command)) {
                AutomationServer.reportAllActivityDuration();
                result = true;
            } else if (COMMAND_GET_CENTER.equalsIgnoreCase(command)) {
                Options options = new Options();
                options.addOption("t", true, "text/id");
                options.addOption("i", true, "index");
                DefaultParser parser = new DefaultParser();
                String[] args = parameters.split(" ");
                try {
                    CommandLine cl = parser.parse(options, args);
                    if (cl.hasOption("i")) {
                        String text = cl.getOptionValue("t");
                        String idx = cl.getOptionValue("i");
                        result = writeObject(mClient, AutomationServer.getViewCenter(text, Integer.parseInt(idx)));
                    } else {
                        String id = cl.getOptionValue("t");
                        result = writeObject(mClient, AutomationServer.getViewCenter(id));
                    }
                } catch (ParseException e) {
                    Log.w(LOG_TAG, e.getCause());
                    result = false;
                }
            } else {
                result = windowCommand(mClient, command, parameters);
            }
            Log.w(LOG_TAG, "execute command: " + request);
            if (!result) {
                Log.w(LOG_TAG, "An error occurred with the command: " + request);
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "Connection error: ", e);
        } finally {
            if (in != null) {
                try {
                    in.close();

                } catch (IOException e) {
                    Log.w(LOG_TAG, "error: ", e);
                }
            }
            if (mClient != null) {
                try {
                    mClient.close();
                } catch (IOException e) {
                    Log.w(LOG_TAG, "error: ", e);
                }
            }
        }
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
                out.write("DONE");
                out.newLine();
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
                    out.write("LIST UPDATE");
                    out.newLine();
                    out.flush();
                }
                if (needFocusedWindowUpdate) {
                    out.write("FOCUS UPDATE");
                    out.newLine();
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
}
