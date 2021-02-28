package bridge

/**
 * @Description: 测试桥接模式
 * @Author: maoqitian
 * @CreateDate: 2021/2/27 23:39
 */


fun main() {

    var sugar = Sugar()
    var normal = Normal()
    var ice = Ice()

    var largeCoffeesugar = LargeCoffee(sugar)
    largeCoffeesugar.makeCoffer()

    var largeCoffeeice = LargeCoffee(ice)
    largeCoffeeice.makeCoffer()

    var largeCoffeenor= LargeCoffee(normal)
    largeCoffeenor.makeCoffer()


    var middlerCoffeesugar = MinddleCoffee(sugar)
    middlerCoffeesugar.makeCoffer()

    var middlerCoffeeice = MinddleCoffee(ice)
    middlerCoffeeice.makeCoffer()

    var middlerCoffeenor= MinddleCoffee(normal)
    middlerCoffeenor.makeCoffer()

    var smallCoffeesugar = SmallCoffee(sugar)
    smallCoffeesugar.makeCoffer()

    var smallCoffeeice = SmallCoffee(ice)
    smallCoffeeice.makeCoffer()

    var smallCoffeenor= SmallCoffee(normal)
    smallCoffeenor.makeCoffer()

}