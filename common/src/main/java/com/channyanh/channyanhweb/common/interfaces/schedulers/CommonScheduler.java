package com.channyanh.channyanhweb.common.interfaces.schedulers;

public interface CommonScheduler {
    /**
     * Run a task on the main thread
     */
    void run(Runnable runnable);

    /**
     * Run a task asynchronously
     */
    void runAsync(Runnable runnable);
}
