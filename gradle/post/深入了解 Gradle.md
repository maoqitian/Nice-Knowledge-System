# 深入了解 Gradle
- 看问题角度不同会有不一样的理解，平时写项目，在build.gradle 添加一些配置，写多了配置，就会理所当然以为Gradle就是一个配置文件，而在学习 Gradle 之前，需要明确一点，要深入学习Gradle 说白了我们需要把他看成是一个编程框架，而我们需要了解的就是它的 API，并利用这些 API 完成一些任务。

## Gradle 工作流程生命周期

![gradle工作流程](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/Gradle%20%E4%BA%86%E8%A7%A3%E4%B8%8E%E4%BD%BF%E7%94%A8/gradle%E5%B7%A5%E4%BD%9C%E6%B5%81%E7%A8%8B.png)

- Initialization phase（初始化）：这个阶段执行 settings.gradle文件，解析本项目包含多少个 project
- Configration阶段的目标是解析每个project中的build.gradle。解析每个子目录中的 build.gradle，分别是加载插件，加载依赖，加载 Task 和执行脚本
- Execution phase（执行）：这个阶段就是执行任务

- [生命周期官方文档描述](https://docs.gradle.org/current/userguide/build_lifecycle.html#heade)

### Gradle 编程模型

- Gradle是 groovy 语言编写的，而 groovy 又基于Java，所以 Gradle 在执行 groovy 脚本的时候其实是将其解析转换成 Java 对象，而这种对象有三种基本类型
- Gradle 对象:当我们执行 gradle xxx 命令的时候，gradle 会从默认的配置脚本中构造出一个 Gradle 对象。在整个执行过程中，只有这么一个对象。Gradle 对象的数据类型就是 Gradle。

- Project 对象: 每一个 build.gradle 会转换成一个 Project 对象
- Settings 对象: 每一个 settings.gradle 都会转换成一个 Settings 对象

> 每个Gradle脚本都实现该Script接口。该接口定义了可以在脚本中使用的许多属性和方法 

- [官方文档描述](https://docs.gradle.org/current/dsl/)

![官方文档描述](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/Gradle%20%E4%BA%86%E8%A7%A3%E4%B8%8E%E4%BD%BF%E7%94%A8/%E5%AE%98%E6%96%B9%E6%96%87%E6%A1%A3%E6%8F%8F%E8%BF%B0.png)

## Project

- [Project api 文档](https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html)

- 由上一小节的Gradle编程模型中，每一个 build.gradle 文件都会转换成一个 Project 对象，Project和 build.gradle 文件之间存在一对一的关系。在 Gradle 术语中，Project 对象对 应的是 Build Script

- 每一个Project 项目包含很多个Task，Task就是对应插件，也可以这样说**一个 Project中有多少个 Task 其实是由插件的多少来决定的**
，所以在build.gradle中，需要加载插件，加载依赖，设置属性

### 加载插件

- 一个 Project 对应 build.gradle，插件加载调用的是 Project 的 apply 函数，它其实定义在 Project 实现的 PluginAware接口中，如下为[官方文档描述](https://docs.gradle.org/current/javadoc/org/gradle/api/plugins/PluginAware.html#apply)

![apply官方文档描述](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/Gradle%20%E4%BA%86%E8%A7%A3%E4%B8%8E%E4%BD%BF%E7%94%A8/apply%E5%AE%98%E6%96%B9%E6%96%87%E6%A1%A3%E6%8F%8F%E8%BF%B0.png)

- 平时加载插件可以是二级制 jar 包如下，也就是我们很熟悉的加载插件，在 Project 目录下 build.gradle

```
    //如果 project 是编译 Android APP，，则加载此插件
    apply plugin: 'com.android.application'
    //如果 project 是编译 Library，则加载此插件
    apply plugin: 'com.android.library'

```
- 同时 apply 函数也是可以加载 gradle 文件的，比如创建统一依赖管理，可以创建 config.gradle，里面写入依赖配置和版本号，然后在 build.gradle 中如使用

> 注意：build.gradle 中 apply 其他 gradle 文件需要在同一个目录下，否则需要文件路径 + 文件名格式来 apply

```
apply from: 'config.gradle'
//文件路径 + 文件名
apply from: rootProject.getRootDir().absolutePath+'/config.gradle'

```
- config.gradle 文件
> **下文中有提到闭包，闭包，英文叫 Closure，是 Groovy 中非常重要的一个数据类型或者说一种概念；==它代表了一段可执行的代码==**
```
//除了 ext.xxx=value 这种定义方法外，还可以使用 ext{}这种书写方法
ext {
    //闭包
    android = [
            compileSdkVersion       : 28,
            buildToolsVersion       : "28.0.0",
            minSdkVersion           : 21,
            targetSdkVersion        : 28,
            versionCode             : 7,
            versionName             : "1.0.6",
            renderscriptTargetApi   : 21
    ]

    version = [
            supportLibraryVersion   : "28.0.0",
            smartrefreshVersion     : "1.1.0-alpha-25",
            okhttpVersion           : "3.12.0",
            retrofitVersion         : "2.3.0",
            glideVersion            : "4.8.0",
            daggerVersion           : "2.22.1",
            butterknifeVersion      : "8.8.1",
            fragmentationVersion    : "1.3.6",
    ]

    dependencies = [
            //base
            "appcompat-v7"                      : "com.android.support:appcompat-v7:${version["supportLibraryVersion"]}",
            "cardview-v7"                       : "com.android.support:cardview-v7:${version["supportLibraryVersion"]}",
            "support-v4"                        : "com.android.support:support-v4:${version["supportLibraryVersion"]}",
            "design"                            : "com.android.support:design:${version["supportLibraryVersion"]}",
            "recyclerview"                      : "com.android.support:recyclerview-v7:${version["supportLibraryVersion"]}",
            "constraint-layout"                 : "com.android.support.constraint:constraint-layout:1.1.3",
             .......
    ]
}

```
- build.gradle 中使用 config.gradle 定义依赖也需要文件路径和名称结合

```
dependencies {
    implementation rootProject.ext.dependencies["appcompat-v7"]
}
```
## Task

- 首先照例贴出[Task的Api文档](https://docs.gradle.org/current/dsl/org.gradle.api.Task.html)地址

- Task 是 Gradle 中的一种数据类型，它代表了一些要执行或者要干的工作。不同的插件
可以添加不同的 Task。每一个 Task 都需要和一个 Project 关联

- 可以在 build.gradle 如下定义 task
```
// Task 是和 Project 关联的，所以可以利用 Project 的 task 函数来创建一个 Task
task myTask

task myTask { 
//闭包配置
configure closure 
}

//eg:
project.task("hello1"){
            doLast {
                println("Hello from the GreetingPlugin")
            }
}

Task myType << { task action } //注意，<<符号 是 doLast 的缩写


//Task 创建的时候可以指定 Type
task myTask(type: SomeType)

//eg：
task myTask(type:Copy) //创建的 Task 就是一个 Copy Task

task myTask(type: SomeType) { 
//闭包配置
configure closure 
}
```
- 一个 Task 可以有若干个 Action，每个Task 都有doFirst 和 doLast 两个函数，用于 添加需要最先执行的 Action 和需要和需要最后执行的 Action。Action 就是一个closure（闭包）。

- 使用 task myTask { xxx}的时候，括号是一个 closure（闭包）。 gradle 在创建这个 Task 之后，返回给用户之前，会先执行 closure 的逻辑任务

- Task myType << { task action }，可以指定 Type，通过 type:名字表达。Gradle 本 身提供了一些通用的 Task，最常见的有 Copy 任务。Copy 是 Gradle 中的一个类。 当我们:task myTask(type:Copy)的时候，创建的 Task 就是一个 Copy Task。

```
//文档复制任务
task copyDocs(type: Copy) {
    from 'src/main/doc' //从src/main/doc目录
    into 'build/target/doc' //复制到build/target/doc目录
}
//更多例子可以查看官方文档
```
- [Copy文档](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Copy.html)

- 前面只是对 Task 一些概念进行了解，更多细节还需自行查看官方文档。

## Transform
> 很多第三方框架，都需要依赖自定义插件，比如阿里路由框架 ARouter，而它的实现中就有包含 Transform 的使用，了解并使用Transform 是读懂第三方框架的基础。

### 什么是Transform

- 首先可以看看[官方文档](https://google.github.io/android-gradle-dsl/javadoc/2.1/com/android/build/api/transform/Transform.html)，从1.5.0-beta1开始，Gradle插件包含一个Transform API，允许第三方插件在将已编译的类文件转换为dex文件之前对其进行操作。

- Transform 说白了也是一个Task，平时在 Android 编译项目，项目代码会先通过 compileJava 这个task 将项目源码编译成 .class文件，而 Transform 则可以接收这些编译产生的Class文件，并且 Transform 会在 compileJava 这个task 之后执行，这样就表示可以在 Android 项目生成 dex 之前做一些自定义操作。

- Transform 依赖引入 

```
implementation 'com.android.tools.build:gradle:4.0.0'
```
### Transform API

- Transform gradle 插件中的一个抽象类，有四个必须要实现的抽象方法,如下所示

```
@SuppressWarnings("MethodMayBeStatic")
public abstract class Transform {

    @NonNull
    public abstract String getName();

    @NonNull
    public abstract Set<ContentType> getInputTypes();
  
    @NonNull
    public abstract Set<? super Scope> getScopes();
    /**
     * Returns whether the Transform can perform incremental work.
     * 是否支持增量编译
     * <p>If it does, then the TransformInput may contain a list of changed/removed/added files, unless
     * something else triggers a non incremental run.
     */
    public abstract boolean isIncremental();
    .....
}
```
#### Transform Task 名称设置

- 抽象方法 getName()：实现该方法返回就是就是插件 Task 的名称

#### Task 处理输入文件类型

- 抽象方法 getInputTypes()：返回的是MutableSet<QualifiedContent.ContentType>类型集合，CLASSES类型代表只检索 .class 文件，RESOURCES类型代表检索 java 标准资源文件。

```
    /**
     * The type of of the content.
     */
    enum DefaultContentType implements ContentType {
        /**
         * .class 文件
         */
        CLASSES(0x01),

        /**标准Java资源 */
        RESOURCES(0x02);

        private final int value;

        DefaultContentType(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }
```

#### Task 处理输入文件范围

- 抽象方法 getScopes()：返回的是 MutableSet<in QualifiedContent.Scope> 类型集合，Scope是个枚举类型，取值含义如下

```
enum Scope implements ScopeType {
        /** 只检索项目内容 */
        PROJECT(0x01),
        /** 只检索子项目内容 */
        SUB_PROJECTS(0x04),
        /**只有外部库 */
        EXTERNAL_LIBRARIES(0x10),
        /** 由当前变量测试的代码，包括依赖项 */
        TESTED_CODE(0x20),
        /** 仅提供的本地或远程依赖项 */
        PROVIDED_ONLY(0x40),
       ......
    }
```
- 在TransformManager类中已经给我们定义了一些枚举的组合

```
public static final Set<ScopeType> PROJECT_ONLY = ImmutableSet.of(Scope.PROJECT);

public static final Set<ScopeType> SCOPE_FULL_PROJECT =
            ImmutableSet.of(Scope.PROJECT, Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES);
            
public static final Set<ScopeType> SCOPE_FULL_WITH_FEATURES =
            new ImmutableSet.Builder<ScopeType>()
                    .addAll(SCOPE_FULL_PROJECT)
                    .add(InternalScope.FEATURES)
                    .build();
public static final Set<ScopeType> SCOPE_FEATURES = ImmutableSet.of(InternalScope.FEATURES);

public static final Set<ScopeType> SCOPE_FULL_LIBRARY_WITH_LOCAL_JARS =
            ImmutableSet.of(Scope.PROJECT, InternalScope.LOCAL_DEPS);
            
public static final Set<ScopeType> SCOPE_FULL_PROJECT_WITH_LOCAL_JARS =
            new ImmutableSet.Builder<ScopeType>()
                    .addAll(SCOPE_FULL_PROJECT)
                    .add(InternalScope.LOCAL_DEPS)
                    .build();
```

#### Transform 方法

- 核心处理方法 transform 它是一个空实现，核心的输入内容 inputs 则是封装到 TransformInvocation 对象中

```
override fun transform(transformInvocation: TransformInvocation) {
    
}
```
##### TransformInvocation

- 它的成员参数如下

```
public interface TransformInvocation {


    /**
     * Returns the inputs/outputs of the transform.
     * @return the inputs/outputs of the transform.
     */
    @NonNull
    Collection<TransformInput> getInputs();
    
    /**
     * Returns the output provider allowing to create content.
     * @return he output provider allowing to create content.
     */
    @Nullable
    TransformOutputProvider getOutputProvider();

    .....
}
```

- 输入 inputs 对象为 TransformInput，有输入必然有输出，输出对象就是我们把输入文件做一些自定义处理好之后的文件，通过 TransformOutputProvider 获取到输出目录，最后将修改的文件复制到输出目录

```
/**
 * The input to a Transform.
 * <p>
 * It is mostly composed of a list of {@link JarInput} and a list of {@link DirectoryInput}.
 */
public interface TransformInput {

    /**
     * Returns a collection of {@link JarInput}.
     */
    @NonNull
    Collection<JarInput> getJarInputs();

    /**
     * Returns a collection of {@link DirectoryInput}.
     */
    @NonNull
    Collection<DirectoryInput> getDirectoryInputs();
}
```
- 通过接口 TransformInput 的定义可以知道 transform 方法可以出来到两种输入类型的文件，一直是 jar 包的集合jarInputs，另一种是文件目录集合 directoryInputs

- 如下一个例子就是分别打印 输入的 jar 包和 .class 文件名称

```
override fun transform(transformInvocation: TransformInvocation) {
        println("transform 方法调用")
        //获取 输入 文件集合
        val transformInputs = transformInvocation.inputs
        transformInputs.forEach { transformInput ->
            // jar 文件处理
            transformInput.jarInputs.forEach { jarInput ->
                val file = jarInput.file
                println("find jar input: " + file.name)
            }
            //源码文件处理
            //directoryInputs代表着以源码方式参与项目编译的所有目录结构及其目录下的源码文件
            transformInput.directoryInputs.forEach { directoryInput ->
                //遍历所有文件和文件夹 找到 class 结尾文件
                directoryInput.file.walkTopDown()
                    .filter { it.isFile }
                    .filter { it.extension == "class" }
                    .forEach { file ->
                        println("find class file：${file.name}")
                }
            }
        }
    }
```
- 运行结果

![transform遍历](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/Gradle%20%E4%BA%86%E8%A7%A3%E4%B8%8E%E4%BD%BF%E7%94%A8/transform%E9%81%8D%E5%8E%86.png)

### 在插件中注册 Transform

- 根据前面学习，既然 Transform 是一个Task，要让其生效，则要运行Gradle 的 Project中，Project 会加载插件，所以我们需要在插件中注册Transform task，如下所示，自定义 ASMLifecycleTransform ，在自定义插件中注册它

```
/**
 * @Description: kotlin 代码编写自定义插件
 * @author maoqitian
 * @date 2020/11/13 0013 17:01
 */
class MainPlugin :Plugin<Project> {
    override fun apply(project: Project) {
        println("======自定义MainPlugin加载===========")
        //注册执行自定义的 Transform task

        val asmTransform = project.extensions.getByType(AppExtension::class.java)
        println("=======registerTransform ASMLifecycleTransform ==========")
        val transform =  ASMLifecycleTransform()
        asmTransform.registerTransform(transform)
    }
}
```
## 最后

- 本文大多数内容都是来之官方文档，要想深入了解更多，可以自己多多翻阅官方文档资料，下一篇接着看自定义Gradle插件内容。