

import org.gradle.api.Plugin
import org.gradle.api.Project

class TestPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        println("====== buildSrc TestPlugin Plugin加载===========")
        //执行自定义的  task
        project.task("TestPlugin"){
            doLast {
                println("buildSrc TestPlugin task 任务执行")
            }
        }
    }
}