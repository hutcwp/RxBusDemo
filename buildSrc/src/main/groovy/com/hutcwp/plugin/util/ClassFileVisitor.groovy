package com.hutcwp.plugin.util

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.annotation.BooleanMemberValue

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * @author huangfan ( kael )
 * @time 2017/11/15 11:15
 */
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
        boolean miscCheck = filePath.endsWith(".class") && !filePath.contains('R$') && !filePath.contains('$$') &&
                !filePath.contains('R.class') && !filePath.contains("BuildConfig.class") &&
                !filePath.contains('androidTest') && !filePath.contains("Manifest.class")
        if (!DartsInjector.getDartsAptClass(filePath, packageName) && miscCheck) {
            int index = filePath.indexOf(packageName)
            boolean isMyPackage = index != -1
            if (isMyPackage) {
                String className = SniperUtils.getClassName(index + 1, filePath)
//                println ' -------------> ' + className
                if (pluginSet == null || pluginSet.size() == 0 || pluginSet.contains(className)) {
                    needInject = needInject | SniperInject.injectClass(filePath, baseDir, className, isJar,
                            { String classPath, String classDirectory, String clazzName, ClassPool pool, boolean processJar ->
                                CtClass ctClass = safeGetClass(clazzName)
                                if (ctClass != null && !Intercepts.values().any {
                                    it.intercept(ctClass)
                                }) {
                                    println "start parse class-------------->" + className
                                    CtMethod dartsInitializeMethod = ClassInnerIntercept.findDartsInitialize(ctClass)
                                    if (ctClass != null && dartsInitializeMethod != null) {
                                        BooleanMemberValue automatic = SniperUtils.getAnnotationValue(BooleanMemberValue.class,
                                                dartsInitializeMethod.getMethodInfo(), InjectCodeDef.DartsInitializeAnnotation, "automatic")
                                        boolean needInject = automatic != null ? automatic.getValue() : true
                                        println "DartsInitialize automatic inject code decided by ${needInject}"
                                        ctClass.detach()
                                        if (processJar) {
                                            if (needInject) throw SniperException("@DartsInitialize will inject code, so just define in app project, can't in library")
                                        } else {
                                            if (DartsInjector.dartsInitClass == null) {
                                                DartsInjector.dartsInitClass = [baseDir, classPath, className]
                                            } else {
                                                throw SniperException("@DartsInitialize just init once in whole project, include all library," +
                                                        " original DartsInitializeClassName is " + DartsInjector.dartsInitClass.classPath + ">>>current DartsInitializeClassName is " + className)
                                            }
                                        }
                                        return false
                                    } else {
                                        ctClass.detach()
                                        return SniperProcessor.process(project, pool, classPath, classDirectory, className)
                                    }
                                } else {
                                    if(ctClass == null){
                                        println "${className}-------------->can't find CtClass in ClassPool"
                                    }else{
                                        println "intercept ${className}-------------->this class need not inject"
                                    }
                                }
                            })
                }
            }
        }
        if (miscCheck) {
            needInject = needInject | walkFileForReplacer(filePath)
        }
        return super.visitFile(file, attrs)
    }

    private static CtClass safeGetClass(String clazzName) {
        try {
            return ClassPool.getDefault().getCtClass(clazzName)
        } catch (Exception e) {
            return null
        }
    }

    private boolean walkFileForReplacer(String classPath) {
        if (!ReplacerConfigUtils.INSTANCE.createReplacerConfig(project).enable) {
            return false
        }
        String clazzName = classPath.replace(baseDir + File.separator, "")
        try {
            CtClass ctClass = ClassPool.getDefault().getCtClass(clazzName)
            //make a cache
            ctClass.detach()
        } catch (Exception e) {
//            println("getClass failed:" + e)
//            return false
        }
        println "start parse replace class-------------->" + clazzName
        return ReplacerInjector.INSTANCE.inject(project, ClassPool.getDefault(), classPath, baseDir)
    }
}
