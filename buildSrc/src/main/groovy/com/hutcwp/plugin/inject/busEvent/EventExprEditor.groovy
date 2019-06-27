package com.hutcwp.plugin.inject.busEvent

import javassist.CannotCompileException
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

class EventExprEditor extends ExprEditor{

    boolean superFlag = false

    @Override
    void edit(MethodCall m) throws CannotCompileException {
        super.edit(m)
        def methodName = m.methodName
        def className = m.className
//        println "EventExprEditor: methodName-------->"+m.method.name+"---------className--------->"+className+"---------signature--------->"+m.signature+"\n"
        if(methodName in ["onEventBind", "onEventUnBind"]){
            println " GetEventCompatCall => ${methodName}():${m.lineNumber}"
            superFlag = superFlag | true
        }
    }
}