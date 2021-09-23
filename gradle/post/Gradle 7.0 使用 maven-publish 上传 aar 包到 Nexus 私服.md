# Gradle 7.0 使用 maven-publish 上传 aar 包到 Nexus 私服
> 在组件化项目架构中每个组件管理我们一般使用分仓库管理，每个组件分别打包成aar包引入项目依赖。老版本 gradle 我们一般使用 maven 插件来上传aar包，而 Gradle 6.x 版本更新了上传插件为 maven-publish

```
# 低版本使用
apply plugin: 'maven'

# 6.X 以上版本变化
apply plugin: 'maven-publish'
```

## 如何生成 aar 包

- Android Gradle 插件有两种，一个Application，一个是library，Android 插件所创建的组件取决于模块是否使用应用或库插件，而library模块编译生成的产物就是 aar 包，如下[官方文档描述](https://developer.android.com/studio/build/maven-publish-plugin?hl=zh-cn#groovy)

![aar-apk对比](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/GradleMavenPublish/aar-apk%E5%AF%B9%E6%AF%94.png)

- 可以在 library 组件下执行 **assemble** 来生成 aar 包，输出目录为 *++build/outputs/aar++*，如下图所示

![生成aar包](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/GradleMavenPublish/%E7%94%9F%E6%88%90aar%E5%8C%85.png)

- [**可以先在官方文档了解maven-*publish插件配置***](https://docs.gradle.org/current/userguide/publishing_maven.html)

## aar 包上传本地仓库

### Gradle 6.0以下 maven 插件

- 直接使用 maven 测试发布在本项目目录中创建本地仓库目录

```
// maven-upload.gradle
apply plugin: 'maven'

group='com.mao.testmavenpush'
version='1.0.0'

uploadArchives {
    repositories {
        mavenDeployer {
            //在本项目目录下创建的Maven仓库设置
            repository(url: uri('../local_test_repo'))
        }
    }
}
```
### Gradle 7.0 maven-publish 插件

- 为了方便测试，我们可以吧 aar 包上传到本地创建的仓库文件夹，如下创建一个 gradle 上传本地脚本 maven-push.gradle，在本地 E 盘创建一个 maventestrepository 本地仓库文件夹

```
// maven-push.gradle
apply plugin: 'maven-publish'

afterEvaluate {
    publishing {
        //发布的 jar 包配置
        publications {
            release(MavenPublication) {
                groupId = 'com.mao.testmavenpush'
                artifactId = 'maven-push-test'
                version = '1.0.0'
                //aar 文件
                def projectName = project.getName()
                artifact "build/outputs/aar/${projectName}-release.aar"

                pom.withXml{
                    def dependenciesNode = asNode().appendNode("dependencies")
                    configurations.implementation.allDependencies.forEach(){
                        Dependency dependency ->
                            if (dependency.version != "unspecified" && dependency.name != "unspecified"){
                                def dependencyNode = dependenciesNode.appendNode('dependency')
                                dependencyNode.appendNode('groupId', dependency.group)
                                dependencyNode.appendNode('artifactId', dependency.name)
                                dependencyNode.appendNode('version', dependency.version)
                            }
                    }
                }

            }
        }
        //仓库地址配置
        repositories {
            maven {
                // test, upload local maven repository
                //url = "file:" + new File(project.rootProject.rootDir, "local_test_repo").path
                url = "file://e:/maventestrepository"
            }
        }
    }
}
```
- 在组件Module的 build.gradle 使用这个脚本

```
apply from: 'maven-push.gradle'

```

- 随后会有对应的Task自动生成

![自动生成task](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/GradleMavenPublish/%E8%87%AA%E5%8A%A8%E7%94%9F%E6%88%90task.png)

- 执行Gradle Task publish 后等待任务执行完本地仓库aar发布成功

![本地仓库发布aar成功](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/GradleMavenPublish/%E6%9C%AC%E5%9C%B0%E4%BB%93%E5%BA%93%E5%8F%91%E5%B8%83aar%E6%88%90%E5%8A%9F.png)

##  Nexus 私服发布 aar 包

- 和本地仓库差不多，Nexus 私服也是本地仓库，只不过是部署在内部服务器上，同理，编写一个repo-maven-push.gradle 脚本，

```
apply plugin: 'maven-publish'


def GroupId = 'com.mao.testmavenpush'
def ArtifactId = 'maven-push-test'
def Version = '1.0.1'

def userName = "xxxx"
def passWord = "xxxx"


def releasesRepoUrl = "http://xxx.xxx.116.12:9190/repository/maven-releases/"


task generateSourcesJar(type:Jar){
    from android.sourceSets.main.java.srcDirs
    classifier "sources"
}


afterEvaluate {
    publishing {
        //发布的 arr 包配置
        publications{
            //名字可以自己指定，如果有多渠道，整段多复制一个
            release(MavenPublication){

                groupId = GroupId//公司域名
                artifactId = ArtifactId//该aar包的名称
                version = Version//版本号

                // 必须有这个 否则不会上传AAR包
                afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }
                // 多渠道，可以自行指定aar路径
                // def projectName = project.getName()
                // artifact "build/outputs/aar/${projectName}-release.aar"
                // 上传source，这样使用方可以看到方法注释
                artifact generateSourcesJar
                //依赖关系
                pom.withXml{
                    def dependenciesNode = asNode().appendNode("dependencies")
                    configurations.implementation.allDependencies.forEach(){
                        Dependency dependency ->
                            if (dependency.version != "unspecified" && dependency.name != "unspecified"){
                                def dependencyNode = dependenciesNode.appendNode('dependency')
                                dependencyNode.appendNode('groupId', dependency.group)
                                dependencyNode.appendNode('artifactId', dependency.name)
                                dependencyNode.appendNode('version', dependency.version)
                            }
                    }
                }
           }
        }

        //仓库地址配置
        repositories {
            maven {
                //允许使用 http
                allowInsecureProtocol = true
                url = releasesRepoUrl
                credentials {
                    username = userName
                    password = passWord
                }
            }
        }
    }
}
```
- 由以上脚本，可以知道，和发布到本地仓库其实没什么区别，也就是改变上传地址罢了，但是这里会有一个**坑**，默认需要私服地址为 Https，想要使用 Http 则需要添加 **allowInsecureProtocol** 设置，[官方文档描述](https://docs.gradle.org/7.0.2/dsl/org.gradle.api.artifacts.repositories.UrlArtifactRepository.html#org.gradle.api.artifacts.repositories.UrlArtifactRepository:allowInsecureProtocol)

- 同理执行 Gradle Task publish 发布到 Nexus 私服

![发布aar到Nexus私服](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/GradleMavenPublish/%E5%8F%91%E5%B8%83aar%E5%88%B0Nexus%E7%A7%81%E6%9C%8D.png)

## 依赖使用发布Nexus私服aar包

- 要发布到 Nexus私服，首先需要自己搭建一个Nexus私服，可看我以前文章[Centos下 Nexus 3.x 搭建Maven 私服](https://juejin.cn/post/6844903879910359047)

- 在Gradle设置私服maven地址，同理也需要配置允许使用http，否则 sync 同步报错

```
allprojects {
    repositories {
        ......
        //本地 maven 私服
        maven {
            //允许使用 http
            allowInsecureProtocol = true
            url = "http://xxx.xxx.116.12:9190/repository/maven-public/" }
    }
}
```
- 工程中依赖使用，接下来就可以愉快的玩耍了

```
dependencies {
    .....
    //测试 aar 引入
    implementation 'com.mao.testmavenpush:maven-push-test:1.0.1'
}
```
![aar包引入成功](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/GradleMavenPublish/aar%E5%8C%85%E5%BC%95%E5%85%A5%E6%88%90%E5%8A%9F.png)

### 测试发布项目Demo地址

- [demo](https://github.com/maoqitian/HenCoderPractice/tree/main/MavenPublishProject)
