package com.qa.automation.android.View;

import android.view.View;

import java.util.Comparator;

/**
 * The type View location comparator.
 */
public class ViewLocationComparator implements Comparator<View> {

    private final int a[] = new int[2];
    private final int b[] = new int[2];
    private final int axis1, axis2;

    /**
     * Instantiates a new View location comparator.
     */
    public ViewLocationComparator() {
        this(true);
    }

    /**
     * Instantiates a new View location comparator.
     *
     * @param yAxisFirst Whether the y-axis should be compared before the x-axis.
     */
    public ViewLocationComparator(boolean yAxisFirst) {
        this.axis1 = yAxisFirst ? 1 : 0;
        this.axis2 = yAxisFirst ? 0 : 1;
    }

    public int compare(View lhs, View rhs) {
        lhs.getLocationOnScreen(a);
        rhs.getLocationOnScreen(b);

        if (a[axis1] != b[axis1]) {
            return a[axis1] < b[axis1] ? -1 : 1;
        }
        if (a[axis2] < b[axis2]) {
            return -1;
        }
        return a[axis2] == b[axis2] ? 0 : 1;
    }
}
