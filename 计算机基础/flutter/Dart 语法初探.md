
# Dart 语法

![logo-dart](https://raw.githubusercontent.com/maoqitian/MaoMdPhoto/master/flutter/dart/logo-dart.png)

- 我们知道Flutter框架是使用Dart 语言来编写的，Dart 是一个面向对象编程语言， 每个对象都是一个类的实例，所有的类都继承于 Object.如果熟悉Java，语言是很容易上手的。首先来熟悉一下Dart语法

## Dart 变量
- var 声明变量，和 kt 、js语法很像，需要注意的是如下示例 name 只要复制字符串，则他就是String类型，number就是int 类型，不能再更改它的类型，而没有初始化的变量自动获取一个默认值为 null。

```
var name = 'maoqitian';
var number = 1;
```

## Dart 常量

- final 和 const声明都是表示常量，一个 final 变量只能赋值一次，可以省略变量类型，如下声明一个存放WordPair值的List 数组

```
final List _suggestions = new List<WordPair>();

final _suggestions = <WordPair>[];

```
- const 关键字不仅仅只用来定义常量， 也可以用来创建不变的值

```
//如下定义一个字体大小的值一直都是 18 ，不会改变
final _biggerFont = const TextStyle(fontSize: 18.0)
```
### final 和 const区别
-  const 的值在编译期确定，final 的值要到运行时才确定

## Dart 函数方法
- Dart 是一个真正的面向对象语言，方法也是对象他的类型是 Function。 这意味着，方法可以赋值给变量，也可以当做其他方法的参数。

```
//定义一个返回 bool(布尔)类型的方法 
bool isNoble(int atomicNumber) {
  return _nobleGases[atomicNumber] != null;
}
//转换如下可以忽略类型定义
isNoble(atomicNumber) {
  return _nobleGases[atomicNumber] != null;
}

//只有一个表达式的方法，你可以选择 使用缩写语法来定义
// => expr 语法是 { return expr; } 形式的缩写
bool isNoble(int atomicNumber) => _nobleGases[atomicNumber] != null;

```
### Dart 方法参数
- 方法可以定义两种类型的参数：必需的和可选的。 必需的参数在参数列表前面， 后面是可选参数，必选参数没啥好说的，我们来了解可选参数。可选参数可以是自己命名参数或者基于可选位置的参数，但是这两种参数不能同时当做可选参数来一起用。

#### 可选命名参数

- 调用可选命名参数方法的时候可以使用 paramName: value （key:value形式，只不是过key 是参数名称）来指定参数值

```
//调用有可选命名参数方法 playGames
playGames(bold: true, hidden: false);

//playGames 方法
playGames({bool bold, bool hidden}) {
  // ...
}
```
#### 可选位置参数

- 方法参数列表中用[]修饰的参数就是可选位置参数
```
// 定义可选位置参数方法
String playGames (String from, String msg, [String sports]) {
  var result = '$from suggest $msg';
  if (sports != null) {
    result = '$result playing $sports together';
  }
  return result;
}
 
// 不用可选参数 
playGames('Bob', 'Howdy'); // 返回值 Bob suggest Howdy

//使用可选参数
playGames('I', 'Xiao Ming', 'basketball'); //返回值 I suggest Xiao Ming playing basketball together.
```
### Dart 方法参数默认值

- 在定义方法的时候，可以使用 = 来定义可选参数的默认值。 默认值只能是编译时常量。 如果没有提供默认值，则默认值为 null

```
// 定义可选位置参数方法
String playGames (String from , String msg, [String sports = 'football']) {
  var result = '$from suggest $msg';
  if (sports != null) {
    result = '$result playing $sports together';
  }
  return result;
}

playGames('I', 'Xiao Ming'); //返回值 I suggest Xiao Ming playing football together.

```
### 入口函数（The main() function）

- 每个应用都需要有个顶级的 main() 入口方法才能执行

```
// Android studio 创建Demo 项目  main.dart 文件开头 
void main() => runApp(MyApp());

//可以转换为
void main(){
    runApp(MyApp());
}
```

### 异步操作
- async 方法和 await 异步操作，直接看看一个网络请求例子就能够了解

```
static Future<ArticleListData> getArticleData(int pageNum) async{
    String path = '/article/list/$pageNum/json';
    Response response = await HttpUtils.get(Api.BASE_URL+path);
    ArticleBaseData articleBaseData = ArticleBaseData.fromJson(response.data);
    return articleBaseData.data;
  }
```


> 先了解这么多，更多Dart 相关内容可以查看[Dart语言官网](http://dart.goodev.org/)


## 参考

- [Dart 官方网站](https://dart.dev/)
- [Dart 语言中文网](http://dart.goodev.org/)



