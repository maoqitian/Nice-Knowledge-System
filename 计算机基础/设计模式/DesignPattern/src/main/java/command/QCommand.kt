package command

/**
 * @Description: 命令模式 命令具体实现 (ConcreteCommand)
 * @Author: maoqitian
 * @CreateDate: 2021/4/28 22:55
 */
class QCommand(val leagueOfLegends: LeagueOfLegends) :Command{
    override fun execute() {
        leagueOfLegends.Q()
    }
}