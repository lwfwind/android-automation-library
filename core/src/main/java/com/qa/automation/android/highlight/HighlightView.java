package com.qa.automation.android.highlight;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.qa.automation.android.find.ViewFetcher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Highlight view.
 */
public class HighlightView {
    private static String LOG_TAG = "HighlightView";
    private static ShapeDrawable shape = null;
    private static String currActivityName = null;
    private static ArrayList<String> highlightedActivityList = new ArrayList<>();
    private static ArrayList<HashMap<String, View>> existedActivityViewList = new ArrayList<HashMap<String, View>>();
    private static HashMap<View, Drawable> highlightedViewDrawableMap = new HashMap<>();
    private static HashMap<String, View> highlightedActivityViewMap = new HashMap<>();
    private static ViewFetcher viewFetcher = new ViewFetcher();

    static {
        // Create a border programmatically
        shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(Color.RED);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(15);
    }

    /**
     * Remove highlighted activity.
     *
     * @param activityName the activity name
     */
    public static void removeHighlightedActivity(String activityName) {
        currActivityName = activityName;
        if (highlightedActivityList.contains(activityName)) {
            if (highlightedActivityViewMap.get(activityName) != null) {
                View v = highlightedActivityViewMap.get(activityName);
                Drawable drawable = highlightedViewDrawableMap.get(v);
                setBackground(v, drawable);
            }
        }
    }

    /**
     * Highlight.
     *
     * @param activityName the activity name
     * @param view         the view
     */
    public static void highlight(String activityName, View view) {
        HashMap<String, View> activityViewHashMap = new HashMap<String, View>();
        activityViewHashMap.put(activityName, view);
        if (!existedActivityViewList.contains(activityViewHashMap)) {
            existedActivityViewList.add(activityViewHashMap);
        }
        currActivityName = activityName;
        highlightedActivityList.add(currActivityName);
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBackground(v);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        break;
                    }
                }
                return false;
            }
        };
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackground(v);
            }
        };

        ArrayList<View> allViews = new ArrayList<View>();
        try {
            viewFetcher.addChildren(allViews, (ViewGroup) view, false);
        } catch (Exception ignored) {
        }
        for (View v : allViews) {
            if (!(v instanceof ViewGroup)) {
                boolean flag = false;
                if (!hasTouchListener(v)) {
                    v.setOnTouchListener(touchListener);
                    flag = true;
                }
                if (!hasClickListener(v) && !flag) {
                    v.setOnClickListener(clickListener);
                }
            }
        }
    }

    private static void setBackground(View v) {
        if (v.getBackground() != shape) {
            highlightedViewDrawableMap.put(v, v.getBackground());
            if (highlightedActivityViewMap.get(currActivityName) == null) {
                highlightedActivityViewMap.put(currActivityName, v);
            } else {
                View preView = highlightedActivityViewMap.get(currActivityName);
                setBackground(preView, highlightedViewDrawableMap.get(preView));
                highlightedActivityViewMap.put(currActivityName, v);
            }
            // Assign the created border to view
            setBackground(v, shape);
        }
    }

    private static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //Android系统大于等于API16，使用setBackground
            view.setBackground(drawable);
        } else {
            //Android系统小于API16，使用setBackground
            view.setBackgroundDrawable(drawable);
        }
        view.invalidate();
    }

    private static boolean hasTouchListener(View view) {
        // get the nested class `android.view.View$ListenerInfo`
        Field listenerInfoField = null;
        Object listenerInfoObject = null;
        try {
            listenerInfoField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
                listenerInfoObject = listenerInfoField.get(view);
                if (listenerInfoObject == null) {
                    return false;
                }
            }

            // get the field mOnTouchListener, that holds the listener and cast it to a listener
            Field touchListenerField = Class.forName("android.view.View$ListenerInfo").getDeclaredField("mOnTouchListener");
            if (touchListenerField != null && listenerInfoObject != null) {
                touchListenerField.setAccessible(true);
                View.OnTouchListener touchListener = (View.OnTouchListener) touchListenerField.get(listenerInfoObject);
                return touchListener != null;
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "hasTouchListener exception:" + e.getMessage());
        }
        return true;
    }

    private static boolean hasClickListener(View view) {
        // get the nested class `android.view.View$ListenerInfo`
        Field listenerInfoField = null;
        Object listenerInfoObject = null;
        try {
            listenerInfoField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
                listenerInfoObject = listenerInfoField.get(view);
                if (listenerInfoObject == null) {
                    return false;
                }
            }

            // get the field mOnClickListener, that holds the listener and cast it to a listener
            Field clickListenerField = Class.forName("android.view.View$ListenerInfo").getDeclaredField("mOnClickListener");
            if (clickListenerField != null && listenerInfoObject != null) {
                clickListenerField.setAccessible(true);
                View.OnClickListener clickListener = (View.OnClickListener) clickListenerField.get(listenerInfoObject);
                return clickListener != null;
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "hasClickListener exception:" + e.getMessage());
        }
        return true;
    }

}
