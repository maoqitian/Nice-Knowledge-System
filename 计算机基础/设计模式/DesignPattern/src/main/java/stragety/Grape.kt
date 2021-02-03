package stragety

/**
 * @Description: 葡萄
 * @Author: maoqitian
 * @CreateDate: 2021/2/3 22:40
 */
class Grape :Fruits{

    override fun calculatePrice(size: Int) {
        //葡萄价格八块钱一斤
        System.out.println("葡萄八块钱")
        var price = 3*size
        System.out.println("你买了$size 斤香蕉，价格为：$price 元")
    }

}