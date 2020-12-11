>  可以使用任何您喜欢的语言来实现Gradle插件，前提是该实现源码最终被编译为JVM字节码

## Gradle 脑图

![Gradle知识体系](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/Gradle.png)

## 自定义插件

- 自定义插件有三种方式，分别为build.gradle中编写、buildSrc工程项目中编写（统一依赖管理）和插件以独立项目编译提供jar包形式引入项目

### build.gradle 自定义插件
- 要创建 Gradle 插件，您需要编写一个实现 Plugin 接口。将插件应用于项目时，Gradle 将创建插件类的实例，并调用该实例的 Plugin.apply（）方法。项目对象作为参数传递，插件可以使用它来配置项目。下面的示例包含一个Greeting插件，该插件将一个 hello 任务（task）添加到项目中。
- 直接在 build.gradle 添加如下代码

```
class GreetingPlugin implements Plugin<Project>{

    @Override
    void apply(Project target) {

        target.task("hello"){
            doLast {
                println("Hello from the GreetingPlugin")
            }
        }
    }
}

apply plugin: GreetingPlugin
```

- 运行 gradle 命令（注意本地gradle版本与AS 版本想匹配，否则会编译失败）

```
gradle -q hello

输出 Hello from the GreetingPlugin
```

### build.gradle 自定义插件配置扩展

- 为构建插件提供了一些配置选项，插件使用扩展对象执行此操作。Gradle 项目具有一个关联的ExtensionContainer对象，该对象包含已应用于该项目的插件的所有设置和属性。您可以通过向该容器添加扩展对象来为您的插件提供配置。扩展对象只是具有表示配置的Java Bean属性的对象

```
//build.gradle   自定义插件
class GreetingPlugin2 implements Plugin<Project> {
    void apply(Project project) {
        //获取配置
        def extension = project.extensions.create('greeting', GreetingPluginExtension)

        project.task('hello2') { //名字为 hello 的task
            doLast {
                //获取 extension 配置信息
                println "${extension.message} from ${extension.greeter}"
            }
        }
    }
}

//引入插件
apply plugin: GreetingPlugin2

// 配置 extension
greeting{
    greeter = 'Gradle'
    message = "Hi"
}
```
- 运行 gradle 命令

```
gradle -q hello2

输出 > Task :app:hello2
Hi from Gradle
```
- 以上分别创建了两个名为包含 hello 和 hello2 任务（task）的自定义插件，在 Tasks列表的 other 中也可找到

![hello和hello2task](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/hello%E5%92%8Chello2task.png)

### buildSrc工程项目自定义插件

- 新建 buildSrc module，删除不必要文件如下所示，并新建 groovy目录添加自定义插件 TestPlugin，同时也简单设置一个叫TestPlugin的task打印日志
> 注意：记得删除 settings.gradle buildSrc配置，否则会报
'buildSrc' cannot be used as a project name as it is a reserved name
错误

![buildSrc自定义插件](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/buildSrc%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6.png)

```
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
```

- 然后在 app 项目下 build.gradle 引入buildSrc 刚刚创建好的插件

```
apply plugin: TestPlugin
```

- 同样可以在 Task other 下找到 TestPlugin 执行

![Taskother下找到TestPlugin](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/Taskother%E4%B8%8B%E6%89%BE%E5%88%B0TestPlugin.png)

### 自定义插件编译成 jar 包
- AS 中新建 module，删除其他文件，只保留 src 目和 build.gradle 脚本文件，如下图所示

![自定义插件新建项目](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6%E6%96%B0%E5%BB%BA%E9%A1%B9%E7%9B%AE.png)

- 删除原有 build.gradle 脚本文件内容，修改为如下

```
## 需要引入的插件
apply plugin: 'groovy'
apply plugin: 'maven'

//gradle 开发 sdk 依赖
dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])

    implementation gradleApi()
    implementation localGroovy()

    implementation 'com.android.tools.build:gradle:4.0.0'

}

//设置插件 group 和版本号 在项目中使用的时候会用到
group='com.maoasm.plugin'
version='1.0.0'

uploadArchives {
    repositories {
        mavenDeployer {
            //本地的Maven地址设置
            repository(url: uri('../asm_test_repo'))
        }
    }
}
```
- 然后 在 mian 目录下新建 groovy 目录，因为gradle 是groovy写的，所以该目录用来存放插件相关的.groovy类，然后我们创建 MainPlugin.groovy 文件，并实现插件接口，project 则代表引入插件的项目

```
package com.maoasm.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class MainPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        println("======自定义MainPlugin加载===========")
        //执行自定义的  task
        project.task("TestPluginTask"){
            doLast {
                println("自定义插件task 任务执行")
            }
        }
    }
}
```
- 最后创建 properties 文件，maven 项目都需要这个配置，properties 文件名标识项目名称

```
## 本文件名称就是插件 apply 名称
implementation-class=com.maoasm.plugin.MainPlugin
```
![创建项目文件](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E5%88%9B%E5%BB%BA%E9%A1%B9%E7%9B%AE%E6%96%87%E4%BB%B6.png)

> 注意，上图中的目录层级需要一一对应创建目录，保证父子目录，否则 apply 插件会出现以下错误

```
Plugin with id 'XXXXX' not found
```
### 插件上传到本地 maven 仓库

- 前面在插件 build.gradle 脚本文件中我们配置了上传 jar 的 uploadArchives task 任务，找到 gradle task 执行上传 jar 包，如下图为执行任务和上传 jar 成功

![执行uploadArchives任务](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E6%89%A7%E8%A1%8CuploadArchives%E4%BB%BB%E5%8A%A1.png)

![自定义插件 jar 上传本地仓库成功](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6%20jar%20%E4%B8%8A%E4%BC%A0%E6%9C%AC%E5%9C%B0%E4%BB%93%E5%BA%93%E6%88%90%E5%8A%9F.png)

### 测试自定义插件

- 在app项目的 build.gradle 引入我们刚刚写好的插件

```
//引入自定义插件
apply plugin: 'com.mao.asmtest'
buildscript {
    repositories {
        google()
        jcenter()
        //自定义插件maven地址，这里以本地目录作为仓库地址目录
        maven { url '../asm_test_repo' }
    }
    dependencies {
        //加载自定义插件 group + module + version
        classpath 'com.maoasm.plugin:asm_test_plugin:1.0.0'
    }
}
```
![引入自定义插件](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E5%BC%95%E5%85%A5%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6.png)

- 插件引入编译成功，说明此时自定义插件加载成功

![插件引入编译成功](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E6%8F%92%E4%BB%B6%E5%BC%95%E5%85%A5%E7%BC%96%E8%AF%91%E6%88%90%E5%8A%9F.png)

- 在项目Task 选项中执行这个自定义 TestPluginTask 

![TestPluginTask执行成功](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/TestPluginTask%E6%89%A7%E8%A1%8C%E6%88%90%E5%8A%9F.png)

## 自定义插件实现Activity生命周期方法插桩打印log

- 前面例子都是使用 grrovy 语法来编写自定义插件，开头已经说过，只有编写插件语言能编译成字节码就行，所以接下来使用 kotlin 来实现一个自定义插件，实现Activity生命周期方法插桩打印log。
- 首先要改写插件的 build.gradle 引入依赖让插件能够编译 kotlin，如下所示加入kotlin依赖

```
apply plugin: 'groovy'
apply plugin: 'maven'
apply plugin: 'kotlin'

dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])

    implementation gradleApi()
    implementation localGroovy()

    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'com.android.tools.build:gradle:4.0.0'
}


group='com.mao.asm'
version='1.0.1'

uploadArchives {
    repositories {
        mavenDeployer {
            //本地的Maven地址设置
            repository(url: uri('../asm_test_repo'))
        }
    }
}
```

### Transform是什么？

- 前面我们写的代码在编译的时候可以使用 project.task 来指定编译过程要做什么操作，要完成自动插桩，首先就要找到项目中对应的.class文件修改，编译过程中 compileJava 这个task 将Java文件变成 .class ，如果编写一个 Transform 注册后 gradle 会将其看做是一个Task，并在 compileJava task 之后执行，Transform 接收这些 class 文件在执行插桩这就是这个自定义插件实现思路。

- 详情请看我的另一篇文章[深入了解 Gradle](https://note.youdao.com/)

### 使用 Transform Task 处理字节码文件

- 在前面自定义插件编译基础上新建一个 kotlin 目录，这样插件才会编译 kotlin 目录代码，新建 MainPlugin.kt 类继承 Plugin 接口，泛型定义为加载插件的 Project

```
/**
 * @Description:
 * @author maoqitian
 * @date 2020/11/13 0013 17:01
 */
class MainPlugin :Plugin<Project> {
    override fun apply(project: Project) {
        println("======自定义MainPlugin加载===========")
    }
}
```
- 然后定义 ASMLifecycleTransform.kt 类 处理字节码文件，分别获取输入输出文件集合，遍历得到.class 文件，其中涉及 ASM 框架的 ClassReader和 ClassWriter下面会介绍。

```
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
                        //字节码插桩处理
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
                //复制到对应目录
                val dest = transformOutputProvider.getContentLocation(directoryInput.name,directoryInput.contentTypes,directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file,dest)
            }
        }
    }
}
```
###  ASM 字节码操作

- 字节码操作已有现成的框架ASM，[官方文档](https://asm.ow2.io/#Q10)

- 在插件 build.gradle 中依赖引入

```
implementation 'org.ow2.asm:asm:9.0'
implementation 'org.ow2.asm:asm-commons:9.0'
```
####  ASM 几个关键类

- ClassReader：读取字节码文件的字节数组，并将字节码传递给ClassWriter
- ClassWriter：它的父类是ClassVisitor，作用是生成修改后的字节码，并输出字节数组；字节码文件由无符号数和表组成，最终其实为十六进制数，在 ASM 修改了字节码文件之后，肯定会影响到常量池的大小，此外包括本地变量表和操作数栈等变化，不过放心，只要在实例化 ClassWriter 操作类的时候设置 COMPUTE_MAXS 后，ASM 就会自动计算本地变量表和操作数栈。(

```
val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
```

- ClassVisitor：用来解析字节码文件结构的，当解析到某些特定结构时（比如类、方法、变量、），则调用内部相应的 FieldVisitor 或者 MethodVisitor 的方法，进一步解析或者可以修改 字节码文件的内容帮助完成字节码插桩功能

- 更多字节码结果组成内容可看我的另一篇文章 [重新认识Java字节码](https://github.com/maoqitian/Nice-Knowledge-System/blob/master/%E8%AE%A1%E7%AE%97%E6%9C%BA%E5%9F%BA%E7%A1%80/Java/%E9%87%8D%E6%96%B0%E8%AE%A4%E8%AF%86Java%E5%AD%97%E8%8A%82%E7%A0%81.md) 

#### 其他插桩框架
- [AspectJ](https://www.eclipse.org/aspectj/)，做过后端开发应该对其不陌生。它在 Android 中使用比较难搞，Android 可以使用 [AspectJX](https://github.com/HujiangTechnology/gradle_plugin_android_aspectjx)
- facebook 的 [redex](https://github.com/facebook/redex) , 它有提供在所有方法或者指定方法前面插入一段跟踪代码，具体我也没研究过，可以自行查看项目例子[InstrumentTest.config](https://github.com/facebook/redex/blob/5d0d4f429198a56c83c013b26b1093d80edc842b/test/instr/InstrumentTest.config) 

####  ASM 插桩实现

- 使用 ClassVisitor 读取目标 Activity 的 .class 文件，并过滤对应生命周期方法

```
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
            if (name.startsWith("onRestart")){
                println("do ASM ClassVisitor visitMethod onRestart")
                return ASMLifecycleMethodVisitor(methodVisitor, className!!, name)
            }
            if (name.startsWith("onPause")){
                println("do ASM ClassVisitor visitMethod onPause")
                return ASMLifecycleMethodVisitor(methodVisitor, className!!, name)
            }
            if (name.startsWith("onStop")){
                println("do ASM ClassVisitor visitMethod onStop")
                return ASMLifecycleMethodVisitor(methodVisitor, className!!, name)
            }
            if (name.startsWith("onDestroy")){
                println("do ASM ClassVisitor visitMethod onDestroy")
                return ASMLifecycleMethodVisitor(methodVisitor, className!!, name)
            }
        }
        return methodVisitor
    }

    override fun visitEnd() {
        super.visitEnd()
    }
}
```
- 使用 MethodVisitor 执行字节码插桩

```
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
```
- 在Demo 项目中写了两个Activity，两个Activity都实现了一些生命周期方法，具体代码就不贴了，可自行查看[demo源码](https://github.com/maoqitian/Nice-Knowledge-System/tree/master/gradle/ASMGradleTest)编译运行项目

![项目编译自动插桩完成](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E9%A1%B9%E7%9B%AE%E7%BC%96%E8%AF%91%E8%87%AA%E5%8A%A8%E6%8F%92%E6%A1%A9%E5%AE%8C%E6%88%90.png)

- 插件效果展示，从 MainActivity 跳转 SecondActivity再返回如下，可以看到只有Activity实现了生命周期方法就会自动插入日志打印代码

![自动插桩效果](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E8%87%AA%E5%8A%A8%E6%8F%92%E6%A1%A9%E6%95%88%E6%9E%9C.pngo)

- 最后以一张图来说明自定义插件介入修改字节码的过程

![自定义插件介入插桩示意图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6%E4%BB%8B%E5%85%A5%E6%8F%92%E6%A1%A9%E7%A4%BA%E6%84%8F%E5%9B%BE.png)

## 自定义插件调试

- 插件写好了难免有 bug，这时就需要用到插件调试来解决问题。
- 首先和平时调试一样，在插件代码需要打断点的地方点上断点

![插件代码打断点](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E6%8F%92%E4%BB%B6%E8%B0%83%E8%AF%95/%E6%8F%92%E4%BB%B6%E4%BB%A3%E7%A0%81%E6%89%93%E6%96%AD%E7%82%B9.png)

- gradle 命令 daemon 进程执行编译等待 debug

```
gradlew assembleDebug -Dorg.gradle.daemon=false -Dorg.gradle.debug=true
```
![gradle命令daemon进程执行编译等待 debug](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E6%8F%92%E4%BB%B6%E8%B0%83%E8%AF%95/gradle%E5%91%BD%E4%BB%A4daemon%E8%BF%9B%E7%A8%8B%E6%89%A7%E8%A1%8C%E7%BC%96%E8%AF%91%E7%AD%89%E5%BE%85%20debug.png)

- 添加 remote 编译配置，保持默认配置就行

![remote编译配置](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E6%8F%92%E4%BB%B6%E8%B0%83%E8%AF%95/remote%E7%BC%96%E8%AF%91%E9%85%8D%E7%BD%AE.png)

- 点击 debug 进入断点调试

![点击 debug 进入断点调试](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E6%8F%92%E4%BB%B6%E8%B0%83%E8%AF%95/%E7%82%B9%E5%87%BB%20debug%20%E8%BF%9B%E5%85%A5%E6%96%AD%E7%82%B9%E8%B0%83%E8%AF%95.png)

![控制台等待编译等待调试下一步](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8F%92%E4%BB%B6/%E6%8F%92%E4%BB%B6%E8%B0%83%E8%AF%95/%E6%8E%A7%E5%88%B6%E5%8F%B0%E7%AD%89%E5%BE%85%E7%BC%96%E8%AF%91%E7%AD%89%E5%BE%85%E8%B0%83%E8%AF%95%E4%B8%8B%E4%B8%80%E6%AD%A5.png)

## 参考

- [gradle 官方文档](https://docs.gradle.org/current/userguide/custom_plugins.html)
- [Gradle插件开发系列之gradle插件调试方法](https://blog.bihe0832.com/gradle_plugin_debug.html)