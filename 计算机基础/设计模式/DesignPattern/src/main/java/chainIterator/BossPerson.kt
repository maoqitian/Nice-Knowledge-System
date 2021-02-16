package chainIterator

/**
 * @Description: 公司领导 ConcreateHandler 角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/15 23:49
 */
class BossPerson :Person(){

    //处理请假
    override fun handleRequest(request: AbstractRequest) {
        if(request.getLeave() <= getMaxLeave()){
            //请假天数在职权范围内
            println("公司领导 批准请假，请假人：${request.getName()}")
        }else{
            //移交上级领导审批
            println("超过十五天 公司领导不批假")
        }
    }

    //能批准假期 十五天
    override fun getMaxLeave(): Int {
        return 15
    }
}