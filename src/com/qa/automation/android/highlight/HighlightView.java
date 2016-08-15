package com.qa.automation.android.highlight;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.Field;

public class HighlightView {
    public void highlight(Activity activity) {
        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Create a border programmatically
                        ShapeDrawable shape = new ShapeDrawable(new RectShape());
                        shape.getPaint().setColor(Color.RED);
                        shape.getPaint().setStyle(Paint.Style.STROKE);
                        shape.getPaint().setStrokeWidth(5);
                        // Assign the created border to view
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            v.setBackgroundDrawable(shape);
                        } else {
                            v.setBackground(shape);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        break;
                    }
                }
                return false;
            }
        };
        IterateView iterateView = new IterateView();
        iterateView.iterate(activity.getWindow().getDecorView().getRootView());
        for (View view : iterateView.getViews()) {
            if (!hasTouchListener(view)) {
                view.setOnTouchListener(listener);
            }
        }
    }

    private boolean hasTouchListener(View view) {
        // get the nested class `android.view.View$ListenerInfo`
        Field listenerInfoField = null;
        Object listenerInfoObject = null;
        try {
            listenerInfoField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
                listenerInfoObject = listenerInfoField.get(view);
            }

            // get the field mOnClickListener, that holds the listener and cast it to a listener
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

}
