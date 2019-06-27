package com.hutcwp.plugin.util

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Created by huangfan on 2017/7/4.
 */

class JarZipUtils {

    /**
     * 将该jar包解压到指定目录
     * @param jarPath
     * @param destDirPath
     */
    static void unZipJar(String jarPath, String destDirPath){
        if(jarPath.endsWith(".jar")){
            JarFile jarFile = new JarFile(jarPath)
            Enumeration<JarEntry> jarEntrys = jarFile.entries()
            while(jarEntrys.hasMoreElements()){
                JarEntry jarEntry = jarEntrys.nextElement()
                if(jarEntry.isDirectory()){
                    continue
                }
                String entryName = jarEntry.getName()
                String outFileName = destDirPath + File.separator + entryName
                File outFile = new File(outFileName)
                outFile.getParentFile().mkdirs()
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                FileOutputStream fileOutputStream = new FileOutputStream(outFile)
                fileOutputStream << inputStream
                fileOutputStream.close()
                inputStream.close()
            }
            jarFile.close()
        }
    }

    /**
     * 重新打包jar
     * @param packagePath 将这个目录下的所有文件打包成jar
     * @param destPath 打包好的jar包的绝对路径
     */
    static void zipJar(String packagePath, String destPath) {
        File file = new File(packagePath)
        JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(destPath))
        file.eachFileRecurse { File f ->
            String entryName = f.getAbsolutePath().substring(file.absolutePath.length() + 1)
            outputStream.putNextEntry(new ZipEntry(entryName))
            if(!f.directory) {
                InputStream inputStream = new FileInputStream(f)
                outputStream << inputStream
                inputStream.close()
            }
        }
        outputStream.close()
    }

    /**
     * 压缩 dirPath 到 zipFilePath
     */
    def static zipJarByAnt(String dirPath, String zipFilePath) {
        new AntBuilder().zip(destfile: zipFilePath, basedir: dirPath)
    }

    /**
     * 解压 zipFilePath 到 目录 dirPath
     */
    static boolean unZipJarByAnt(String zipFilePath, String dirPath) {
        if (isZipEmpty(zipFilePath)) {
            println ">>> Zip file is empty! Ignore";
            return false
        }
        new AntBuilder().unzip(src: zipFilePath, dest: dirPath, overwrite: 'true')
        true
    }

    static boolean isZipEmpty(String zipFilePath) {
        ZipFile z
        try {
            z = new ZipFile(zipFilePath)
            return z.size() == 0
        } finally {
            z.close()
        }
    }
}
