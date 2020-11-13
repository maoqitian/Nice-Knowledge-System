package com.mao.asm

import com.android.build.gradle.AppExtension
import com.mao.asm.transform.ASMLifecycleTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @Description:
 * @author maoqitian
 * @date 2020/11/13 0013 17:01
 */
class MainPlugin :Plugin<Project> {
    override fun apply(project: Project) {
        println("======自定义MainPlugin加载===========")
        //执行自定义的 Transform task

        val asmTransform = project.extensions.getByType(AppExtension::class.java)
        println("=======registerTransform ASMLifecycleTransform ==========")
        val transform =  ASMLifecycleTransform()
        asmTransform.registerTransform(transform)
    }
}