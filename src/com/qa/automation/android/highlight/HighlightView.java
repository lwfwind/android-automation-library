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
            if(!hasClickListener(view) && !flag){
                view.setOnClickListener(clickListener);
            }
        }
    }

    private void setBackground(View v){
        // Create a border programmatically
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(Color.RED);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(15);
        // Assign the created border to view
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackgroundDrawable(shape);
        } else {
            v.setBackground(shape);
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
                if(listenerInfoObject == null){
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

    private boolean hasClickListener(View view) {
        // get the nested class `android.view.View$ListenerInfo`
        Field listenerInfoField = null;
        Object listenerInfoObject = null;
        try {
            listenerInfoField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
                listenerInfoObject = listenerInfoField.get(view);
                if(listenerInfoObject == null){
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
