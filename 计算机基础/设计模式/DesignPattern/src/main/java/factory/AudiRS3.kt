package factory

/**
 * @Description: 具体产品 product 奥迪 RS3
 * @Author: maoqitian
 * @CreateDate: 2021/2/13 15:28
 */
class AudiRS3 :AudiCarProduct(){
    override fun engine() {
        println("直列五缸 涡轮增压 400 马力")
    }

    override fun is4WD(): Boolean {
        return true
    }

    override fun price() {
        println("奥迪RS3售价 51.38 万")
    }


}