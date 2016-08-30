package com.qa;

import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class AndFixApplicationTest extends ApplicationTestCase<AndFixApplication> {

    /**
     * Instantiates a new Application test.
     */
    public AndFixApplicationTest() {
        super(AndFixApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test and fix.
     */
    public void testAndFix() {
        createApplication();
    }

}