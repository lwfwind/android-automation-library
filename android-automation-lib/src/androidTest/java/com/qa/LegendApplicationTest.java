package com.qa;

import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class LegendApplicationTest extends ApplicationTestCase<LegendApplication> {

    /**
     * Instantiates a new Application test.
     */
    public LegendApplicationTest() {
        super(LegendApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test and fix.
     */
    public void testLegend() {
        createApplication();
    }

}