package single;

/**
 * @Description: 静态内部类实现 单例模式 处理 DCL 失效问题 最推荐
 * @Author: maoqitian
 * @CreateDate: 2021/2/1 23:08
 */
public class SingletonPro {

    //私有构造
    private SingletonPro(){}

    public static SingletonPro getInstance(){
        return SingletonProHolder.mInstance;
    }

    /**
     * 静态内部类 提供 单例对象
     */
    private static class SingletonProHolder{
        private static final SingletonPro mInstance = new SingletonPro();

    }
}
