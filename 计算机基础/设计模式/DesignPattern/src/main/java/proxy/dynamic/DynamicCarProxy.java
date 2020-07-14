package proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author maoqitian
 * @Description 动态代理 汽车生产 CarMaker（汽车生产实现可以自由定义）
 * 动态代理继承InvocationHandler 接口
 */
public class DynamicCarProxy implements InvocationHandler {


    private Object mObject;

    public DynamicCarProxy(Object object){
        this.mObject = object;

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //动态代理添加自己处理逻辑
        System.out.println("动态代理，这边有客人下单了一辆宝马M3车迷限定版");
        Object object = method.invoke(mObject,args);
        System.out.println("动态代理,你的车已经下单");
        return object;
    }
}
