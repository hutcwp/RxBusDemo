package com.hutcwp.plugin.util;

import com.hutcwp.plugin.extension.EventConfigExtension;
import org.gradle.api.Project;

public class AptUtils {


    public static EventConfigExtension createEventCongfigExtension(Project project) {
        EventConfigExtension eventConfigExtension = new EventConfigExtension();
        if (project.hasProperty("sniperIgnoredProjects")) {
            eventConfigExtension.setSniperIgnoredProjects(project.property("sniperIgnoredProjects"));
        }
        return eventConfigExtension;
    }

}
