package abstractfactory

/**
 * @Description: 普通刹车 ConcreateProduct：具体产品角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/14 13:50
 */
class NormalBrake:IBrake{
    override fun brake() {
        println("配备 四活塞刹车卡钳 普通刹车")
    }
}