package com.hutcwp.plugin

import com.hutcwp.plugin.util.ClassFileVisitor
import javassist.ClassPool
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths

public class SniperInject {

    static boolean injectClass(String classPath, String classDirectory, String className, boolean isJar, Closure closure) {
        closure.call(classPath, classDirectory, className, ClassPool.getDefault())
    }

    static boolean injectDir(String rawPath, String path, String packageName, Project project, boolean isJar, Set<String> pluginSet) {
        def visitor = new ClassFileVisitor(packageName, isJar, project, path, rawPath, pluginSet)
        Files.walkFileTree(Paths.get(path), visitor)
        visitor.needInject
    }
}
