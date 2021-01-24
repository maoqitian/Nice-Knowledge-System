package builder;

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
