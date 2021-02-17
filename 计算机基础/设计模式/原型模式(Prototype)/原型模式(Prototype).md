# 原型模式(Prototype)

> 原型模式也是创建型设计模式的一种，原型模式本质就是对象的拷贝。

## 定义

- 用原型实例创建对象的种类，并通过拷贝这些原型创建新的对象

### 角色

- Prototype 角色：原型抽象
- ConcreatePrototype 角色 ： 原型实现
- Client：原型数据使用客户端

## 原型模式 UML

![prototype](https://github.com/maoqitian/MaoMdPhoto/raw/master/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/%E5%8E%9F%E5%9E%8B%E6%A8%A1%E5%BC%8F(Prototype)/prototype.png)

## 深拷贝与浅拷贝

- 浅拷贝：实现 clone 方法中对象都指向原拷贝对象引用，则改变克隆对象变量值也会改变原对象的变量值
- 深拷贝：在使用深拷贝时，对象拷贝也需要使用 clone 方法进行拷贝，这样改变对象数据不会对原对象数据产生影响

## demo

- 以下简单实现一个对象，Cloneable 接口就相当于Prototype 角色，而对象实现则相当于 ConcreatePrototype 角色


```
/**
 * @Description: 数据对象 相当于 ConcreatePrototype ，Cloneable接口相当于原型接口角色 Prototype
 * @Author: maoqitian
 * @CreateDate: 2021/2/17 15:11
 */
class DataModel :Cloneable{

    var name = ""
    var age = 0
    var images = ArrayList<String>()

    //实现 clone 方法 该方法不是 Cloneable 接口方法 而是 Object 的方法
    public override fun clone(): DataModel {
         var dataModel:DataModel = super.clone() as DataModel
         dataModel.age = this.age
         dataModel.name = this.name
         dataModel.images = this.images.clone() as ArrayList<String>
         return dataModel
    }

    override fun toString(): String {
        return "DataModel(name='$name', age=$age, images=$images)"
    }


}
```
- 简单实用 client

```
/**
 * @Description:
 * @Author: maoqitian
 * @CreateDate: 2021/2/17 15:23
 */


fun main() {

    var dataModel = DataModel()
    dataModel.name = "maoqitian"
    dataModel.age = 18
    dataModel.images.add("demo1")

    var dataModel1 = dataModel.clone()

    println(dataModel.toString())
    println(dataModel1.toString())
    dataModel1.name = "shuya"
    dataModel1.age = 16

    //深拷贝 改变对象数据 不会对原对象数据产生影响
    dataModel1.images.add("demo2")

    println(dataModel.toString())

    println(dataModel1.toString())

}
```
- 日志打印

```
DataModel(name='maoqitian', age=18, images=[demo1])
DataModel(name='maoqitian', age=18, images=[demo1])
DataModel(name='maoqitian', age=18, images=[demo1])
DataModel(name='shuya', age=16, images=[demo1, demo2])
```

## 原型模式优缺点

### 优点

- 原型模式是对在内存中的二进制流对象进行拷贝，性能要比 new 一个对象好不少，提高程序性能，避免构造函数创建对象的约束

## 缺点

- 必须实现Cloneable接口，灵活性下降
- 直接拷贝二进制流，是不会执行对象的构造函数，减少约束是优点也是缺点

## demo源码地址

[prototype-demo](https://github.com/maoqitian/Nice-Knowledge-System/tree/master/%E8%AE%A1%E7%AE%97%E6%9C%BA%E5%9F%BA%E7%A1%80/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/DesignPattern/src/main/java/prototype)