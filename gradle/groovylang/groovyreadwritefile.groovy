

def testFile = new File("TestFile")

//读文件每一行 eachLine

testFile.eachLine{
    String oneLine ->
        println "TestFile oneLine：$oneLine"
}


//获取文件 byte 数组

//def bytes = testFile.getBytes()
//
//
////获取文件输入流
//
//def is = testFile.newInputStream()
//
////和Java 一样不用需要关闭
//is.close
//
////闭包 Groovy 会自动替你 close
//targetFile.withInputStream{ ips ->
//    //逻辑代码
//}


//================写文件==================

//文件复制

def copyFile = new File("CopyFile")

copyFile.withOutputStream{
    os->
        testFile.withInputStream{
            ips ->
            //逻辑代码
                os << ips
        }
}

copyFile.eachLine{
    String oneLine ->
        println "copyFile oneLine：$oneLine"
}