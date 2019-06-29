package com.hutcwp.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.Sets
import com.hutcwp.plugin.inject.SniperConstant
import javassist.ClassPool
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import com.hutcwp.plugin.util.SniperUtils

class BusEventTransform extends Transform {

    Project project
    String packName

    BusEventTransform(Project project) {
        this.project = project
        packName = File.separator + "com" + File.separator
        println 'packageName is ' + packName
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        def classPathList = []
        try {
            doTransform(context, inputs, referencedInputs, outputProvider, isIncremental, classPathList)
        } catch (Exception e) {
            println('transform exception is ' + e)
        } finally {
            println("delete file")
            classPathList.each { ClassPool.getDefault().removeClassPath(it) }
            classPathList.clear()
            ClassPool.getDefault().clearImportedPackages()

            SniperUtils.deleteFile(SniperUtils.getTmpDirRootPath(project))
            SniperUtils.deleteFile(SniperUtils.getAptFile(project))
        }
    }

    void doTransform(Context context, Collection<TransformInput> inputs,
                     Collection<TransformInput> referencedInputs,
                     TransformOutputProvider outputProvider, boolean isIncremental,
                     def classPathList) {
        SniperUtils.deleteFile(SniperUtils.getTmpDirRootPath(project))
        SniperUtils.deleteFile(SniperUtils.getAptFile(project))
        SniperUtils.deleteFile(SniperUtils.getAptFile(project))
        println('transform...')

        Set<DirectoryInput> directoryInputs = new HashSet<>()
        Set<JarInput> libJar = new HashSet<>()

        SniperUtils.importBaseClass(ClassPool.getDefault())
        classPathList.add(SniperUtils.appendClassPath(project.android.bootClasspath[0].toString()))

        inputs.each { TransformInput input ->
            if (input.jarInputs.size() > 0) {
                input.jarInputs.each { JarInput jarInput ->
                    println 'find jar, name---->' + jarInput.name + "----absolutePath---->" + jarInput.file.absolutePath
                    boolean needInject = false
                    String jarPath = jarInput.file.absolutePath
                    File copyFile = jarInput.file

                    println 'append jar'
                    classPathList.add(SniperUtils.appendClassPath(jarPath))
                    boolean isForSniper = needInjectFile()
                    if (isForSniper) {
                        println 'find subProject, name---->' + jarInput.name + "-----absolutePath---->" + jarInput.file.absolutePath
                        libJar.add(jarInput)
                    }

                    def output = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    FileUtils.copyFile(copyFile, output)
                    SniperUtils.deleteFile(copyFile.absolutePath)
                }
            }
            if (input.directoryInputs.size() > 0) {
                directoryInputs.addAll(input.directoryInputs)
            }
        }

        directoryInputs.each { DirectoryInput directoryInput ->
            project.logger.error(">>>>>>>>>>>SniperTransform inject directory's path :" + directoryInput.file.absolutePath + "<<<<<<<<<<<")
            /*  考虑先拷贝文件再进入注入 */
            classPathList.add(SniperUtils.appendClassPath(directoryInput.file.path))

            def dest = outputProvider.getContentLocation(directoryInput.name,
                    directoryInput.contentTypes, directoryInput.scopes,
                    Format.DIRECTORY)
            FileUtils.deleteDirectory(dest)
            FileUtils.copyDirectory(directoryInput.file, dest)
            Injectors.INSTANCE.injectDir(directoryInput.file.absolutePath, dest.absolutePath, packName, project, false, null)
        }
        def classCt = ClassPool.getDefault().get(SniperConstant.EVENT_COMPAT)
        println 'classCt = ' + classCt
    }

    boolean needInjectFile() {
        return true
    }

    @Override
    String getName() {
        return "BusEventTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return Sets.immutableEnumSet(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }
}