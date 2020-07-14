package proxy;

/**
 * 代理模式接口 描述 被代理类提供的抽象方法
 * 这里以汽车生产比喻
 */
public interface ICarFactory {

    /**
     * 生产汽车
     */
    void makeCar();
}
