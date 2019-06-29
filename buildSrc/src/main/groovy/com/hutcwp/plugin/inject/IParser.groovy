package com.hutcwp.plugin.inject


import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project

/**
 * @author huangfan(kael)
 * @time 2017/8/27 22:35
 */

interface IParser {

    /**
     * 解析
     * @param project
     * @param path
     * @param pool
     * @return
     */
    SniperInfo parse(Project project, ClassPool pool, CtClass ctClass)
}
