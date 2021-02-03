package stragety

/**
 * @Description: 策略模式上下文角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/3 22:47
 */
class Context(var fruits: Fruits){

    //买水果方法
    fun byFruits(size:Int){
        fruits.calculatePrice(size)
    }
}