package com.hutcwp.annotation.auxiliary;


public class BusType {

    /**
     * 全局范围application
     */
    public static final int SCOPE_PROJECT = 0;
    /**
     * 插件范围plugin，需要配合中的busName
     */
    public static final int SCOPE_PLUGIN = 1;
    /**
     * 类范围class
     */
    public static final int SCOPE_CLASS = 2;
}
