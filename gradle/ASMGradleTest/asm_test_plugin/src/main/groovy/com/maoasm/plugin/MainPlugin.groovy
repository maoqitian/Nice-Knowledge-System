package com.maoasm.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class MainPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        println("======自定义MainPlugin加载===========")
        //执行自定义的 Transform task

        def asmTransform = project.extensions.getByType(AppExtension)
        println("=======registerTransform ASMLifecycleTransform ==========")
        ASMLifecycleTransform transform = new ASMLifecycleTransform()
        asmTransform.registerTransform(transform)
    }
}