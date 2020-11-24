# Gradle 安装与 Gradle 命令
> 在学习 Gradle 之前，需要明确一点，要深入学习Gradle 说白了我们需要把他看成是一个**编程框架**，而我们需要了解的就是它的 API，并利用这些 API 完成一些任务

## 前提条件 
- 需要 JDK 8 或者更高版本

## 命令安装 
- macOs 基于 Homebrew

```
brew install gradle
```

## 手动安装 
- 首先下载资源包，[下载地址](https://gradle.org/releases/)

- 解压到自己想要指定的安装目录。在 .bash_profile 文件中配置环境变量

```
export PATH=$PATH:/opt/gradle/gradle-6.7/bin
```

- 验证是否安装成功

```
gradle -v
```
![gradle 版本信息](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/gradle-install-command/gradle%E7%89%88%E6%9C%AC%E4%BF%A1%E6%81%AF.png)

## Gradle 命令

### 查看工程project 信息

```
gradle project
```
![gradle-project](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/gradle-install-command/gradle-project.png)
> 如上图 ASMGradleTest 项目根目录对应的 build.gradle 叫 Root
Project，并包含两个子project，这和setting.gradle 中的定义是一样的

### 查看 Task 任务信息
- 查看 Task 任务信息命令，如果不在对应目录下首先需要**project-path 目录名，后面必须跟冒号**，如果已经在需要查看 task 的 project目录下，则直接使用 ==gradle tasks== 命令就行

```
gradle project-path:tasks
```
![gradle project-path:tasks](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/gradle-install-command/gradle-project-path-tasks.png) 

### gradle 执行 task 任务

- 在上一步中可以看到每个 project 中都有很多类型的 task，比如经常使用的claen选项，它其实也是一个task,是执行清理任务。那么要执行这些任务则可以使用 gradle task-name

```
gradle clean
```

### 更多命令

- 更多的命令可以查看帮助

```
gradle --help
```

