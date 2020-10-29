package reentrantLocktest;

import reentrantLocktest.queue.MyBlockingQueue;

/**
 * @Description: 测试自定义阻塞队列
 * @Author: maoqitian
 * @CreateDate: 2020/10/29 23:13
 */
public class BlockingQueueTest {

    public static void main(String[] args) {
        //创建多个线程交替读写数据 阻塞队列大小为 2
        MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(2);
        for (int i = 0; i < 10; i++) {
            int data = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        queue.enqueue(data);
                    } catch (InterruptedException e) {

                    }
                }
            }).start();

        }
        for(int i=0;i<10;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //出队
                        Integer data = queue.dequeue();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
