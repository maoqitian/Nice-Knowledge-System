package com.mao.asm.transform

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.mao.asm.visitor.ASMLifecycleClassVisitor
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.FileOutputStream

/**
 * @Description: Transform 可以被看作是 Gradle 在编译项目时的一个 task
 * @author maoqitian
 * @date 2020/11/13 0013 17:03
 */
class ASMLifecycleTransform :Transform() {

    /**
     * 设置我们自定义的 Transform 对应的 Task 名称。Gradle 在编译的时候，会将这个名称显示在控制台上
     * @return String
     */
    override fun getName(): String = "ASMLifecycleTransform111"

    /**
     * 在项目中会有各种各样格式的文件，该方法可以设置 Transform 接收的文件类型
     * 具体取值范围
     * CONTENT_CLASS  .class 文件
     * CONTENT_JARS  jar 包
     * CONTENT_RESOURCES  资源 包含 java 文件
     * CONTENT_NATIVE_LIBS native lib
     * CONTENT_DEX dex 文件
     * CONTENT_DEX_WITH_RESOURCES  dex 文件
     * @return
     */
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS


    /**
     * 定义 Transform 检索的范围
     * PROJECT 只检索项目内容
     * SUB_PROJECTS 只检索子项目内容
     * EXTERNAL_LIBRARIES 只有外部库
     * TESTED_CODE 由当前变量测试的代码，包括依赖项
     * PROVIDED_ONLY 仅提供的本地或远程依赖项
     * @return
     */
    //只检索项目内容
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = TransformManager.PROJECT_ONLY

    /**
     * 表示当前 Transform 是否支持增量编译 返回 true 标识支持 目前测试插件不需要
     * @return Boolean
     */
    override fun isIncremental(): Boolean = false
    //对项目 class 检索操作
    override fun transform(transformInvocation: TransformInvocation) {
        println("transform 方法调用")

        //获取所有 输入 文件集合
        val transformInputs = transformInvocation.inputs
        val transformOutputProvider = transformInvocation.outputProvider

        transformOutputProvider?.deleteAll()

        transformInputs.forEach { transformInput ->
            // Caused by: java.lang.ClassNotFoundException: Didn't find class "androidx.appcompat.R$drawable" on path 问题
            // gradle 3.6.0以上R类不会转为.class文件而会转成jar，因此在Transform实现中需要单独拷贝，TransformInvocation.inputs.jarInputs
            // jar 文件处理
            transformInput.jarInputs.forEach { jarInput ->
                val file = jarInput.file
                println("find jar input:$file.name")
                val dest = transformOutputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(file, dest)
            }
            //源码文件处理
            //directoryInputs代表着以源码方式参与项目编译的所有目录结构及其目录下的源码文件
            transformInput.directoryInputs.forEach { directoryInput ->
                //遍历所有文件和文件夹 找到 class 结尾文件
                directoryInput.file.walkTopDown()
                    .filter { it.isFile }
                    .filter { it.extension == "class" }
                    .forEach { file ->
                        println("find class file:${file.name}")
                        val classReader = ClassReader(file.readBytes())
                        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                        //2.class 读取传入 ASM visitor
                        val asmLifecycleClassVisitor = ASMLifecycleClassVisitor(classWriter)
                        //3.通过ClassVisitor api 处理
                        classReader.accept(asmLifecycleClassVisitor,ClassReader.EXPAND_FRAMES)
                        //4.处理修改成功的字节码
                        val bytes = classWriter.toByteArray()
                        //写回文件中
                        val fos =  FileOutputStream(file.path)
                        fos.write(bytes)
                        fos.close()
                }
                val dest = transformOutputProvider.getContentLocation(directoryInput.name,directoryInput.contentTypes,directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file,dest)
            }
        }
    }
}