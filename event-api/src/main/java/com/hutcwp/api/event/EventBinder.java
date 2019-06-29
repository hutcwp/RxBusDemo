package com.hutcwp.api.event;

/**
 * RXBus事件绑定代理类接口类
 */

public interface EventBinder<T> {

    /**
     * 绑定RXBus，register等操作
     * @param t
     */
    void bindEvent(T t);

    /**
     * 取消绑定
     */
    void unBindEvent();

}
