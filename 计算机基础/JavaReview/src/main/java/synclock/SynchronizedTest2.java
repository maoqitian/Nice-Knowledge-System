package synclock;

import java.util.Vector;

/**
 * @Description:
 * @Author: maoqitian
 * @CreateDate: 2020/10/28 21:54
 */
public class SynchronizedTest2 {

    public void printLog(){

        for (int i = 0; i <5 ; i++) {
            synchronized (this){
                System.out.println(Thread.currentThread().getName()+" is print" +i);

            }
        }
    }



    public static void main(String[] args)  {
        //创建不同对象
        SynchronizedTest2 test2 = new SynchronizedTest2();
        SynchronizedTest2 test3 = new SynchronizedTest2();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //不同锁对象，线程交替执行
                test2.printLog();
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                test3.printLog();
            }
        });

        thread.start();
        thread2.start();
    }
}
