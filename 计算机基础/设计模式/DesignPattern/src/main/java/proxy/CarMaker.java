package proxy;

/**
 * 生产汽车实现 工人
 * 也是代理模式中被代理对象
 */
public class CarMaker implements ICarFactory {

    @Override
    public void makeCar() {
        System.out.println("我是汽车生产商，现在生产了一辆汽车");
    }
}
