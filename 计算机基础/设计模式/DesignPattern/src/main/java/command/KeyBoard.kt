package command

/**
 * @Description: 命令模式 命令请求者角色 （Invoker）
 * @Author: maoqitian
 * @CreateDate: 2021/4/28 23:03
 */
class KeyBoard(var qCommand: QCommand,var wCommand: WCommand, var eCommand: ECommand,
              var rCommand: RCommand,var dCommand: DCommand,var fCommand: FCommand) {

    /**
     * Q 操作
     */
    fun Q(){
        qCommand.execute()
    }

    /**
     * W 操作
     */
    fun W(){
        wCommand.execute()
    }

    /**
     * E 操作
     */
    fun E(){
       eCommand.execute()
    }

    /**
     * R 操作
     */
    fun R(){
        rCommand.execute()
    }
    /**
     * D 操作
     */
    fun D(){
        dCommand.execute()
    }
    /**
     * R 操作
     */
    fun F(){
       fCommand.execute()
    }

}