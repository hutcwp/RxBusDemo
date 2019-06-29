package com.hutcwp.api.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class EventCenter {

    private static Map<String, Object> rxBusMap = new ConcurrentHashMap<>();

    static <T> void registerPluginBus(String name, T rxBus) {
        if (rxBusMap.get(name) == null) {
            rxBusMap.put(name, rxBus);
        }
    }

    static void unRegisterPluginBus(String name) {
        rxBusMap.remove(name);
    }

    static <T> T getPluginBus(String name) {
        if (rxBusMap.get(name) != null) {
            return (T) rxBusMap.get(name);
        }
        return null;
    }
}
