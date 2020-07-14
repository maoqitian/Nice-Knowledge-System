package proxy.staticagent;

import proxy.ICarFactory;

public class SellCar implements ICarFactory {

    private ICarFactory carMaker;

    public SellCar(ICarFactory carMaker){
        this.carMaker = carMaker;
    }

    public void buyCar(){
       System.out.println("这边有客人下单了一辆宝马M3车迷限定版");
       makeCar();
       System.out.println("你的车已经下单");
    }

    @Override
    public void makeCar() {
       carMaker.makeCar();
    }
}
