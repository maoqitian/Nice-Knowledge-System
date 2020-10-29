package reentrantLocktest;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: condition 阻塞 唤醒
 * @Author: maoqitian
 * @CreateDate: 2020/10/29 23:08
 */
public class ConditionTest {
    static ReentrantLock lock = new ReentrantLock();
    //由锁创建 condition
    static Condition condition = lock.newCondition();

    public static void main(String[] args)  {

        //先获取锁
        lock.lock();
        new Thread(new ThreadDemo()).start();
        System.out.println("主线程等待通知");
        try {
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        System.out.println("主线程恢复运行");

    }

    static class ThreadDemo implements Runnable {

        @Override
        public void run() {
            //线程获取锁
            lock.lock();
            try {
                //子线程通知外部线程唤醒
                condition.signal();
                System.out.println("子线程唤醒外部condition");
            } finally {
                lock.unlock();
            }

        }
    }
}
