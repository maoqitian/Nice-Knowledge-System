package chainIterator

/**
 * @Description: 抽象处理者 领导 组长 老板 AbstractHandler角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/15 11:58
 */
abstract class Person {

    //上级对象
    var superrior: Person? = null
    //处理方法的抽象
    abstract fun handleRequest(request: AbstractRequest)


    //最大批准请假天数
    abstract fun getMaxLeave():Int
}