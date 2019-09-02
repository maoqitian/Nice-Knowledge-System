![Kotlin_logo](https://github.com/maoqitian/MaoMdPhoto/raw/master/Kotlin%20picture/Kotlin_logo.png)
>Kotlin是一门静态类型编程语言，支持JVM平台，Android平台，浏览器JS运行环境，本地机器码等。支持与Java，Android 100% 完全互操作。

# 编译Kotlin代码

- 和Java一样，Kotlin是一个编译语言。这意味着在你运行Kotlin代码之前，你必须编译它。
- Kt.class在java命令执行前，需要从kotlin-runner.jar这个逻辑里走一遍。同时，我们也能知道Kt.class跟Java.class文件有着这个kotlin-runner.jar的逻辑映射上的区别。也就是说，Kotlin的Bytecode跟纯的JVM bytecode存在一个kotlin-runner.jar的映射关系。
![Kotlin编译和Java编译对比图](https://raw.githubusercontent.com/maoqitian/MaoMdPhoto/master/Kotlin%20picture/Kotlin%E7%BC%96%E8%AF%91%E5%92%8CJava%E7%BC%96%E8%AF%91%E5%AF%B9%E6%AF%94%E5%9B%BE.png)

# Kotlin Hello World
- 国际惯例，我们先来一个Kotlin 版的Hello World!程序
    
- 新建项目，这里使用的IDE是 IDEA,选择gradle构建项目
      
![新建Kotlin项目](https://raw.githubusercontent.com/maoqitian/MaoMdPhoto/master/Kotlin%20picture/%E6%96%B0%E5%BB%BAKotlin%E9%A1%B9%E7%9B%AE.png)
   
```
    /**
     * @Author: maoqitian
     * @Date: 2018/9/19 0019 11:52
     * @Description: Kotlin HelloWorld
     */
     fun main(array: Array<String>){
     println("Hello Kotlin")
     }
```


# Kotlin语法基础
## 函数
- fun关键词被用来声明一个函数，相当于Java的方法

## 声明变量和值
- 在Kotlin中， 一切都是对象，所有变量也都是对象。
- Kotlin 变量分为 var（可以被重新赋值变量） 和 val（只能赋值一次变量，相当于Java的final）

## 变量类型
- 使用这两个关键字定义变量的赋值Kotlin会自动判断变量的类型
```
     package com.easy.kotlin.chapter1

    /**
     * @Author: maoqitian
     * @Date: 2018/9/19 0019 17:42
     * @Description: 测试变量值
     */
     class VariableVSValue{
     fun declareVar(){
        var x = 5 //自动判断类型是 int 初始值是5
        println(x)
        x = 3  // 重新赋值之后是 3
        println(x)
        println(x::class)
        println(x::class.java)
     }


     fun delareVal(){
        val s="mao" //val 修饰只能赋值一次 自动判断为String
        println(s)
        //s="mmm"
        println(s::class)
        println(s::class.java)
     }

     }

      fun main(array: Array<String>){

      var va=VariableVSValue()
      va.declareVar()
      va.delareVal()
     }

```
## 流程控制语句
### if表达式
- if-else语句是控制程序流程的最基本的形式，其中else是可选的。在 Kotlin 中，if 是一个表达式，即它会返回一个值(跟Scala一样)。
      
 ```
      fun Max(a:Int,b:Int):Int{ //方法后面的参数为方法返回值
      var max= if (a>b) a else b 
       return max
       }

       fun main(array: Array<String>){
       println(Max(4,5))
       }

```
### When 表达式
- 它可以被理解为Java中switch的替代 
- when构造比Java中的switch更为强大。跟要求你使用常量（枚举常量，字符 串或者数字字面量）作为分支条件的switch不同，when允许任意的对象
```
     /**
      * @Author: maoqitian
        @Date: 2018/9/20 0020 16:01
      * @Description: When 语句
      */


      enum class Color(val r:Int,val g:Int,val b:Int){
        RED(255,	0,	0),
        ORANGE(255,265,0), //当每个常量被创建时指定属性值	YELLOW(255,	255,0),	GREEN(0,255,0),	BLUE(0,0,255),
        INDIGO(75,0,130),
        VIOLET(238,130,238);//	分号（;）在这里是必须的
        //在枚举类中定义了一个方法
        fun	rgb()=	(r*	256	+g)	*256+b
        }

        class WhenDemo{
        
        fun getColorIfString(color: Color):String{
        if (color == Color.RED) {
            return "红色"
        }else if(color == Color.ORANGE){
            return "橘色"
        }else if(color ==  Color.INDIGO ){
            return "靛蓝色"
        }else if(color == Color.VIOLET){
            return "紫色"
        }else{
            throw	Exception("Dirty color")
        }
        }
        //使用 when 改写上面的 if 表达式
         
         fun getColorString(color: Color):String{
          when(color){
            Color.RED -> return "红色"
            Color.ORANGE -> return  "橘色"
            Color.INDIGO -> return "靛蓝色"
            Color.VIOLET ->return "紫色"
            else	->	throw	Exception("Dirty color")
          }
        }
      }

       fun main(array: Array<String>){
       val whenDemo = WhenDemo()
       println(whenDemo.getColorIfString(Color.INDIGO))
       println(whenDemo.getColorString(Color.INDIGO))
      }

```
### while 循环
- Kotlin的while循环和do-while循环与Java想不没有什么区别，这里就不细说了
```
      while	(condition)	{//	当while条件为真时，执行主体代码				
      /*...*/ 
          
      }
     
      do {/*...*/ 
          
      }	while	(condition)	//第一次无条件的执行主体代码。在这之后，当条件为真时才执行。

```
### 使用 in 检查
- 使用in操作符来检查一个值是否在某个范围内，或者相反的，!in（操作符）来检查一个值是否不再某个范围内

```
     fun isLetter(c: Char) = c in 'a'..'z'|| c in 'A'..'Z'
     fun	isNotDigit(c:Char)= c !in '0'..'9'

     fun recognize(c:Char):String{
     when(c){
        in 'a'..'z'-> return "这是字母"
        in  '0'..'9'->return "这是数字"
        else -> return "不认识"
      }
    }

    fun main(array: Array<String>){
    println(isLetter('a'))
    println(isNotDigit('0'))
    println(recognize('a'))
    }

```
### Kotlin中的异常处理
- Kotlin中的异常处理和Java是非常类似的，只是在方法中你不需要像Java一样要显式的声明这个函数会抛出哪些异常
- 跟在Java中一样，你可以使用带有catch的try语法和finally从句来处理异常
- try也可以作为一个表达式，就是if一样，但是你必须把声明主体放在闭合的大括号里。和其他声明一样，如果主体包含多个表达式，try表达式的值作为一个整体的值是最后一个表达式的值。
```
      fun percentage(number:Int){
       if (number in 1..100)
        number
       else throw IllegalArgumentException("百分比数值必须在1-100之间")
       }

      fun readNumber(reader:BufferedReader):Int?{ //不需像Java一样要显式的声明这个函数会抛出哪些异常
      try {
        val readLine = reader.readLine()
        return Integer.parseInt(readLine)
        }catch (e:NumberFormatException){
        println(e.toString())
        return null
         }finally { //finally 和Java 中是一样的
        reader.close()
         }
        }

        fun readNumber2(reader:BufferedReader){
        val t = try {
        val readLine = reader.readLine()
         Integer.parseInt(readLine) //当没有异常发生时使用这个值
         } catch (e: NumberFormatException) {
        println(e.toString())
        null  //	当发生异常时使用null值	
        }
        println(t)
        }

        fun main(array: Array<String>){
        percentage(10)
        println(readNumber(BufferedReader(StringReader("mao"))))
        readNumber2(BufferedReader(StringReader("mao")))
        }
        
      //运行结果
      
      /**
      java.lang.NumberFormatException: For input string: "mao"
      null
      java.lang.NumberFormatException: For input string: "mao"
      null
      */
```
# 总结
   
- fun	关键词用来声明一个函数。Kotlin 变量分为 var（可以被重新赋值变量） 和 val（只能赋值一次变量，相当于Java的final）。
- if表达式是一个有返回值的表达式。
- when和Java中的switch差不多，但是它比switch更强大。
- Kotlin中的异常处理跟Java很相似。不同的地方是Kotlin并不要求你什么方法可能会抛出的异常。
## [Demo地址](https://github.com/maoqitian/kotlin-learn-demo)
## 参考文章、链接
  
  - 《Kotlin实战》
  - 《Kotlin极简教程》