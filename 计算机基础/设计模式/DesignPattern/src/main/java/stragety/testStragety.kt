package stragety

/**
 * @Description: 测试策略模式
 * @Author: maoqitian
 * @CreateDate: 2021/2/3 22:50
 */

fun main() {

    var context = Context(Apple())
    context.byFruits(2)

    var context1 = Context(Banana())
    context1.byFruits(5)

    var context2 = Context(Grape())
    context2.byFruits(3)

}