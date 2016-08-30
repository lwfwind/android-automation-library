package com.qa.android.exception;

/**
 * The type Log queue global.
 */
public class LogQueueGlobal {

    private static LogQueueGlobal mGlobal;
    private static LogQueue<String> logQueue = new LogQueue<>();

    private LogQueueGlobal() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static LogQueueGlobal getInstance() {
        synchronized (LogQueueGlobal.class) {
            if (mGlobal == null) {
                mGlobal = new LogQueueGlobal();
            }
            return mGlobal;
        }
    }

    /**
     * Add.
     *
     * @param s the s
     */
    public void add(String s){
        logQueue.inque(s);
    }

    /**
     * Get log queue log queue.
     *
     * @return the log queue
     */
    public LogQueue<String> getLogQueue(){
        return logQueue;
    }

}
