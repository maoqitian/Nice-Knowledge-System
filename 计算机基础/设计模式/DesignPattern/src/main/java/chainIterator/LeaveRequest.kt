package chainIterator

/**
 * @Description: 请假请求 ConcreateRequest 对象
 * @Author: maoqitian
 * @CreateDate: 2021/2/15 23:54
 */
class LeaveRequest :AbstractRequest(){
    override fun getLeave(): Int {
        return 15 //请假十五天
    }

    override fun getName(): String {
        return "maoqitian"
    }
}