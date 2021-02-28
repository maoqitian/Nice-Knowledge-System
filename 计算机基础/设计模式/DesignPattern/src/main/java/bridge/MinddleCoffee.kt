package bridge

/**
 * @Description: RefinedAbstraction 角色 对抽象的重新定义
 * 中杯咖啡
 * @Author: maoqitian
 * @CreateDate: 2021/2/27 23:27
 */
class MinddleCoffee(coffeeAddSomething: CoffeeAddSomething) :Coffee(coffeeAddSomething) {

    override fun makeCoffer() {
        println("制作中杯的咖啡,${coffeeAddSomething.addSomething()}")
    }
}