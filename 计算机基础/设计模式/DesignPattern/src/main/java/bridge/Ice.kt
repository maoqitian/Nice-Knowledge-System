package bridge

/**
 * @Description: 咖啡加冰对象 ConcreateImplementor 角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/27 23:33
 */
class Ice :CoffeeAddSomething(){
    override fun addSomething() :String{
        return "加冰"
    }
}