package builder;

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
}
