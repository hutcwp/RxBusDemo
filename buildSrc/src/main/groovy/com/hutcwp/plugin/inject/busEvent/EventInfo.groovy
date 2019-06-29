package com.hutcwp.plugin.inject.busEvent

import com.hutcwp.plugin.inject.SniperInfo
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

class EventInfo extends SniperInfo{

    /**
     * 带有BusEvent注解的方法
     */
    List<CtMethod> busEventMethods = new ArrayList<>()

    /**
     * 自定义view或者dialog是否存在onAttachedToWindow 方法
     */
    CtMethod onAttachedToWindow

    /**
     * 自定义view或者dialog是否存在onDetachedFromWindow 方法
     */
    CtMethod onDetachedFromWindow

    /**
     * activity 是否存在onCreate方法
     */
    CtMethod onCreate

    /**
     * activity 是否存在onDestroy方法
     */
    CtMethod onDestroy

    /**
     * fragment 是否存在onViewCreated方法
     */
    CtMethod onViewCreated

    /**
     * fragment 是否存在onDestroyView方法
     */
    CtMethod onDestroyView

    /**
     * EventCompat兼容类绑定事件方法
     */
    CtMethod onEventBind

    /**
     * EventCompat兼容类绑定事件方法
     */
    CtMethod onEventUnBind

    EventInfo(Project project, ClassPool pool, CtClass clazz) {
        super(project, pool, clazz)
    }

    EventInfo(SniperInfo sniperInfo){
        super(sniperInfo)
    }

    @Override
    public String toString() {
        return "EventInfo{" +
                "busEventMethods=" + busEventMethods +
                ", onAttachedToWindow=" + onAttachedToWindow +
                ", onDetachedFromWindow=" + onDetachedFromWindow +
                ", onCreate=" + onCreate +
                ", onDestroy=" + onDestroy +
                ", onViewCreated=" + onViewCreated +
                ", onDestroyView=" + onDestroyView +
                ", onEventBind=" + onEventBind +
                ", onEventUnBind=" + onEventUnBind +
                '}';
    }
}
