package factory

/**
 * @Description: 具体汽车工厂 实现抽象工厂具体逻辑
 * @Author: maoqitian
 * @CreateDate: 2021/2/13 15:25
 */
class ConcreateCarFactory :CarFactory(){


    override fun <T : AudiCarProduct> createAudiCar(clz: Class<T>): T {

        var  audiCarProduct = Class.forName(clz.name).newInstance() as AudiCarProduct

        return audiCarProduct as T
    }

}