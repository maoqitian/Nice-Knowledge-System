package bridge

/**
 * @Description: 咖啡原味对象 ConcreateImplementor 角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/27 23:33
 */
class Normal :CoffeeAddSomething(){
    override fun addSomething() :String{
        return "原味"
    }
}