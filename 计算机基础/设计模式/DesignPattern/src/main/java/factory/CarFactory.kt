package factory

/**
 * @Description: 汽车工厂接口(角色 抽象工厂 定义生成产品的抽象方法)
 * @Author: maoqitian
 * @CreateDate: 2021/2/13 14:52
 */
abstract class CarFactory {

    /**
     * 生产 adui汽车 抽象方法
     */
    abstract fun <T : AudiCarProduct> createAudiCar(clz : Class<T>) : T

}