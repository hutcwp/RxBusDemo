package com.hutcwp.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.Sets
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import com.hutcwp.plugin.util.SniperUtils

class BusEventTransform extends Transform {

    Project project
    String packName

    BusEventTransform(Project project) {
        this.project = project
        packName = File.separator + "com" + File.separator
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        println('transform...')
        Set<DirectoryInput> directoryInputs = new HashSet<>()
        Set<JarInput> libJar = new HashSet<>()

        inputs.each { TransformInput input ->
            if (input.jarInputs.size() > 0) {
                input.jarInputs.each { JarInput jarInput ->
                    println 'find jar, name---->' + jarInput.name + "----absolutePath---->" + jarInput.file.absolutePath
                    boolean needInject = false
                    String jarPath = jarInput.file.absolutePath
                    File copyFile = jarInput.file

                    boolean isForSniper = needInjectFile()
                    if (isForSniper) {
                        println 'find subProject, name---->' + jarInput.name + "-----absolutePath---->" + jarInput.file.absolutePath
                        libJar.add(jarInput)
                        needInject = false
                    }

                    if (!needInject) {
                        def output = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                        FileUtils.copyFile(copyFile, output)
                    }
                }
            }
            if (input.directoryInputs.size() > 0) {
                directoryInputs.addAll(input.directoryInputs)

            }
        }

        directoryInputs.each { DirectoryInput directoryInput ->
            project.logger.error(">>>>>>>>>>>SniperTransform inject directory's path :" + directoryInput.file.absolutePath + "<<<<<<<<<<<")
            /*  考虑先拷贝文件再进入注入 */
            def dest = outputProvider.getContentLocation(directoryInput.name,
                    directoryInput.contentTypes, directoryInput.scopes,
                    Format.DIRECTORY)
            FileUtils.deleteDirectory(dest)
            FileUtils.copyDirectory(directoryInput.file, dest)
//            SniperInject.injectDir(directoryInput.file.absolutePath, dest.absolutePath, packName, project, false, pluginClasses)

        }
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