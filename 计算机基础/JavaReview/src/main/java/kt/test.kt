package kt

import kotlinx.coroutines.*

/**
 * @Description: kotlin 协程 测试
 * @Author: maoqitian
 * @CreateDate: 2020/10/29 23:35
 */

fun main() {

    /**
     * 什么是协程？
     * 它就是 kotlin 官方提供的线程 API
     */


      //顶层作用域 不会等待作用域中代码运行完就结束
    /*  GlobalScope.launch {
          println("GlobalScope hello kotlin")
          delay(5000)
      }*/

      //会等待作用域代码执行完成才结束（在测试环境运行，正式环境会产生性能问题）
     /* runBlocking {
          println("runBlocking hello kotlin")
      }*/

      //十万个协程并发
      /*val start = System.currentTimeMillis()

      runBlocking {
          repeat(100000){
              println(".")
          }
      }
     val end = System.currentTimeMillis()
     println(end -start)*/

    //和runBlocking类似，但是只会阻塞当前协程，不会影响其他协程(需要在协程作用域或者挂起函数值调用)
    /*runBlocking {
        coroutineScope {
            for ( i in 1..5){
                println(i)
            }
            println("coroutineScope hello kotlin")

        }
        println("runBlocking hello kotlin")
    }*/

    //非阻塞式挂起
    runBlocking {
        launch(Dispatchers.Default) {
         println(Thread.currentThread())
            val a = async { test1() }.await()
            //val b =async { test2() }.await()
            println("返回结果 c = $a  "+Thread.currentThread())
        }
    }


}

//挂起函数
suspend fun test1():Int{
    return withContext(Dispatchers.IO) {
        println("执行网络请求1")
        println(Thread.currentThread())
        delay(1000)
        1
    }
}
suspend fun test2():Int{
   return withContext(Dispatchers.IO){
        println("执行网络请求2")
        println(Thread.currentThread())
        delay(1000)
        2
    }

}