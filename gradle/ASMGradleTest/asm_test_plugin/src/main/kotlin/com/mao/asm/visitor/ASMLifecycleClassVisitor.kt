package com.mao.asm.visitor

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @Description: class Visitor
 * @author maoqitian
 * @date 2020/11/13 0013 11:47
 */
class ASMLifecycleClassVisitor(classVisitor: ClassVisitor?) : ClassVisitor(Opcodes.ASM5, classVisitor) {

     private var className:String? = null
     private var superName:String? = null

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
        this.superName = superName
    }


    override fun visitMethod(access: Int, name: String, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val methodVisitor = cv.visitMethod(access,name,descriptor,signature,exceptions)
        //找到 androidX 包下的 Activity 类
        if (superName == "androidx/appcompat/app/AppCompatActivity"){
            //对 onCreate 方法处理 加入日志打印
            if (name.startsWith("onCreate")){
                println("do ASM ClassVisitor visitMethod onCreate")
                return ASMLifecycleMethodVisitor(methodVisitor, className!!, name)
            }
            if (name.startsWith("onStart")){
                println("do ASM ClassVisitor visitMethod onStart")
                return ASMLifecycleMethodVisitor(methodVisitor, className!!, name)
            }
            if (name.startsWith("onResume")){
                println("do ASM ClassVisitor visitMethod onResume")
                return ASMLifecycleMethodVisitor(methodVisitor, className!!, name)
            }
        }
        return methodVisitor
    }

    override fun visitEnd() {
        super.visitEnd()
    }
}