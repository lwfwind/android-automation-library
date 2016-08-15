package com.qa.automation.android.highlight;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Iterate a view and all children recursively and generate a visual hierarchy
 */
public class IterateView {
    private List<View> views = new ArrayList<View>();
    public List<View> getViews(){
        return views;
    }

    public void iterate(View view) {
        if (view instanceof ViewGroup) {
            iterateViewChildren(view);
        } else {
            if (view.getId() != -1) {
                views.add(view);
            }
        }
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
