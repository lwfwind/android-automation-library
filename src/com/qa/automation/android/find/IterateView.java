package com.qa.automation.android.find;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Iterate a view and all children recursively and generate a visual hierarchy
 */
public class IterateView {
    private List<View> views = new ArrayList<View>();

    /**
     * Gets views.
     *
     * @return the views
     */
    public List<View> getViews() {
        return views;
    }

    /**
     * Iterate list.
     *
     * @param view the view
     * @return the list
     */
    public List<View> iterate(View view) {
        if (view instanceof ViewGroup) {
            iterateViewChildren(view);
        } else {
            if (view.getId() != -1) {
                views.add(view);
            }
        }
        return views;
    }

    private void iterateViewChildren(View view) {
        ViewGroup vGroup = (ViewGroup) view;
        for (int i = 0; i < vGroup.getChildCount(); i++) {
            View vChild = vGroup.getChildAt(i);
            if (!(vChild instanceof ViewGroup)) {
                if (vChild.getId() != -1) {
                    views.add(vChild);
                }
            } else {
                iterateViewChildren(vChild);
            }
        }
    }
}
