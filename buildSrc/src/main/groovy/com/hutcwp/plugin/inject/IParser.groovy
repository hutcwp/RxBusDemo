package com.hutcwp.plugin.inject

import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project

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
