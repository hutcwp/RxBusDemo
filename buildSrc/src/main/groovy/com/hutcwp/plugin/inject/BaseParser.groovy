package com.hutcwp.plugin.inject


import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project
import com.hutcwp.plugin.inject.*

/**
 * 基础解析类
 */

class BaseParser implements IParser {

    protected def OnAttachedToWindow = "onAttachedToWindow"
    protected def OnDetachedFromWindow = "onDetachedFromWindow"
    protected def OnCreate = "onCreate"
    protected def OnDestroy = "onDestroy"
    protected def OnViewCreated = "onViewCreated"
    protected def OnDestroyView = "onDestroyView"
    protected def OnEventBind = "onEventBind"
    protected def OnEventUnBind = "onEventUnBind"

    @Override
    SniperInfo parse(Project project, ClassPool pool, CtClass ctClass) {
        SniperInfo sniperInfo = new SniperInfo(project, pool, ctClass)
        analysisClassType(sniperInfo)
        sniperInfo
    }

    /**
     * 通过父类类型或者名称判断class对象是否为android的activity，fragment，view还是eventcompat
     * @param sniperInfo
     * @return
     */
    private void analysisClassType(SniperInfo sniperInfo) {
//        println sniperInfo.clazz.name+"--------------->  analysisClassType start"
        try {
            if (sniperInfo.clazz.subtypeOf(ClassPool.getDefault().get(SniperConstant.EVENT_COMPAT))) {
                sniperInfo.setEventCompat(true)
                return
            }
            def superCls = sniperInfo.clazz.superclass
            while (superCls != null) {
                switch (true) {
                    case superCls.name in SniperConstant.ACTIVITY_RULES:
                        sniperInfo.setActivity(true)
                        return
                    case superCls.name in SniperConstant.FRAGMENT_RULES:
                        sniperInfo.setFragment(true)
                        return
                    case superCls.name in SniperConstant.VIEW_RULES:
                        sniperInfo.setView(true)
                        return
                    default:
                        superCls = superCls.superclass
                }
            }
        } catch (Exception exception) {
            sniperInfo.project.logger.error "---------------> analysisClassType NotFoundException appear, need analysisClassTypeByMethod"
            //父类是provided进来的第三方包,编译的时候jar包不会一起编译，会报找不到，这个时候只能通过getMethods方法去进行判断
            exception.printStackTrace()
            analysisClassTypeByMethod(sniperInfo)
        }
//        println "---------------> analysisClassType end"
    }

    /**
     * 通过所有方法获取class对象是android的activity，fragment，view还是普通class类
     * @param ctClass
     * @param sniperInfo
     */
    private void analysisClassTypeByMethod(SniperInfo sniperInfo) {
//        println "---------------> analysisClassTypeByMethod start"
        int flag = 0
        sniperInfo.clazz.getMethods().each { CtMethod method ->
            switch (SniperUtils.getSimpleName(method)) {
                case OnEventBind:
                    if (method.getParameterTypes().size() == 0) flag = flag | 128
                    break
                case OnEventUnBind:
                    if (method.getParameterTypes().size() == 0) flag = flag | 64
                    break
                case OnViewCreated:
                    if (method.getParameterTypes().size() == 2 && method.getParameterTypes()[0].simpleName == "View"
                            && method.getParameterTypes()[1].simpleName == "Bundle") flag = flag | 32
                    break
                case OnDestroyView:
                    if (method.getParameterTypes().size() == 0) flag = flag | 16
                    break
                case OnCreate:
                    if (method.getParameterTypes().size() == 1 && method.getParameterTypes()[0].simpleName == "Bundle") flag = flag | 8
                    break
                case OnDestroy:
                    if (method.getParameterTypes().size() == 0) flag = flag | 4
                    break
                case OnAttachedToWindow:
                    if (method.getParameterTypes().size() == 0) flag = flag | 2
                    break
                case OnDetachedFromWindow:
                    if (method.getParameterTypes().size() == 0) flag = flag | 1
                    break
            }
        }
        switch (3) {
            case flag & 48 >> 4:
                sniperInfo.setFragment(true)
                break
            case flag & 12 >> 2:
                sniperInfo.setActivity(true)
                break
            case flag & 3:
                sniperInfo.setView(true)
                break
            case flag & 192 >> 6:
                sniperInfo.setEventCompat(true)
                break
        }
//        println "---------------> analysisClassTypeByMethod end"
    }

}
