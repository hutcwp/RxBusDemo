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
            print "======start====="
            print "======end====="

            /* 注册SniperTransform */
            project.android.registerTransform(new BusEventTransform(project))
//            project.android.registerTransform(new MyTransform(project))
        }
    }
}
