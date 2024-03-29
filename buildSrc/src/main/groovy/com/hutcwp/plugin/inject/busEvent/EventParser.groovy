package com.hutcwp.plugin.inject.busEvent

import com.hutcwp.plugin.inject.BaseParser
import com.hutcwp.plugin.inject.InjectCodeDef
import com.hutcwp.plugin.inject.SniperInfo
import com.hutcwp.plugin.inject.busEvent.EventInfo

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project
import com.hutcwp.plugin.util.*

import java.lang.annotation.Annotation

class EventParser extends BaseParser {

    @Override
    SniperInfo parse(Project project, ClassPool pool, CtClass ctClass) {
        EventInfo eventInfo = new EventInfo(super.parse(project, pool, ctClass))
        findBusEventMethod(eventInfo)
        eventInfo
    }

    /**
     * 找出是EventBus类型的
     * 注解类型：BusEvent
     * @param ctClass
     * @param mInfo
     */
    private void findBusEventMethod(EventInfo eventInfo) {
//        println "---------------> findBusEventMethod start"
        eventInfo.clazz.getDeclaredMethods().each { CtMethod method ->
            String methodName = EventUtils.getSimpleName(method)
//            println('method name ' + methodName)
            switch (methodName) {
                case OnEventBind:
                    if (eventInfo.isEventCompat() && method.getParameterTypes().size() == 0) eventInfo.setOnEventBind(method)
                    break
                case OnEventUnBind:
                    if (eventInfo.isEventCompat() && method.getParameterTypes().size() == 0) eventInfo.setOnEventUnBind(method)
                    break
                case OnAttachedToWindow:
                    if (eventInfo.isView() && method.getParameterTypes().size() == 0) eventInfo.setOnAttachedToWindow(method)
                    break
                case OnDetachedFromWindow:
                    if (eventInfo.isView() && method.getParameterTypes().size() == 0) eventInfo.setOnDetachedFromWindow(method)
                    break
                case OnCreate:
                    if (eventInfo.isActivity() && method.getParameterTypes().size() == 1 &&
                            method.getParameterTypes()[0].simpleName == "Bundle") eventInfo.setOnCreate(method)
                    break
                case OnDestroy:
                    if (eventInfo.isActivity() && method.getParameterTypes().size() == 0) eventInfo.setOnDestroy(method)
                    break
                case OnViewCreated:
                    if (eventInfo.isFragment() && method.getParameterTypes().size() == 2 &&
                            method.getParameterTypes()[0].simpleName == "View" && method.getParameterTypes()[1].simpleName == "Bundle") eventInfo.setOnViewCreated(method)
                    break
                case OnDestroyView:
                    if (eventInfo.isFragment() && method.getParameterTypes().size() == 0) eventInfo.setOnDestroyView(method)
                    break
            }
            if (EventUtils.existValidAnnotation(method)) {
                method.getAnnotations().each { Annotation annotation ->
                    if (annotation.annotationType().name.equalsIgnoreCase(InjectCodeDef.busEventAnnotation)) {
//                        println " method:" + method + " annotation -" + annotation.metaPropertyValues.get(0)
                        eventInfo.getBusEventMethods().add(method)
                    }
                }
            }
        }
//        println "---------------> findBusEventMethod end"
    }
}
