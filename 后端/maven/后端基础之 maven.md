## 项目管理利器

>1.下载 maven 
  
  - [下载地址](http://maven.apache.org/download.cgi)

>2. 配置环境变量
  
- 变量名 MAVEN_HOME
- 路径(将上一步下载好的maven压缩包解压的路径路径)，我这里是 D:\Program Files\maven\apache-maven-3.5.4-bin
  
- 在系统Path 中加入 %MAVEN_HOME%\bin
- 如图所示配置好环境变量
    
![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/maven%E9%85%8D%E7%BD%AE/%E9%85%8D%E7%BD%AEmaven%E7%8E%AF%E5%A2%83%E5%8F%98%E9%87%8F1.png)
     
![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/maven%E9%85%8D%E7%BD%AE/maven%E7%8E%AF%E5%A2%83%E5%8F%98%E9%87%8F%E9%85%8D%E7%BD%AE2.png) 
   
- 配置成功查看 maven 版本
     
     ![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/maven%E9%85%8D%E7%BD%AE/maven%E7%8E%AF%E5%A2%83%E5%8F%98%E9%87%8F%E9%85%8D%E7%BD%AE%E6%88%90%E5%8A%9F.png)

> maven 会在用户的根目录下生成 .m2 文件夹，里面会存放maven 下载的依赖包，为了减小C盘控件，同时也可以配置maven的国内镜像增加下载速度，在.m2文件夹下配置 setting.xml
## 配置本地仓库
    
- 可以把repository文件夹放到C盘以外的盘，并setting.xml在指定repository文件夹目录（没有setting.xml文件可以在maven安装目录的conf目录下复制一个到.m2文件夹下）
    ![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/maven%E9%85%8D%E7%BD%AE/maven%E9%85%8D%E7%BD%AE%E6%9C%AC%E5%9C%B0%E4%BB%93%E5%BA%93%E8%B7%AF%E5%BE%84.png)
   
- 配置阿里云镜像加快依赖包下载速度 
      
        
      ```
      <mirrors>
	  <id>nexus-aliyun</id>  
      <name>nexus-aliyun</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public</url>  
      <mirrorOf>central</mirrorOf>  
      </mirrors>

      ```
      
      
      
      
  ![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/maven%E9%85%8D%E7%BD%AE/maven%E9%85%8D%E7%BD%AE%E9%98%BF%E9%87%8C%E9%95%9C%E5%83%8F%E4%BB%93%E5%BA%93.png)
