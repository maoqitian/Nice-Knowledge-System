package prototype

/**
 * @Description:
 * @Author: maoqitian
 * @CreateDate: 2021/2/17 15:23
 */


fun main() {

    var dataModel = DataModel()
    dataModel.name = "maoqitian"
    dataModel.age = 18
    dataModel.images.add("demo1")

    var dataModel1 = dataModel.clone()

    println(dataModel.toString())
    println(dataModel1.toString())
    dataModel1.name = "shuya"
    dataModel1.age = 16

    //深拷贝 改变对象数据 不会对原对象数据产生影响
    dataModel1.images.add("demo2")

    println(dataModel.toString())

    println(dataModel1.toString())

}