package factory

/**
 * @Description: 抽象产品(product) audi 汽车
 * @Author: maoqitian
 * @CreateDate: 2021/2/13 15:27
 */
abstract class AudiCarProduct {

    //引擎
    abstract fun engine()

    //四轮驱动

    abstract fun is4WD():Boolean

    //售价

    abstract fun price()

}