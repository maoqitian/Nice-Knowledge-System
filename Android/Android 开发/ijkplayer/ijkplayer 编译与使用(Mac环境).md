# ijkplayer 编译与使用(Mac环境)

>题外话：ijkplayer 为何叫 ijk呢？
本人好奇查了一下，ijk应该是“爱 JK”的缩写 ，而“JK”这个词是日本的网络流行语，取自“女子高校生”日语的罗马音简写，也就是女高中生的意思（～(￣▽￣～)(～￣▽￣)～），哈哈，懂的都懂，B站这个开源播放器取名老二次元了。不废话了，进入正题。

## 编译环境
系统软件 | 版本|功能
---|---|---
macOs| 10.15.5|系统
Homebrew | 2.4.4 | 软件包管理工具
jdk | java version "1.8.0_251" | Java开发工具
git | 2.27.0 | 版本控制
yasm|1.3.0 | 汇编编译器
NDK | android-ndk-r14b|Android NDK 是一个工具集，可让您使用 C 和 C++ 等语言以原生代码实现应用的各个部分（注意编译ijkplayer NDK 版本控制在 r10 ~ r14 范围）
Android Studio |  4.0 |Android 开发IDE

## 手机 CPU 架构类型了解

- Android 设备的CPU类型，不同的 Android 设备使用不同的 CPU，而不同的 CPU 支持不同的指令集

CPU 架构类型 | 说明
---|---
armeabi/mips / mips64 | 第5代、第6代的ARM处理器，早期的手机用的比较多，NDK 以前支持 ARMv5 (armeabi) 以及 32 位和 64 位 MIPS，但 NDK r17 已不再支持
armeabi-v7a | 第7代及以上的 ARM，此 ABI 适用于基于 32 位 ARM 的 CPU 处理器。
arm64-v8a | 此 ABI 适用于基于 ARMv8-A 的 CPU，第8代、64位ARM处理器，**目前市场主流的版本**
x86 、x86_64| intel CPU , 平板、模拟器、64位的平板
 

- adb 命令查看设备CPU架构

```
adb -s 设备名称 shell getprop ro.product.cpu.abi
```
- 分析比较细致的文章推荐：[为何大厂APP如微信、支付宝、淘宝、手Q等只适配了armeabi-v7a/armeabi](https://juejin.im/post/5eae6f86e51d454ddb0b3dc6#heading-4)

## 编译准备
- 自行下载git,yasm,Android sdk、ndk、并配置环境变量，下面给出Android sdk、ndk环境变量配置

- git、yasm 安装

```
brew install git 
brew install yasm 
```
- Android Studio、sdk 、ndk 下载地址

##### [Android Studio、sdk下载地址 ](https://developer.android.google.cn/studio?hl=zh_cn)

##### [ndk下载地址](https://developer.android.google.cn/ndk/downloads)

```
export ANDROID_SDK=/Users/{你的路径XXX}/Library/Android/sdk
export ANDROID_NDK=/Users/{你的路径XXX}/Library/Android/ndk/android-ndk-r14b
export PATH=$PATH:$ANDROID_SDK/tools
export PATH=$PATH:$ANDROID_SDK/platform-tools
export PATH=$PATH:$ANDROID_NDK
```

### ijk项目下载和拉取fmpeg代码

```
# clone项目
git clone https://github.com/Bilibili/ijkplayer.git ijkplayer-android

# 进入ijkplayer-android目录
cd ijkplayer-android

# 切换到最新代码分支
git checkout -B latest k0.8.8

# 会检查下载ffmpeg代码 
./init-android.sh

#初始化openSSL（使ijk编译后支持https）
./init-android-openssl.sh
```
### 编译前选择你的配置

- 在官方库说明中提供了三种配置支持，每个sh脚本里有对应的配置信息，包含支持编码格式、流媒体协议类型等，如下截取一些decoders，enable标识支持该格式，disable则标识不支持

```
## 支持解码格式
# ./configure --list-decoders
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --disable-decoders"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=aac"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=aac_latm"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=flv"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=h264"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=mp3*"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=vp6f"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=flac"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=hevc"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=vp8"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --enable-decoder=vp9"
```
- 选择配置文件，ln -s 命令标识软连接，module.sh可以直接获取module-default.sh的配置
```
#If you prefer more codec/format
cd config
rm module.sh
ln -s module-default.sh module.sh

#If you prefer less codec/format for smaller binary size (include hevc function)
cd config
rm module.sh
ln -s module-lite-hevc.sh module.sh

#If you prefer less codec/format for smaller binary size (by default)
cd config
rm module.sh
ln -s module-lite.sh module.sh
```

## 编译

### 开始编译
- 这里编译的是Android项目，所以先 cd 到 android/contrib下 执行清除命令，然后编译对于的 so 库，all 标识编译所有架构的 so，想编译x86架构则将 all 替换成x86
```
./compile-openssl.sh clean//清除
./compile-ffmpeg.sh clean//清除
./compile-openssl.sh all//编译
./compile-ffmpeg.sh all//编译
```
![编译生成的ffmpeg.so文件](https://github.com/maoqitian/MaoMdPhoto/raw/master/ijkplayer%E7%BC%96%E8%AF%91/%E7%BC%96%E8%AF%91%E7%94%9F%E6%88%90%E7%9A%84ffmpeg.so%E6%96%87%E4%BB%B6.png)
- 生成ijkplayer 对应架构 so 文件（all 同上输入对应架构则生成对应架构动态链接库），动态链接库生成路径如下图所示（路径示例：ijkplayer-android/android/ijkplayer/ijkplayer-armv7a/src/main/libs/armeabi-v7a）
> 注意本步骤需要同意不受信任软件权限，[具体参考地址](https://support.apple.com/en-us/HT202491)
```
# 注意回到android 路径下
cd ..
# 执行脚步生成so 文件
./compile-ijk.sh all
```
![ijkplayer编译成功生成的动态链接库文件](https://github.com/maoqitian/MaoMdPhoto/raw/master/ijkplayer%E7%BC%96%E8%AF%91/ijkplayer%E7%BC%96%E8%AF%91%E6%88%90%E5%8A%9F%E7%94%9F%E6%88%90%E7%9A%84%E5%8A%A8%E6%80%81%E9%93%BE%E6%8E%A5%E5%BA%93%E6%96%87%E4%BB%B6.png)

- 到此 ijkplayer 编译完成，如果播放器之前逻辑已经写好，则直接替换项目中对应的动态链接库文件就行

## 使用

- 实际前面编译的各个CPU架构的动态链接库（so）文件生成后帮我们都对应放到了 ijkplayer 项目中已经预留的创建好的空项目中，也就是路径 ijkplayer-android/android/ijkplayer 中的项目这是一个可以直接导入 Android Stuido 的Demo项目，但是平时开发都是将播放器作为library导入项目的，接下来尝试新建一个项目将 ijkplayer 作为library 导入

### 项目导入

#### 新建项目
1. 首先新建一个 ijkplayerdemo 工程， 将文件夹的文件ijkplayer-android/android/ijkplayer目录下的 tool 初始化工程脚本 gradle 文件复制到刚新穿件的项目根目录，然后在根项目的build.gradle中加入统一版本 ext 配置，相当于一个 map,方便导入library 统一引入配置

```
ext {

    compileSdkVersion = 30
    buildToolsVersion = "30.0.0"

    targetSdkVersion = 30

    versionCode = 800800
    versionName = "0.8.8"
}
```
#### 导入 ijkplayer-example 
- 这个项目，他本身是一个可运行的项目，并且依赖前面编译好的各个版本动态链接库 library，将其变为library
则修改该模块 build.gradle 将 apply plugin: 'com.android.application' 改为 apply plugin: 'com.android.library'。

- ijkplayer-example 的清单文件还设置了启动的 Activity过滤器，将其删除

```
<intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
```
#### 各个CPU架构动态链接库 library 合并，简化项目依赖
- 在导入的ijkplayer-java项目中ijkplayer-java\src\main\ 目录新建 jniLibs 并将我们各个ijkplayer-armv5、ijkplayer-armv7a、ijkplayer-x86、ijkplayer-x86_64 目录中的 \src\main\libs 目录下的文件夹拷贝到 jniLibs 文件夹中

#### 处理依赖报错

- ijkplayer-example依赖的包版本比较低，应该更改为我们对于版本support包，需要将ijkplayer-example的一些报错的类正确导包
- 和ijkplayer-java中一样，在ijkplayer-example目录下新建gradle.properties 文件并加入项目配置

```
POM_NAME=ijkplayer-example
POM_ARTIFACT_ID=ijkplayer-example
POM_PACKAGING=aar
```
- ijkplayer-example 的 build.gardle 中productFlavors 配置删除

#### 将ijkplayer-example依赖到主项目工程

```
implementation project(':ijkplayer-example')

```
#### 编译通过项目结构

![改完编译通过项目配置](https://github.com/maoqitian/MaoMdPhoto/raw/master/ijkplayer%E7%BC%96%E8%AF%91/%E6%94%B9%E5%AE%8C%E7%BC%96%E8%AF%91%E9%80%9A%E8%BF%87%E9%A1%B9%E7%9B%AE%E9%85%8D%E7%BD%AE.png)

#### 测试简单播放

- 简单调用ijkplayer-example中已经写好的IjkVideoView测试ijkplayer视频播放，当然如果你觉得它写得不好则前面步骤可以不导入ijkplayer-example，只导入 ijkplayer-java 并自己写一个VideoView都是没问题的，这里我就偷懒了，直接测试一下，分别测试了 https、rtmp、http、rtsp等流媒体协议的媒体资源，rtsp播放不了，哈哈哈，[可以参考module-lite-more.sh](https://github.com/CarGuo/GSYVideoPlayer/blob/master/module-lite-more.sh)配置自己编译动态链接库，给出简单实用代码，布局文件就不贴了，敢兴趣可以在查看本[demo源码](https://note.youdao.com/)

```
/**
 * 简单使用 ijkplayer  demo 提供的  IjkVideoView
 */

class MainActivity : AppCompatActivity() {


    private var setting:Settings? = null
    private var mAndroidMediaController:AndroidMediaController? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setting = Settings(this)

        mAndroidMediaController = AndroidMediaController(this,false)

        IjkMediaPlayer.loadLibrariesOnce(null)
        IjkMediaPlayer.native_profileBegin("libijkplayer.so")

        //https
        val mVideoPath1 = "https://www.apple.com/105/media/us/iphone-x/2017/01df5b43-28e4-4848-bf20-490c34a926a7/films/feature/iphone-x-feature-tpl-cc-us-20170912_1920x1080h.mp4"
        //rtmp
        val mVideoPath2 = "rtmp://58.200.131.2:1935/livetv/hunantv"
        //hls
        val mVideoPath3 = "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8"
        //rtsp
        val mVideoPath4 = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov"

        mVideoView.setMediaController(mAndroidMediaController)
        mVideoView.setHudView(video_msg)

        editVideoPath.setText(mVideoPath3)


        btplay.setOnClickListener{

            if (TextUtils.isEmpty(editVideoPath.text)) {
                Toast.makeText(this, "视频地址不能为空", Toast.LENGTH_LONG).show();
            } else {
                mVideoView.setVideoURI(Uri.parse(editVideoPath.text.toString().trim()))
                mVideoView.start()
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()

        mVideoView.stopPlayback()
        mVideoView.release(true)
        mVideoView.stopBackgroundPlay()
        IjkMediaPlayer.native_profileEnd()
    }
}
```
#### 播放效果

![简单测试播放功能](https://github.com/maoqitian/MaoMdPhoto/raw/master/ijkplayer%E7%BC%96%E8%AF%91/%E7%AE%80%E5%8D%95%E6%B5%8B%E8%AF%95%E6%92%AD%E6%94%BE%E5%8A%9F%E8%83%BD.jpg)

#### demo地址
[ijkplayerdemo](https://github.com/maoqitian/ijkplayerdemo)

## 编译遇到问题

- “You must define ANDROID_NDK before starting”，出现该问题说明没有正确配置ndk环境变量，在.bash_profile 文件配置参照如下

```
export ANDROID_SDK=/Users/{你的路径XXX}/Library/Android/sdk
export ANDROID_NDK=/Users/{你的路径XXX}/Library/Android/ndk/android-ndk-r14b
export PATH=$PATH:$ANDROID_SDK/tools
export PATH=$PATH:$ANDROID_SDK/platform-tools
export PATH=$PATH:$ANDROID_NDK
```

- 执行./compile-ffmpeg.sh脚本编译错误提示“fatal error: linux/perf_event.h: No such file or directory”，出现该错误是该库是linux下的，在mac直接禁用就行，修改方式为 config 目录下的module-default.sh脚本加入如下配置，再次回到
android/contrib下执行./compile-ffmpeg.sh clean 清除操作然后继续编译 ./compile-openssl.sh all
```
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --disable-linux-perf"
export COMMON_FF_CFG_FLAGS="$COMMON_FF_CFG_FLAGS --disable-bzlib"
```

## 总结反思

- 前面弄了一大堆，现在想想其实已经有许多大佬已经将播放器，各种适配动态链接库都已经封装打包好成了开源库，比如GSY大佬的[GSYVideoPlayer](https://github.com/CarGuo/GSYVideoPlayer)，那我这么费劲搞这些干嘛。哈哈，我相信多折腾总是没错的，实践出真知，根据自己的需求配置编译出 so 文件，再站在巨人肩膀结合 GSYVideoPlayer
会不会更香呢？
## 参考

- [bilibili/ijkplayer](https://github.com/bilibili/ijkplayer)
- [IJKPlayer编译so支持HTTPS的踩坑历程](https://www.jianshu.com/p/bd289e25d272)
- [Android ABI](https://developer.android.com/ndk/guides/abis)