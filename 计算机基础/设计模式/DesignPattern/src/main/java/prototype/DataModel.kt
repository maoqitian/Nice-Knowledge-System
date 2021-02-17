package prototype

/**
 * @Description: 数据对象 相当于 ConcreatePrototype ，Cloneable接口相当于原型接口角色 Prototype
 * @Author: maoqitian
 * @CreateDate: 2021/2/17 15:11
 */
class DataModel :Cloneable{

    var name = ""
    var age = 0
    var images = ArrayList<String>()

    //实现 clone 方法 该方法不是 Cloneable 接口方法 而是 Object 的方法
    public override fun clone(): DataModel {
         var dataModel:DataModel = super.clone() as DataModel
         dataModel.age = this.age
         dataModel.name = this.name
         dataModel.images = this.images.clone() as ArrayList<String>
         return dataModel
    }

    override fun toString(): String {
        return "DataModel(name='$name', age=$age, images=$images)"
    }


}