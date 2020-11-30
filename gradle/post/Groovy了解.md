# Groovy了解
> Apache Groovy是一种强大的、可选类型的动态语言，具有静态类型和静态编译功能，由于具有简洁、熟悉和易于学习的语法，Java平台旨在提高开发人员的工作效率。它可以与任何Java程序顺畅地集成，并立即向应用程序交付强大的特性，包括脚本功能、特定领域的语言创作、运行时和编译时元编程以及函数式编程(官方文档对groovy描述)。

## Groovy 开发环境安装
- [groovy 下载地址](http://www.groovy-lang.org/download.html#gvm)
- 如果是 Mac 系统，可以使用 brew 命令安装

```
brew install groovy
```
- 安装完成在.bash_profile 文件中配置环境变量（MacOs）

```
You should set GROOVY_HOME:
  export GROOVY_HOME=/usr/local/opt/groovy/libexec
```
- 最后看看版本号是否安装成功

```
groovy -v

## 打印输出
Groovy Version: 3.0.4 JVM: 14.0.1 Vendor: Oracle Corporation OS: Mac OS X
```

## Groovy 基本认识

- groovy 注释和 Java 一样 支持 // 和 /**/
- groovy 语法和 kotlin 一样不需要写 ；
- groovy 支持动态类型，定义变量可以使用 def 关键字

```
def a = 1
def s = "test"
def int b = 1 //定义变量也可以知道类型
```
- 定义函数也无需指定参数类型和和返回值类型

```
def test(arg1,arg2){ //定义两个参数 arg1 arg2
     //逻辑代码
     ....
    
    // 函数最后一行为返回值 函数返回值为 int 类型
    10
}

String test2(arg1,arg2){ //指定返回值类型 String 则不必写关键字 def
     //逻辑代码
     ....
    
    // 函数最后一行为返回值 函数返回值为 String 类型
    //函数指定返回值 则返回值必须一致
    // return 可写可不写
    return "aaa"
}

```
- 调用函数可以不带括号，但是建议还是要带上括号，否则如何函数需要输入参数就会吧函数调用和属性混淆

```
//打印输出 hello
println("hello")

//可以写成
println "hello"
```

## Groovy 语法

###  Gradle Hello

- 国际惯例，首先来看看一个语言的 hello world，新建一个 test.groovy，使用 groovy test.groovy 运行

```
println("hello groovy")
```
- 运行结果
 
![groovy test.groovy](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/groovylang/groovy-hello-world.png)


### 基本数据类型

- groovy 语言所有东西都是对象，和Java一样，它也有 int，long ，boolean 这些基本数据类型，不过我们不要显示声明，groovy会自行判变量类型，在 Groovy 代码中其实对应的是它们的包装数据类型。比如 int 对应为 Integer，long 对应为 Long。

```
def a = 1
def b = "hello groovy"
def c = false
def d = 10000000000000
println a.getClass().getCanonicalName()
println b.getClass().getCanonicalName()
println c.getClass().getCanonicalName()
println d.getClass().getCanonicalName()
```
- 执行结果

![groovy基本数据类型执行结果](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/groovylang/groovy%E5%9F%BA%E6%9C%AC%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B%E6%89%A7%E8%A1%8C%E7%BB%93%E6%9E%9C.png)

### Groovy 中的容器类

- groovy 容器类有三种，分别为为 List、Map和Range，对应到 Java 则为 ArrayList、LinkedHashMap 和对 List 的扩展

#### groovy List

- List 对应由 [] 括号定义，其数据类型可以使任何的对象

```
//随意添加各种类型对象 变量打印

def aList = [5,'string',true]
//元素变量
aList.each {
   //it是是与当前元素对应的隐式参数
    println "Item: $it"
}

//添加元素

//查找元素 find 方法
//
println(aList.find{ it > 1 })
println(aList.findAll{ it > 1 })

//删除元素

//执行结果
Item: 5
Item: string
Item: true
```
![groovyList操作执行结果](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/groovylang/groovyList%E6%93%8D%E4%BD%9C%E6%89%A7%E8%A1%8C%E7%BB%93%E6%9E%9C.png)

#### groovy Map

- Map 变量由 [:] 符号定义，**冒号左边是 key，右边是 Value。key 必须是字符串，value 可以是任何对 象。另外，key 可以用''或""包起来，也可以不用引号包起来**

```
//其中的 key1 和 key2 默认被处理成字符串"key1"和"key2"
def aNewMap = [key1:"hello",key2:false]
//map 取值
println aNewMap.key1
println aNewMap['key2']
//为 map 添加新元素
aNewMap.anotherkey = "i am map"

aNewMap.each{
    println "Item: $it"
}

//执行结果
hello
false
Item: key1=hello
Item: key2=false
Item: anotherkey=i am map
```

#### groovy Range

- Range 由字面意思是范围，它其实为List的扩展，代表一个List 的范围，**由 begin 值+两个点+end 值表示**

```
//标识 list 相当于数学闭包 [1,5]
def mRange = 1..5

mRange.each {
    println "Item: $it"
}

//标识 list 相当于数学闭包 [1,5)
def mRange1 = 1..<5

mRange1.each {
    println "other Item: $it"
}

//获取开头结尾元素
println mRange.from
println mRange.to
```
![groovyRange执行结果](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/groovylang/groovyRange%E6%89%A7%E8%A1%8C%E7%BB%93%E6%9E%9C.png)

#### groovy-lang API 文档

- 更多使用可以自行查看 [groovy-lang api文档](http://www.groovy-lang.org/api.html)

### Groovy 闭包（Closure）

- Groovy语言的闭包，英文叫 Closure，是一种数据类型，它代表了一段可执行的代码
- 闭包格式

```
//格式一 有参数
def xxx = {
params -> 
//返回值
code逻辑
} 

//格式二 无参数 不需要 ->
def xxx = {
  code 逻辑
} 
```
- 示例
```
//闭包是一段代码，所以需要用花括号括起来
def testClosure = {
        //箭头前面是参数定义，箭头后面是代码
String param1, int param2 ->
        //逻辑代码，闭包最后一句是返回值
    println "hello groovy，$param1,$param2"
    //也可以使用 return，和 groovy 中普通函数一样
}
//闭包调用
testClosure.call("参数1",20)
testClosure("参数2",40)
//输出结果
hello groovy，参数1,20
hello groovy，参数2,40
```
- 如果闭包没定义参数的话，则隐含有一个参数 it，和 this 的作用类
似，it 代表闭包的参数，如下示例。

```
def greeting = {
//隐含参数
    "Hello, $it!"
}
println greeting('groovy') == 'Hello, groovy!'
//等同于：
def greeting1 = {
        //也可写出隐含参数
    it -> "Hello, $it!"
}
println greeting1('groovy') == 'Hello, groovy!'

//输出结果 不用说肯定都为 true
```
- 闭包省略括号，常用 doLast 函数

```
task hello{
    doLast ({
        //逻辑代码
        println'aaaaaa'
    })
}

//省略括号变成常用写法
task hello{
    doLast {
        //逻辑代码
        println'aaaaaa'
    }
}
```
### groovy 文件 I/O 操作

- 对于文件的读取写入，groovy也是有 api 的，它其实是对于原本的 Java 的 I/O 操作进行了一些封装，并加入闭包（Closure）来简化代码。新建测试文件 TestFile.txt

#### 读文件

```
def testFile = new File("TestFile")

//读文件每一行 eachLine

testFile.eachLine{
    String oneLine ->
    //打印每一行内容
        println oneLine
}

//获取文件 byte 数组

def bytes = testFile.getBytes()


//获取文件输入流

def is = testFile.newInputStream()

//和Java 一样不用需要关闭
is.close

//闭包 Groovy 会自动替你 close 
targetFile.withInputStream{ ips ->
    //逻辑代码
}
```
#### 写文件
- 将上述 TestFile 复制到 CopyFile
```
def copyFile = new File("CopyFile")

copyFile.withOutputStream{
    os->
        testFile.withInputStream{
            ips ->
            // << 是 groovy OutputStream 的操作符，它可以完成 InputStream 到 OutputStream 输出
                os << ips
        }
}

copyFile.eachLine{
    String oneLine ->
        println "copyFile oneLine：$oneLine"
}
```
![groovy复制文件内容执行结果](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/groovylang/groovy%E5%A4%8D%E5%88%B6%E6%96%87%E4%BB%B6%E5%86%85%E5%AE%B9%E6%89%A7%E8%A1%8C%E7%BB%93%E6%9E%9C.png)

#### XML操作

- 除了读取普通文件，groovy 也可以解析 XML 文件，它提供了一个 GPath 类来帮助解析 XML，平时使用 Gradle 可能需要读取清单文件（AndroidManifest）中一些数据

```
//创建 XmlSlurper 类

def xmlspr = new XmlSlurper()

//获取清单文件 file

def file = new File("AndroidManifest.xml")

//获取解析对象
//获取清单文件根元素，也就是 manifest 标签
def manifest = xmlspr.parse(file)

// 声明命名空间
//manifest.declareNamespace('android':'http://schemas.android.com/apk/res/android')
//获取包名
println manifest.'@package'

//获取 activity intent-filter

def activity = manifest.application.activity
//获取 intent-filter 设置的过滤条件 也可以此判断是否为应用程序入口 activity
activity.find{
   it.'intent-filter'.find { filter ->
       filter.action.find{
           println it.'@android:name'.text()
       }
       filter.category.find{
           println it.'@android:name'.text()
       }
   }
}
```
![解析清单文件执行结果](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/groovylang/%E8%A7%A3%E6%9E%90%E6%B8%85%E5%8D%95%E6%96%87%E4%BB%B6%E6%89%A7%E8%A1%8C%E7%BB%93%E6%9E%9C.png)

#### 更多介绍 api 官方文档
- [groovy File api 文档地址](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/File.html)
- [groovy InputStream](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/InputStream.html)

- [groovy OutputStream](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/OutputStream.html)
- [groovy Reader](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/Reader.html)
- [groovy Writer](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/Writer.html)
- [groovy Path](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/nio/file/Path.html)
## groovy 脚本到底是什么

- 首先可以像Java 那样写 groovy class，新建一个groovyclass目录，然后编写GroovyClass类

```
package groovyclass

class GroovyClass{

    String p1;
    int p2;

    GroovyClass(p1,p2){
        this.p1 = p1
        this.p2 = p2
    }

    //和 Java 类似 如果不声明 public/private
    //等访问权限的话，Groovy 中类及其变量默认都是 public

    def printParams(){
        println "参数：$p1,$p2"
    }
}
```
- 上面代码是不是很熟悉，而在其他文件需要使用则 improt 引入，如下新建 testClass.groovy 文件

```
import groovyclass.GroovyClass

def g = new GroovyClass("hello",100)
g.printParams()
```
![groovyclass执行结果](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/groovylang/groovyclass%E6%89%A7%E8%A1%8C%E7%BB%93%E6%9E%9C.png)

### groovy 脚本原理

- 前面这么多例子，执行都是利用命令 **groovy XXXX** 命令执行 groovy 脚本文件，而 groovy 最终会变成字节码.class 基于JVM 虚拟机的运行的，则可以猜想，在变成字节码之前是否会转变成 Java 文件呢？如下再次看到文章开头的 test.groovy 文件

```
println("hello groovy")
```
- 对该文件执行如下命令

```
groovyc -d classes test.groovy
```
- groovyc 命令是 groovy 的编译命令，同时将编译之后的字节码文件复制到当前目录下的classes文件夹，得到的 class 文件如下

![groovyclass文件](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20Gradle/groovylang/groovyclass%E6%96%87%E4%BB%B6.png)

- 1.由上图，首先 test.groovy 被转换成了 test 类，并继续 Script 类
- 2.其次，使用 CallSite 类型数组分别保存类对象和 groovy脚本中编写的代码
- 3.然后为这个类创建了静态 main 方法，并在 main 方法中动态代理调用 test 类的 run 方法
- 4.最后 run 方法执行 groovy 脚本编写的代码
- 到此，是否会有豁然开朗的感觉，当我们使用命令**groovy XXXX**执行 groovy 脚本，**其实是执行编译生成类对象的静态main方法，并在main方法中动态代理类对象执行了它的run方法来执行脚本中的逻辑。**

## demo

- [本文demo地址](https://note.youdao.com/)

## 参考

- [groovy-lang api文档](http://www.groovy-lang.org/api.html)
