package com.hutcwp.plugin.internal

import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project

/**
 * Created by huangfan on 2017/7/4.
 * @BusEvent 注解 处理 数据类
 */

class SniperInfo {

    /**
     * 当前工程应用
     */
    Project project

    /**
     * 当前classPool应用，用于根据限定名加载相应ctclass类
     */
    ClassPool pool

    /**
     * 当前正在处理的CtClass
     */
    CtClass clazz

    /**
     * 是否是android view
     */
    boolean view = false

    /**
     * 是否是android Activity
     */
    boolean activity = false

    /**
     * 是否是android Fragment
     */
    boolean fragment = false

    /**
     * 是否EventCompat接口的实现类
     */
    boolean eventCompat = false

    SniperInfo(){}

    SniperInfo(Project project, ClassPool pool, CtClass clazz){
        this.project = project
        this.pool = pool
        this.clazz = clazz
    }

    SniperInfo(SniperInfo copy){
        this.project = copy.project
        this.pool = copy.pool
        this.clazz = copy.clazz
        this.activity = copy.activity
        this.fragment = copy.fragment
        this.view = copy.view
        this.eventCompat = copy.eventCompat
    }
}
