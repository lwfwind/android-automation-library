package com.qa.automation.android.popupwindow;


import android.view.View;
import android.widget.TextView;
import com.qa.automation.android.AutomationServer;
import com.qa.automation.android.highlight.IterateView;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Popup window.
 */
public class PopupWindow {

    private static IterateView iterateView = new IterateView();


    /**
     * Gets element center by text.
     *
     * @param text  the text
     * @param index the index
     * @return the element center by text
     */
    public static Point getElementCenterByText(String text, int index) {
        ArrayList<View> matchedList = new ArrayList<View>();
        List<View> views = iterateView.iterate(AutomationServer.getActivity().getWindow().getDecorView().getRootView());
        for (View view : views) {
            if (view instanceof TextView) {
                if (((TextView) view).getText().toString().contains(text)) {
                    matchedList.add(view);
                    if (matchedList.size() > index) {
                        int centreX = (int) (view.getX() + view.getWidth() / 2);
                        int centreY = (int) (view.getY() + view.getHeight() / 2);
                        return new Point(centreX, centreY);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets element center by id.
     *
     * @param id the id
     * @return the element center by id
     */
    public static Point getElementCenterById(String id) {
        List<View> views = iterateView.iterate(AutomationServer.getActivity().getWindow().getDecorView().getRootView());
        for (View view : views) {
            if (view.getId() != -1) {
                int viewId = AutomationServer.getCurrContext().getResources().getIdentifier(id, "id", AutomationServer.getCurrContext().getPackageName());
                if (viewId == 0) {
                    viewId = AutomationServer.getCurrContext().getResources().getIdentifier(id, "id", "android");
                }
                if (viewId != 0) {
                    int centreX = (int) (view.getX() + view.getWidth() / 2);
                    int centreY = (int) (view.getY() + view.getHeight() / 2);
                    return new Point(centreX, centreY);
                }
            }
        }
        return null;
    }

}
