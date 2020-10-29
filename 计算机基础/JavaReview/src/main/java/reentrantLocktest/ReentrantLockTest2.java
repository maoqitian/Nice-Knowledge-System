package reentrantLocktest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: 测试公平锁与非公平锁
 * @Author: maoqitian
 * @CreateDate: 2020/10/29 23:00
 */
class ReentrantLockTest2 {

    static ReentrantLock lock = new ReentrantLock();
    static ReentrantLock fairlock = new ReentrantLock(true);

    public static void main(String[] args)  {


        for (int i = 0; i <=3; i++) {
            new Thread(new ThreadDemo(i)).start();
        }

    }

    static class ThreadDemo implements Runnable {
        Integer id;

        public ThreadDemo(Integer id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i=0;i<2;i++){
                //每个线程同时获取释放两次锁
                lock.lock();
                System.out.println("获得锁的线程："+id);
                lock.unlock();
            }
        }
    }

}