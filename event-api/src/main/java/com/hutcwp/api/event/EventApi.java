package com.hutcwp.api.event;


public class EventApi {

    /**
     * 存储通过com.yy.mobile.RxBus create(final int maxBufferSize,
     *
     * @param name
     * @param rxBus
     * @NonNull final String name) 方法创建的RxBus实例
     */
    public static <T> void registerPluginBus(String name, T rxBus) {
        EventCenter.registerPluginBus(name, rxBus);
    }

    /**
     * 释放存储的com.yy.mobile.RxBus
     *
     * @param name
     */
    public static void unRegisterPluginBus(String name) {
        EventCenter.unRegisterPluginBus(name);
    }


    /**
     * 获取通过com.yy.mobile.RxBus create(final int maxBufferSize,
     *
     * @param name
     * @return
     * @NonNull final String name) 方法创建的RxBus实例
     */
    public static <T> T getPluginBus(String name) {
        return EventCenter.getPluginBus(name);
    }
}
