# 重新认识Java字节码
> Java 语言一次编译，处处运行？Java 虚拟机（JVM）只能识别运行 .class 文件，而我们写的 Java 文件经过 javac 编译变成.class（字节码） 文件，就能被Java虚拟机识别运行；但是 JVM 也不局限 Java 语言，也支持其他语言 Groovy、Scala 、kotlin ，而他们最终也会编译成 .class 字节码问题运行在 JVM中。上面所述都最终关联到 .class 字节码，接下来就看看 .class 到底是啥。

## 字节码数据结构
- 字节码文件由无符号数和表组成

### 无符号数
- 属于基本的数据类型，以u1、u2、u4、u8来分别代表1个字节、2个字节、4个字节和8个字节的无符号数，无符号数可以用来描述数字、索引引用、数量值或者字符串（UTF-8编码）

### 字节码表
- 表是由多个无符号数或者其他表作为数据项构成的复合数据类型，class文件中所有的表都以“_info”结尾。其实，整个 Class 文件本质上就是一张表

## 字节码文件结构

- 前面提到字节码由无符号数和表两种数据结构组成，而**这些结构按照预先规定好的顺序紧密的从前向后排列，之间没有间隔**，他们顺序如下表所示

#### class 文件结构组成排序表

 1 | 2| 3| 4| 5| 6| 7| 8
---|---|---|---|---|---|---|---
魔数 | 版本号| 常量池| 访问标志| 类/父类/接口| 字段描述集合| 方法描述结合| 属性描述集合


- 当 JVM 加载某个 class 文件时，JVM 就是根据上图中的结构去解析 class 文件，加载 class 文件到内存中，并在内存中分配相应的空间，具体结构空间占用可以对照下表

字段 | 名称| 数据类型| 数量
---|---|---|---
magic number | 魔数| u4（4字节）|  1 
major version| 版本号| u2（2字节）|  1 
minor version | 副版本号| u2| 1 
constant_pool_count | 常量池大小| u2| 1
constant_pool | 常量池| cp_info| constant_pool_count - 1
access_flag | 访问标志| u2| 1 
this_class | 当前类索引| u2|  1 
super_class | 父类索引| u2| 1
interfaces_count | 接口索引集合大小| u2| 1
interfaces | 接口索引集合| u2| interfaces_count
fields_count | 字段索引集合大小| u2| 1
fields | 接口索引集合| field_info| fields_count
methods_count | 方法索引集合大小| u2| 1
methods | 方法索引集合| method_info| methods_count
attributes_count | 属性索引集合大小| u2| 1
attributes | 属性索引集合| attributes_info| attributes_count

## 字节码文件实例解析
- 简单Java 代码，javac 编译之后用编辑器打开如下图

```
/**
 * @Description: class 字节码解析测试
 * @Author: maoqitian
 * @CreateDate: 2020/10/21 22:47
 */
public class ClazzTest {

    int a = 0;

    public int add(int x,int y){
        return  x+y;
    }

   
}
```
![class_new_open文件打开](https://raw.githubusercontent.com/maoqitian/MaoMdPhoto/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/class_new_open.png)

> 注意：要查看以上图可以使用vim 编辑器
命令模式切换为16进制查看 %!xxd
切换为正常模式 %!xxd -r

>如上图看到之后你可能会懵逼，没关系，前面了解过class 文件结构组成排序表，图中字符都是16进制，每两个为一组代表一个字节，接下来就按照表的顺序逐步解析看看

### 魔数（magic）

- 排在最前面的就是魔数，魔数由前面表可得它的空间是u4，也就是四个字节，如下图

![magic_number魔数](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/magic_numbe.png)
- 魔数固定为-0XCAFEBABE，它是一个字节码class 文件的标志，JVM 首先解析魔数正确才认定这是一个标准的字节码文件

### 副版本号minor_version，主版本号 major_version

![class_version版本号](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/class_version.png)

- 接着是版本号，版本号分为主版本号和副版本号，如上图；两个版本号分别占用u2空间，副版本号（minor version）在前主版本号在后（major version），加起来也是四个字节，0034转换十进制为52，加上副版本号变成52.0，对应为 jdk 版本 java version "1.8.0_251"。
- Java各个主版本号，以供参考

JDK版本	| 字节码中的主版本号
---|---
Java 1.2 | 0x002E=46
Java 1.3 | 0x002F=47
Java 1.4 | 0x0030=48
Java 5 | 0x0031=49
Java 6 | 0x0032=50
Java 7 | 0x0033=51
Java 7 | 0x0034=52

### 常量池（constant_pool）

- 版本号之后为常量池，它是一个表为cp_info；在常量池中保存了类的各种相关信息，比如类的名称、父类的名称、类中的方法名、参数名称、参数类型等，这些信息都是以各种表的形式保存在常量池中的

- 常量池每一项都是一个表，每个表的信息如下

![常量池总体表结构](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E5%B8%B8%E9%87%8F%E6%B1%A0%E6%80%BB%E4%BD%93%E8%A1%A8%E7%BB%93%E6%9E%84.png)

- 常量池表结构

![常量池表](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E5%B8%B8%E9%87%8F%E6%B1%A0%E8%A1%A8.png)

- 可以看到每个表结构都有一个 tag，对应数字标识这个表的类型。接着前面分析,常量池大小为两个字节，也就是 0018 标识常量池大小，十进制为24，其中常量池 index = 0 是被JVM保留，所以实际常量池大小为 23

![constant_size常量池大小](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/constant_size.png)

#### 常量池常亮解析

- 继续往下看，如下图，常量池第一个常量tag **0a**等于10，表为CONSTANT_Methodref_info，接下来两个常量分别指向此方法的所属类在常量池index为4，方法名称和类型在常量池index为20，这样常量池第一个常量分析完成

```
CONSTANT_Methodref_info{

 u1 tag=10;

 u2 class_index;指向此方法的所属类

 u2 name_type_index;指向此方法的名称和类型

}
```
![first_constant](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/first_constant.png)

- 接着常量池下一个常量，**09** 标识 tag 为 9，常量表结构为
CONSTANT_FIeldref_info，同理接下来的四个字节分别代表所属类在常量池index为3，指向此方法的名称和类型在常量池index为21

```
CONSTANT_Fieldref_info{
    u1 tag;
    u2 class_index;        指向此字段的所属类
    u2 name_type_index;    指向此字段的名称和类型
}
```
![second_constant](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/second_constant.png)

- 接下来常量都是一样的解析，其实我们可以使用Java 命令来查看 .class 文件，

```
javap -v ClazzTest.class //替换为需要查看 class 文件名
```
![命令查看字节码文件](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E5%91%BD%E4%BB%A4%E6%9F%A5%E7%9C%8B%E5%AD%97%E8%8A%82%E7%A0%81%E6%96%87%E4%BB%B6.png)

- 通过上图，前面我们解析的第一个常量保存的是 Java 的 默认 Object 对象的构造方法，第二个常量保存时当前ClazzTest的字段 a
- 常量池就分析到这

### 访问标志（access_flags）
- 接着常量池之后的就是访问标志，如下图

![访问标志](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E8%AE%BF%E9%97%AE%E6%A0%87%E5%BF%97.png)

标志名称 | 值| 含义
---|---|---
ACC_PUBLIC | 0x0001| 标记为 public，可以被类外访问
ACC_FINAL | 0x0010| 标记定义为 final，不允许有子类
ACC_SUPER | 0x0020| 当调用到 invokespecial 指令时，需要特殊处理的父类方法（编译器默认添加）
ACC_INTERFACE | 0x0200	| 是一个接口
ACC_ABSTRACT | 0x0400	| 是一个抽象类，不能够被实例化
ACC_SYNTHETIC | 0x1000	| 标记是由编译器产生的，不存在于源码中
ACC_ANNOTATION | 0x2000| 标记为注解类型
ACC_ENUM | 0x4000| 标记为枚举类型
- 访问标志用于标识对应类或者接口的访问权限，平时定义类一般都为public，或者抽象类abstract，由上图访问标志位 为十六进制21，也就是 0x0001 和 0x0020组合，在命令查看字节码文件图中也可以看到access_flags对应 ACC_PUBLIC 和 ACC_SUPER。

### 类/父类/接口索引

- 排在标志位后面的分别为类本身索引、父类索引和接口索引，如下图

![类索引父类索引接口索引](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E7%B1%BB%E7%B4%A2%E5%BC%95%E7%88%B6%E7%B1%BB%E7%B4%A2%E5%BC%95%E6%8E%A5%E5%8F%A3%E7%B4%A2%E5%BC%95.png)
- 由上图，类索引在常量池中 index 为 0003，父类索引 index 为0004，接口索引为 0000，也就是当前类没有实现接口，结合前面命令字节码图

![类方法接口索引命令图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E7%B1%BB%E6%96%B9%E6%B3%95%E6%8E%A5%E5%8F%A3%E7%B4%A2%E5%BC%95%E5%91%BD%E4%BB%A4%E5%9B%BE.png)
- 可以得出结论，当前类是ClazzTest，它的父类为 Java 所有类的父类 Object，没有实现接口。

### 字段表

- 接着类、父类和接口索引后面的就是字段表，字段表主要描述当前类中声明的变量。但是一个类中声明的变量总是不固定的，所以在字段表前有一个2字节大小的计算器来标识当前类中的变量个数，如下图

![类中变量个数](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E7%B1%BB%E4%B8%AD%E5%8F%98%E9%87%8F%E4%B8%AA%E6%95%B0.png)

- 由上图我们声明了一个变量，接着就是字段表，字段表数据结构如下

```
CONSTANT_Fieldref_info{
    u2  access_flags    字段的访问标志
    u2  name_index          字段的名称索引(也就是变量名)
    u2  descriptor_index    字段的描述索引(也就是变量的类型)
    u2  attributes_count    属性计数器
    attribute_info
}
```
- 在字段表结构我们可以看到字段表的访问标志access_flags，它代表当前变量是 public 、private、final、static 等标识，它对于关系在常量池小结已经列出表格，接着看下图

![字段表分析截图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E5%AD%97%E6%AE%B5%E8%A1%A8%E5%88%86%E6%9E%90%E6%88%AA%E5%9B%BE.png)

- 可以看到字段表 access_flags 为0002 也就是 private，变量名称 index 为0005，变量类型index 为 0006，结合字段表常量池命令图

![字段表常量池命令截图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E5%AD%97%E6%AE%B5%E8%A1%A8%E5%B8%B8%E9%87%8F%E6%B1%A0%E5%91%BD%E4%BB%A4%E6%88%AA%E5%9B%BE.png)

- 可以看到声明了一个私有 Int 类型的变量 a。

### 方法表

- 方法表同理，类中定义的方法也会不止一个，所以方法表也是从方法计数器开始的，如下图，显示有了两个方法，而我们代码值定义了一个，别忘了，构造方法也是方法。

![方法计数器截图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E6%96%B9%E6%B3%95%E8%AE%A1%E6%95%B0%E5%99%A8%E6%88%AA%E5%9B%BE.png)
- 方法表结构如下
```
CONSTANT_Methodref_info{
    u2  access_flags;        方法的访问标志
    u2  name_index;          指向方法名的索引
    u2  descriptor_index;    指向方法类型的索引
    u2  attributes_count;    方法属性计数器
    attribute_info attributes;
}
```
- 方法也是有自己的访问标志，方法也可以是 publice , static，private 等，表关系如下

标志名称 | 值| 含义
---|---|---
ACC_PUBLIC | 0x0001| 标记为 public，可以被类外访问
ACC_PRIVATE | 0x0002| 标记定义为 final
ACC_PROTECTED | 0x0004| 方法为 protected
ACC_STATIC | 0x0008	| 方法为 static
ACC_FINAL | 0x0010	| 方法为 final
ACC_SYNCHRONIZED | 0x0080	| 方法为 synchornized
ACC_NATIVE | 0x0100| 方法为 native
ACC_ABSTRACT | 0x0400| 方法为 abstract

- 结合十六进制码分析，可以看到定义的add 方法如下

![方法表分析截图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E6%96%B9%E6%B3%95%E8%A1%A8%E5%88%86%E6%9E%90%E6%88%AA%E5%9B%BE.png)

- 可以得出结论，access_flags = 0001 也就是访问权限为 public，定义方法名为 add ,接收 int 参数并返回 int 参数

### 属性表

- 之前分析字段表和方法表结构中可以看到都有attribute_info的字段结构，这个就是属性表，它有很多种类型的结构，属性表其中一种结构如下

```
CONSTANT_Attribute_info{
    u2 name_index;
    u2 attribute_length length;
    u1[] info;
}
```
![属性表分析](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/%E5%B1%9E%E6%80%A7%E8%A1%A8%E5%88%86%E6%9E%90.png)

- 如上图所示，属性表计数器 0001 ，属性表索引指向 0009，对应为 Code 属性表，主要包含方法的字节码指令，看到add 方法字节码指令，最终JVM执行 add 方法就是执行这些字节码指令进行操作。

![code 属性表和 add 方法字节码指令](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/class/code%20%E5%B1%9E%E6%80%A7%E8%A1%A8%E5%92%8C%20add%20%E6%96%B9%E6%B3%95%E5%AD%97%E8%8A%82%E7%A0%81%E6%8C%87%E4%BB%A4.png)

- [oracle 文档属性表多种结构](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7)


### 最后以文件名结尾 0013 ,对应就是常量池中文件名

# 参考资料
[oracle 文档](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.6)
