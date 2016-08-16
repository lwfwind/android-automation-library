package com.qa.automation.android.highlight;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class HighlightView {
    private static ShapeDrawable shape = null;
    private static Activity activity = null;
    private static ArrayList<Activity> highlightedActivityList = new ArrayList<>();
    private static HashMap<View, Drawable> highlightedViewDrawableMap = new HashMap<>();
    private static HashMap<Activity, ArrayList<View>> highlightedActivityViewListMap = new HashMap<>();

    static {
        // Create a border programmatically
        shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(Color.RED);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(15);
    }

    public static void highlight(Activity act) {
        activity = act;
        if (highlightedActivityList.indexOf(activity) > -1) {
            for (View v : highlightedActivityViewListMap.get(activity)) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    v.setBackgroundDrawable(highlightedViewDrawableMap.get(v));
                } else {
                    v.setBackground(highlightedViewDrawableMap.get(v));
                }
                v.invalidate();
            }
            return;
        }
        highlightedActivityList.add(activity);
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

        IterateView iterateView = new IterateView();
        iterateView.iterate(activity.getWindow().getDecorView().getRootView());
        for (View view : iterateView.getViews()) {
            boolean flag = false;
            if (!hasTouchListener(view)) {
                view.setOnTouchListener(touchListener);
                flag = true;
            }
            if (!hasClickListener(view) && !flag) {
                view.setOnClickListener(clickListener);
            }
        }
    }

    private static void setBackground(View v) {
        if (v.getBackground() != shape) {
            highlightedViewDrawableMap.put(v, v.getBackground());
            if (highlightedActivityViewListMap.get(activity) != null) {
                highlightedActivityViewListMap.get(activity).add(v);
            } else {
                ArrayList<View> viewList = new ArrayList<>();
                viewList.add(v);
                highlightedActivityViewListMap.put(activity, viewList);
            }
            // Assign the created border to view
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                v.setBackgroundDrawable(shape);
            } else {
                v.setBackground(shape);
            }
            v.invalidate();
        }
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return true;
    }

}
