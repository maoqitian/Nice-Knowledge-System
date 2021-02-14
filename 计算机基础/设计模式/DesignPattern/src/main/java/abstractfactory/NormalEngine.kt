package abstractfactory

/**
 * @Description: 宝马普通 引擎 ConcreateProduct：具体产品角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/14 13:53
 */
class NormalEngine:IEngine{
    override fun engine() {
        println("配备代号 B48 直列四缸涡轮增压引擎 184匹马力")
    }


}