package abstractfactory

/**
 * @Description: 测试抽象工厂
 * @Author: maoqitian
 * @CreateDate: 2021/2/14 14:02
 */

fun main() {

    println("生产一辆宝马M3")
    var BMWM3 = BMWM3Factory()
    BMWM3.createEngine().engine()
    BMWM3.createBrake().brake()
    println("=====================")
    println("生产一辆宝马325Li")
    var bmW325LiFactory = BMW325LiFactory()
    bmW325LiFactory.createEngine().engine()
    bmW325LiFactory.createBrake().brake()

}