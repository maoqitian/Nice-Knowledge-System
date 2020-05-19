# 深入理解Android之 BroadcastReceiver
> Android 应用与 Android 系统和其他 Android 应用之间可以相互收发广播消息，这与发布-订阅设计模式相似。这些广播会在所关注的事件发生时发送。举例来说，Android 系统会在发生各种系统事件时发送广播，例如系统启动或设备开始充电时。再比如，应用可以发送自定义广播来通知其他应用它们可能感兴趣的事件（本文源码基于[Android 10](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:) ）

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/BroadcastReceiver/BroadcastReceiver.png)

## 广播注册

### 注册广播接收者组件调用到AMS
- 广播注册一般分为静态注册广播和动态注册广播，动态注册又可以远程注册和本地注册（LocalBroadcastManager）,平时使用最多的应该也是动态注册广播，接下来则从 registerReceiver 方法入手看看广播是如何注册的。简单注册一个广播，设置action为**BROADCAST_TEST_MESSAGE**

```
IntentFilter intentFilter = new IntentFilter(BROADCAST_TEST_MESSAGE);
        registerReceiver(receiver,intentFilter);
```
- 回顾以前分析Service的文章[深入理解Android 之Service启动流程](https://juejin.im/post/5eaa84bae51d454dc7454c12)，registerReceiver方法同样调用的是ContextWrapper的方法，而ContextWrapper调用的是ContextImpl的registerReceiver方法。
>frameworks/base/core/java/android/app/ContextImpl.java
```
@Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return registerReceiver(receiver, filter, null, null);
    }
    
@Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter,
            String broadcastPermission, Handler scheduler) {
        return registerReceiverInternal(receiver, getUserId(),
                filter, broadcastPermission, scheduler, getOuterContext(), 0);//1
    }    
```
- 由以上源码，registerReceiver调用了四个参数的registerReceiver方法，而后调用了registerReceiverInternal方法，这里需要注意getOuterContext()对应的是ContextImpl的mOuterContext变量，对应的也就是调用注册广播方法的Activity组件。接着看到registerReceiverInternal方法
>frameworks/base/core/java/android/app/ContextImpl.java
```
private Intent registerReceiverInternal(BroadcastReceiver receiver, int userId,
            IntentFilter filter, String broadcastPermission,
            Handler scheduler, Context context, int flags) {
        IIntentReceiver rd = null;//1
        if (receiver != null) {
            if (mPackageInfo != null && context != null) {
                if (scheduler == null) {
                    scheduler = mMainThread.getHandler();//2
                }
                rd = mPackageInfo.getReceiverDispatcher(
                    receiver, context, scheduler,
                    mMainThread.getInstrumentation(), true);//3
            } 
            .....
        }
        try {
            final Intent intent = ActivityManager.getService().registerReceiver(
                    mMainThread.getApplicationThread(), mBasePackageName, rd, filter,
                    broadcastPermission, userId, flags);//4
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
```
- 由以上registerReceiverInternal方法源码，看到注释1处是不是感觉和Service的绑定过程似成相识，IIntentReceiver就是一个用于进程间通信的本地Binder对象，具体实现则为LoadApk.ReceiverDispatcher.InnerReceiver
1. 注释2处获取一个Handler对象，它指向的就是ActivityThread的主线程handler对象H
2. 注释3处将传入的广播接收者BroadcastReceiver作为参数调用了mPackageInfo对象的getReceiverDispatcher方法，mPackageInfo也就是LoadApk对象,
3. 看到注释4，毫无疑问这里借助AIDL调用了AMS的registerReceiver方法，并传入**ApplicationThread**方便后续AMS与当前注册了广播接收者的进程通信。

- 接着回看到LoadApk对象的getReceiverDispatcher方法
>frameworks/base/core/java/android/app/LoadedApk.java

```
 private final ArrayMap<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> mReceivers
        = new ArrayMap<>();
static final class ReceiverDispatcher {

        final static class InnerReceiver extends IIntentReceiver.Stub {
            .......
        }//1
        
        ReceiverDispatcher(BroadcastReceiver receiver, Context context,
                Handler activityThread, Instrumentation instrumentation,
                boolean registered) {
            ......

            mIIntentReceiver = new InnerReceiver(this, !registered);
            mReceiver = receiver;
            mContext = context;
            mActivityThread = activityThread;
            mInstrumentation = instrumentation;
            mRegistered = registered;
            mLocation = new IntentReceiverLeaked(null);
            mLocation.fillInStackTrace();
        }//2
        
        .......
        public IIntentReceiver getReceiverDispatcher(BroadcastReceiver r,
            Context context, Handler handler,
            Instrumentation instrumentation, boolean registered) {
        synchronized (mReceivers) {
            LoadedApk.ReceiverDispatcher rd = null;
            ArrayMap<BroadcastReceiver, LoadedApk.ReceiverDispatcher> map = null;
            if (registered) {
                map = mReceivers.get(context);
                if (map != null) {
                    rd = map.get(r);
                }
            }
            if (rd == null) {
                rd = new ReceiverDispatcher(r, context, handler,
                        instrumentation, registered);//3
                if (registered) {
                    if (map == null) {
                        map = new ArrayMap<BroadcastReceiver, LoadedApk.ReceiverDispatcher>();
                        mReceivers.put(context, map);
                    }
                    map.put(r, rd);//4
                }
            } else {
                rd.validate(context, handler);
            }
            rd.mForgotten = false;
            return rd.getIIntentReceiver();//5
        }
    }
    .....
    
    IIntentReceiver getIIntentReceiver() {
            return mIIntentReceiver;
        }//6
}
```
- 由以上源码
1. 看到注释3处，和Activity绑定Service一个组件一样，每一个注册广播接收者的Activity组件都会有一个对应ReceiverDispatcher对象，不存在则新建它
2. 看到注释2处ReceiverDispatcher构造方法，它持有了我们注册的广播接收者，对应指向注册广播的Activity组件的context和该组件的主线程handler对象，并创建了实现 IIntentReceiver 本地Binder接口的 **InnerReceiver**
3. 注释4处将每个ReceiverDispatcher以广播接收者为key存放到ArrayMap中，并将指向对应Actiivty组件的context为key，将保存广播接收者与ReceiverDispatcher的对应关系ArrayMap保存到LoadApk的mReceivers中
4. 结合注释5和6 最终返回的就是 **InnerReceiver** 支持进程间通信的本地Binder对象
- 接着继续分析AMS的registerReceiver
>frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java
```
public Intent registerReceiver(IApplicationThread caller, String callerPackage,
            IIntentReceiver receiver, IntentFilter filter, String permission, int userId,
            int flags) {
        ......
            
        synchronized (this) {
           
            ReceiverList rl = mRegisteredReceivers.get(receiver.asBinder());
            if (rl == null) {
                rl = new ReceiverList(this, callerApp, callingPid, callingUid,
                        userId, receiver);
                if (rl.app != null) {
                    ......
                    rl.app.receivers.add(rl);
                } else {
                    try {
                        receiver.asBinder().linkToDeath(rl, 0);
                    } catch (RemoteException e) {
                        return sticky;
                    }
                    rl.linkedToDeath = true;
                }
                mRegisteredReceivers.put(receiver.asBinder(), rl);//1
            } 
            .......
            BroadcastFilter bf = new BroadcastFilter(filter, rl, callerPackage,
                    permission, callingUid, userId, instantApp, visibleToInstantApps);//2
            if (rl.containsFilter(filter)) {
                Slog.w(TAG, "Receiver with filter " + filter
                        + " already registered for pid " + rl.pid
                        + ", callerPackage is " + callerPackage);
            } else {
                rl.add(bf);//3
                .....
                mReceiverResolver.addFilter(bf);//4
            }                
                
            .....
}
```
- 由以上源码，省略部分有处理粘性广播的代码，粘性事件会一直保存在AMS，直到有新的粘性事件。谷歌官方已经将粘性事件作为过时方法处理，这里就不进行展开了。
1. 注释1处将receiver作为key，receiver集合位置存放在AMS的HashMap类型的mRegisteredReceivers中，广播的注册并不是把广播接收者注册到AMS中，而把和注册广播接收者关联的对应
IIntentReceiver对象也就是**InnerReceiver**保存（注册）到了AMS中
- 注释2处则将设置的IntentFilter包装成了BroadcastFilter对象。也就是包含了开始传入的action为**BROADCAST_TEST_MESSAGE**，也就是说BroadcastFilter可以作为广播接收者的描述，实现一个广播接收者可以在多个Activity组件中注册，所以注释3处则是用ReceiverList来保存每个组件的广播接收者
- 注释4处在mReceiverResolver中保存广播接收者对象描述，后续AMS接收到对应广播消息发送，则可以在其中找到对应广播接收者。
- 广播注册过程就了解到这里，下一节继续了解广播发送过程

### Activity注册广播到AMS时序图

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/BroadcastReceiver/Activity%E6%B3%A8%E5%86%8C%E5%B9%BF%E6%92%AD%E5%88%B0AMS%E6%97%B6%E5%BA%8F%E5%9B%BE.jpg)

## 发送广播

### AMS找到动态注册的广播接收者

```
sendBroadcast(new Intent(BROADCAST_TEST_MESSAGE));
```

- 上一小节已经了解了广播的注册过程，本小节继续了解广播的发送过程，发送广播同样有几种，分别是普通广播，有序广播和粘性广播，基本过程大同小异，所以本小节就从sendBroadcast方法开始。如上所示发送一个广播，Action也是**BROADCAST_TEST_MESSAGE**；同样的发送广播的sendBroadcast方法最终调用的还是ContextImpl的sendBroadcast方法
> frameworks/base/core/java/android/app/ContextImpl.java

```
 @Override
    public void sendBroadcast(Intent intent) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(
                    mMainThread.getApplicationThread(), intent, resolvedType, null,
                    Activity.RESULT_OK, null, null, null, AppOpsManager.OP_NONE, null, false, false,
                    getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
```
- 逻辑很简单，直接借助AIDL进程间通信调用了AMS的broadcastIntent方法，接着往下看
>frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java
```
public final int broadcastIntent(IApplicationThread caller,
            Intent intent, String resolvedType, IIntentReceiver resultTo,
            int resultCode, String resultData, Bundle resultExtras,
            String[] requiredPermissions, int appOp, Bundle bOptions,
            boolean serialized, boolean sticky, int userId) {
        enforceNotIsolatedCaller("broadcastIntent");
        synchronized(this) {
            intent = verifyBroadcastLocked(intent);

            final ProcessRecord callerApp = getRecordForAppLocked(caller);
            final int callingPid = Binder.getCallingPid();
            final int callingUid = Binder.getCallingUid();

            final long origId = Binder.clearCallingIdentity();
            try {
                return broadcastIntentLocked(callerApp,
                        callerApp != null ? callerApp.info.packageName : null,
                        intent, resolvedType, resultTo, resultCode, resultData, resultExtras,
                        requiredPermissions, appOp, bOptions, serialized, sticky,
                        callingPid, callingUid, callingUid, callingPid, userId);//2
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
```
- 由以上代码，逻辑也很清晰，获取进程Id明确发送广播的进程的是谁，接着继续调用了broadcastIntentLocked方法。
>frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java
```
final int broadcastIntentLocked(ProcessRecord callerApp,
            String callerPackage, Intent intent, String resolvedType,
            IIntentReceiver resultTo, int resultCode, String resultData,
            Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions,
            boolean ordered, boolean sticky, int callingPid, int callingUid, int realCallingUid,
            int realCallingPid, int userId, boolean allowBackgroundActivityStarts) {
        intent = new Intent(intent);
        .....
         // Add to the sticky list if requested.
        if (sticky) {
            if (checkPermission(android.Manifest.permission.BROADCAST_STICKY,
                    callingPid, callingUid)//1
                    != PackageManager.PERMISSION_GRANTED) {
               ......
            }
            ......
            // We use userId directly here, since the "all" target is maintained
            // as a separate set of sticky broadcasts.
            if (userId != UserHandle.USER_ALL) {
                // But first, if this is not a broadcast to all users, then
                // make sure it doesn't conflict with an existing broadcast to
                // all users.
                ........
                }
            }
        ArrayMap<String, ArrayList<Intent>> stickies = mStickyBroadcasts.get(userId);
            if (stickies == null) {
                stickies = new ArrayMap<>();
                mStickyBroadcasts.put(userId, stickies);//2
            }
        ......    
        
```
1. broadcastIntentLocked方法源码很长，开始省略的源码中包含了Intent定义好的各种Action处理，之后便是粘性广播的处理，看到注释1处如果是粘性广播会检查对应应用程序进程是否注册了android.Manifest.permission.BROADCAST_STICKY权限，然后注释2处通过以用户id为key，粘性广播集合list为值存放在AMS的SparseArray（该map的key只能是Int）类型mStickyBroadcasts对象中。接着看broadcastIntentLocked方法的下一段代码
>frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java

```
// Figure out who all will receive this broadcast.
// 找到是谁来接收广播消息
        List receivers = null;
        List<BroadcastFilter> registeredReceivers = null;
        // Need to resolve the intent to interested receivers...
        if ((intent.getFlags()&Intent.FLAG_RECEIVER_REGISTERED_ONLY)
                 == 0) {
            receivers = collectReceiverComponents(intent, resolvedType, callingUid, users);//1
        }
        if (intent.getComponent() == null) {
            if (userId == UserHandle.USER_ALL && callingUid == SHELL_UID) {
                // Query one target user at a time, excluding shell-restricted users
                .......
            } else {
                registeredReceivers = mReceiverResolver.queryIntent(intent,
                        resolvedType, false /*defaultOnly*/, userId);//2
            }
        }
        
        final boolean replacePending =
                (intent.getFlags()&Intent.FLAG_RECEIVER_REPLACE_PENDING) != 0;//3

        ......

        int NR = registeredReceivers != null ? registeredReceivers.size() : 0;
        if (!ordered && NR > 0) {
            // If we are not serializing this broadcast, then send the
            // registered receivers separately so they don't wait for the
            // components to be launched.
            if (isCallerSystem) {
                checkBroadcastFromSystem(intent, callerApp, callerPackage, callingUid,
                        isProtectedBroadcast, registeredReceivers);
            }
            final BroadcastQueue queue = broadcastQueueForIntent(intent);
            BroadcastRecord r = new BroadcastRecord(queue, intent, callerApp,
                    callerPackage, callingPid, callingUid, callerInstantApp, resolvedType,
                    requiredPermissions, appOp, brOptions, registeredReceivers, resultTo,
                    resultCode, resultData, resultExtras, ordered, sticky, false, userId,
                    allowBackgroundActivityStarts, timeoutExempt);//4
            
            final boolean replaced = replacePending
                    && (queue.replaceParallelBroadcastLocked(r) != null);//5
            // Note: We assume resultTo is null for non-ordered broadcasts.
            if (!replaced) {//6
                queue.enqueueParallelBroadcastLocked(r);//7
                queue.scheduleBroadcastsLocked();//8
            }
            registeredReceivers = null;//9
            NR = 0;
        }
```
1. 由以上源码，注释1处通过Intent设置的组件名称去PackageManagerService寻找在清单文件注册的静态广播，并存储在receivers变量中
2. 注释2处结合上一小节广播注册分析，注册的广播是放在AMS的mReceiverResolver对象中，所以这里获取需要接收广播的广播接收者在registeredReceivers变量存放
3. 结合上面两点分析**receivers变量存放的是静态广播，registeredReceivers变量存放的是动态广播**
4. 广播发送不是发送了就马上接收的，需要存放到调度队列，并通过消息机制Handler来发送消息传给广播接收者，所以如果当前发送的广播没来得及发送又有新的广播发来，则看到注释3和注释5返回replacePending为true，会把旧的广播消息替换成当前新的
5. 接着看到注释4代码AMS会把要处理的广播接收者包装成BroadcastRecord对象
6. 结合注释6、7、8，如果当前消息不要替换，说明是最新需要发送的消息，则调用enqueueParallelBroadcastLocked将要处理的广播保存在消息队列BroadcastQueue的**mParallelBroadcasts**集合中，也就是保存无序广播；接着广播消息队列BroadcastQueue的scheduleBroadcastsLocked方法来发送消息给广播接收者
7. 最后注释9发送之后则将当前动态广播保存对象置为null

- 接着看到BroadcastQueue的scheduleBroadcastsLocked方法
>frameworks/base/services/core/java/com/android/server/am/BroadcastQueue.java

```
 public void scheduleBroadcastsLocked() {
        .......

        if (mBroadcastsScheduled) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(BROADCAST_INTENT_MSG, this));//1
        mBroadcastsScheduled = true;
    }
```
- 由以上代码，逻辑很简单，使用了BroadcastQueue的Handler来发送**BROADCAST_INTENT_MSG**消息来进行处理，并且设置mBroadcastsScheduled为true。由此可以明白广播的发送和接收是异步执行的，广播发送之后不会等待AMS处理完毕在转发给接收者。接着看到Hnadler的消息处理
>frameworks/base/services/core/java/com/android/server/am/BroadcastQueue.java

```
final BroadcastHandler mHandler;

    private final class BroadcastHandler extends Handler {
        public BroadcastHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BROADCAST_INTENT_MSG: {
                    .....
                    processNextBroadcast(true);//1
                } break;
                .......
            }
        }
    }
```
- 由以上源码注释1，只是调用了processNextBroadcast方法，接着往下看
>frameworks/base/services/core/java/com/android/server/am/BroadcastQueue.java
```
final void processNextBroadcast(boolean fromMsg) {
        synchronized (mService) {
            processNextBroadcastLocked(fromMsg, false);
        }
    }

    final void processNextBroadcastLocked(boolean fromMsg, boolean skipOomAdj) {
        BroadcastRecord r;
       .......

        if (fromMsg) {
            mBroadcastsScheduled = false;//1
        }
        
        // First, deliver any non-serialized broadcasts right away.
        while (mParallelBroadcasts.size() > 0) {
            r = mParallelBroadcasts.remove(0);//2
            ......

            final int N = r.receivers.size();
            ......
            for (int i=0; i<N; i++) {
                Object target = r.receivers.get(i);//3
                .......
                deliverToRegisteredReceiverLocked(r, (BroadcastFilter)target, false, i);//4
            }
            addBroadcastToHistoryLocked(r);
            .........
        }
    
        .....
    }
```
- 由以上源码
1. 注释1处方法传入fromMsg为ture，接着讲mBroadcastsScheduled置为false说明前面Handler发送处理的BROADCAST_INTENT_MSG消息已经被处理
2. 接着注释从前面提到AMS包装的广播接收者BroadcastRecord对象从无序广播集合mParallelBroadcasts取出，接着注释3处获取BroadcastFilter也就注册中广播接收者描述一并交由deliverToRegisteredReceiverLocked方法进行处理，该方法负责将广播消息发送给特点的接收者

> frameworks/base/services/core/java/com/android/server/am/BroadcastQueue.java 
```
private void deliverToRegisteredReceiverLocked(BroadcastRecord r,
            BroadcastFilter filter, boolean ordered, int index) {
       .......
    try {
          .......
                performReceiveLocked(filter.receiverList.app, filter.receiverList.receiver,
                        new Intent(r.intent), r.resultCode, r.resultData,
                        r.resultExtras, r.ordered, r.initialSticky, r.userId);
         .......
            }
            
        } catch (RemoteException e) {                
        .......
}
            
```
- 由以上源码，省略源码中经过一些权限判断，之后调用BroadcastQueue的performReceiveLocked方法将**BroadcastRecord对象描述的广播发送给对应BroadcastFilter描述的广播接收者处理**
>frameworks/base/services/core/java/com/android/server/am/BroadcastQueue.java

```
 void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver,
            Intent intent, int resultCode, String data, Bundle extras,
            boolean ordered, boolean sticky, int sendingUser)
            throws RemoteException {
        // Send the intent to the receiver asynchronously using one-way binder calls.
        if (app != null) {
            if (app.thread != null) {
                // If we have an app thread, do the call through that so it is
                // correctly ordered with other one-way calls.
                try {
                    app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode,
                            data, extras, ordered, sticky, sendingUser, app.getReportedProcState());//1
            
                } catch (RemoteException ex) {
                    ......
                }
           ....
        } else {
            receiver.performReceive(intent, resultCode, data, extras, ordered,
                    sticky, sendingUser);//2
        }
    }
```
- 由以上源码
1. 上一小节分析注册广播时我们知道Activity组件注册广播实际上是将广播接收者包装成IIntentReceiver本地Binder对象InnerReceiver保存在AMS，如果Activity组件应用程序进程存在，则借助AIDL调用注释1处应用程序进程ActivityThread.ApplicationThread的scheduleRegisteredReceiver方法，否则注释2处直接调用与其关联的实现IIntentReceiver代理Binder引用的performReceive方法。
2. 如果应该程序进程，则将描述目标广播接收者的 IIntentReceiver作为参数调用scheduleRegisteredReceiver方法，这样进回到了注册广播的应用程序进程

### 广播接收者接收消息回调onReceive方法
>frameworks/base/core/java/android/app/ActivityThread.java
```
// This function exists to make sure all receiver dispatching is
        // correctly ordered, since these are one-way calls and the binder driver
        // applies transaction ordering per object for such calls.
        public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent,
                int resultCode, String dataStr, Bundle extras, boolean ordered,
                boolean sticky, int sendingUser, int processState) throws RemoteException {
            updateProcessState(processState, false);
            receiver.performReceive(intent, resultCode, dataStr, extras, ordered,
                    sticky, sendingUser);//1
        }
```
- 由前面分析，ApplicationThread的源码scheduleRegisteredReceiver方法如上所示，以上源码调用performReceive方法其实就是InnerReceiver的方法，接着往下看

> frameworks/base/core/java/android/app/LoadedApk.java
```
final static class InnerReceiver extends IIntentReceiver.Stub {
            final WeakReference<LoadedApk.ReceiverDispatcher> mDispatcher;
            final LoadedApk.ReceiverDispatcher mStrongRef;

            InnerReceiver(LoadedApk.ReceiverDispatcher rd, boolean strong) {
                mDispatcher = new WeakReference<LoadedApk.ReceiverDispatcher>(rd);
                mStrongRef = strong ? rd : null;
            }

            @Override
            public void performReceive(Intent intent, int resultCode, String data,
                    Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                final LoadedApk.ReceiverDispatcher rd;
                .....
                rd = mDispatcher.get();
                .......
                if (rd != null) {
                    rd.performReceive(intent, resultCode, data, extras,
                            ordered, sticky, sendingUser);//1
                  } 
                .......
                }
            }
        }
```
- 看到注释1处，rd获取指向的就是LoadedApk.ReceiverDispatcher对象，他通过弱引用mDispatcher对象获取，最后调用ReceiverDispatcher的performReceive方法来处理Intent所描述的广播消息，接着往下看
> frameworks/base/core/java/android/app/LoadedApk.java

```
static final class ReceiverDispatcher {
......
final Handler mActivityThread;
......
public void performReceive(Intent intent, int resultCode, String data,
                Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
            final Args args = new Args(intent, resultCode, data, extras, ordered,
                    sticky, sendingUser);//1
            
            if (intent == null || !mActivityThread.post(args.getRunnable())//2 {
                .......
            }
        }
```

1. 由以上源码，注释1处创建了Args对象并包装描述广播消息的Intent
2. 这里还需要注意到mActivityThread它指向的就是ActivityThread对象代表主线成的内部Handler类H，并执行Runnable如下所示
>frameworks/base/core/java/android/app/LoadedApk.java
```
final class Args extends BroadcastReceiver.PendingResult {
            private Intent mCurIntent;
            private final boolean mOrdered;
            private boolean mDispatched;
            private boolean mRunCalled;

           ........

            public final Runnable getRunnable() {
                return () -> {
                    final BroadcastReceiver receiver = mReceiver;
                    .......
                    final IActivityManager mgr = ActivityManager.getService();
                    final Intent intent = mCurIntent;
                    
                    mCurIntent = null;
                    mDispatched = true;
                    mRunCalled = true;
                   .......
            
                    try {
                        .....
                        receiver.onReceive(mContext, intent);//1
                    } catch (Exception e) {
                        .......
                    }
                   .....
                };
            }
        }
```
- 看到以上源码，Runnable的run方法执行了BroadcastReceiver的onReceive方法并将广播消息Intent回调传入，这里也能说明广播运行在主线程。


### 发送广播时序图

![发送广播时序图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/BroadcastReceiver/%E5%8F%91%E9%80%81%E5%B9%BF%E6%92%AD%E6%97%B6%E5%BA%8F%E5%9B%BE.jpg)

## 回顾

- 广播的发送与接收都是异步的，通信组件可以在同一个进程也可以在不同进程
- 广播的注册实际是将于广播关联的InnerReceiver保存到AMS，方便后续接收到广播消息将消息转发给对应的广播接收者
- 可以对比Service的绑定过程与广播的注册过程，它们流程有相似之处，便于理解
- 广播机制基于消息发布和订阅的事件驱动模型，这不就是观察者模式在源码中的体现嘛
- 广播机制主要用于Android组件之间传递消息，实际底层依靠的是Binder机制来实现，它的注册中心依靠的是AMS，所以广播发送者不需要知道广播接收者是否存在，同时降低它们之间的耦合度

### 参考
- 书籍《Android 系统情景源代码分析》第三版
- [Android 10 源码地址](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:)