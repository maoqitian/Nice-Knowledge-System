package factory

/**
 * @Description: 具体产品 product 奥迪 RS4
 * @Author: maoqitian
 * @CreateDate: 2021/2/13 15:28
 */
class AudiRS4 :AudiCarProduct(){
    override fun engine() {
        println("直列六缸 双涡轮增压 450 马力")
    }

    override fun is4WD(): Boolean {
        return true
    }

    override fun price() {
        println("奥迪RS4售价 81.28 万")
    }


}