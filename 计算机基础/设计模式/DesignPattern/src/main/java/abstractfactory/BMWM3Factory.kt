package abstractfactory

/**
 * @Description: 宝马 M3 车型工厂 ConcreateFactory：具体工厂角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/14 13:44
 */
class BMWM3Factory :CarFactory(){
    override fun createEngine(): IEngine {
        return MPowerEngine()
    }

    override fun createBrake(): IBrake {
        return CarbonCeramicBrake()
    }
}