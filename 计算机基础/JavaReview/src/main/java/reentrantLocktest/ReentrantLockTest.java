package reentrantLocktest;

/**
 * @Description: 公屏上与非公平锁
 * @Author: maoqitian
 * @CreateDate: 2020/10/28 23:34
 */
public class ReentrantLockTest {


    static MyReentrantLock lock = new MyReentrantLock();

    public  void printLog(){
        for (int i = 0; i <5 ; i++) {
               try {
                   //上锁
                   lock.lock();
                   System.out.println(Thread.currentThread().getName()+" is print" +i);
               }catch (Exception e){
                   e.printStackTrace();
               }finally {
                   //执行完成释放锁
                   lock.unLock();
               }


        }
    }



    public static void main(String[] args)  {

        //两个不同的对象
        ReentrantLockTest test1 = new ReentrantLockTest();
        ReentrantLockTest test2 = new ReentrantLockTest();

        //不同锁对象，线程交替执行 实际不是

        Thread thread = new Thread(test1::printLog);

        Thread thread2 = new Thread(test2::printLog);

        thread.start();
        thread2.start();


    }
}
