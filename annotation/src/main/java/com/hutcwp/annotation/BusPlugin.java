package com.hutcwp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于插件自动创建RxBus总线实例
 * 名称和buffer大小在顶级工程的sniper.gradle中定义
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface BusPlugin {
}
