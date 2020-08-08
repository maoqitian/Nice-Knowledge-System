package observepattern;

/**
 * @Description: 被观察者 抽象 提供 添加观察者 删除观察者 通知观察者 抽象方法
 * @Author: maoqitian
 * @CreateDate: 2020/8/8 16:14
 */
public interface Observable {

    //添加观察者
    void addListener(Observer observer);

    //删除观察者

    void removeListener(Observer observer);

    //通知 状态变化
    void notifyListener(String msg);
}
