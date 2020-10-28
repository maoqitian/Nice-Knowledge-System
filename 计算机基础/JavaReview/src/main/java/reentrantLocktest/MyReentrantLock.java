package reentrantLocktest;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @Description: 自定义ReentrantLock
 * @Author: maoqitian
 * @CreateDate: 2020/10/28 23:57
 */
public class MyReentrantLock {

    private Sync sync = new Sync();

    /**
     * 加锁
     */
    public void lock(){
        sync.acquire(1);
    }

    /**
     * 释放锁
     */
    public void unLock(){
        sync.release(1);
    }

    static class Sync extends AbstractQueuedSynchronizer{

        //获取锁
        @Override
        protected boolean tryAcquire(int arg) {
            return compareAndSetState(0,1);
        }

        //释放锁
        @Override
        protected boolean tryRelease(int arg) {
            setState(0);
            return true;
        }
    }
}
