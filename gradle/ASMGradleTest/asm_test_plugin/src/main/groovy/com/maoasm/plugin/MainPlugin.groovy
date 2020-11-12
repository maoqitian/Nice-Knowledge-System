package com.maoasm.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class MainPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        System.out.println("======自定义MainPlugin加载===========")
    }
}