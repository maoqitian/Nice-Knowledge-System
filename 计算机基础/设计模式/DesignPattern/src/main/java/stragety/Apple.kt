package stragety

/**
 * @Description: 苹果
 * @Author: maoqitian
 * @CreateDate: 2021/2/3 22:40
 */
class Apple :Fruits{

    override fun calculatePrice(size: Int) {
        //苹果价格五块钱一斤
        System.out.println("苹果五块钱一斤")
        var price = 5*size
        System.out.println("你买了$size 斤苹果，价格为：$price 元")
    }

}