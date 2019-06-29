package com.hutcwp.plugin.util

import com.hutcwp.plugin.SniperInject
import com.hutcwp.plugin.Injectors
import javassist.ClassPool
import javassist.CtClass

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class ClassFileVisitor extends SimpleFileVisitor<Path> {

    def packageName
    def isJar
    def project
    def baseDir
    def rawDir
    Set<String> pluginSet

    def needInject = false

    ClassFileVisitor(
            def packageName, def isJar, def project, def baseDir, def rawDir, def pluginSet) {
        this.packageName = packageName
        this.isJar = isJar
        this.project = project
        this.baseDir = baseDir
        this.rawDir = rawDir
        this.pluginSet = pluginSet
    }

    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String filePath = file.toString()
        //  过滤掉无用的项
        boolean miscCheck = filePath.endsWith(".class") && !filePath.contains('R$') && !filePath.contains('$$') &&
                !filePath.contains('R.class') && !filePath.contains("BuildConfig.class") &&
                !filePath.contains('androidTest') && !filePath.contains("Manifest.class")
        if (miscCheck) {
            println ' -------------> filePath = ' +filePath
            int index = filePath.indexOf(packageName)
            boolean isMyPackage = index != -1
            if (isMyPackage) {
                String className = EventUtils.getClassName(index + 1, filePath)
                println ' ------------->isMyPackage ' + className
                SniperInject.injectClass(filePath, baseDir, className, isJar, {
                    String classPath, String classDirectory, String clazzName, ClassPool pool ->
                        return Injectors.INSTANCE.inject(project, pool, classPath, classDirectory)
                })
            }
        }
        return super.visitFile(file, attrs)
    }
}
