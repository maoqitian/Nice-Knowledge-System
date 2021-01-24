package builder;

/**
 * @Description: Builder 模式测试
 * @Author: maoqitian
 * @CreateDate: 2021/1/23 23:02
 */
public class TestBuilder {

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
}
