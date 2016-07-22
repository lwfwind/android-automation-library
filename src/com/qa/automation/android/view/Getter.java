package com.qa.automation.android.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

/**
 * The type Getter.
 */
public class Getter {
    private final String LOG_TAG = "Getter";
    private final Context mcontext;
    private int timeout = 5000;
    ;

    /**
     * Instantiates a new Getter.
     *
     * @param context the context
     */
    public Getter(Context context) {
        mcontext = context;
    }

    /**
     * Instantiates a new Getter.
     *
     * @param context the context
     * @param timeout the timeout
     */
    public Getter(Context context, int timeout) {
        mcontext = context;
        this.timeout = timeout;
    }


    /**
     * Waits for a certain view.
     *
     * @param id      the id of the view to wait for
     * @param index   the index of the {@link View}. {@code 0} if only one is available
     * @param timeout the timeout
     * @return the specified View
     * @throws Exception the exception
     */
    public View getView(int id, int index, int timeout) throws Exception {
        Sleeper sleeper = new Sleeper();
        ViewFetcher viewFetcher = new ViewFetcher(mcontext, sleeper);
        Set<View> uniqueViewsMatchingId = new HashSet<View>();
        long endTime = SystemClock.uptimeMillis() + timeout;

        while (SystemClock.uptimeMillis() <= endTime) {
            for (View view : viewFetcher.getAllViews(false)) {
                Integer idOfView = view.getId();
                if (idOfView.equals(id)) {
                    uniqueViewsMatchingId.add(view);

                    if (uniqueViewsMatchingId.size() > index) {
                        return view;
                    }
                }
            }
            sleeper.sleep();
        }
        return null;
    }

    /**
     * Waits for a certain view.
     *
     * @param id          the id of the view to wait for
     * @param index       the index of the {@link View}. {@code 0} if only one is available
     * @param excludeText exclude the specify text of the view
     * @param timeout     the timeout
     * @return the specified View
     * @throws Exception the exception
     */
    public View getTextView(int id, int index, String excludeText, int timeout) throws Exception {
        Sleeper sleeper = new Sleeper();
        ViewFetcher viewFetcher = new ViewFetcher(mcontext, sleeper);
        Set<View> uniqueViewsMatchingId = new HashSet<View>();
        long endTime = SystemClock.uptimeMillis() + timeout;

        while (SystemClock.uptimeMillis() <= endTime) {
            for (View view : viewFetcher.getAllViews(false)) {
                Integer idOfView = view.getId();
                if (idOfView.equals(id) && view instanceof TextView) {
                    TextView textView = (TextView) view;
                    if (!textView.getText().toString().equalsIgnoreCase(excludeText)) {
                        uniqueViewsMatchingId.add(textView);

                        if (uniqueViewsMatchingId.size() > index) {
                            return textView;
                        }
                    }
                }
            }
            sleeper.sleep();
        }
        return null;
    }

    /**
     * Returns a {@code View} with a given id.
     *
     * @param id    the id of the {@link View} to return
     * @param index the index of the {@link View}. {@code 0} if only one is available
     * @return a {@code View} with a given id
     */
    public View getView(String id, int index) {
        View viewToReturn = null;
        try {
            String packageName = mcontext.getPackageName();
            int viewId = mcontext.getResources().getIdentifier(id, "id", packageName);

            if (viewId != 0) {
                viewToReturn = getView(viewId, index, this.timeout);
            }

            if (viewToReturn == null) {
                int androidViewId = mcontext.getResources().getIdentifier(id, "id", "android");
                if (androidViewId != 0) {
                    viewToReturn = getView(androidViewId, index, this.timeout);
                }
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "getView exception:" + e.getMessage());
        }
        return viewToReturn;
    }


    /**
     * Returns a {@code View} with a given id.
     *
     * @param id          the id of the {@link View} to return
     * @param excludeText exclude the specify text of the view
     * @param index       the index of the {@link View}. {@code 0} if only one is available
     * @return a {@code View} with a given id
     */
    public View getTextView(String id, String excludeText, int index) {
        View viewToReturn = null;
        try {
            String packageName = mcontext.getPackageName();
            int viewId = mcontext.getResources().getIdentifier(id, "id", packageName);

            if (viewId != 0) {
                viewToReturn = getTextView(viewId, index, excludeText, this.timeout);
            }

            if (viewToReturn == null) {
                int androidViewId = mcontext.getResources().getIdentifier(id, "id", "android");
                if (androidViewId != 0) {
                    viewToReturn = getTextView(androidViewId, index, excludeText, this.timeout);
                }
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "getView exception:" + e.getMessage());
        }
        return viewToReturn;
    }

}