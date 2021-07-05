## 项目模块组件化架构

![Android组件化架构](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%BB%84%E4%BB%B6%E5%8C%96/Android%E7%BB%84%E4%BB%B6%E5%8C%96%E6%9E%B6%E6%9E%84.jpg)

## 单个组件架构

- 单个组件架构参照谷歌官方推荐 MVVM 模式
- 利用架构组件 LiveData 和 ViewModel 方便完成 MVVM 模式
- 依赖注入使用 Koin 
- [官方推荐应用架构指南](https://developer.android.com/jetpack/guide?hl=zh-cn)

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%BB%84%E4%BB%B6%E5%8C%96/Android-final-architecture.png)

## 组件拆分依赖

![组件化依赖关系](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%BB%84%E4%BB%B6%E5%8C%96/%E7%BB%84%E4%BB%B6%E5%8C%96%E4%BE%9D%E8%B5%96%E5%85%B3%E7%B3%BB.jpg)

## 组件单独与组合运行

### 配置单独调试与整合运行

- gradle.properties 配置模块单独运行开关

```
isModule = false
```
- 每个组件项目 build.gradle 配置组件单独运行时标志插件 apply plugin: 'com.android.application'，如果作为Module 则需要使用 apply plugin: 'com.android.library' 插件
- 配置组件单独运行的清单文件，组件单独运行需要有入口 Activity 启动
- 最后添加组件单独运行时的applicationId，对应组件单独运行时的包名
```
if(isModule.toBoolean()){ //如果是单独调试模式
    apply plugin: 'com.android.application'
}else {
    apply plugin: 'com.android.library'
}
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-android-extensions'
apply plugin: 'kotlin-kapt'
// 路由表自动加载插件
apply plugin: 'com.alibaba.arouter'
android {
    compileSdkVersion BuildVersions.compileSdkVersion
    buildToolsVersion BuildVersions.buildToolsVersion

    defaultConfig {
        if(isModule.toBoolean()){
            // 独立调试时添加 applicationId ，集成调试时移除
            applicationId "com.gdxmt.live_component"
            //开启 dex 分包 65536
            multiDexEnabled true
        }
        minSdkVersion BuildVersions.minSdkVersion
        targetSdkVersion BuildVersions.targetSdkVersion
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles "consumer-rules.pro"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [AROUTER_MODULE_NAME: project.getName()]
            }
        }
    }

    sourceSets {
        main {
            // 单独调试与集成调试时使用不同的 AndroidManifest.xml 文件
            if (isModule.toBoolean()) {
                manifest.srcFile 'src/main/moduleManifest/AndroidManifest.xml'
            } else {
                manifest.srcFile 'src/main/AndroidManifest.xml'
            }
        }
    }

   ......
}

dependencies {
        implementation fileTree(dir: "libs", include: ["*.jar"])
        //也可以根据单独运行开关配置依赖库
}
```
## 组件间通信

### 组件跳转
- [Arouter](https://github.com/alibaba/ARouter)

### 不同进程组件间通信

- binder ?

### 自动生成组件配置，模板代码自动生成 
