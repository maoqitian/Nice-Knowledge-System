package observepattern;

/**
 * @Description: 观察者模式 观察者抽象
 * @Author: maoqitian
 * @CreateDate: 2020/8/8 16:09
 */
public interface Observer  {
    //接收到更新的消息
    void update(String msg);
}
