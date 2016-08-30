package com.qa.android.popupwindow;


import android.view.View;

import com.qa.android.AutomationServer;
import com.qa.android.find.Finder;
import com.qa.serializable.Point;

/**
 * The type Popup window.
 */
public class PopupWindow {

    /**
     * Gets element center by text.
     *
     * @param text  the text
     * @param index the index
     * @return the element center by text
     */
    public static Point getElementCenterByText(String text, int index) {
        Finder finder = new Finder(AutomationServer.getCurrContext());
        View view = finder.getTextView(text, index);
        if (view != null) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int centreX = location[0];
            int centreY = location[1];
            return new Point(centreX, centreY);
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
        Finder finder = new Finder(AutomationServer.getCurrContext());
        View view = finder.getView(id, 0);
        if (view != null) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int centreX = location[0];
            int centreY = location[1];
            return new Point(centreX, centreY);
        }
        return null;
    }

}
