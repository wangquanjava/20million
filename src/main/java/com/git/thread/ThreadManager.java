package com.git.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wqquan.wang on 2018/8/29.
 */
public class ThreadManager {
    public static final ExecutorService COMMEN_POOL = new ThreadPoolExecutor(
            500,
            700,
            1,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
}
