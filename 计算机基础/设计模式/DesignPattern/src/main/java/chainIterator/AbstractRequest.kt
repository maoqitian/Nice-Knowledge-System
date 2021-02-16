package chainIterator

/**
 * @Description: 请求的抽象 请假对象 AbstractRequest 角色
 * @Author: maoqitian
 * @CreateDate: 2021/2/15 12:00
 */
abstract class AbstractRequest {

    //请假天数
    abstract fun getLeave():Int

    //请假人名称
    abstract fun getName():String
}