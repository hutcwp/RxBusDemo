package com.hutcwp.plugin.inject.busEvent

import com.hutcwp.plugin.inject.IClassInjector
import com.hutcwp.plugin.inject.InjectCodeDef
import com.hutcwp.plugin.inject.SniperConstant
import com.hutcwp.plugin.inject.SniperInfo
import javassist.*
import com.hutcwp.plugin.util.LogUtil

import java.lang.annotation.Annotation

class EventInjector implements IClassInjector {

    private def Body_RxBusUnRegister

    private def Body_RxBusRegister

    /**
     * 具体处理各个功能注入的方法
     * @param sniperInfo
     */
    boolean inject(SniperInfo sniperInfo) {
        println('inject sniperInfo ' + sniperInfo)
        if (sniperInfo instanceof EventInfo) {
            return processRxBus((EventInfo) sniperInfo)
        }
        return false
    }

    /**
     * 处理@BusEvent 相关代码生成
     * @param info
     */
    boolean processRxBus(EventInfo info) {
        println('process rx bus... siez = ' + info.busEventMethods?.size())
        if (info.busEventMethods?.size() > 0) {
            injectEventField(info.clazz)
            //插入EventBinder事件代理类订阅和取消订阅代码
            injectEventBinder(info)
            return true
        }
        false
    }

    /**
     * 注入field m%sSniperEventBinder
     * @param ctClass
     */
    void injectEventField(CtClass ctClass) {
        println("injectEventField")
        def Field_EventBinder = String.format("m%sSniperEventBinder", ctClass.simpleName)
        ctClass.addField(CtField.make("private EventBinder " + Field_EventBinder + ";", ctClass))

        Body_RxBusUnRegister = String.format("if(%s != null)%s.unBindEvent();", Field_EventBinder, Field_EventBinder)
        Body_RxBusRegister = String.format("if(%s == null)%s = new %s\$\$EventBinder();\n%s.bindEvent(this);",
                Field_EventBinder, Field_EventBinder, ctClass.name, Field_EventBinder)
    }

    /**
     * 插入RxBus订阅和取消订阅代码
     * @param info
     */
    void injectEventBinder(EventInfo info) {
        LogUtil.info('injectEventBinder info')
        switch (true) {
            case info.isView():
                injectViewEventBinder(info)
                break
            case info.isActivity():
                injectActivityEventBinder(info)
                break
            case info.isFragment():
                injectFragmentEventBinder(info)
                break
            case info.isEventCompat():
                injectEventCompatEventBinder(info)
                break
            default:
                injectOtherEventBinder(info)
        }
    }

    /**
     * 对view inject RxBus代码
     * @param info
     */
    void injectViewEventBinder(EventInfo info) {
        if (info.onAttachedToWindow != null) {
            info.onAttachedToWindow.insertAfter(Body_RxBusRegister)
        } else {
            CtMethod method = CtMethod.make(getRxBusRegisterMethodStr(info), info.clazz)
            info.clazz.addMethod(method)
        }
        if (info.onDetachedFromWindow != null) {
            info.onDetachedFromWindow.insertAfter(Body_RxBusUnRegister)
        } else {
            CtMethod method = CtMethod.make(getRxBusUnRegisterMethodStr(info), info.clazz)
            info.clazz.addMethod(method)
        }
    }

    /**
     * 对activity inject RxBus代码
     * @param info
     */
    void injectActivityEventBinder(EventInfo info) {
        println('injectActivityEventBinder')
        if (info.onCreate != null) {
            info.onCreate.insertAfter(Body_RxBusRegister)
        } else {
            CtMethod method = CtMethod.make(getRxBusRegisterMethodStr(info), info.clazz)
            info.clazz.addMethod(method)
        }
        if (info.onDestroy != null) {
            info.onDestroy.insertAfter(Body_RxBusUnRegister)
        } else {
            CtMethod method = CtMethod.make(getRxBusUnRegisterMethodStr(info), info.clazz)
            info.clazz.addMethod(method)
        }
    }

    /**
     * 对fragment inject RxBus代码
     * @param info
     */
    void injectFragmentEventBinder(EventInfo info) {
        if (info.onViewCreated != null) {
            info.onViewCreated.insertBefore(Body_RxBusRegister)
        } else {
            CtMethod method = CtMethod.make(getRxBusRegisterMethodStr(info), info.clazz)
            info.clazz.addMethod(method)
        }
        if (info.onDestroyView != null) {
            info.onDestroyView.insertAfter(Body_RxBusUnRegister)
        } else {
            CtMethod method = CtMethod.make(getRxBusUnRegisterMethodStr(info), info.clazz)
            info.clazz.addMethod(method)
        }
    }

    /**
     * 对EventCompat inject RxBus代码
     * @param info
     */
    void injectEventCompatEventBinder(EventInfo info) {
        println('injectEventCompatEventBinder')
        EventExprEditor exprEditor
        if (info.onEventBind != null) {
            exprEditor = new EventExprEditor()
            info.onEventBind.instrument(exprEditor)
            StringBuffer codeBody = new StringBuffer()
            if (!exprEditor.superFlag && checkParentImplementsEventCompat(info)) {
                codeBody.append(InjectCodeDef.SUPER_ONEVENTBIND)
            }
            codeBody.append(Body_RxBusRegister)
            info.onEventBind.insertBefore(codeBody.toString())
        } else if (!info.clazz.interface) {
            CtMethod method = CtMethod.make(getRxBusRegisterMethodStr(info), info.clazz)
            info.clazz.addMethod(method)
        }
        if (info.onEventUnBind != null) {
            exprEditor = new EventExprEditor()
            info.onEventUnBind.instrument(exprEditor)
            StringBuffer codeBody = new StringBuffer()
            if (!exprEditor.superFlag && checkParentImplementsEventCompat(info)) {
                codeBody.append(InjectCodeDef.SUPER_ONEVENTUNBIND)
            }
            codeBody.append(Body_RxBusUnRegister)
            info.onEventUnBind.insertBefore(codeBody.toString())
        } else if (!info.clazz.interface) {
            CtMethod method = CtMethod.make(getRxBusUnRegisterMethodStr(info), info.clazz)
            info.clazz.addMethod(method)
        }
    }

    /**
     * 对EventCompat inject RxBus代码
     * @param info
     */
    void injectOtherEventBinder(EventInfo info) {
        println('injectOtherEventBinder')
        if (checkHasDartsAnnotation(info)) {
            CtConstructor[] array = info.clazz.getDeclaredConstructors()
            if (array.length > 0) {
//                array[0].insertBeforeBody(Body_RxBusRegister)
                array[0].insertAfter(Body_RxBusRegister)
            } else {
                CtConstructor ctConstructor = CtNewConstructor.defaultConstructor(info.clazz)
                ctConstructor.setBody(Body_RxBusRegister)
                info.clazz.addConstructor(ctConstructor)
            }
        } else {
            throw new Exception("@BusEvent can't apply " + info.clazz.name + " what is not Activity, Fragment, View or EventCompat 's subClass " +
                    "and is not single class with darts")
        }
    }

    /**
     * 创建rxbus 订阅方法体，
     * @param info
     * @return
     */
    String getRxBusRegisterMethodStr(EventInfo info) {
        def methodCode
        StringBuffer bodyCode = new StringBuffer()
        switch (true) {
            case info.isEventCompat():
                methodCode = InjectCodeDef.METHOD_ONEVENTBIND
                if (checkParentImplementsEventCompat(info)) {
                    println info.clazz.name + "----------------------->parentImplementsEventCompat"
                    bodyCode.append(InjectCodeDef.SUPER_ONEVENTBIND)
                }
                bodyCode.append(Body_RxBusRegister)
                break
            case info.isView():
                methodCode = InjectCodeDef.METHOD_ONATTACHEDTOWINDOW
                bodyCode.append(InjectCodeDef.SUPER_ONATTACHEDTOWINDOW)
                bodyCode.append(Body_RxBusRegister)
                break
            case info.isActivity():
                methodCode = InjectCodeDef.METHOD_ONCREATE
                bodyCode.append(InjectCodeDef.SUPER_ONCREATE)
                bodyCode.append(Body_RxBusRegister)
                break
            case info.isFragment():
                methodCode = InjectCodeDef.METHOD_ONVIEWCREATED
                bodyCode.append(Body_RxBusRegister)
                bodyCode.append(InjectCodeDef.SUPER_ONVIEWCREATED)
                break
            default:
                return ""
        }
//        info.project.logger.error "RxBusRegisterMethod method : \n" + String.format(methodCode, bodyCode.toString())
        String.format(methodCode, bodyCode.toString())
    }

    /**
     * 创建rxbus 取消订阅方法体
     * @param info
     * @return
     */
    String getRxBusUnRegisterMethodStr(EventInfo info) {
        def methodCode
        StringBuffer bodyCode = new StringBuffer()
        switch (true) {
            case info.isEventCompat():
                methodCode = InjectCodeDef.METHOD_ONEVENTUNBIND
                if (checkParentImplementsEventCompat(info)) {
                    println info.clazz.name + "----------------------->parentImplementsEventCompat"
                    bodyCode.append(InjectCodeDef.SUPER_ONEVENTUNBIND)
                }
                break
            case info.isView():
                methodCode = InjectCodeDef.METHOD_ONDETACHEDTOWINDOW
                bodyCode.append(InjectCodeDef.SUPER_ONDETACHEDTOWINDOW)
                break
            case info.isActivity():
                methodCode = InjectCodeDef.METHOD_ONDESTROY
                bodyCode.append(InjectCodeDef.SUPER_ONDESTROY)
                break
            case info.isFragment():
                methodCode = InjectCodeDef.METHOD_ONDESTROYVIEW
                bodyCode.append(InjectCodeDef.SUPER_ONDESTROYVIEW)
                break
            default:
                return ""
        }
        bodyCode.append(Body_RxBusUnRegister)
//        info.project.logger.error "RxBusUnRegisterMethod method : \n" + String.format(methodCode, bodyCode.toString())
        String.format(methodCode, bodyCode.toString())
    }

    /**
     * 判断普通java类是否有注解@DartsRegister，有则可以事件注册
     * @param info
     * @return
     */
    boolean checkHasDartsAnnotation(EventInfo info) {
        info.clazz.getAnnotations().any { Annotation annotation ->
            annotation.annotationType().name.equalsIgnoreCase(InjectCodeDef.DartsRegisterAnnotation)
        }
    }

    /**
     *  判断是此类还是父类 实现接口EventCompat的
     * @param info
     * @return
     */
    boolean checkParentImplementsEventCompat(EventInfo info) {
        return info.clazz.superclass?.subtypeOf(ClassPool.getDefault().get(SniperConstant.EVENT_COMPAT))
    }
}
