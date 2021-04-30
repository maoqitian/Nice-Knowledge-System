# 命令模式
> 命令模式是设计模式类型中行为设计模式的一种。Android源码事件机制中底层对事件转发逻辑，或者GUI设计中按钮的控制逻辑都能看到命令模式的身影，又或者前端 vue 框架中的 store 执行逻辑都是命令模式的具体体现。

## 定义

- 将一个命令请求封装成为一个对象，从而用户使用不同的命令请求把客户端参数化，执行命令同时可以记录操作步骤，同时也方便做 undo 和 redo 操作。

### 角色

- 客户端角色（client）: 命令执行起始点，有用户灵活决定命令执行
- 接受者角色 ( Receiver)：命令最终逻辑的具体实现，接收请求者的请求执行命令
- ConcreteCommand：命令具体实现角色
- Command ：命令角色
- Invoker：命令请求这角色，负责调用命令对象执行具体的命令请求，持有命令具体实现和命令接受者引用

## 命令模式 UML 图

![cmmand-pattern](https://github.com/maoqitian/MaoMdPhoto/blob/master/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/%E5%91%BD%E4%BB%A4%E6%A8%A1%E5%BC%8F(Command)/commandPattern.png)

## 命令模式简单实现

- 英雄联盟简称 LOL 想必大家都知道，他有基本的操作 QWERDF 等按键，接下来通过命令模式来实现玩家操作游戏的代码简单实现
- 先实现英雄联盟游戏，也就是命令模式中的接受者角色 ( Receiver)，这里是游戏操作的具体实现，也就是游戏本身该有的具体实现，外部操作最终都是调用到游戏的实现
```
/**
 * @Description: 英雄联盟游戏 命令模式中的 接受者角色 ( Receiver)
 * @Author: maoqitian
 * @CreateDate: 2021/4/28 22:41
 */
class LeagueOfLegends {

    //英雄联盟每个英雄都有四个主动技能 QWER 按键 召唤师技能 DF
    /**
     * Q 操作
     */
    fun Q(){
        printf("执行了Q操作")
    }

    /**
     * W 操作
     */
    fun W(){
        printf("执行了W操作")

    }

    /**
     * E 操作
     */
    fun E(){
        printf("执行了E操作")

    }

    /**
     * R 操作
     */
    fun R(){
        printf("执行了R操作")

    }
    /**
     * D 操作
     */
    fun D(){
        printf("执行了D操作")

    }
    /**
     * R 操作
     */
    fun F(){
        printf("执行了F操作")

    }
}
```
- 随后实现命令模式命令者抽象角色 (Command)

```
/**
 * @Description: 命令模式 命令者抽象角色 (Command)
 * @Author: maoqitian
 * @CreateDate: 2021/4/28 22:48
 */
interface Command {
    //执行命令方法抽象
    fun execute()
}
```
- 接着实现命令具体实现 (ConcreteCommand)，其他操作同理

```
/**
 * @Description: 命令模式 命令具体实现 (ConcreteCommand) Q操作
 * @Author: maoqitian
 * @CreateDate: 2021/4/28 22:55
 */
class QCommand(val leagueOfLegends: LeagueOfLegends) :Command{
    override fun execute() {
        leagueOfLegends.Q()
    }
}
```
- 然后实现命令执行器，也就是命令请求者角色（Invoker），持有各种命令的引用，最终调用的还是游戏本身的实现

```
/**
 * @Description: 命令模式 命令请求者角色 （Invoker）
 * @Author: maoqitian
 * @CreateDate: 2021/4/28 23:03
 */
class KeyBoard(var qCommand: QCommand,var wCommand: WCommand, var eCommand: ECommand,
              var rCommand: RCommand,var dCommand: DCommand,var fCommand: FCommand) {

    /**
     * Q 操作
     */
    fun Q(){
        qCommand.execute()
    }

    /**
     * W 操作
     */
    fun W(){
        wCommand.execute()
    }

    /**
     * E 操作
     */
    fun E(){
       eCommand.execute()
    }

    /**
     * R 操作
     */
    fun R(){
        rCommand.execute()
    }
    /**
     * D 操作
     */
    fun D(){
        dCommand.execute()
    }
    /**
     * R 操作
     */
    fun F(){
       fCommand.execute()
    }

}
```
- 最后实现客户也就是玩家角色（Client）,具体该如何操作游戏，释放什么骚操作按键都是玩家来玩，玩家必须有游戏，同时也得有键盘才能操作游戏

```
/**
 * @Description: 英雄联盟玩家 命令模式 客户类 （Client）
 * @Author: maoqitian
 * @CreateDate: 2021/4/28 23:13
 */

fun main() {
    //开启游戏
    val leagueOfLegends = LeagueOfLegends()

    //游戏构造出命令 QWERDF
    val qCommand = QCommand(leagueOfLegends)
    val wCommand = WCommand(leagueOfLegends)
    val eCommand = ECommand(leagueOfLegends)
    val rCommand = RCommand(leagueOfLegends)
    val dCommand = DCommand(leagueOfLegends)
    val fCommand = FCommand(leagueOfLegends)

    //玩家得有键盘才能玩游戏
    val keyBoard = KeyBoard(qCommand, wCommand, eCommand, rCommand, dCommand, fCommand)

    //具体放什么技能则由玩家控制键盘决定
    //滚键盘
    keyBoard.Q()
    keyBoard.W()
    keyBoard.E()
    keyBoard.R()

    //DF 二连
    keyBoard.D()
    keyBoard.F()

}
```

## 命令模式优缺点

### 优点

- 更好的代码结构，更弱的解耦性和灵活控制的扩展性

### 缺点

- 设计模式的通病，需要创建大量的类

### demo源码地址

[command-demo](https://github.com/maoqitian/Nice-Knowledge-System/tree/master/%E8%AE%A1%E7%AE%97%E6%9C%BA%E5%9F%BA%E7%A1%80/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/DesignPattern/src/main/java/command)