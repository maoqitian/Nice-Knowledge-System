package threadlocal;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description: ThreadLocal 测试
 * @Author: maoqitian
 * @CreateDate: 2020/10/29 23:21
 */
public class ThreadLocalTest {



        static ThreadLocal<String> name =new ThreadLocal<>();

        public static void main(String[] args) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    name.set("xiaoming");
                    System.out.println("---------------"+name.get()+"-------------------");
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    System.out.println("---------------"+name.get()+"-------------------");
                }
            }).start();
        }

}
