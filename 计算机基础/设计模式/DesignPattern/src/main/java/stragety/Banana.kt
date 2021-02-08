package stragety

/**
 * @Description: 香蕉
 * @Author: maoqitian
 * @CreateDate: 2021/2/3 22:40
 */
class Banana :Fruits{

    override fun calculatePrice(size: Int) {
        //香蕉价格三块钱一斤
        System.out.println("香蕉三块钱一斤")
        var price = 3*size
        System.out.println("你买了$size 斤香蕉，价格为：$price 元")
    }

}