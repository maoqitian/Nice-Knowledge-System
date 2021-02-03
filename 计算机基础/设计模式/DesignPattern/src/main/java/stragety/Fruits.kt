package stragety

/**
 * @Description: 水果基类 每种水果的单价 有所不同，所以计算公式也不同
 * @Author: maoqitian
 * @CreateDate: 2021/2/3 22:34
 */
public interface Fruits {

    //计算水果的价格
    fun calculatePrice(size:Int)

}