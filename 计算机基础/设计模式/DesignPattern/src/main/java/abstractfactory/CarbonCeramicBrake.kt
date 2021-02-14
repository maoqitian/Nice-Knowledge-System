package abstractfactory

/**
 * @Description: 碳陶刹车 ConcreateProduct：具体产品角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/14 13:50
 */
class CarbonCeramicBrake:IBrake{
    override fun brake() {
        println("配备 八活塞刹车卡钳 碳陶刹车")
    }

}