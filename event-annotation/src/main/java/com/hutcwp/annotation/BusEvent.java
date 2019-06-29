package com.hutcwp.annotation;

import com.hutcwp.annotation.auxiliary.BusType;
import com.hutcwp.annotation.auxiliary.SchedulerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于RxBus事件分发，定义在事件处理函数上，自动注册并在订阅回调中调用该方法
 * 用于RxBus 默认全局总线
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface BusEvent {
    /**
     * 推荐使用scheduler()参数
     * 异步执行订阅事件执行线程，默认在主线程（只有{@link #sync()}为false才有作用）
     * @return
     */
    @Deprecated
    boolean mainThread() default true;

    /**
     * 事件总线名称(BusType.SCOPE_PLUGIN 才用的到)
     * 通过{@link BusPlugin}创建的事件总线，不需要再传总线名称
     * @return
     */
    @Deprecated
    String busName() default "";

    /**
     * 事件总线类型，参考{@link }类
     * @return
     */
    int busType() default BusType.SCOPE_PROJECT;

    /**
     * 推荐使用scheduler()参数
     * 同步还是异步执行，默认false异步,如果sync值为true（表示同步当前线程阻塞调用，则{@link #mainThread()}设置不起作用）
     * @return
     */
    @Deprecated
    boolean sync() default false;

    /**
     * 订阅调用线程参数（当前线程同步调用，主线程、io线程异步调用）
     * @return
     */
    int scheduler() default SchedulerType.THREAD_UNKNOWN;
}
