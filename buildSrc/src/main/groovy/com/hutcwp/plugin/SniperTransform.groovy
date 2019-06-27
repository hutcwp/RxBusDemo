package com.hutcwp.plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.hutcwp.plugin.inject.SniperInject
import javassist.ClassPool
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import com.hutcwp.plugin.util.SniperUtils

/**
 * Created by huangfan on 2017/7/4.
 */
class SniperTransform extends Transform {
    Project project
    String packName


    SniperTransform(Project project) {
        this.project = project
        packName = File.separator + "com" + File.separator
    }

    @Override
    String getName() {
        return "SniperTransform"
    }

    /**
     * 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        HashSet<QualifiedContent.ContentType> set = new HashSet()
        set.add(QualifiedContent.DefaultContentType.CLASSES)
        return set
    }

    /**
     * 指定Transform的作用范围
     * @return
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        def startTime = System.currentTimeMillis()
        project.logger.error("-------------->SniperTransform inject start<--------------")

        def classPathList = []
        SniperUtils.importBaseClass(ClassPool.getDefault())
        classPathList.add(ClassPool.getDefault().appendClassPath(project.android.bootClasspath[0].toString()))

        Exception exception
        try {
            doTransform(inputs, outputProvider, classPathList)
        } catch (Exception e) {
            exception = e
        } finally {
            classPathList.each { ClassPool.getDefault().removeClassPath(it) }
            classPathList.clear()
            ClassPool.getDefault().clearImportedPackages()
            SniperUtils.deleteFile(SniperUtils.getTmpDirRootPath(project))
            SniperUtils.deleteFile(SniperUtils.getAptFile(project))
            if (exception != null) throw exception
        }

        project.logger.error("-------------->SniperTransform finish, spend time :" + (System.currentTimeMillis() - startTime) / 1000 + " secs<--------------")
    }

    /**
     * do inject code in jars and folder
     * @param inputs
     * @param outputProvider
     * @param classPathList
     * @return
     */
    def doTransform(Collection<TransformInput> inputs, TransformOutputProvider outputProvider,
                    def classPathList) {
        /** 主要耗时不是在类遍历，而是在文件目录copy和jar解压缩，因此先禁用此功能**/
        Set<String> pluginClasses = null

        Set<DirectoryInput> directoryInputs = new HashSet<>()
        Set<JarInput> libJar = new HashSet<>()
        long d1 = System.currentTimeMillis()
        inputs.each { TransformInput input ->
            if (input.jarInputs.size() > 0) {
                input.jarInputs.each { JarInput jarInput ->
                    println 'find jar, name---->' + jarInput.name + "----absolutePath---->" + jarInput.file.absolutePath
                    boolean needInject = true
                    String jarPath = jarInput.file.absolutePath
                    File copyFile = jarInput.file

                    classPathList.add(ClassPool.getDefault().appendClassPath(jarPath))
                    if (!needInject) {
                        def output = outputProvider.getContentLocation(SniperUtils.getNameByMD5(jarInput, isSubProject(jarInput)), jarInput.contentTypes, jarInput.scopes, Format.JAR)
                        FileUtils.copyFile(copyFile, output)
                    }
                }
            }
            if (input.directoryInputs.size() > 0) {
                directoryInputs.addAll(input.directoryInputs)
                input.directoryInputs.each { DirectoryInput directoryInput ->
                    classPathList.add(ClassPool.getDefault().appendClassPath(directoryInput.file.absolutePath))
                }
            }
        }
        long d2 = System.currentTimeMillis()
        project.logger.error(">>>>>>>>>>>>SniperTransform inputs.each:" + (d2 - d1) / 1000 + " secs<<<<<<<<<<<<<<<<<<")

        libJar.each { JarInput jarInput ->
            File copyFile = SniperInject.injectJar(jarInput.file.absolutePath, packName, project, pluginClasses)
            def output = outputProvider.getContentLocation(SniperUtils.getNameByMD5(jarInput, isSubProject(jarInput)), jarInput.contentTypes, jarInput.scopes, Format.JAR)
            FileUtils.copyFile(copyFile, output)
            if (!copyFile.absolutePath.equalsIgnoreCase(jarInput.file.absolutePath)) {
                SniperUtils.deleteFile(copyFile.absolutePath)
            }
        }
        long d3 = System.currentTimeMillis()
        project.logger.error(">>>>>>>>>>>>SniperTransform libJar.each:" + (d3 - d2) / 1000 + " secs<<<<<<<<<<<<<<<<<<")

        directoryInputs.each { DirectoryInput directoryInput ->
            project.logger.error(">>>>>>>>>>>SniperTransform inject directory's path :" + directoryInput.file.absolutePath + "<<<<<<<<<<<")

            /*  考虑先拷贝文件再进入注入 */
            def dest = outputProvider.getContentLocation(directoryInput.name,
                    directoryInput.contentTypes, directoryInput.scopes,
                    Format.DIRECTORY)
            FileUtils.deleteDirectory(dest)
            long s31 = System.currentTimeMillis()
            FileUtils.copyDirectory(directoryInput.file, dest)
            long s32 = System.currentTimeMillis()
            project.logger.error(">>>>>>>>>>>>SniperTransform dir copy :" + (s32 - s31) / 1000 + " secs<<<<<<<<<<<<<<<<<<")

            long s33 = System.currentTimeMillis()
            SniperInject.injectDir(directoryInput.file.absolutePath, dest.absolutePath, packName, project, false, pluginClasses)
            long s34 = System.currentTimeMillis()
            project.logger.error(">>>>>>>>>>>>SniperTransform dir inject :" + (s34 - s33) / 1000 + " secs<<<<<<<<<<<<<<<<<<")

        }

//        SniperInject.injectDoLast(project)

        long d4 = System.currentTimeMillis()
        project.logger.error(">>>>>>>>>>>>SniperTransform directoryInputs.each :" + (d4 - d3) / 1000 + " secs<<<<<<<<<<<<<<<<<<")
    }

    /**
     * Determines whether a content is an sub project.
     * @param content
     * @return
     */
    boolean isSubProject(@NonNull QualifiedContent content) {
        content.getScopes() == Collections.singleton(QualifiedContent.Scope.SUB_PROJECTS)
    }

}