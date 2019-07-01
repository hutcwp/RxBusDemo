package com.hutcwp.plugin

import com.hutcwp.plugin.inject.busEvent.EventInjector
import com.hutcwp.plugin.inject.busEvent.EventParser
import javassist.ClassPool
import org.gradle.api.Project

/**
 * 代码注入
 */
enum Injectors {

    INSTANCE

    EventInjector injector = new EventInjector()
    EventParser parser = new EventParser()

    boolean inject(Project project, ClassPool pool, String classPath, String classDirectory) {
        boolean inject = false
        def stream, ctClass
        println 'inject classpath = ' + classPath
        stream = new FileInputStream(classPath)
        ctClass = pool.makeClass(stream)
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }
        if (injector.inject(parser.parse(project, pool, ctClass))) {
            println 'writeFile,ctClass=' + ctClass.name
            ctClass.writeFile(classDirectory)
            ctClass.defrost()
            inject = true
        }
        stream?.close()
        ctClass?.detach()
        inject
    }
}