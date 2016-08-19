package com.qa.automation.android.window;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by kcgw001 on 2016/6/21.
 */
public class WindowManager {

    private final List<WindowListener> mListeners =
            new CopyOnWriteArrayList<WindowListener>();
    private final HashMap<View, String> mWindows = new HashMap<View, String>();
    private final ReentrantReadWriteLock mWindowsLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock mFocusLock = new ReentrantReadWriteLock();
    private View mFocusedWindow;

    /**
     * Find window view.
     *
     * @param hashCode the hash code
     * @return the view
     */
    public View findWindow(int hashCode) {
        if (hashCode == -1) {
            View window = null;
            mWindowsLock.readLock().lock();
            try {
                window = mFocusedWindow;
            } finally {
                mWindowsLock.readLock().unlock();
            }
            return window;
        }


        mWindowsLock.readLock().lock();
        try {
            for (Map.Entry<View, String> entry : mWindows.entrySet()) {
                if (System.identityHashCode(entry.getKey()) == hashCode) {
                    return entry.getKey();
                }
            }
        } finally {
            mWindowsLock.readLock().unlock();
        }

        return null;
    }

    /**
     * List windows boolean.
     *
     * @param client the client
     * @return the boolean
     */
    public boolean listWindows(Socket client) {
        boolean result = true;
        BufferedWriter out = null;

        try {
            mWindowsLock.readLock().lock();

            OutputStream clientStream = client.getOutputStream();
            out = new BufferedWriter(new OutputStreamWriter(clientStream), 8 * 1024);

            for (Map.Entry<View, String> entry : mWindows.entrySet()) {
                out.write(Integer.toHexString(System.identityHashCode(entry.getKey())));
                out.write(' ');
                out.append(entry.getValue());
                out.write('\n');
            }

            out.write("DONE.\n");
            out.flush();
        } catch (Exception e) {
            result = false;
        } finally {
            mWindowsLock.readLock().unlock();

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
     * Gets focused window.
     *
     * @param client the client
     * @return the focused window
     */
    public boolean getFocusedWindow(Socket client) {
        boolean result = true;
        String focusName = null;

        BufferedWriter out = null;
        try {
            OutputStream clientStream = client.getOutputStream();
            out = new BufferedWriter(new OutputStreamWriter(clientStream), 8 * 1024);

            View focusedWindow = null;

            mFocusLock.readLock().lock();
            try {
                focusedWindow = mFocusedWindow;
            } finally {
                mFocusLock.readLock().unlock();
            }

            if (focusedWindow != null) {
                mWindowsLock.readLock().lock();
                try {
                    focusName = mWindows.get(mFocusedWindow);
                } finally {
                    mWindowsLock.readLock().unlock();
                }

                out.write(Integer.toHexString(System.identityHashCode(focusedWindow)));
                out.write(' ');
                out.append(focusName);
            }
            out.write('\n');
            out.flush();
        } catch (Exception e) {
            result = false;
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
     * Invoke this method to register a new view hierarchy.
     *
     * @param activity The activity whose view hierarchy/window to register
     * @see #addWindow(View, String) #addWindow(View, String)#addWindow(View, String)#addWindow(View, String)#addWindow(View, String)
     * @see #removeWindow(Activity) #removeWindow(Activity)#removeWindow(Activity)#removeWindow(Activity)#removeWindow(Activity)
     */
    public void addWindow(Activity activity) {
        String name = activity.getTitle().toString();
        if (TextUtils.isEmpty(name)) {
            name = activity.getClass().getCanonicalName() +
                    "/0x" + System.identityHashCode(activity);
        } else {
            name += "(" + activity.getClass().getCanonicalName() + ")";
        }
        addWindow(activity.getWindow().getDecorView(), name);
    }

    /**
     * Invoke this method to unregister a view hierarchy.
     *
     * @param activity The activity whose view hierarchy/window to unregister
     * @see #addWindow(Activity) #addWindow(Activity)#addWindow(Activity)#addWindow(Activity)#addWindow(Activity)
     * @see #removeWindow(View) #removeWindow(View)#removeWindow(View)#removeWindow(View)#removeWindow(View)
     */
    public void removeWindow(Activity activity) {
        removeWindow(activity.getWindow().getDecorView());
    }

    /**
     * Invoke this method to register a new view hierarchy.
     *
     * @param view A view that belongs to the view hierarchy/window to register
     * @param name The name of the view hierarchy/window to register
     * @see #removeWindow(View) #removeWindow(View)#removeWindow(View)#removeWindow(View)#removeWindow(View)
     */
    public void addWindow(View view, String name) {
        mWindowsLock.writeLock().lock();
        try {
            mWindows.put(view.getRootView(), name);
        } finally {
            mWindowsLock.writeLock().unlock();
        }
        fireWindowsChangedEvent();
    }

    /**
     * Invoke this method to unregister a view hierarchy.
     *
     * @param view A view that belongs to the view hierarchy/window to unregister
     * @see #addWindow(View, String) #addWindow(View, String)#addWindow(View, String)#addWindow(View, String)#addWindow(View, String)
     */
    public void removeWindow(View view) {
        View rootView;
        mWindowsLock.writeLock().lock();
        try {
            rootView = view.getRootView();
            mWindows.remove(rootView);
        } finally {
            mWindowsLock.writeLock().unlock();
        }
        mFocusLock.writeLock().lock();
        try {
            if (mFocusedWindow == rootView) {
                mFocusedWindow = null;
            }
        } finally {
            mFocusLock.writeLock().unlock();
        }
        fireWindowsChangedEvent();
    }

    /**
     * Clear windows.
     */
    public void clearWindows() {
        mWindowsLock.writeLock().lock();
        try {
            mWindows.clear();
        } finally {
            mWindowsLock.writeLock().unlock();
        }
    }

    /**
     * Invoke this method to change the currently focused window.
     *
     * @param activity The activity whose view hierarchy/window hasfocus,                 or null to remove focus
     */
    public void setFocusedWindow(Activity activity) {
        setFocusedWindow(activity.getWindow().getDecorView());
    }

    /**
     * Invoke this method to change the currently focused window.
     *
     * @param view A view that belongs to the view hierarchy/window that has focus,             or null to remove focus
     */
    public void setFocusedWindow(View view) {
        mFocusLock.writeLock().lock();
        try {
            mFocusedWindow = view == null ? null : view.getRootView();
        } finally {
            mFocusLock.writeLock().unlock();
        }
        fireFocusChangedEvent();
    }

    /**
     * Clear focused window.
     */
    public void clearFocusedWindow() {
        mFocusLock.writeLock().lock();
        try {
            mFocusedWindow = null;
        } finally {
            mFocusLock.writeLock().unlock();
        }
        fireFocusChangedEvent();
    }

    private void fireWindowsChangedEvent() {
        for (WindowListener listener : mListeners) {
            listener.windowsChanged();
        }
    }

    private void fireFocusChangedEvent() {
        for (WindowListener listener : mListeners) {
            listener.focusChanged();
        }
    }

    /**
     * Add window listener.
     *
     * @param listener the listener
     */
    public void addWindowListener(WindowListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Remove window listener.
     *
     * @param listener the listener
     */
    public void removeWindowListener(WindowListener listener) {
        mListeners.remove(listener);
    }
}
