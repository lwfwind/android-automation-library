package com.qa.automation.android.popupwindow;


import android.view.View;
import android.widget.TextView;
import com.qa.automation.android.AutomationServer;
import com.qa.automation.android.find.IterateView;
import com.qa.automation.android.find.ViewFetcher;
import com.qa.serializable.Point;

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
        ViewFetcher viewFetcher = new ViewFetcher(AutomationServer.getCurrContext());
        for (View view : viewFetcher.getAllViews(false)) {
            if (view instanceof TextView) {
                if (((TextView) view).getText().toString().contains(text)) {
                    matchedList.add(view);
                    if (matchedList.size() > index) {
                        int[] location = new int[2];
                        view.getLocationOnScreen(location);
                        int centreX = location[0];
                        int centreY = location[1];
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
        int viewId = AutomationServer.getCurrContext().getResources().getIdentifier(id, "id", AutomationServer.getCurrContext().getPackageName());
        if (viewId == 0) {
            viewId = AutomationServer.getCurrContext().getResources().getIdentifier(id, "id", "android");
        }
        if(viewId != 0) {
            List<View> views = iterateView.iterate(AutomationServer.getActivity().getWindow().getDecorView().getRootView());
            for (View view : views) {
                if (view.getId() != -1 && viewId == view.getId()) {
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    int centreX = location[0];
                    int centreY = location[1];
                    return new Point(centreX, centreY);
                }
            }
        }
        return null;
    }

}
