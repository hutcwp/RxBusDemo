package com.hutcwp.annotation.auxiliary;



public class SchedulerType {

    /**
     * 设置此参数，scheduler无效
     */
    public static final int THREAD_UNKNOWN = 0;

    /**
     * 当前线程同步调用
     */
    public static final int THREAD_CURRENT = 1;
    /**
     * android主线程异步调用
     */
    public static final int THREAD_MAIN = 2;
    /**
     * io线程异步调用
     */
    public static final int THREAD_IO = 3;
}
