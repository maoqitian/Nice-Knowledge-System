## 为什么要组件化

- 业务模块之间的解耦，利于项目维护
- 代码复用，组件功能复用
- 利于多人协同开发，每人只需要专注自己模块
- 业务组件分配不同git版本管理仓库，减少代码提交冲突

## 项目模块组件化架构

![Android组件化架构](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%BB%84%E4%BB%B6%E5%8C%96/Android%E5%AE%A2%E6%88%B7%E7%AB%AF%E7%BB%84%E4%BB%B6%E5%8C%96%E6%9E%B6%E6%9E%84.jpg)

## 单个组件架构
- 单个组件架构参照谷歌官方推荐架构模型，每个组件仅依赖于其下一级的组件。例如，Activity 和 Fragment 仅依赖于视图模型（ViewModel）。存储区是唯一依赖于其他多个类的类；存储区（Repository）依赖于持久性数据模型和远程后端数据源
- 利用谷歌 Jetpack 架构组件 LiveData 和 ViewModel 方便完成架构模式
- 依赖注入使用 Koin 
- [官方推荐应用架构指南](https://developer.android.com/jetpack/guide?hl=zh-cn)

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%BB%84%E4%BB%B6%E5%8C%96/Android-final-architecture.png)

## 组件拆分依赖

![组件化依赖关系](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%BB%84%E4%BB%B6%E5%8C%96/%E7%BB%84%E4%BB%B6%E5%8C%96%E4%BE%9D%E8%B5%96%E5%85%B3%E7%B3%BB.jpg)

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

### 组件间通信

#### 对外暴露服务组件

- 包含对外暴露服务的接口定义
- 路由信息常量
- 提供获取组件服务工具方法（目前使用ARouter.getInstance().build("/xxx/xxx").navigation()）
- 同时也提供相互通信获取数据的方法（getStr()）
```
object LiveChannelUtils {

    //获取直播组件对外暴露服务服务 直接跳转直播页
    fun getLiveService(context:Context):LiveChannelService{
        // ARouter.getInstance().build() 方式来获取服务实现类实例
        return ARouter.getInstance().build(LiveChannelService.LIVE_CHANNEL_PATH).navigation(context,object :NavCallback(){
            override fun onLost(postcard: Postcard?) {
                super.onLost(postcard)
                Toast.makeText(context, "获取直播频道失败", Toast.LENGTH_SHORT).show()
            }
            override fun onArrival(postcard: Postcard?) {

            }

        }) as LiveChannelService
    }

    fun getStr(context:Context):String{
        return getLiveService(context).getStr()
    }
}
```
#### 业务组件实现暴露服务接口方法
```
@Route(path = LiveChannelService.LIVE_CHANNEL_PATH)
class LiveChannelServiceImpl :LiveChannelService{
    override fun start(context: Context) {
        LiveChannelActivity.start(context)
    }

    override fun startTVod(context: Context) {
        TVodActivity.startTVod(context)
    }

    override fun getStr(): String {
        return "直播页数据返回"
    }

    override fun init(context: Context?) {
    }

}
```
### 多进程组件间通信

- 基于 binder 路由服务？（待研究）

### 自动生成组件配置，模板代码自动生成 
- 待研究
