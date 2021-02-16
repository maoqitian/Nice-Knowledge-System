package chainIterator

/**
 * @Description: 测试责任链模式
 * @Author: maoqitian
 * @CreateDate: 2021/2/16 00:03
 */

fun main() {
    var request = LeaveRequest()

    var leaderPerson = LeaderPerson()
    var managerPerson = ManagerPerson()
    var bossPerson = BossPerson()

    leaderPerson.superrior = managerPerson
    managerPerson.superrior = bossPerson
    println("${request.getName()} 提交请假申请")
    leaderPerson.handleRequest(request)
}