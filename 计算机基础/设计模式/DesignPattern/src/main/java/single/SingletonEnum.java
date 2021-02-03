package single;

/**
 * @Description: 枚举实现单例
 * @Author: maoqitian
 * @CreateDate: 2021/2/1 23:14
 */
public enum SingletonEnum {

    INSTANCE;

    public void print(){
        System.out.println("单例枚举方法");
    }
}
