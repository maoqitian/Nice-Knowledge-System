package reentrantLocktest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: tryLock 处理死锁
 * @Author: maoqitian
 * @CreateDate: 2020/10/29 23:05
 */
/*public class ReentrantLock3 {
    //不同的两把锁
    static ReentrantLock lock1 = new ReentrantLock();
    static ReentrantLock lock2 = new ReentrantLock();

    public static void main(String[] args)  {

        //分被使用两个线程来获取锁操作
        //先获取 1 后获取 2
        Thread t1 = new Thread(new ThreadDemo(lock1,lock2));
        t1.start();
        //先获取 2 后获取 1
        new Thread(new ThreadDemo(lock2,lock1)).start();
        //中断退出死锁 如果没有中断 则资源互相抢占 死锁
        //t1.interrupt();
    }

    static class ThreadDemo implements Runnable {
        Lock firstLock;
        Lock secondLock;

        public ThreadDemo(Lock firstLock, Lock secondLock) {
            this.firstLock = firstLock;
            this.secondLock = secondLock;
        }
        @Override
        public void run() {
            try {
                firstLock.lockInterruptibly();
                //休眠更好的来触发死锁
                TimeUnit.MILLISECONDS.sleep(20);
                secondLock.lockInterruptibly();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                firstLock.unlock();
                secondLock.unlock();
                System.out.println(Thread.currentThread().getName()+"正常结束!");
            }

        }
    }
}*/

/**
 * @author maoqitian
 * @Description ReentrantLock 锁
 * @create 2020-10-28 20:24
 */
class ReentrantLock3 {

    //不同的两把锁
    static ReentrantLock lock1 = new ReentrantLock();
    static ReentrantLock lock2 = new ReentrantLock();

    public static void main(String[] args)  {

        //分被使用两个线程来获取锁操作
        //先获取 1 后获取 2
        Thread t1 = new Thread(new ThreadDemo(lock1,lock2));
        t1.start();
        //先获取 2 后获取 1
        new Thread(new ThreadDemo(lock2,lock1)).start();
        //中断退出死锁
        //t1.interrupt();
    }

    static class ThreadDemo implements Runnable {
        Lock firstLock;
        Lock secondLock;

        public ThreadDemo(Lock firstLock, Lock secondLock) {
            this.firstLock = firstLock;
            this.secondLock = secondLock;
        }
        @Override
        public void run() {
            try {
                while(!firstLock.tryLock(1,TimeUnit.SECONDS)){
                    //获取锁失败
                    //休眠一会
                    TimeUnit.MILLISECONDS.sleep(20);
                }
                while(!secondLock.tryLock(1,TimeUnit.SECONDS)){
                    //第二把锁获取失败，则释放第一把锁 防止资源抢占 解决死锁
                    firstLock.unlock();
                    TimeUnit.MILLISECONDS.sleep(20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                firstLock.unlock();
                secondLock.unlock();
                System.out.println(Thread.currentThread().getName()+"正常结束!");
            }

        }
    }
}
