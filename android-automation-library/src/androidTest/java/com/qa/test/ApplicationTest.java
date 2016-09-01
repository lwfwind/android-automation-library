package com.qa.test;

import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<MyApplication> {

    /**
     * Instantiates a new MyApplication test.
     */
    public ApplicationTest() {
        super(MyApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    /**
     * Test and fix.
     */
    public void testAndFix() {
        getApplication().AndFix();
    }

}