package volatiletest;

/**
 * @Description:
 * @Author: maoqitian
 * @CreateDate: 2020/10/27 20:01
 */
public class VolatileClass {


    public static void main(String[] args) {

        TestThread testThread = new TestThread();
         testThread.start();

         //主线程
         //System.out.println("currentThread()"+ Thread.currentThread().getName());
         while (true){
            //synchronized (testThread){
                if(testThread.isFlag()){
                    System.out.println("wdnmd .....");

                }
            //}

        }

    }

    static class TestThread extends Thread{

        public boolean isFlag() {
            return flag;
        }

        //private  boolean flag = false;
        private volatile boolean flag = false;


        @Override
        public void run() {
            super.run();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            flag = true;
            System.out.println("flag : "+ flag+" threadid " + currentThread().getName());
        }
    }
}
