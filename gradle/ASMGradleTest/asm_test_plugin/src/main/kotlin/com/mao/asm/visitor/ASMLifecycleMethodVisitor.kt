package com.mao.asm.visitor

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @Description: 方法 Method Visitor 为每个方法加入日志打印
 * @author maoqitian
 * @date 2020/11/13 0013 11:47
 */
class ASMLifecycleMethodVisitor(private val methodVisitor:MethodVisitor, private val className:String,private val methodName:String) : MethodVisitor(Opcodes.ASM5, methodVisitor) {


    //在方法执行前插入日志字节码
    override fun visitCode() {
        super.visitCode()
        println("do ASMLifecycleMethodVisitor visitCode method......")

        methodVisitor.visitLdcInsn("毛麒添")

        methodVisitor.visitLdcInsn("$className -> $methodName")
        //字节码 插入方法 日志
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(Opcodes.POP)
    }

    override fun visitEnd() {
        super.visitEnd()
    }
}