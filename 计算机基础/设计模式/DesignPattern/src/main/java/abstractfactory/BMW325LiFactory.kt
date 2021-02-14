package abstractfactory

/**
 * @Description: 宝马 325Li 车型工厂 ConcreateFactory：具体工厂角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/14 13:44
 */
class BMW325LiFactory :CarFactory(){
    override fun createEngine(): IEngine {
        return NormalEngine()
    }

    override fun createBrake(): IBrake {
        return NormalBrake()
    }
}