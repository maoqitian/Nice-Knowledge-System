package single;

/**
 * @Description: 懒汉式 单例
 * @Author: maoqitian
 * @CreateDate: 2021/2/1 22:54
 */
public class LazySingleton {

    private  static LazySingleton mInstance = null;

    //私有构造
    private LazySingleton(){}

    public static synchronized LazySingleton getInstance(){
        if(mInstance == null){ //需要的时候才实例对象
            mInstance = new LazySingleton();
        }
        return mInstance;
    }
}
