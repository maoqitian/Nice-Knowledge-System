# FFmpeg 4.3.1 + ndk-r21b 编译
> 看下文，干就完了
## 环境

系统软件 | 版本|功能
---|---|---
macOs| 10.15.5 |系统
NDK | android-ndk-r21b|Android NDK 是一个工具集，可让您使用 C 和 C++ 等语言以原生代码实现应用的各个部分
FFmpeg  | 4.3 | 流媒体解决方案

## 准备工作

- 仓库下载
```
git clone https://git.ffmpeg.org/ffmpeg.git ffmpeg
```
- [官网下载](http://ffmpeg.org/download.html#releases)
- NDK 安装 使用 [ndk-r21b](https://developer.android.google.cn/ndk/downloads)

## 编译

- shell 编译脚本如下

```
#!/bin/bash

# ndk路径
NDK=/Users/maoqitian/Library/Android/ndk/android-ndk-r21b
# 编译工具链目录，ndk17版本以上用的是clang，以下是gcc
TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/darwin-x86_64
# 版本号
API=21
# 交叉编译树的根目录(查找相应头文件和库用)
SYSROOT="${TOOLCHAIN}/sysroot"

# 定义执行configure的shell方法
function build_android() {
    ./configure \
        --prefix=$PREFIX \
        --enable-shared \
        --disable-static \
        --enable-jni \
        --disable-doc \
        --disable-programs \
        --disable-symver \
        --target-os=android \
        --arch=$ARCH \
        --cpu=$CPU \
        --cc=$CC \
        --cxx=$CXX \
        --enable-cross-compile \
        --sysroot=$SYSROOT \
        --extra-cflags="-Os -fpic $OPTIMIZE_CFLAGS" \
        --extra-ldflags="" \
        --enable-asm \
        --enable-neon \
         --disable-encoders \
         --enable-encoder=aac \
         --enable-encoder=mjpeg \
         --enable-encoder=png \
         --enable-encoder=mpeg4 \
         --disable-decoders \
         --enable-decoder=aac \
         --enable-decoder=aac_latm \
         --enable-decoder=h264 \
         --enable-decoder=mpeg4 \
         --enable-decoder=mjpeg \
         --enable-decoder=png \
         --disable-demuxers \
         --enable-demuxer=image2 \
         --enable-demuxer=h264 \
         --enable-demuxer=aac \
         --enable-demuxer=mpegvideo \
         --enable-demuxer=avi \
         --enable-demuxer=mov \
         --disable-parsers \
         --enable-parser=aac \
         --enable-parser=ac3 \
         --enable-parser=h264 \
         --enable-parser=mpeg4video \
         --enable-parser=mjpeg \
         --enable-gpl \
         --enable-pic \
         --disable-doc \
         --disable-ffmpeg \
         --disable-ffplay \
         --disable-ffprobe \
         --disable-symver \
         --disable-debug \
         --enable-small \
        $COMMON_FF_CFG_FLAGS
    make clean
    make -j16
    make install

    echo "编译完成 $CPU"
}
# armv7-a
OUTPUT_FOLDER="armeabi-v7a"
ARCH="arm"
CPU="armv7-a"
TOOL_CPU_NAME=armv7a
TOOL_PREFIX="$TOOLCHAIN/bin/${TOOL_CPU_NAME}-linux-androideabi"
OPTIMIZE_CFLAGS="-march=$CPU"
# 输出目录
PREFIX="${PWD}/android/$OUTPUT_FOLDER"
# 编译器
CC="${TOOL_PREFIX}${API}-clang"
CXX="${TOOL_PREFIX}${API}-clang++"
#开始编译
build_android

# arm64-v8a，这个指令集最低支持api21
OUTPUT_FOLDER="arm64-v8a"
ARCH="aarch64"
CPU="armv8-a"
TOOL_CPU_NAME=aarch64
TOOL_PREFIX="$TOOLCHAIN/bin/${TOOL_CPU_NAME}-linux-android"
OPTIMIZE_CFLAGS="-march=$CPU"
PREFIX="${PWD}/android/$OUTPUT_FOLDER"
CC="${TOOL_PREFIX}${API}-clang"
CXX="${TOOL_PREFIX}${API}-clang++"
build_android

# x86
OUTPUT_FOLDER="x86"
ARCH="x86"
CPU="x86"
TOOL_CPU_NAME="i686"
TOOL_PREFIX="$TOOLCHAIN/bin/${TOOL_CPU_NAME}-linux-android"
OPTIMIZE_CFLAGS="-march=i686 -mtune=intel -mssse3 -mfpmath=sse -m32"
PREFIX="${PWD}/android/$OUTPUT_FOLDER"
CC="${TOOL_PREFIX}${API}-clang"
CXX="${TOOL_PREFIX}${API}-clang++"
build_android

# x86_64，这个指令集最低支持api21
OUTPUT_FOLDER="x86_64"
ARCH="x86_64"
CPU="x86-64"
TOOL_CPU_NAME="x86_64"
TOOL_PREFIX="$TOOLCHAIN/bin/${TOOL_CPU_NAME}-linux-android"
OPTIMIZE_CFLAGS="-march=$CPU -msse4.2 -mpopcnt -m64 -mtune=intel"
PREFIX="${PWD}/android/$OUTPUT_FOLDER"
CC="${TOOL_PREFIX}${API}-clang"
CXX="${TOOL_PREFIX}${API}-clang++"
build_android
```

### 编译完成

- 编译完成，会在 FFmpeg项目目录生成一个 android 文件夹，里面就是帮我们生成好的动态链接库和头文件，如下图所示

![编译生成动态链接库](https://github.com/maoqitian/MaoMdPhoto/raw/master/AndroidFFmpeg/FFmpeg%E7%BC%96%E8%AF%91/%E7%BC%96%E8%AF%91%E7%94%9F%E6%88%90%E5%8A%A8%E6%80%81%E9%93%BE%E6%8E%A5%E5%BA%93.png) 


```
libavformat：用于各种音视频封装格式的生成和解析；
libavcodec：用于各种类型声音、图像编解码；
libavutil：包含一些公共的工具函数；
libswscale：用于视频场景比例缩放、色彩映射转换；
libpostproc：用于后期效果处理；
ffmpeg：该项目提供的一个工具，可用于格式转换、解码或电视卡即时编码等；
ffsever：一个 HTTP 多媒体即时广播串流服务器；
ffplay：是一个简单的播放器，使用ffmpeg 库解析和解码，通过SDL显示；
```


### FFmpeg 编译成一个动态链接库

- 前面编译每个CPU架构都生成好几个动态链接库，使用配置CMakeLists麻烦，我们可以考虑将其编译成一个 libffmpeg.so 库
- 合并逻辑脚本 union_ffmpeg_so.sh
- arm64
```
echo "开始编译ffmpeg so"

#NDK路径.
NDK=/Users/maoqitian/Library/Android/ndk/android-ndk-r21b


PLATFORM=$NDK/platforms/android-21/arch-arm64
TOOLCHAIN=$NDK/toolchains/aarch64-linux-android-4.9/prebuilt/darwin-x86_64

PREFIX=$(pwd)


$TOOLCHAIN/bin/aarch64-linux-android-ld \
-rpath-link=$PLATFORM/usr/lib \
-L$PLATFORM/usr/lib \
-L$PREFIX/lib \
-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
$PREFIX/libffmpeg.so \
    libavcodec.a \
    libavfilter.a \
    libswresample.a \
    libavformat.a \
    libavutil.a \
    libswscale.a \
    -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
    $TOOLCHAIN/lib/gcc/aarch64-linux-android/4.9.x/libgcc.a \
    ##压缩大小
    $TOOLCHAIN/bin/aarch64-linux-android-strip  $PREFIX/libffmpeg.so

echo "完成编译ffmpeg so"
```
- armv7
```
echo "开始编译ffmpeg so"

#NDK路径.
NDK=/Users/maoqitian/Library/Android/ndk/android-ndk-r21b


PLATFORM=$NDK/platforms/android-21/arch-arm
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64

PREFIX=$(pwd)

#如果不需要依赖x264，去掉/usr/x264/x264-master/android/armeabi-v7a/lib/libx264.a \就可以了

$TOOLCHAIN/bin/arm-linux-androideabi-ld \
-rpath-link=$PLATFORM/usr/lib \
-L$PLATFORM/usr/lib \
-L$PREFIX/lib \
-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
$PREFIX/libffmpeg.so \
    libavcodec.a \
    libavfilter.a \
    libswresample.a \
    libavformat.a \
    libavutil.a \
    libswscale.a \
    -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
    $TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a \

    $TOOLCHAIN/bin/arm-linux-androideabi-strip  $PREFIX/libffmpeg.so

echo "完成编译ffmpeg so"
```
- x86

```
echo "开始编译ffmpeg so"

#NDK路径.
NDK=/Users/maoqitian/Library/Android/ndk/android-ndk-r21b


PLATFORM=$NDK/platforms/android-21/arch-x86
TOOLCHAIN=$NDK/toolchains/x86-4.9/prebuilt/darwin-x86_64

PREFIX=$(pwd)

#如果不需要依赖x264，去掉/usr/x264/x264-master/android/armeabi-v7a/lib/libx264.a \就可以了

$TOOLCHAIN/bin/i686-linux-android-ld \
-rpath-link=$PLATFORM/usr/lib \
-L$PLATFORM/usr/lib \
-L$PREFIX/lib \
-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
$PREFIX/libffmpeg.so \
    libavcodec.a \
    libavfilter.a \
    libswresample.a \
    libavformat.a \
    libavutil.a \
    libswscale.a \
    -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
    $TOOLCHAIN/lib/gcc/i686-linux-android/4.9.x/libgcc.a \
# strip 精简文件
$TOOLCHAIN/bin/i686-linux-android-strip  $PREFIX/libffmpeg.so

echo "完成编译ffmpeg so"
```

## 集成到 Android 环境测试

- 新建普通项目，添加一个 Moudle -> Android Library
- 新建 jniLibs 导入上面合并好的 so 库
- 新建 cpp 目录导入FFmpeg 头文件
- 新建 CMakeLists.txt 文件指定要编译的路径，注意CMakeLists.txt 要放在Library 根目录，否则访问文件路径会有问题

```
#设置构建本机库文件所需的 CMake的最小版本
cmake_minimum_required(VERSION 3.4.1)

# 添加头文件路径
include_directories(
        src/main/cpp
        src/main/cpp/include
)

# 定义源码所在目录
aux_source_directory(src/main/cpp SRC)

#添加外部的库(可以是动态库或静态库)
set(distribution_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

MESSAGE(WARNING "${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}")

#添加自己写的 C/C++源文件 CMAKE_SOURCE_DIR被定为到app/src/main/cpp下，所以配置的src/main/…/会定位不到，
#所以将app/src/main/cpp目录下的CMakeLists剪切到app目录下与src同级
add_library(
        ffplayer #so名称
        SHARED #动态库
        ${SRC} #源码目录
)

#libavutil.so
add_library(
        ffmpeg
        SHARED
        IMPORTED)
#指定libavutil.so库的位置
set_target_properties(
        ffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libffmpeg.so)

#依赖NDK中自带的log库
find_library(log-lib log)

#链接库
#链接ffmpeg so，ffmpeg模块链接有些有先后顺序，如果不注意某些方法可能在使用时报错
#用 -Wl,--start-group  -Wl,--end-group 包裹起来可以不用去留意so的顺序
target_link_libraries(
        ffplayer
        ffmpeg

        android
        OpenSLES

        ${log-lib})
```
- 新建 ffmpeg_utils.cpp，编写JNI 方法可以测试 FFmpeg 是否编译成功

```
#include <jni.h>
#include <string>

// FFmpeg 是 C 语言写的，需要使用extern "C" 引入
extern "C" {
#include "libavformat/avformat.h"
#include <libavutil/avutil.h>
#include "libavcodec/avcodec.h"
#include "libavfilter/avfilter.h"

//获取版本 实际获取的是实际的发布版本号或git提交描述
JNIEXPORT jstring JNICALL
Java_com_mao_ffmpegplayer_MainActivity_getVersion(JNIEnv *env, jobject clazz) {
    const char *version = av_version_info();
    return env->NewStringUTF(version);
}

//格式信息
JNIEXPORT jstring JNICALL
Java_com_mao_ffmpegplayer_MainActivity_avformatInfo(JNIEnv *env, jobject instance) {

    char info[40000] = { 0 };

    AVInputFormat *if_temp = av_iformat_next(NULL);
    AVOutputFormat *of_temp = av_oformat_next(NULL);
    //Input
    while (if_temp != NULL){
        sprintf(info, "%s[In ][%10s]\n", info, if_temp->name);
        if_temp = if_temp->next;
    }
    //Output
    while (of_temp != NULL) {
        sprintf(info, "%s[Out][%10s]\n", info, of_temp->name);
        of_temp = of_temp->next;
    }

    return env->NewStringUTF(info);
}

//编码信息
JNIEXPORT jstring JNICALL
Java_com_mao_ffmpegplayer_MainActivity_avcodecInfo(JNIEnv *env, jobject instance) {

    char info[40000] = { 0 };

    AVCodec *c_temp = av_codec_next(NULL);

    while (c_temp!=NULL) {
        if (c_temp->decode!=NULL){
            sprintf(info, "%s[Dec]", info);
        }
        else{
            sprintf(info, "%s[Enc]", info);
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s[Video]", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s[Audio]", info);
                break;
            default:
                sprintf(info, "%s[Other]", info);
                break;
        }
        sprintf(info, "%s[%10s]\n", info, c_temp->name);


        c_temp=c_temp->next;
    }

    return env->NewStringUTF(info);
}

JNIEXPORT jstring JNICALL
Java_com_mao_ffmpegplayer_MainActivity_avfilterInfo(JNIEnv *env, jobject instance) {

    char info[40000] = { 0 };
    AVFilter *f_temp = (AVFilter *)avfilter_next(NULL);
    while (f_temp != NULL){
        sprintf(info, "%s[%10s]\n", info, f_temp->name);
        f_temp = f_temp->next;
    }
    return env->NewStringUTF(info);
}

//获取配置 so 文件编译配置信息
extern "C" JNIEXPORT jstring JNICALL
Java_com_mao_ffmpegplayer_MainActivity_configurationInfo(JNIEnv *env, jobject instance) {

    std::string hello = avcodec_configuration();
    return env->NewStringUTF(hello.c_str());
}

}
```
![library 项目目录](https://github.com/maoqitian/MaoMdPhoto/raw/master/AndroidFFmpeg/FFmpeg%E7%BC%96%E8%AF%91/library%20%E9%A1%B9%E7%9B%AE%E7%9B%AE%E5%BD%95.png)

### 简单调用测试
- 随意编写一个界面，加载刚刚CMakeLists.txt设置编译生成的**libffplayer.so**，具体其他代码就不贴了，可以自行查看文末项目中代码
```
/**
     * A native method that is implemented by the  native library,
     * which is packaged with this application.
     */
    //获取版本 实际获取的是实际的发布版本号或git提交描述
    private external fun getVersion(): String
    private external fun avformatInfo(): String
    private external fun avcodecInfo(): String
    private external fun avfilterInfo(): String
    //获取配置 so 文件编译配置信息
    private external fun configurationInfo(): String

    companion object {
        init {
            System.loadLibrary("ffplayer")
        }
    }
```

### 测试结果

<img src="https://github.com/maoqitian/MaoMdPhoto/raw/master/AndroidFFmpeg/FFmpeg%E7%BC%96%E8%AF%91/ffmpegdemo.gif"  height="300" width="170"><img>

## 项目地址

[项目地址](https://github.com/maoqitian/XFFmpegPlayer)

## 编译错误

- https://blog.k-res.net/archives/2521.html

## 参考链接

- [最简单的基于FFmpeg的移动端例子](https://blog.csdn.net/leixiaohua1020/article/details/47008825)
- [Android NDK交叉编译FFmpeg](https://xucanhui.com/2018/07/22/android-ndk-ffmpeg-compile/)
- [原来FFmpeg这么有意思 (二)](https://juejin.cn/post/6844903798486351880#heading-7)