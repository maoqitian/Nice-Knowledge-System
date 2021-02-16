package chainIterator

/**
 * @Description: 部门组长 ConcreateHandler 角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/15 23:49
 */
class LeaderPerson :Person(){

    //处理请假
    override fun handleRequest(request: AbstractRequest) {
        if(request.getLeave() <= getMaxLeave()){
            //请假天数在职权范围内
            println("部门组长 批准请假，请假人：${request.getName()}")
        }else{
            println("部门组长 请假超过职位权限，交由上级领导审批")
            //移交上级领导审批
            superrior?.handleRequest(request)
        }
    }

    //能批准假期 三天
    override fun getMaxLeave(): Int {
        return 3
    }
}