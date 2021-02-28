package leak;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * @Description: 内存泄漏测试
 * @Author: maoqitian
 * @CreateDate: 2021/2/21 22:35
 */
public class LeakTest {


    public static void main(String[] args) throws InterruptedException {

        //WeakReference 是弱引用类型，WeakReference 的构造函数可以传入 ReferenceQueue，
        // 当 WeakReference 指向的对象被垃圾回收器回收时，会把 WeakReference 放入 ReferenceQueue 中。
        ReferenceQueue<BigObject> referenceQueue = new ReferenceQueue<>();

        //BigObject 是强引用 gc 不会对其回收 会引发内存泄漏
        BigObject bigObject = new BigObject();

        WeakReference<BigObject> bigObjectWeakReference = new WeakReference<>(bigObject,referenceQueue);

        System.out.println("before gc Reference.get() ：" + bigObjectWeakReference.get());
        System.out.println("before gc referenceQueue ：" + referenceQueue.poll());

        System.gc();
        Thread.sleep(1000);

        System.out.println("after gc Reference.get()：" + bigObjectWeakReference.get());
        System.out.println("after gc referenceQueue ：" + referenceQueue.poll());

    }


    static class BigObject{}

}
