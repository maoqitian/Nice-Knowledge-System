- https://blog.csdn.net/s13383754499/article/details/78914592
- apktool:反编译apk 

  可以查看 smali语法 .class 文件
  
  - aapt.exe，apktool.bat，apktool.jar ，将需要反编译的APK文件放到该目录下
  - 输入以下命令：
  
    ```
    apktool.bat d -f app-wandoujia-release.apk -o app-wandoujia-release
    ```

  - 如果你想将反编译完的文件重新打包成apk，那你可以：输入apktool.bat   b    test（你编译出来文件夹）便可
  

- dex2jar-0.0.9.15：将akp 解压之后的 .dex文件变成 .jar 文件

  - 将要反编译的APK后缀名改为.rar或则 .zip，并解压，得到其中的额classes.dex文件（它就是java文件编译再通过dx工具打包而成的），将获取到的classes.dex放到之前解压出来的工具dex2jar-0.0.9.15 文件夹内
  - 在命令行下定位到dex2jar.bat所在目录，
  - 输入dex2jar classes.dex
   



- jd-gui-0.3.5.windows：查看上面步骤 .jar 文件中的源代码

  - 在改目录下会生成一个classes_dex2jar.jar的文件，然后打开工具jd-gui文件夹里的jd-gui.exe，之后用该工具打开之前生成的classes_dex2jar.jar文件，便可以看到源码了
