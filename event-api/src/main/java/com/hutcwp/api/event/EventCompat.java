package com.hutcwp.api.event;

/**
 * 普通类（非fragment，activity，view以及mvppresenter类）的事件兼容接口
 * RxBus的订阅和取消订阅会自动插入onEventBind和onEventUnBind方法中去，否则只在类构造函数插入订阅代码
 */

public interface EventCompat {

    void onEventBind();

    void onEventUnBind();
}
