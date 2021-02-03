package single;

/**
 * @Description:
 * @Author: maoqitian
 * @CreateDate: 2021/2/2 23:01
 */
public class TestSingle {


    public static void main(String[] args) {
        HungerSingleton hungerSingleton = HungerSingleton.getInstance();

        LazySingleton lazySingleton = LazySingleton.getInstance();

        Singleton singleton = Singleton.getInstance();

        SingletonPro singletonPro = SingletonPro.getInstance();

        SingletonEnum singletonEnum = SingletonEnum.INSTANCE;

        SingleManager.registerService("singleObject",new Object());
        Object object = SingleManager.getService("singleObject");
    }
}
