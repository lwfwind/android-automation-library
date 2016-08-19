package com.qa.automation.android.find;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The type Finder.
 */
public class Finder {
    private final String LOG_TAG = "Finder";
    private final Context mContext;
    private int timeout = 5000;

    /**
     * Instantiates a new Finder.
     *
     * @param context the context
     */
    public Finder(Context context) {
        mContext = context;
    }

    /**
     * Instantiates a new Finder.
     *
     * @param context the context
     * @param timeout the timeout
     */
    public Finder(Context context, int timeout) {
        mContext = context;
        this.timeout = timeout;
    }
    ;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Waits for a certain view.
     *
     * @param id    the id of the view to wait for
     * @param index the index of the {@link View}. {@code 0} if only one is available
     * @return the specified View
     * @throws Exception the exception
     */
    public View getView(int id, int index) throws Exception {
        Sleeper sleeper = new Sleeper();
        ViewFetcher viewFetcher = new ViewFetcher(mContext, sleeper);
        Set<View> uniqueViewsMatchingId = new HashSet<View>();
        long endTime = SystemClock.uptimeMillis() + this.timeout;

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
     * Returns a {@code View} with a given id.
     *
     * @param id    the id of the {@link View} to return
     * @param index the index of the {@link View}. {@code 0} if only one is available
     * @return a {@code View} with a given id
     */
    public View getView(String id, int index) {
        View viewToReturn = null;
        try {
            String packageName = mContext.getPackageName();
            int viewId = mContext.getResources().getIdentifier(id, "id", packageName);

            if (viewId != 0) {
                viewToReturn = getView(viewId, index);
            }

            if (viewToReturn == null) {
                int androidViewId = mContext.getResources().getIdentifier(id, "id", "android");
                if (androidViewId != 0) {
                    viewToReturn = getView(androidViewId, index);
                }
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "getView exception:" + e.getMessage());
        }
        return viewToReturn;
    }

    /**
     * Waits for a certain view.
     *
     * @param id          the id of the view to wait for
     * @param index       the index of the {@link View}. {@code 0} if only one is available
     * @param excludeText exclude the specify text of the view
     * @return the specified View
     * @throws Exception the exception
     */
    public View getTextView(int id, String excludeText, int index) throws Exception {
        Sleeper sleeper = new Sleeper();
        ViewFetcher viewFetcher = new ViewFetcher(mContext, sleeper);
        Set<View> uniqueViewsMatchingId = new HashSet<View>();
        long endTime = SystemClock.uptimeMillis() + this.timeout;

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
     * get all text views
     *
     * @return the all text views
     * @throws Exception the exception
     */
    public List<TextView> getAllTextView() throws Exception {
        List<TextView> viewList = new ArrayList<TextView>();
        ViewFetcher viewFetcher = new ViewFetcher(mContext);
        for (View view : viewFetcher.getAllViews(false)) {
            if (view instanceof TextView) {
                viewList.add((TextView) view);
            }
        }
        return viewList;
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
            String packageName = mContext.getPackageName();
            int viewId = mContext.getResources().getIdentifier(id, "id", packageName);

            if (viewId != 0) {
                viewToReturn = getTextView(viewId, excludeText, index);
            }

            if (viewToReturn == null) {
                int androidViewId = mContext.getResources().getIdentifier(id, "id", "android");
                if (androidViewId != 0) {
                    viewToReturn = getTextView(androidViewId, excludeText, index);
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
     * @param text  the text of the {@link View} to return
     * @param index the index of the {@link View}. {@code 0} if only one is available
     * @return a {@code View} with a given id
     */
    public View getTextView(String text, int index) {
        View viewToReturn = null;
        try {
            List<TextView> viewList = getAllTextView();
            List<TextView> matchedViewList = ViewUtils.filterViewsByText(viewList, text);
            viewToReturn = matchedViewList.get(index);
        } catch (Exception e) {
            Log.w(LOG_TAG, "getTextView exception:" + e.getMessage());
        }
        return viewToReturn;
    }

}