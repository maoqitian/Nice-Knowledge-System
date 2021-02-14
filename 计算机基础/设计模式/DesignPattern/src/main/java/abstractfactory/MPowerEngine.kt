package abstractfactory

/**
 * @Description: MPower 高性能引擎 ConcreateProduct：具体产品角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/14 13:53
 */
class MPowerEngine:IEngine{
    override fun engine() {
        println("配备代号 S58 MPower 高性能 直列六缸双涡轮增压引擎 510匹马力")
    }
}