package bridge

/**
 * @Description: RefinedAbstraction 角色 对抽象的重新定义
 * 小杯咖啡
 * @Author: maoqitian
 * @CreateDate: 2021/2/27 23:27
 */
class SmallCoffee(coffeeAddSomething: CoffeeAddSomething) :Coffee(coffeeAddSomething) {

    override fun makeCoffer() {
        println("制作小杯的咖啡,${coffeeAddSomething.addSomething()}")
    }
}