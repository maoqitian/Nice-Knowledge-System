package command

/**
 * @Description: 英雄联盟游戏 命令模式中的 接受者角色 ( Receiver)
 * @Author: maoqitian
 * @CreateDate: 2021/4/28 22:41
 */
class LeagueOfLegends {

    //英雄联盟每个英雄都有四个主动技能 QWER 按键 召唤师技能 DF
    /**
     * Q 操作
     */
    fun Q(){
        printf("执行了Q操作")
    }

    /**
     * W 操作
     */
    fun W(){
        printf("执行了W操作")

    }

    /**
     * E 操作
     */
    fun E(){
        printf("执行了E操作")

    }

    /**
     * R 操作
     */
    fun R(){
        printf("执行了R操作")

    }
    /**
     * D 操作
     */
    fun D(){
        printf("执行了D操作")

    }
    /**
     * R 操作
     */
    fun F(){
        printf("执行了F操作")

    }
}