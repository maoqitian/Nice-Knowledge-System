package abstractfactory

/**
 * @Description: 汽车工厂抽象类 生产车的工厂肯定会有车的引擎和刹车灯等等其他零件 AbstractFactory：抽象工厂角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/14 13:31
 */
abstract class CarFactory {

    //引擎
    abstract fun createEngine():IEngine
    //刹车
    abstract fun createBrake():IBrake

}