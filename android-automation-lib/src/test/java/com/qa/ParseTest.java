package com.qa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

/**
 * The type Parse test.
 */
public class ParseTest {

    /**
     * Test parse.
     */
    @Test
    public void testParse() {
        String parameters = "-t 20000 -ex 请稍候...";
        Options options = new Options();
        options.addOption("t", true, "timeout");
        options.addOption("ex", true, "excludeText");
        DefaultParser parser = new DefaultParser();
        String[] args = parameters.split(" ");
        try {
            CommandLine cl = parser.parse(options, args);
            if (cl.hasOption("ex")) {
                String timeout = cl.getOptionValue("t");
                String excludeText = cl.getOptionValue("ex");
                System.out.println(timeout);
                System.out.println(excludeText);
            } else {
                String timeout = cl.getOptionValue("t");
                System.out.println(timeout);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test parse 2.
     */
    @Test
    public void testParse2() {
        String parameters = "-t 20000";
        Options options = new Options();
        options.addOption("t", true, "timeout");
        options.addOption("ex", true, "excludeText");
        DefaultParser parser = new DefaultParser();
        String[] args = parameters.split(" ");
        try {
            CommandLine cl = parser.parse(options, args);
            if (cl.hasOption("ex")) {
                String timeout = cl.getOptionValue("t");
                String excludeText = cl.getOptionValue("ex");
                System.out.println(timeout);
                System.out.println(excludeText);
            } else {
                String timeout = cl.getOptionValue("t");
                System.out.println(timeout);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
