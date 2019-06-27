package com.hutcwp.plugin

import com.android.build.gradle.AppPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency;

/**
 * Created by hutcwp on 2019-06-27 16:51
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/

class BusEventPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        System.out.println("======BusEventPlugin=====");
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
//            rootProject.subprojects.each {
//                if (it.name != project.name) {
//                    it.afterEvaluate {
//                        it.apply plugin: "busevent"
//                    }
//                }
//            }

            print "======start====="
            /* 获取现在使用的sniper插件版本号 */
//            def version
//            def configuration = rootProject.buildscript.configurations.classpath
//            ResolvedConfiguration resolvedConfiguration = configuration.resolvedConfiguration
//            resolvedConfiguration.firstLevelModuleDependencies.each { ResolvedDependency d ->
////                project.logger.error "---->d.moduleGroup--->"+d.moduleGroup+"----->d.moduleName------>"+d.moduleName+"---->d.moduleVersion----->"+d.moduleVersion
//                if (d.moduleGroup == SniperConstant.MODULE_GROUP && d.moduleName == SniperConstant.MODULE_NAME) {
//                    version = d.moduleVersion
//                }
//            }
            print "======end====="

            /* 注册SniperTransform */
            project.android.registerTransform(new BusEventTransform(project))
//            project.android.registerTransform(new BusEventTransform())

            println "================================================================="
//            println "Sniper plugin ${version}(gradle version ${project.gradle.gradleVersion}) startup!"
//            println "RxBus Class Name is ----- " + config.sniperRxBusClassConfig
//            println "================================================================="
        }
    }
}
