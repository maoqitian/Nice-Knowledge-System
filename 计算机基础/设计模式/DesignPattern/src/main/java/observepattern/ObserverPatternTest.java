package observepattern;

/**
 * @Description: 观察者模式测试类
 * @Author: maoqitian
 * @CreateDate: 2020/8/8 16:42
 */
public class ObserverPatternTest {

    public static void main(String[] args) {

        TaoBao taoBao = new TaoBao();

        Customs customs1 = new Customs("詹姆斯");
        Customs customs2 = new Customs("安东尼");
        Customs customs3 = new Customs("韦德");
        Customs customs4 = new Customs("保罗");

        taoBao.addListener(customs1);
        taoBao.addListener(customs2);
        taoBao.addListener(customs3);
        taoBao.addListener(customs4);

        taoBao.notifyListener("aj 34 篮球鞋已经可以抢购了");
    }
}
