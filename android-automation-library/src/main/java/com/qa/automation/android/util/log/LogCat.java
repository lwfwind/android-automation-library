package com.qa.automation.android.util.log;

import android.os.SystemClock;

import com.qa.automation.android.find.Sleeper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by kcgw001 on 2016/9/9.
 */
public class LogCat {
    private final static Sleeper sleeper = new Sleeper();

    /**
     * Waits for a log message to appear.
     * Requires read logs permission (android.permission.READ_LOGS) in AndroidManifest.xml of the application under test.
     *
     * @param logMessage the log message to wait for
     * @param timeout the amount of time in milliseconds to wait
     * @return true if log message appears and false if it does not appear before the timeout
     */
    public static boolean getLogMessages(String logMessage, int timeout){


        long endTime = SystemClock.uptimeMillis() + timeout;
        while (SystemClock.uptimeMillis() <= endTime) {
            if(getLog().lastIndexOf(logMessage) != -1){
                return true;
            }
            sleeper.sleep();
        }
        return false;
    }

    /**
     * Returns the log in the given stringBuilder.
     *
     * @return the log
     */

    private static StringBuilder getLog() {
        StringBuilder stringBuilder = new StringBuilder();
        Process p = null;
        BufferedReader reader = null;
        String line = null;

        try {
            // read output from logcat
            p = Runtime.getRuntime().exec("logcat -d");
            reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

            stringBuilder.setLength(0);
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            // read error from logcat
            StringBuilder errorLog = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));
            errorLog.append("logcat returns error: ");
            while ((line = reader.readLine()) != null) {
                errorLog.append(line);
            }
            reader.close();

            // Exception would be thrown if we get exitValue without waiting for the process
            // to finish
            p.waitFor();

            // if exit value of logcat is non-zero, it means error
            if (p.exitValue() != 0) {
                destroy(p, reader);

                throw new Exception(errorLog.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        destroy(p, reader);
        return stringBuilder;
    }

    /**
     * Destroys the process and closes the BufferedReader.
     *
     * @param p the process to destroy
     * @param reader the BufferedReader to close
     */
    private static void destroy(Process p, BufferedReader reader){
        p.destroy();
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
