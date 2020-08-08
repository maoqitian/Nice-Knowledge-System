package observepattern;

/**
 * @Description: 观察者实现类 消费者角色
 * @Author: maoqitian
 * @CreateDate: 2020/8/8 16:38
 */
public class Customs implements Observer{

    private final String name;

    Customs(String name){
        this.name = name;
    }

    @Override
    public void update(String msg) {
        System.out.println(name+"收到订阅通知："+msg);
    }
}
