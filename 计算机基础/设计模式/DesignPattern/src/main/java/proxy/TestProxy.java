package proxy;

import proxy.dynamic.DynamicCarProxy;
import proxy.staticagent.SellCar;

import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * 代理模式测试
 */

public class TestProxy {



    public static void main(String[] args) {
        System.out.println("买一辆宝马M3车迷限量版");
        //静态代理
        SellCar sellCar = new SellCar(new CarMaker());

        sellCar.buyCar();

        //动态代理
        ICarFactory iCarFactory =
                (ICarFactory) Proxy.newProxyInstance(ICarFactory.class.getClassLoader(),
                        new Class<?>[]{ICarFactory.class},
                        new DynamicCarProxy(new CarMaker()));
        iCarFactory.makeCar();


        String[] strings = {"dog","racecar","car"};
        Arrays.sort(strings);
        System.out.println(Arrays.toString(strings));

    }

}
