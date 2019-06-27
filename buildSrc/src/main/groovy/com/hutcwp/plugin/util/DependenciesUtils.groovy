/*
 * Copyright 2015-present wequick.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.hutcwp.plugin.util

import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

/** Class to resolve project dependencies */
final class DependenciesUtils {

    static generateCompileDependInfo(File dependFile, Project proj) {
        def allDependencies = DependenciesUtils.getAllResolveDependencies(proj, 'compile')
        if (allDependencies.size() > 0) {
            def dependFilePw = new PrintWriter(dependFile.newWriter(false))

            allDependencies.each { d ->
                dependFilePw.println d.name
            }
            dependFilePw.flush()
            dependFilePw.close()
        }
    }

    static Set<ResolvedDependency> getAllResolveDependencies(Project project, String config) {
        Configuration configuration
        try {
            configuration = project.configurations[config]
        } catch (UnknownConfigurationException ignored) {
            return null
        }

        return getAllResolveDependencies(configuration)
    }

    static Set<ResolvedDependency> getExportDependencies(Project project) {
        def configuration = project.configurations['export']
        def projectDependencies = configuration.dependencies.withType(DefaultProjectDependency.class)
        ResolvedConfiguration resolvedConfiguration = configuration.resolvedConfiguration
        def firstLevelDependencies = resolvedConfiguration.firstLevelModuleDependencies
        Set<ResolvedDependency> allDependencies = new HashSet<>()

        firstLevelDependencies.each { rd->
            def findResult = projectDependencies.find { it.group == rd.moduleGroup && it.name == rd.moduleName && it.version == rd.moduleVersion}
            // Don't recursively collect Project dependency
            if (findResult == null) {
                collectDependencies(rd, allDependencies);
            }else {
                allDependencies.add(rd)
            }
        }
        return allDependencies
    }

    static void collectAllDependencies(Project prj, Set<Dependency> allDependencies, String config ) {
        //Defining configuration names from which dependencies will be taken (debugCompile or releaseCompile and compile)
        prj.configurations["${config}"].allDependencies.each { depend ->
            if (allDependencies.find { addedNode -> addedNode.group == depend.group && addedNode.name == depend.name } == null) {
                allDependencies.add(depend)
            }
            if (depend instanceof DefaultProjectDependency) {
                collectAllDependencies(depend.dependencyProject, allDependencies, config)
            }
        }
    }

    static Set<ResolvedDependency> getAllResolveDependencies(Configuration configuration) {
        ResolvedConfiguration resolvedConfiguration = configuration.resolvedConfiguration
        def firstLevelDependencies = resolvedConfiguration.firstLevelModuleDependencies
        Set<ResolvedDependency> allDependencies = new HashSet<>()
        firstLevelDependencies.each {
            collectDependencies(it, allDependencies)
        }
        return allDependencies
    }

    private static void collectDependencies(ResolvedDependency node, Set<ResolvedDependency> out) {
        if (out.find { addedNode -> addedNode.name == node.name } == null) {
            out.add(node)
        }
        // Recursively
        node.children.each { newNode ->
            collectDependencies(newNode, out)
        }
    }

    static void collectProjectDependencies(Project prj, allDependencies ) {
        //Defining configuration names from which dependencies will be taken (debugCompile or releaseCompile and compile)
        prj.configurations['compile'].dependencies.withType(DefaultProjectDependency.class).each { depend ->
            if (allDependencies.find { addedNode -> addedNode.group == depend.group && addedNode.name == depend.name } == null) {
                allDependencies.add(depend)
                collectProjectDependencies(depend.dependencyProject, allDependencies)
            }
        }
    }

    static void collectAars(File d, Set outAars) {
        if (!d.exists()) return
        d.eachLine { line ->
            def module = line.split(':')
            def N = module.size()
            def aar = [group: module[0], name: module[1], version: (N == 3) ? module[2] : '']
            if (!outAars.contains(aar)) {
                outAars.add(aar)
            }
        }
    }

}