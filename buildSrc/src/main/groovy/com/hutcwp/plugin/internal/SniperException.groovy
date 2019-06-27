package com.hutcwp.plugin.internal

import org.gradle.api.Project

/**
 * Created by huangfan on 2017/8/21.
 * sniper 异常类
 */

class SniperException extends Exception{

    SniperException(Project project, Exception ex){
        super(ex.toString())
        StackTraceElement stackTraceElement= ex.getStackTrace()[0]// 得到异常棧的首个元素
        String line = stackTraceElement.getLineNumber()
        String file = stackTraceElement.getFileName()
        String output = ">>>>>>>>>>sniper inject fail, error msg is: " + ex.toString() +
                ">>>>>>>error fileName is: " + file + ">>>>>>>>>>>>error line is:" + line
        project.logger.error output
        ex.printStackTrace() // 输出完整的异常堆栈信息，方便对位问题
    }
}
