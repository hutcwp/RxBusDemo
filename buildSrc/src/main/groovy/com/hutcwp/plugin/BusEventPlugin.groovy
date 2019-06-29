package com.hutcwp.plugin

import com.android.build.gradle.AppPlugin
import com.hutcwp.plugin.extension.EventConfigExtension
import com.hutcwp.plugin.util.AptUtils
import com.hutcwp.plugin.util.LogUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by hutcwp on 2019-06-27 16:51
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
class BusEventPlugin implements Plugin<Project> {

    @Override
    void apply(Project rootProject) {
        LogUtil.info("======BusEventPlugin=====")
        EventConfigExtension eventConfigExtension = AptUtils.createEventCongfigExtension(rootProject)
        LogUtil.info('eventConfigExtension = ' + eventConfigExtension.sniperIgnoredProjects)

        def isApp = rootProject.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            rootProject.subprojects.each {
                if (it.name != rootProject.name) {
                    it.afterEvaluate {
                        it.apply plugin: "busevent"
                    }
                }
            }
            LogUtil.info("======start=====")
            rootProject.android.registerTransform(new BusEventTransform(rootProject))
//            rootProject.android.registerTransform(new MyTransform(rootProject))
            LogUtil.info("======end=====")
        }
    }
}
