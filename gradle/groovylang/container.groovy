// groovy 容器类

def aList = [5,'string',true]

aList.each {
    println "Item: $it"
}
println "===========添加元素==============="
//添加元素 使用 += 或者 << (+= 符合性能低于 <<)
aList << 1
aList += 6
aList.each {
    println "加入元素后item : $it"
}
println "===========查找元素==============="
def aList1 = [5,6,7]
//查找元素 find 方法
//
println(aList1.find{ it > 1 })
println(aList1.findAll{ it > 1 })

println "===========删除元素==============="
//删除元素

println(6 in aList)
aList -= 6

aList.each {
    println "删除元素后item : $it"
}

println "===========map==============="
//其中的 key1 和 key2 默认被处理成字符串"key1"和"key2"
def aNewMap = [key1:"hello",key2:false]
//map 取值
println aNewMap.key1
println aNewMap['key2']
//为 map 添加新元素
aNewMap.anotherkey = "i am map"

aNewMap.each{
    println "Item: $it"
}

println "===========Range==============="

//标识 list 相当于数学闭包 [1,5]
def mRange = 1..5

mRange.each {
    println "Item: $it"
}

//标识 list 相当于数学闭包 [1,5)
def mRange1 = 1..<5

mRange1.each {
    println "other Item: $it"
}

//获取开头结尾元素
println mRange.from
println mRange.to