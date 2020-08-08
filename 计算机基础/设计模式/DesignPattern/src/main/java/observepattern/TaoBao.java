package observepattern;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 被观察者者实现 例如这里比作淘宝客户端 可以订阅准点开抢的商品
 * @Author: maoqitian
 * @CreateDate: 2020/8/8 16:20
 */
public class TaoBao implements Observable{

    List<Observer> observerList = new ArrayList<>();

    @Override
    public void addListener(Observer observer) {
        if(!observerList.contains(observer)){
            observerList.add(observer);
        }else {
            System.out.println("你已经订阅了该商品");
        }
    }

    @Override
    public void removeListener(Observer observer) {
        if(observerList.contains(observer)){
            observerList.remove(observer);
        }else {
            System.out.println("你还没订阅了该商品，无法删除");
        }
    }
    @Override
    public void notifyListener(String msg) {

        for (Observer item:observerList) {
            item.update(msg);
        }

    }
}
