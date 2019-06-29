package com.hutcwp.plugin

import com.hutcwp.plugin.inject.EventInjector
import com.hutcwp.plugin.inject.EventParser
import com.hutcwp.plugin.util.ClassFileVisitor
import javassist.ClassPool
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths

/**
 * 存储所有injector的枚举
 */
enum Injectors {

    INSTANCE

    EventInjector injector = new EventInjector()
    EventParser parser = new EventParser()

    static boolean injectClass(String classPath, String classDirectory, String className, boolean isJar, Closure closure) {
        closure.call(classPath, classDirectory, className, ClassPool.getDefault())
    }

    static boolean injectDir(String rawPath, String path, String packageName, Project project, boolean isJar, Set<String> pluginSet) {
        def visitor = new ClassFileVisitor(packageName, isJar, project, path, rawPath, pluginSet)
        Files.walkFileTree(Paths.get(path), visitor)
        visitor.needInject
    }

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
            println 'ctClass writeFile,ctClass=' + ctClass.name
            ctClass.writeFile(classDirectory)
            ctClass.defrost()
            inject = true
        }
        stream?.close()
        ctClass?.detach()
        inject
    }
}