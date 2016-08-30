package com.qa.android.exception;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.LinkedList;

/**
 * The type Log queue.
 *
 * @param <T> the type parameter
 */
public class LogQueue<T> extends LinkedList {

    private int maxSize = 100;

    /**
     * Instantiates a new Log queue.
     */
    public LogQueue() {
        super();
    }

    /**
     * Instantiates a new Log queue.
     *
     * @param size the size
     */
    public LogQueue(int size) {
        super();
        this.maxSize = size;
    }

    /**
     * Inque.
     *
     * @param t the t
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unchecked")
    public synchronized void inque(T t) {
        if (this.size() >= maxSize) {
            this.pop();
        }
        this.add(t);
    }


}
