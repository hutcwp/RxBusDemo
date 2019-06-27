package com.hutcwp.plugin.inject

import com.hutcwp.plugin.util.ClassFileVisitor
import com.hutcwp.plugin.util.JarZipUtils
import com.hutcwp.plugin.util.SniperUtils
import javassist.ClassPool
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by huangfan on 2017/7/4.
 */
class SniperInject {

    /**
     * 扫描jar包，并注入相应代码
     * @param jarPath
     * @param packageName
     * @param project
     * @param classPathList
     * @return
     */
    static File injectJar(String jarPath, String packageName, Project project, Set<String> pluginSet) {
        String tmpFile = SniperUtils.getTmpDirRootPath(project) + File.separator + System.currentTimeMillis()
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
        SniperUtils.deleteFile(tmpFile)
        return new File(jarPath)
    }

    /**
     * 扫描文件目录，并注入相应代码
     * @param path
     * @param packageName
     * @param project
     */
    static boolean injectDir(String rawPath, String path, String packageName, Project project, boolean isJar, Set<String> pluginSet) {
        def visitor = new ClassFileVisitor(packageName, isJar, project, path, rawPath, pluginSet)
        Files.walkFileTree(Paths.get(path), visitor)
        visitor.needInject
    }

    /**
     * 给限定类名className注入代码
     * @param className
     * @param path
     * @param project
     */
    static boolean injectClass(String classPath, String classDirectory, String className, boolean isJar, Closure closure) {
        closure.call(classPath, classDirectory, className, ClassPool.getDefault(), isJar)
    }


}