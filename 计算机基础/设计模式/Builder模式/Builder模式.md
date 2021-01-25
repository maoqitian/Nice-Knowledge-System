# Builder模式
> Builder 模式将一个复杂的对象与表示分离，使得同样的构建过程可以穿件不同的表示。在 Android Framework 源码中 AlertDialog 的构造，开源网络请求框架 OKHttp 构造中都有用到 Builder 模式。

## Builder 模式 UML 图

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/builder%E6%A8%A1%E5%BC%8F/builder-design.png)

### 角色释义  

- Director：统一对象组装过程，分别调用对象组装方法
- Builder：抽象Buidler类，规范对象组建，
- ConcreateBuilder：抽象 Buidler 实现类
- Product：构造对象的抽象类

## Builder 模式使用场景

- 相同对象，不同的执行顺序，产生不同的对象结果
- 一个对象可以有多个对应的配置方法，统一配置到对应对象同时产生的对象又不同
- 对应产品对象复杂，产品调用顺序不同产生不同结果
- 建造产品对象有独立的算法来对产品的构造装配产生影响

## Builder 模式简单实现

- 一个电脑可以有不同的配置，不同的配置则电脑性能就会有所不同，比如 CPU、内存、系统等
- 创建一个电脑产品抽象类

```
/**
 * @Description: 电脑对象抽象类 相当于 product 角色
 * @Author: maoqitian
 * @CreateDate: 2021/1/23 22:39
 */
public abstract class Computer {

    //处理器
    String process;
    //电脑名称
    String name;
    //系统版本
    String osVersion;
    //内存
    String memory;

    //电脑价格
    public abstract void price();
    .....
}
```
- 具体 product 类

```
/**
 * @Description: mbp 具体 product 类
 * @Author: maoqitian
 * @CreateDate: 2021/1/23 22:45
 */
public class MacBookPro extends Computer{
    @Override
    public void price() {
        System.out.println("电脑价格为20K");
    }


    @Override
    public String toString() {
        price();
        return "电脑配置：MacBookPro{" +
                "process='" + process + '\'' +
                ", name='" + name + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", memory='" + memory + '\'' +
                '}';
    }
    .....
}
```
- 在实际开发中 Director 角色经常会被沈略，而直接实现使用 Buidler 来进行对象组装，在 MacBookPro 静态 Buidler 内部类
```
/**
     * UML图中是作为抽象类 这里直接实现 Builder , 规范 product 创建
     */
    public static class Builder {
        private final ComputerParams P;

        public Builder(){
           P = new ComputerParams();
        }

        public Builder setProcess(String process) {
            P.process = process;
            return this;
        }

        public Builder setName(String name) {
            P.name = name;
            return this;
        }

        public Builder setOsVersion(String osVersion) {
            P.osVersion = osVersion;
            return this;
        }

        public Builder setMemory(String memory) {
            P.memory = memory;
            return this;
        }
        // 对象创建
        public MacBookPro create(){
            final MacBookPro macBookPro = new MacBookPro();
            //参数赋值
            P.apply(macBookPro);
            return macBookPro;
        }



    }

    //参数配置
    public static class ComputerParams {

        //处理器
        String process;
        //电脑名称
        String name;
        //系统版本
        String osVersion;
        //内存
        String memory;

        public void apply(MacBookPro macBookPro){
            if(process!=null){
                macBookPro.setProcess(process);
            }
            if(name!=null){
                macBookPro.setName(name);
            }
            if(osVersion!=null){
                macBookPro.setOsVersion(osVersion);
            }
            if(memory!=null){
                macBookPro.setMemory(memory);
            }
        }

        public String getProcess() {
            return process;
        }

        public void setProcess(String process) {
            this.process = process;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public String getMemory() {
            return memory;
        }

        public void setMemory(String memory) {
            this.memory = memory;
        }
    }
```
### 测试运行


```
  public static void main(String[] args) {

        //构造 mac book pro 对象
        MacBookPro macBookPro =
                new MacBookPro.Builder()
                .setName("MacBook Pro (15-inch, 2019)")
                .setMemory("16 GB 2400 MHz DDR4")
                .setOsVersion("macOs Catalina 10.15.5")
                .setProcess("2.6 GHz 6-Core Intel Core i7")
                .create();

        System.out.println(macBookPro.toString());

    }
```
- 执行结果

![builder模式demo运行结果](https://github.com/maoqitian/MaoMdPhoto/raw/master/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/builder%E6%A8%A1%E5%BC%8F/builder%E6%A8%A1%E5%BC%8Fdemo%E8%BF%90%E8%A1%8C%E7%BB%93%E6%9E%9C.png)

## 参考

- 《Android 源码设计模式解析与实战》
- [demo源码地址](https://github.com/maoqitian/Nice-Knowledge-System/tree/master/%E8%AE%A1%E7%AE%97%E6%9C%BA%E5%9F%BA%E7%A1%80/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/DesignPattern/src/main/java/builder)