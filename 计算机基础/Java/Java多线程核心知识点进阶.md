# Java多线程核心知识点进阶
> 开始本文之前，可以先问问自己一个问题，什么是多线程？

## JavaMemoryModel
- Java内存模型是一套共享内存系统中多线程读写操作行为的规范，这套规范屏蔽了底层各种硬件和操作系统的内存访问差异，解决了 CPU 多级缓存、CPU 优化、指令重排等导致的内存访问问题，从而保证 Java 程序（尤其是多线程程序）在各种平台下对内存的访问效果一致。

![java内存模型](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/Java%E5%86%85%E5%AD%98%E6%A8%A1%E5%9E%8Bnew.jpg)

### Java 多线程特性

- 原子性
- 可见性
- 有序性

### 总线嗅探机制

- CPU 需要每时每刻监听总线上的一切活动，但是不管别的核心的 Cache 是否缓存相同的数据，都需要发出一个广播事件，这无疑会加重总线的负载，大量无效交互可能达到总线带宽峰值，也就是**总线风暴**，如何避免？尽量少用Volatile 关键字

### 缓存一致性协议（MESI）
- 当CPU写数据时，如果发现操作的变量是共享变量，即在其他CPU中也存在该变量的副本，会发出信号通知其他CPU将该变量的缓存行置为无效状态，因此当其他CPU需要读取这个变量时，发现自己缓存中缓存该变量的缓存行是无效的，那么它就会从内存重新读取 

#### MESI 四个状态

- Modified(修改)：已经被更新过，但是还没有写到内存里
- Exclusive(独占)：只有一个线程缓存有改数据，不影响多线程操作
- Share(共享)：其他线程读取独占数据，独占数据标记为共享数据；共享状态代表着相同的数据在多个 CPU 核心的 Cache 里都有，所以当我们要更新 Cache 里面的数据的时候，不能直接修改，而是要先向所有的其他 CPU 核心广播一个请求，要求先把其他核心的 Cache 中对应的 Cache Line 标记为「失效」状态，然后再更新当前 Cache 里面的数据 
- Invalidate(失效)：数据失效，也就不能再读取该数据，也就不用发送消息更新

## Volatile（关键字）

- **对volatile变量的写入操作必须在对该变量的读操作之前执行，保证变量在多线程中的可见性和有序性**

### 可见性原理

- 缓存一致性协议MESI

### 有序性原理

- 内存屏障指令（Memory Barrier），内存屏障会导致cpu缓存的刷新，刷新时，会遵循缓存一致性协议
- 禁止指令重排(为了提高处理速度，JVM会对代码进行编译优化，也就是指令重排序优化，并发编程下指令重排序会带来一些安全隐患：如指令重排序导致的多个线程操作之间的不可见性)


屏障类型 | 指令实例| 说明
---|---|---
LoadLoad  Barrier| Load1;LoadLoad;Load2| 确保load1数据装载优先于load2及所有后续指令的装载
StoreStore  Barrier(有其他所有屏障公共，开销大，强制刷新)| Store1;StoreStore;Store2| 确保Store1数据对其他处理器可见（强制刷新到内存），优先于Store2及所有后续指令的存储
LoadStore  Barrier| Load1;LoadLoad;Store2| 确保load1数据装载优先于Store2及所有后续存储指令优先刷新到内存中
StoreLoad  Barrier| Store1;LoadLoad;Load2| 确保Store1数据对其它处理器可见（刷新到内存）优先于load2及所有后续指令的装载

- volatile写操作， 前后加写屏障，避免CPU重排导致的问题

![内存屏障写](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/volatile/%E5%86%85%E5%AD%98%E5%B1%8F%E9%9A%9C%E5%86%99new.png)

- 读操作后加入读写内存屏障，防止后面读写操作对当前读影响

![内存屏障读](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/volatile/%E5%86%85%E5%AD%98%E5%B1%8F%E9%9A%9C%E8%AF%BBnew.png)

- happens-before 原则，对一个volatile域的写操作，happens-before于任意线程后续对这个volatile域的读

- as-if-serial 原则，不管怎么重排序，单线程下的执行结果不能被改变，如下所示, y 的赋值依赖于 x ,  x 不先赋值则 y 为空导致结果不正确

```
int x = 0;
int y = x;
```


> 禁止指令重排好处
对象创建三个步骤
1. 分配内存空间
2. 调用构造器，初始化实例
3. 返回引用地址

- 如上所述，这里单例模式首先防止指针重排，从而防止了单例对象的空指针异常，好处二则是 volatile 保证到单例对象的可见性，从而保证了单例的唯一


```
/**
 * @Description: 双重效验锁单例
 * @Author: maoqitian
 * @CreateDate: 2020/10/27 22:41
 */
public class Singleton {

    //volatile 保证可见性 和 禁止指令重排 可见性保证之后其他线程就拿到已经初始化的对象，而不是新建一个对象，从而保证对象单例
    private volatile static Singleton mInstance = null;

    //私有构造
    private Singleton(){}

    public static Singleton getInstance(){
        //第一重检查锁定
        if(mInstance == null){
            //同步锁定代码块
            synchronized (Singleton.class){
                //第二次检查
                if(mInstance == null){ //禁止指令重排 有可能分配内存空间直接返回引用地址 没有初始化 造成空指针移除

                    mInstance = new Singleton();
                }
            }
        }
        return mInstance;
    }
}
```

## Java 锁

### Synchronized(悲观锁)

#### 作用域

- 锁修饰方法

```
public synchronized void printLog(){
    .....
}
```
- 锁修饰静态方法，修饰静态方法当前锁对象为当前 class 对象
```
public static synchronized void printLog(){
    .....
}
```
- 锁修饰代码块

```
 synchronized (SynchronizedTest.class){ //当前class 也可以使用 this 代替
           for (int i = 0; i <5 ; i++) {
               System.out.println(Thread.currentThread().getName()+" is print" +i);
           }
       }
```

- 在了解锁的同时也要注意锁对象是否为同一个，如下所示，锁对象不同，则不会产生互斥效果

```
/**
 * @author maoqitian
 * @Description
 * @create 2020-10-28 19:40
 */
public class SynchronizedTest2 {

         public  synchronized void printLog(){

           for (int i = 0; i <5 ; i++) {
               System.out.println(Thread.currentThread().getName()+" is print" +i);
           }
         }

    public static void main(String[] args)  {

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
```
- 锁代码块和静态修饰锁方法作用是同一个class 锁对象，自然是没问题

```
public class SynchronizedTest2 {

        public static synchronized void printLog(){

        for (int i = 0; i <5 ; i++) {
            System.out.println(Thread.currentThread().getName()+" is print" +i);
        }
    }

    public static void main(String[] args)  {

        SynchronizedTest2 test2 = new SynchronizedTest2();
        SynchronizedTest2 test3 = new SynchronizedTest2();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //static 静态方法和对象实例无关
                SynchronizedTest2.printLog();
                //test2.printLog();
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                //static 静态方法和对象实例无关
                SynchronizedTest2.printLog();
                //test3.printLog();
            }
        });
        thread.start();
        thread2.start();
    }
}
```
- 同理锁代码块

```
public class SynchronizedTest2 {

        public void printLog(){

        synchronized (this){
            for (int i = 0; i <5 ; i++) {
                System.out.println(Thread.currentThread().getName()+" is print" +i);
            }
        }
    }

    public static void main(String[] args)  {

        SynchronizedTest2 test2 = new SynchronizedTest2();
        SynchronizedTest2 test3 = new SynchronizedTest2();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

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
```
#### Synchronized 原理

- 锁方法和锁代码块字节码变化

![锁方法](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/synchronized/%E9%94%81%E6%96%B9%E6%B3%95.png)

![锁代码块](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/synchronized/%E9%94%81%E4%BB%A3%E7%A0%81%E5%9D%97.png)

- 如上图所示锁代码块是在字节码指令加入 monitorenter 和 monitorexit，锁方法则 ACC_SYNCHRONIZED 方法表标志位（[重新认识Java字节码](https://note.youdao.com/)）

#### 对象组成

- 对象在内存中分为三个区域，分别是对象头、实例数据和对其填充，在JVM中对象映射到 C 中使用了 OOP-Klass模型（[大话Java对象在虚拟机中是什么样子](https://mp.weixin.qq.com/s/fyvoraVu9yjgqX-xhn6EHQ)），**OOP 指的是普通对象指针，用来表示对象的实例信息，Klass 包含元数据和方法信息，用来描述Java类**，创建一个Java对象，也就会创建一个instanceOopDesc示例对象，他的基类为oopDesc如下 C 代码
```
class oopDesc {
  friend class VMStructs;

 private:

  volatile markOop  _mark;

  union _metadata {
    wideKlassOop    _klass;

    narrowOop       _compressed_klass;

  } _metadata;
```
- _metadata 和 _mark 一起组成了对象头

##### 对象头
- Mark Word（标记字段）：也就是上面所说的_mark，默认存储对象的HashCode，分代年龄和锁标志位信息。它会根据对象的状态复用自己的存储空间，也就是说在运行期间Mark Word里存储的数据会随着锁标志位的变化而变化

- Klass Point（类型指针）：对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例

- Synchronized 对应于重量级锁，标志位为 10 ，重量级锁存储 Mark Word 中存放的就是 Monitor 对象，前面字节码指令截图也可看出

#### Monitor

- Monitor对应 C 代码中的实现为 ObjectMonitor

```
ObjectMonitor() {
    _header       = NULL;
    _count        = 0;  //标识线程获取锁的次数
    _waiters      = 0,
    _recursions   = 0;  // 线程重入次数
    _object       = NULL;  // 存储Monitor对象
    _owner        = NULL;  // 持有当前线程的owner
    _WaitSet      = NULL;  // wait状态的线程列表
    _WaitSetLock  = 0 ;
    _Responsible  = NULL ;
    _succ         = NULL ;
    _cxq          = NULL ;  
    FreeNext      = NULL ;
    _EntryList    = NULL ;  // 处于等待锁状态的线程列表（block状态）
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ;
    _previous_owner_tid = 0;
  }
```
- 由 ObjectMonitor 对象与下图对应

![ObjectMonitor](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/synchronized/ObjectMonitor.png)

- 线程1和2 竞争锁对象，这是线程1和2保存到锁对象的_EntryList中，假设获取锁对象的是线程1，则此时_owner 指向线程1，标识线程1获取锁，同时 _count记录 1 ；此时线程1调用wait()，则将线程1保存到_WaitSet中，同时让线程2获取锁。如果在_WaitSet中的线程1调用 notify()方法则将线程1加回到_EntryList列表中继续竞争锁。

#### 重量级锁？

- 为什么说Synchronized是重量级锁？

![user_kernal_space](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/synchronized/user_kernal_space.png)

- 实际上，ObjectMonitor 的同步机制是 JVM 对操作系统级别的 Mutex Lock（互斥锁）的管理过程，其间都会转入操作系统内核态进行操作。也就是说 synchronized 实现锁，在“重量级锁”状态下（JDK 1.6之后轻量级锁），线程切换下沉到内核空间是一个比较重量级的耗费资源操作

#### 锁升级

- JDK 为了优化锁增加了偏向锁、自旋锁、 轻量级锁对 synchronized 的优化，这些优化最主要的方式就是减少用户空间到内核空间上下文切换的频率，从而优化锁性能
- 锁升级方向，锁升级过程不可逆

![lockUpdate](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/synchronized/lockUpdate.png)

- 对象头锁标志位对应表如下

是否偏向锁 | 锁标志位 | 锁状态
---|---|---
0 | 01|无锁
1 | 01|偏向锁
0 | 00|轻量级锁
0 | 10|重量级锁
0 | 11|GC标志

##### 偏向锁

- 由上表格，偏向锁标志位就是对象的默认状态，偏**向锁的意思是如果一个线程获得了一个偏向锁，如果在接下来的一段时间中没有其他线程来竞争锁，那么持有偏向锁的线程再次进入或者退出同一个同步代码块，不需要再次进行抢占锁和释放锁的操作**，也就是一个线程获得了锁，再次获取锁不需要进行任何操作。偏向锁在1.6之后是默认开启的，1.5中是关闭的，偏向锁可以手动通过 -XX:+UseBiasedLocking 开启或者关闭。
- 偏向锁实现是 Mark Word 中的有个 ThreadId 字段，默认它是为空，当线程获取锁对象，则将线程ThreadId写入，下次线程再来获取锁比较线程id是否相同就可以获取锁了。

- 偏向锁使用场景，适合在基本没有线程竞争的单个线程场景下使用，只有一个线程来竞争锁，这样每次锁获取都是同一个线程，这样锁就不会膨胀升级，不耗费资源，例如 Vector 类，但是偏向锁的撤销代价是一个比较大，应酌情考虑开启与关闭。

##### 轻量级锁

- 前面说了偏向锁，基本就需要在无竞争环境下使用，如果多个线程竞争，则直接升级为重量级锁吗？这显然不合适，轻量级锁就是这样产生的，为了避免锁直接升级成重量级锁，轻量级锁会在栈帧中开辟一块空间并复制Mark Word中的数据记录，称为 lock Record

![轻量级锁](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/synchronized/%E8%BD%BB%E9%87%8F%E7%BA%A7%E9%94%81.png)

- 而后 Java 虚拟机会尝试使用 CAS（Compare And Swap）操作，将原本对象的 Mark Word 更新到开辟的 lock Record 空间，更新成功表示加锁成功，然后改变标志位 00
- 最后，如果 CAS 失败，则判断当前 Mark Word 是否指向当前栈帧，是则已经持有，否则获取锁失败，升级锁

##### 自旋锁

- 相当于程序空转，死循环十次，获取不到锁升级为重量级锁

#### 锁升级不可逆？

- 锁升级是不可逆的，如果一个业务在某个时间段有多个线程竞争，则会立即膨胀升级为重量级锁，之后则会一直是重量级锁，这一定程度会影响业务，所以Synchronized锁不可中断成为一个隐患，接下来继续看可以中断的锁。

### CAS(乐观锁)

线程在读取数据时不进行加锁，在准备写回数据时，先去查询原值，操作的时候比较原值是否修改，若未被其他线程修改则写回，若已被修改，则重新执行读取流程。

- CAS 全称为 Compare And Swap，意为比较和替换，调用 Unsafe 中的 API 进行 CAS 操作，它是通过硬件实现并发安全的常用技术，底层通过利用 CPU 的 CAS 指令对缓存加锁或总线加锁的方式来实现多处理器之间的原子操作
- 它的实现过程主要有 3 个操作数：内存值 V，旧的预期值 E，要修改的新值 U，当且仅当预期值 E和内存值 V 相同时，才将内存值 V 修改为 U，否则什么都不做。

- CAS 底层会根据操作系统和处理器的不同来选择对应的调用代码，以 Windows 和 X86 处理器为例，如果是多处理器，通过带 lock 前缀的 cmpxchg 指令对缓存加锁或总线加锁的方式来实现多处理器之间的原子操作；如果是单处理器，通过 cmpxchg 指令完成原子操作

#### 实现自己的CAS逻辑（同时操作线程不能过多）

- 数据库存储一个标志数据，添加版本号（ABA问题），每次有数据改变，改变数据的同时更新版本号

### ReentrantLock（悲观锁）

- JUC 并发包提供的类 jdk1.8.0_144\jre\lib\rt.jar!\java\util\concurrent 目录

#### ReentrantLock 实例

- 测试公平锁与非公平锁

```
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
                fairlock.lock();
                System.out.println("获得锁的线程："+id);
                fairlock.unlock();
            }
        }
    }

}

//打印结果 非公平锁，可以看到基本上是当前线程继续抢占
获得锁的线程：3
获得锁的线程：3
获得锁的线程：2
获得锁的线程：2
获得锁的线程：1
获得锁的线程：1
获得锁的线程：0
获得锁的线程：0

//打印结果 公平锁，线程交替执行
获得锁的线程：3
获得锁的线程：2
获得锁的线程：1
获得锁的线程：0
获得锁的线程：3
获得锁的线程：2
获得锁的线程：1
获得锁的线程：0
```
- 通过 tryLock 判断是否获取锁判断是否有必要释放锁解决死锁问题

```
**
 * @author maoqitian
 * @Description ReentrantLock 锁
 * @create 2020-10-28 20:24
 */
class ReentrantLockTest3 {

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
       t1.interrupt();
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
}
```
- 使用 tryLock 方法尝试看是否获取锁成功，不成功则释放，解决死锁
```
/**
 * @author maoqitian
 * @Description ReentrantLock 锁
 * @create 2020-10-28 20:24
 */
class ReentrantLockTest3 {

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
```
- condition 等待唤醒

```
/**
 * @author maoqitian
 * @Description ReentrantLock 锁 condition
 * @create 2020-10-28 20:34
 */
class ReentrantLockTest {
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

```
- 实现阻塞队列

```
/**
 * @author maoqitian
 * @Description
 * 1.入队和出队线程安全
 * 2.当队列满时,入队线程会被阻塞;当队列为空时,出队线程会被阻塞。
 * @create 2020-10-28 20:44
 */
public class MyBlockingQueue<E> {

    int size;//阻塞队列最大容量

    ReentrantLock lock = new ReentrantLock();

    LinkedList<E> list=new LinkedList<>();//队列底层实现

    Condition notFull = lock.newCondition();//队列满时的等待条件
    Condition notEmpty = lock.newCondition();//队列空时的等待条件

    public MyBlockingQueue(int size) {
        this.size = size;
    }

    public void enqueue(E e) throws InterruptedException {
        lock.lock();
        try {
            while (list.size() ==size)//队列已满,在notFull条件上等待
                notFull.await();
            list.add(e);//入队:加入链表末尾
            System.out.println("入队：" +e);
            notEmpty.signal(); //通知在notEmpty条件上等待的线程
        } finally {
            lock.unlock();
        }
    }

    public E dequeue() throws InterruptedException {
        E e;
        lock.lock();
        try {
            while (list.size() == 0)//队列为空,在notEmpty条件上等待
                notEmpty.await();
            e = list.removeFirst();//出队:移除链表首元素
            System.out.println("出队："+e);
            notFull.signal();//通知在notFull条件上等待的线程
            return e;
        } finally {
            lock.unlock();
        }
    }
}
```
- 测试阻塞队列

```
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
                    Integer data = queue.dequeue();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

```
#### 底层实现 CAS + AQS、公平锁、非公平锁

![AQS执行获取锁逻辑](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/ReentrantLock/AQS%E6%89%A7%E8%A1%8C%E8%8E%B7%E5%8F%96%E9%94%81%E9%80%BB%E8%BE%91.png)

##### 非公平锁
- 如上图，如果是非公平锁，当线程 A 来获取锁，当前 state 为 0 ，CAS操作将state 改为 1 ，同时将锁的exclusiveOwnerThread 指向 线程A，说明线程A获得了锁；此时线程 B 来获取锁，发现 state = 1，CAS修改状态失败，所以只能进入等待队列，同时阻塞线程B；方法A线程执行完成释放锁，此时恰好线程C也来请求锁，这时候C线程就会获取锁，而B线程则继续等待，这也就是为什么说是非公平锁的原因，这样的好处就是可以让有的线程减少了等待时间，提高了利用率

```
static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         */
        final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }
    
     final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
            //非公平锁发现当前没有线程锁则马上占用锁
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            .....
        }
```

##### 公平锁

- 而公平锁则在新的线程来获取锁的时候，发现当前锁没有被占用，或先去判断等待队列中是否保存有阻塞的线程，有的话直接将当前线程加入到等待队列末尾，保证先来的线程优先执行

```
static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }

        /**
         * Fair version of tryAcquire.  Don't grant access unless
         * recursive call or no waiters or is first.
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() && //检查等待队列中是否有阻塞线程
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            ........
        }
    }
```

- 最后来看看ReentrantLock 获取锁的时序图，加深理解

![ReentrantLock非公平锁时序图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/ReentrantLock/ReentrantLock%E9%9D%9E%E5%85%AC%E5%B9%B3%E9%94%81%E6%97%B6%E5%BA%8F%E5%9B%BE.png)
#### 获取锁逻辑
- AQS 的 acquire 通过调用 sync 子类自定义实现的 tryAcquire 方法获取锁；
- 如果获取锁失败，通过 addWaiter 方法将线程构造成 Node 节点插入到同步队列队尾；
- 在 acquirQueued 方法中以自旋的方法尝试获取锁，如果失败则判断是否需要将当前线程阻塞，如果需要阻塞则最终执行 LockSupport(Unsafe) 中的 native API 来实现线程阻塞。
#### AQS原理 

- AQS 本身其实是一套框架，它将大部分的同步逻辑已经封装好，值暴露获取锁的入口逻辑留给我们自己实现，也就是像 sync 这样，如下图

![sync继承关系](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/ReentrantLock/sync%E7%BB%A7%E6%89%BF%E5%85%B3%E7%B3%BB.png)

- AQS 底层基于双端队列和一个标志位，有一个同步队列和一个或多个等待队列，当获取锁的线程需要某个条件时（condition）则进入等待队列，当满足条件才进入同步队列开始竞争锁

![AQS 底层实现](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/ReentrantLock/AQS%E5%BA%95%E5%B1%82%E5%AE%9E%E7%8E%B0.png) 

```
//源码中关于等待双端队列的描述
     *<p>To enqueue into a CLH lock, you atomically splice it in as new
     * tail. To dequeue, you just set the head field.
     * <pre>
     *      +------+  prev +-----+       +-----+
     * head |      | <---- |     | <---- |     |  tail
     *      +------+       +-----+       +-----+
     * </pre>
```
- 队列节点

```
 static final class Node {
        节点模式 独占 或者 共享
        /** Marker to indicate a node is waiting in shared mode */
        static final Node SHARED = new Node();
        /** Marker to indicate a node is waiting in exclusive mode */
        static final Node EXCLUSIVE = null;

        //等待状态
        /** waitStatus value to indicate thread has cancelled */
        static final int CANCELLED =  1;
        /** waitStatus value to indicate successor's thread needs unparking */
        static final int SIGNAL    = -1;
        /** waitStatus value to indicate thread is waiting on condition */
        static final int CONDITION = -2;
        /**
         * waitStatus value to indicate the next acquireShared should
         * unconditionally propagate
         */
        static final int PROPAGATE = -3;
         
         //前驱节点指针
         volatile Node prev;
         //后驱节点指针
         volatile Node next;
         //当前节点线程
         volatile Thread thread;
     ........
 }
```
- 等待状态表

waitStatue 状态 | 含义
---|---
CANCELLED | 当前线程中断或者操作取消，这是终结状态，代表该结点到此为止，不进行其他任何操作
SIGNAL | 当前节点的后继被(或即将)阻塞(通过park)，因此当前节点在释放或取消时必须释放它的后继。为了避免竞争，acquire方法必须首先表明它们需要一个信号，然后重试原子获取，当失败时，阻塞。通常一个线程加入判断前继节点的状态来决定是否阻塞当前线程
CONDITION | 当前线程在 condition 等待队列中
PROPAGATE | 用于唤醒后继节点状态，这个状态的引入是为了完善和增强共享锁的唤醒机制。在一个节点成为头节点之前，是不会跃迁为此状态的
0 | 线程无状态

#### ReentrantLock 获取锁失败分析

- ReentrantLock 的 lock 方法 首先判断是否能够获取锁，获取不成功，则调用 acquire 方法
```
public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
```
- tryAcquire是自定义的实现再次判断能否获取锁逻辑，如果该方法返回 false，也就是获取锁失败，接着调用 acquireQueued，他的两个参数中调用了addWaiter方法，Node.EXCLUSIVE 标识独占模式

```
private Node addWaiter(Node mode) {
        //创建新的结点
        Node node = new Node(Thread.currentThread(), mode);
        //获取尾部节点
        Node pred = tail;
        //通过CAS 将当前结点替换队列尾部
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {//修改失败，说明正在有线程修改队列 需要自旋等待插入
                pred.next = node;
                //CAS 操作成功返回
                return node;
            }
        }
        //放入队列失败 自旋重试
        enq(node);
        return node;
    }
    
private Node enq(final Node node) {
        //自旋操作
        for (;;) {
            Node t = tail;
            //队列都没有，初始化队列，并且放入一个空node作为头节点
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
            //有节点则插入成功返回
                node.prev = t;
                if (compareAndSetTail(t, node)) { 
                    t.next = node;
                    return t;
                }
            }
        }
    }    
```
- 继续回看到 acquireQueued 方法

```
final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            //1
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                //2
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
    
     private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            /*
             * This node has already set status asking a release
             * to signal it, so it can safely park.
             * 前驱节点为 SIGNAL ， 挂起当前线程
             */
            return true;
        if (ws > 0) {
            /*
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             * 前驱节点已经为终止状态，则继续往队列前面遍历，找到状态不为 cancelled 设置为前驱节点，并尝试再次自旋获取锁
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /*
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             * 前驱节点没有状态或者为PROPAGATE，说明前驱节点为头节点，设置前驱节点状态为SIGNAL 先不挂起线程，并再次自旋尝试获取锁
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
```
- acquireQueued 注释1 处会再次自旋尝试获取锁，理由是有可能在插入队列后锁已经释放，所以再次尝试自旋 tryAcquire 获取锁，这也是一个锁获取的优化
- 注释 2 在前面自旋获取锁失败，则进入 shouldParkAfterFailedAcquire 方法根据前驱节点状态是否要挂起当前线程
- 最后shouldParkAfterFailedAcquire返回 true 无法获取锁，挂起当前线程调用 parkAndCheckInterrupt 方法


```
  private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }
      public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(false, 0L);
        setBlocker(t, null);
    }
    
    private static final sun.misc.Unsafe UNSAFE;
    
```
- LockSupport 的 park 方法最终调用 native 底层 C 实现的方法挂起当前线程，也就是 Unsafe 的 API


### CountdownLatch

```
/**
 * @author maoqitian
 * @Description CountdownLatch
 * @create 2020-09-22 11:03
 */
public class CountdownLatchTest {

    public static void main(String[] args) {

        ExecutorService service = Executors.newFixedThreadPool(3);
        CountDownLatch countDownLatch = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("子线程" + Thread.currentThread().getName() + "开始执行");
                        Thread.sleep((long) (Math.random() * 10000));
                        System.out.println("子线程"+Thread.currentThread().getName()+"执行完成");
                        countDownLatch.countDown();//当前线程调用此方法，则计数减一
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            service.execute(runnable);
        }

        try {
            System.out.println("主线程"+Thread.currentThread().getName()+"等待子线程执行完成...");
            countDownLatch.await();//阻塞当前线程，直到计数器的值为0
            System.out.println("主线程"+Thread.currentThread().getName()+"开始执行...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        service.shutdownNow();
    }
}
```

### ThreadLocal

[重看 Android 消息机制](https://github.com/maoqitian/Nice-Knowledge-System/blob/master/AndroidFramework%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/Handler%20%E6%B6%88%E6%81%AF%E6%9C%BA%E5%88%B6/%E9%87%8D%E7%9C%8B%20Android%20%E6%B6%88%E6%81%AF%E6%9C%BA%E5%88%B6.md)

## 线程池
>线程池主要解决两个问题：
一、 当执行大量异步任务时线程池能够提供很好的性能。
二、 线程池提供了一种资源限制和管理的手段，比如可以限制线程的个数，动态新增线程等。
——《Java并发编程之美》

[Java中的线程池和作用，有必要了解一下](https://juejin.im/post/6844903650704392205)

### 线程池原理

- 核心逻辑：执行线程，当核心线程数没有超过最大值，直接创建核心线程执行任务，核心线程数已满，将任务放到阻塞队列，阻塞队列已满，创建非核心线程执行任务，非核心线程数已满，执行拒绝策略

- 线程池内部结构


```
public class ThreadPoolExecutor extends AbstractExecutorService {
        .....
        //线程池状态
        private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
        //当前线程池运行状态
        private static int runStateOf(int c)     { return c & ~CAPACITY; }
        //当前线程数量计算
        private static int workerCountOf(int c)  { return c & CAPACITY; }
        //通过 运行状态和线程数量 获取线程池状态
        private static int ctlOf(int rs, int wc) { return rs | wc; }
        
        //保存所有的核心线程和非核心线程
        private final HashSet<Worker> workers = new HashSet<Worker>();
        //阻塞队列，保存超过核心线程的任务
        private final BlockingQueue<Runnable> workQueue;
         // Packing and unpacking ctl
       
        .....
}
```

![线程池实现原理](https://github.com/maoqitian/MaoMdPhoto/raw/master/Java%E8%99%9A%E6%8B%9F%E6%9C%BA/%E5%A4%9A%E7%BA%BF%E7%A8%8B/%E7%BA%BF%E7%A8%8B%E6%B1%A0/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%A0%B8%E5%BF%83%E7%BB%93%E6%9E%84.png)


## kotlin 协程

- 本质上为 kotlin 对 Java 线程池的封装


> 你知道的越多，你不知道的越多

## 参考资料 

- [10 张图打开 CPU 缓存一致性的大门](https://mp.weixin.qq.com/s/PDUqwAIaUxNkbjvRfovaCg)
- [漫画Java线程池的工作机制](https://mp.weixin.qq.com/s?__biz=MzU3Mjc5NjAzMw==&mid=2247484276&idx=1&sn=31c805234afd7284457f268a74db7ce3&chksm=fcca3e9acbbdb78c3d4c4a58aab058577377e87e612aea435fe08f4f3f90bbd8612abdc07237&token=263838289&lang=zh_CN#rd)
