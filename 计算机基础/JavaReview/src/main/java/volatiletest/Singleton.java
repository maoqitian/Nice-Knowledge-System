package volatiletest;

/**
 * @Description: 双重效验锁单例
 * @Author: maoqitian
 * @CreateDate: 2020/10/27 22:41
 */
public class Singleton {

    //volatile 保证可见性 和 禁止指令重排 可见性保证之后其他线程就拿到已经初始化的对象，而不是新建一个对象，从而保证对象单例
    private volatile static Singleton mInstance = null;

    //私有构造
    private Singleton(){}

    public static Singleton getInstance(){
        //第一重检查锁定
        if(mInstance == null){
            //同步锁定代码块
            synchronized (Singleton.class){
                //第二次检查
                if(mInstance == null){ //禁止指令重排 有可能分配内存空间直接返回引用地址 没有初始化 造成空指针移除

                    mInstance = new Singleton();
                }
            }
        }
        return mInstance;
    }
}
