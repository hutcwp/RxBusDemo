package com.hutcwp.plugin.other

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtMethod
import org.gradle.api.Project

public class MyInject {
    private static ClassPool classPool = ClassPool.getDefault()

    public static void injectOnCreate(String path, Project project) {
        println 'path=' + path
        println 'path2=' + project.android.bootClasspath[0].toString()

        classPool.appendClassPath(path)
        classPool.appendClassPath(project.android.bootClasspath[0].toString())
        classPool.importPackage("android.os.Bundle")

        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                if (file.getName().equals("MainActivity.class")) {
                    // 获取 MainActivity
                    CtClass ctClass = classPool.getCtClass("com.hutcwp.rxbusdemo.MainActivity")
                    println("ctClass = " + ctClass)
                     CtClass ctClass2 = classPool.getCtClass("com.hutcwp.rxbusdemo.rxbus.Presenter")
                    println("ctClass2 = " + ctClass2)

                    // 解冻
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    // 获取到 onCreate() 方法
                    CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")
                    println("ctMethod = " + ctMethod)
                    // 插入日志打印代码
                    String insertBeforeStr = """android.util.Log.e("--->", "Hello");"""

                    ctMethod.insertBefore(insertBeforeStr)
                    ctClass.writeFile(path)
                    ctClass.detach()
                }
            }
        }
    }
}