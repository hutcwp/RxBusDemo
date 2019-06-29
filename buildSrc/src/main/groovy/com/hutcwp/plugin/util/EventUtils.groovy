package com.hutcwp.plugin.util

import com.android.build.api.transform.JarInput
import com.hutcwp.plugin.inject.SniperConstant
import javassist.ClassPath
import javassist.ClassPool
import javassist.CtField
import javassist.CtMethod
import javassist.Modifier
import javassist.bytecode.*
import javassist.bytecode.annotation.Annotation
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Pattern


class EventUtils {


    static ClassPath appendClassPath(String path) {
        println 'appendClassPath path = ' + path
        ClassPool.getDefault().appendClassPath(path)
    }

    /**
     * 事先载入相关类
     * @param pool
     */
    static void importBaseClass(ClassPool pool) {
        pool.importPackage("java.util.ArrayList")
        pool.importPackage("android.view.View")
        pool.importPackage("android.os.Bundle")

        //rxjava
        pool.importPackage("io.reactivex.disposables.Disposable")

        //rxbus
        pool.importPackage("com.hutcwp.api.event")

        //event api
        pool.importPackage("com.hutcwp.api.event.EventBinder")

        pool.importPackage("com.hutcwp.rxbusdemo.rxbus")

        pool.importPackage(SniperConstant.EVENT_COMPAT)


    }

    /**
     * 获取method的名称
     * @param ctMethod
     * @return
     */
    static String getSimpleName(CtMethod ctMethod) {
        def methodName = ctMethod.getName();
        methodName.substring(
                methodName.lastIndexOf('.') + 1, methodName.length());
    }

    /**
     * 获取类的全限定类名
     * @param index
     * @param filePath
     * @return
     */
    static String getClassName(int index, String filePath) {
        int end = filePath.length() - 6 // .class = 6
        filePath.substring(index, end).replace('\\', '.').replace('/', '.')
    }

    /**
     * 获取project的name（过滤无效字符，只支持[A-Za-z0-9_]类名支持的）
     * @param projectName
     * @return
     */
    static String getProjectName(Project project) {
        String regEx = '[A-Za-z0-9_]'
        Pattern pattern = Pattern.compile(regEx)
        StringBuffer name = new StringBuffer()
        char[] array = project.name.toCharArray()
        for (int i = 0; i < array.length; i++) {
            if (pattern.matcher(array[i].toString())) {
                name.append(array[i])
            }
        }
        name.toString()
    }

    /**
     * 获取application的project的插件RxBus事件总线名称（通过apt生成此）
     * @param project
     * @return
     */
    static String getRxBusPluginName(Project applicationProject) {
        StringBuffer className = new StringBuffer()
        className.append(getProjectName(applicationProject))
        className.append("_")
        className.append(DigestUtils.md5Hex(applicationProject.buildDir.absolutePath))
        className.toString()
    }

    static String getAptFile(Project project) {
        "${project.buildDir}" + File.separator + "generated" + File.separator + "aptPlugin" + File.separator + "apt.text"
    }

    static Set<String> getPluginClasses(Project project) {
        Set<String> pluginSet = new HashSet<>()
        try {
            FileReader fr = new FileReader(getAptFile(project))
            BufferedReader br = new BufferedReader(fr)
            String line
            while ((line = br.readLine()) != null) {
                String content = line.trim()
                pluginSet.add(content)
                project.logger.error "pluginSet plugin class------>" + content + "<------"
            }
            br.close()
            fr.close()
        } catch (Exception e) {

        }
        pluginSet
    }

    /**
     * 获取每个第三方包jarInput的唯一md5名称（根据groupId, artifactId），versionId会进行覆盖
     * @param jarInput
     * @param isLibrary
     * @return
     */
    static String getNameByMD5(JarInput jarInput, boolean isLibrary) {
//        def jarName = jarInput.name
//        def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
        //第三方包格式  com.yy.android.yypushsdk:yypush:214.2.7::1 （1是表示这个aar包里面自带的jar包）
        def jarName
        if (!isLibrary && jarInput.name.lastIndexOf(":") > 0) {
            if (jarInput.name.lastIndexOf("::") > 0) {
                String jarIndex = jarInput.name.substring(jarInput.name.lastIndexOf("::") + 1)
                String tmp = jarInput.name.substring(0, jarInput.name.lastIndexOf("::"))
                jarName = tmp.substring(0, tmp.lastIndexOf(":")) + jarIndex
            } else {
                jarName = jarInput.name.substring(0, jarInput.name.lastIndexOf(":"))
            }
        } else {
            jarName = jarInput.name
        }
        def md5Name = DigestUtils.md5Hex(jarName)
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4)
        }
        println "getNameByMD5---->newName>" + jarName + "------->oldName>" + jarInput.name + "---->absolutePath>" + jarInput.file.absolutePath + "--->jarName + '_' +md5Name>" + jarName + '_' + md5Name
        jarName + '_' + md5Name
    }

    /**
     * 根据path对应的文件内容生成md5值
     * @param path
     * @return
     */
    static String md5HexByFileContent(String path) {
        try {
            FileInputStream fis = new FileInputStream(path)
            MessageDigest digest = MessageDigest.getInstance("MD5")
            byte[] buffer = new byte[1024]
            int len
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len)
            }
            fis.close()
            BigInteger bigInt = new BigInteger(1, digest.digest())
            return bigInt.toString(16)
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 获取sniper plugin 临时文件root目录
     * @param project
     * @return
     */
    static String getTmpDirRootPath(Project project) {
        project.buildDir.absolutePath + File.separator + "sniper"
    }

    /**
     * 获取root Project对象
     * @param project
     * @return
     */
    static Project getRootProject(Project project) {
        Project parentProject, tmpProject = project
        while (tmpProject != null) {
            parentProject = tmpProject
            tmpProject = tmpProject.parent
        }
        parentProject
    }

    /**
     * 根据文件路径删除目录或者文件
     * @param path
     */
    static void deleteFile(String path) {
        try {
            File file = new File(path)
            if (file.exists()) {
                if (file.isDirectory()) {
                    FileUtils.deleteDirectory(file)
                } else {
                    FileUtils.deleteQuietly(file)
                }
            }
        } catch (IOException e) {
            println "sniper plugin delete file " + path + " fail, ----->the reason is " + e.message
        }
    }

    /**
     * 过滤RetentionPolicy.RUNTIME类型注解（inject 都是RetentionPolicy.CLASS或者RetentionPolicy.SOURCE）
     * @param method
     * @return
     */
    static boolean existValidAnnotation(CtMethod method) {
        method.getMethodInfo2().getAttribute(AnnotationsAttribute.invisibleTag) != null
    }

    /**
     * 过滤RetentionPolicy.RUNTIME类型注解（inject 都是RetentionPolicy.CLASS或者RetentionPolicy.SOURCE）
     * @param field
     * @return
     */
    static boolean existValidAnnotation(CtField field) {
        field.getFieldInfo().getAttribute(AnnotationsAttribute.invisibleTag) != null
    }

    /**
     * 获取方法上指定annotation的值
     * @param annotationType 值的类型（BooleanMemberValue...）
     * @param info methodInfo
     * @param annotationName annotation的全限定名
     * @param annotationMemberName annotation的成员名称
     * @return
     */
    static <T> T getAnnotationValue(Class<T> annotationType, MethodInfo info, String annotationName, String annotationMemberName) {
        AnnotationsAttribute attribute = info.getAttribute(AnnotationsAttribute.invisibleTag)
        if (attribute != null) {
            Annotation annotation = attribute.getAnnotation(annotationName)
            return (T) annotation.getMemberValue(annotationMemberName)
        }
    }

    /**
     * 获取属性上指定annotation的值
     * @param annotationType 值的类型（BooleanMemberValue...）
     * @param info FieldInfo
     * @param annotationName annotation的全限定名
     * @param annotationMemberName annotation的成员名称
     * @return
     */
    static <T> T getAnnotationValue(Class<T> annotationType, FieldInfo info, String annotationName, String annotationMemberName) {
        AnnotationsAttribute attribute = info.getAttribute(AnnotationsAttribute.invisibleTag)
        if (attribute != null) {
            Annotation annotation = attribute.getAnnotation(annotationName)
            return (T) annotation.getMemberValue(annotationMemberName)
        }
    }

    /**
     * 获取方法的参数变量名称
     * @param method
     * @return
     */
    static String[] getMethodParamNames(CtMethod method) {
        MethodInfo methodInfo = method.getMethodInfo()
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute()
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag)
        if (attr == null) return []
        String[] paramNames = new String[method.getParameterTypes().length]
        int pos = Modifier.isStatic(method.getModifiers()) ? 0 : 1
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = attr.variableName(i + pos)
        }
        paramNames
    }
}
