package factory

/**
 * @Description: 测试工厂模式
 * @Author: maoqitian
 * @CreateDate: 2021/2/13 15:59
 */


fun main() {
    var concreateCarFactory = ConcreateCarFactory()

    var audiRS3 = concreateCarFactory.createAudiCar(AudiRS3::class.java)
    audiRS3.engine()
    audiRS3.price()

    var audiRS4 = concreateCarFactory.createAudiCar(AudiRS4::class.java)
    audiRS4.engine()
    audiRS4.price()

    var audiRS7 = concreateCarFactory.createAudiCar(AudiRS7::class.java)
    audiRS7.engine()
    audiRS7.price()

}