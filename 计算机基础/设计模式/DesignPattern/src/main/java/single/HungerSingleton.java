package single;

/**
 * @Description: 饿汉式 单例
 * @Author: maoqitian
 * @CreateDate: 2021/2/1 22:54
 */
public class HungerSingleton {
    //直接实例化 不管是否需要创建
    private  static final HungerSingleton mInstance =  new HungerSingleton();

    //私有构造
    private HungerSingleton(){}

    public static synchronized HungerSingleton getInstance(){
        return mInstance;
    }
}
