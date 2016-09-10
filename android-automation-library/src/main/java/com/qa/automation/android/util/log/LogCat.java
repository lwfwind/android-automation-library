package com.qa.automation.android.util.log;


import com.qa.automation.android.GlobalVariables;
import com.qa.automation.android.find.Sleeper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The type Log cat.
 */
public class LogCat {
    private final static Sleeper sleeper = new Sleeper();

    /**
     * Returns the log in the given stringBuilder.
     *
     * @return the log
     */
    public static StringBuilder getRecentLogs() {
        StringBuilder stringBuilder = new StringBuilder();
        Process p = null;
        BufferedReader reader = null;
        String line = null;

        try {
            // read output from logcat
            int id = android.os.Process.myPid();
            p = Runtime.getRuntime().exec("logcat -d | grep "+id);
            reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

            stringBuilder.setLength(0);
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                LogQueueGlobal.getInstance().add(line);
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
