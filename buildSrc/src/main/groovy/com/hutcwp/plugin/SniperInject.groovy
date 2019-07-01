package com.hutcwp.plugin

import com.hutcwp.plugin.util.ClassFileVisitor
import com.hutcwp.plugin.util.EventUtils
import com.hutcwp.plugin.util.JarZipUtils
import javassist.ClassPool
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths

class SniperInject {

    static boolean injectClass(String classPath, String classDirectory, String className, boolean isJar, Closure closure) {
        closure.call(classPath, classDirectory, className, ClassPool.getDefault())
    }

    static boolean injectDir(String rawPath, String path, String packageName, Project project, boolean isJar, Set<String> pluginSet) {
        def visitor = new ClassFileVisitor(packageName, isJar, project, path, rawPath, pluginSet)
        Files.walkFileTree(Paths.get(path), visitor)
//        LogUtil.info("visitor.needInject is " + visitor.needInject)
        visitor.needInject
    }

    /**
     * 扫描jar包，并注入相应代码
     * @param jarPath
     * @param packageName
     * @param project
     * @param classPathList
     * @return
     */
    static File injectJar(String jarPath, String packageName, Project project, Set<String> pluginSet) {
        String tmpFile = EventUtils.getTmpDirRootPath(project) + File.separator + System.currentTimeMillis()
        String tmpJar = tmpFile + ".jar"

        long s1 = System.currentTimeMillis()
        JarZipUtils.unZipJarByAnt(jarPath, tmpFile)
        long s2 = System.currentTimeMillis()
        project.logger.error(">>>>>>>>>>>>SniperTransform unZipJarByAnt:" + (s2 - s1) / 1000 + " secs<<<<<<<<<<<<<<<<<<")
        long s3 = System.currentTimeMillis()
        if (injectDir(jarPath, tmpFile, packageName, project, true, pluginSet)) {
            long s4 = System.currentTimeMillis()
            project.logger.error(">>>>>>>>>>>>SniperTransform injectJar:" + (s4 - s3) / 1000 + " secs<<<<<<<<<<<<<<<<<<")
            long s5 = System.currentTimeMillis()
            JarZipUtils.zipJarByAnt(tmpFile, tmpJar)
            long s6 = System.currentTimeMillis()
            project.logger.error(">>>>>>>>>>>>SniperTransform zipJarByAnt:" + (s6 - s5) / 1000 + " secs<<<<<<<<<<<<<<<<<<")
            jarPath = tmpJar
        }
        EventUtils.deleteFile(tmpFile)
        return new File(jarPath)
    }
}
