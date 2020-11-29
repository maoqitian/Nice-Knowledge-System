package groovyclass

class GroovyClass{

    String p1;
    int p2;

    GroovyClass(p1,p2){
        this.p1 = p1
        this.p2 = p2
    }

    //和 Java 类似 如果不声明 public/private
    //等访问权限的话，Groovy 中类及其变量默认都是 public

    def printParams(){
        println "参数：$p1,$p2"
    }
}