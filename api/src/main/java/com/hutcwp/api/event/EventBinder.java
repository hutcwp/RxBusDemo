package com.hutcwp.api.event;

/**
 * @author huangfan(kael)
 * @time 2017/9/22 10:43
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
