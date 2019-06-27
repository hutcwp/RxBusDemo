package com.hutcwp.api.event;

/**
 * @author huangfan(kael)
 * @time 2017/9/22 11:37
 */

public class EventApi {

    /**
     * 存储通过com.yy.mobile.RxBus create(final int maxBufferSize,
     * @NonNull final String name) 方法创建的RxBus实例
     * @param name
     * @param rxBus
     */
    public static <T> void registerPluginBus(String name, T rxBus){
        EventCenter.registerPluginBus(name, rxBus);
    }

    /**
     * 释放存储的com.yy.mobile.RxBus
     * @param name
     */
    public static void unRegisterPluginBus(String name){
        EventCenter.unRegisterPluginBus(name);
    }


    /**
     * 获取通过com.yy.mobile.RxBus create(final int maxBufferSize,
     * @NonNull final String name) 方法创建的RxBus实例
     * @param name
     * @return
     */
    public static <T> T getPluginBus(String name){
        return EventCenter.getPluginBus(name);
    }
}
