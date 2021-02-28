package bridge

/**
 * @Description: 咖啡抽象 abstraction 角色
 * 提供制作咖啡方法 具体咖啡要加什么 由外部抽象 RefinedAbstraction 角色实现决定
 * @Author: maoqitian
 * @CreateDate: 2021/2/27 23:19
 */
abstract class Coffee(protected var coffeeAddSomething: CoffeeAddSomething) {

    abstract fun makeCoffer()

}