package command

/**
 * @Description: 英雄联盟玩家 命令模式 客户类 （Client）
 * @Author: maoqitian
 * @CreateDate: 2021/4/28 23:13
 */

fun main() {
    //开启游戏
    val leagueOfLegends = LeagueOfLegends()

    //游戏构造出命令 QWERDF
    val qCommand = QCommand(leagueOfLegends)
    val wCommand = WCommand(leagueOfLegends)
    val eCommand = ECommand(leagueOfLegends)
    val rCommand = RCommand(leagueOfLegends)
    val dCommand = DCommand(leagueOfLegends)
    val fCommand = FCommand(leagueOfLegends)

    //玩家得有键盘才能玩游戏
    val keyBoard = KeyBoard(qCommand, wCommand, eCommand, rCommand, dCommand, fCommand)

    //具体放什么技能则由玩家控制键盘决定
    //滚键盘
    keyBoard.Q()
    keyBoard.W()
    keyBoard.E()
    keyBoard.R()

    //DF 二连
    keyBoard.D()
    keyBoard.F()

}