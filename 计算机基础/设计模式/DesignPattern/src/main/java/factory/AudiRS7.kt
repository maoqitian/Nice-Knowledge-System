package factory

/**
 * @Description: 具体产品 product 奥迪 RS7
 * @Author: maoqitian
 * @CreateDate: 2021/2/13 15:28
 */
class AudiRS7 :AudiCarProduct(){
    override fun engine() {
        println("直列八缸 涡轮增压 605 马力")
    }

    override fun is4WD(): Boolean {
        return true
    }

    override fun price() {
        println("奥迪RS7售价 172.2 万")
    }


}