def testClosure = {
        //箭头前面是参数定义，箭头后面是代码
    String param1, int param2 ->
        //逻辑代码，闭包最后一句是返回值
        println "hello groovy，$param1,$param2"
        //也可以使用 return，和 groovy 中普通函数一样
}

testClosure.call("参数1",20)
testClosure("参数2",40)


def greeting = {
//隐含参数
    "Hello, $it!"
}
println greeting('groovy') == 'Hello, groovy!'
//等同于：
def greeting1 = {
        //也可写出隐含参数
    it -> "Hello, $it!"
}
println greeting1('groovy') == 'Hello, groovy!'