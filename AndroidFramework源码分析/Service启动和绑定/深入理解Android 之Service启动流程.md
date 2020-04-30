# 深入理解Android 之Service启动流程
> Service 是一种可在后台执行长时间运行操作而不提供界面的应用组件。服务可由其他应用组件启动，而且即使用户切换到其他应用，服务仍将在后台继续运行。此外，组件可通过绑定到服务与之进行交互，甚至是执行进程间通信 (IPC)(截取自官文文档)。服务启动方式有两种，分别是startService和bindService，接下来通过分析源码了解服务启动过程。（本文源码基于[Android 10](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:) ）

## Activity启动服务调用到ActivityManagerService过程

- 启动服务我们可以在Activity中调用startService方法，所以本文从Activity的startService开始进行了解，Activity继承ContextWrapper，所以调用的是ContextWrapper的startService方法

### ContextImpl与Activity进行关联

> frameworks/base/core/java/android/content/ContextWrapper.java

```

   Context mBase;
    /**
     * Set the base context for this ContextWrapper.  All calls will then be
     * delegated to the base context.  Throws
     * IllegalStateException if a base context has already been set.
     * 
     * @param base The new base context for this wrapper.
     */
    protected void attachBaseContext(Context base) {
        if (mBase != null) {
            throw new IllegalStateException("Base context already set");
        }
        mBase = base;//2
    }

 @Override
    public ComponentName startService(Intent service) {
        return mBase.startService(service);//1
    }
```
- 由以上源码注释1，startService方法最终调用的是Context类型的mBase的startService方法，而Context是一个抽象类，则它的实现类是谁呢？这时候可以回顾到前面的文章[深入理解Android 之 Activity启动流程](https://juejin.im/post/5e8407d251882573be11b63c)，在ActivityThread启动Activity的核心方法 performLaunchActivity

> frameworks/base/core/java/android/app/ActivityThread.java

```
/**  Core implementation of activity launch. */
    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        ......
        ContextImpl appContext = createBaseContextForActivity(r);//1
        .......
                appContext.setOuterContext(activity);
                activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback,
                        r.assistToken); //2

                ......

        return activity;
    }
```
- 有以上源码，注释1处新建了一个ContextImpl对象，注释2处将新建的ContextImpl对象对应与attach给Activity对象，接着看到Activity的attach方法

> frameworks/base/core/java/android/app/Activity.java

```
@UnsupportedAppUsage
    final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            NonConfigurationInstances lastNonConfigurationInstances,
            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
            Window window, ActivityConfigCallback activityConfigCallback, IBinder assistToken) {
        
        attachBaseContext(context);//1
        .......
    }
    
      @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase); //2
        if (newBase != null) {
            newBase.setAutofillClient(this);
            newBase.setContentCaptureOptions(getContentCaptureOptions());
        }
    }
```
- 通过以上源码注释1和2，结合前面ContextWrapper源码的注释2，很清晰的可以知道ContextWrapper的mBase指向的就是起到Activity过程创建的ContextImpl对象，所以启动服务startService方法实现就在ContextImpl，接着看它的源码

### ContextImpl调用到AMS

>frameworks/base/core/java/android/app/ContextImpl.java
```
@Override
    public ComponentName startService(Intent service) {
        warnIfCallingFromSystemProcess();
        return startServiceCommon(service, false, mUser);//1
    }
    private ComponentName startServiceCommon(Intent service, boolean requireForeground,
            UserHandle user) {
        try {
            validateServiceIntent(service);
            service.prepareToLeaveProcess(this);
            ComponentName cn = ActivityManager.getService().startService(
                mMainThread.getApplicationThread(), service, service.resolveTypeIfNeeded(
                            getContentResolver()), requireForeground,
                            getOpPackageName(), user.getIdentifier());//2
            .......
            return cn;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
```
- 由以上源码注释1，ContextImpl的startService方法又会调用自身的startServiceCommon方法，接着注释2处又调用了ActivityManager的getService方法，接着往下看

>frameworks/base/core/java/android/app/ActivityManager.java


```
 /**
     * @hide
     */
    @UnsupportedAppUsage
    public static IActivityManager getService() {
        return IActivityManagerSingleton.get();
    }

    @UnsupportedAppUsage
    private static final Singleton<IActivityManager> IActivityManagerSingleton =
            new Singleton<IActivityManager>() {
                @Override
                protected IActivityManager create() {
                    final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                    final IActivityManager am = IActivityManager.Stub.asInterface(b); //1
                    return am;
                }
            };
```
- 结合以上源码注释1，ActivityManager最终获取的是IActivityManager，通过AIDL实现应该程序进程与AMS所在的SystemServer进程通信，而他的实现则在ActivityMangerService中，所以前面分析的ContextImpl的startServiceCommon方法中最终调用的就是AMS的startServie方法，并且将mMainThread.getApplicationThread获取的**IApplicationThread的实现类ApplicationThread**引用传递给AMS，方便后续AMS能与应用程序进程通信启动Service。

### Activity启动服务调用到AMS过程时序图
- 本小节最后还是通过时序图来对上面的步骤进行回顾

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/Service/startService/Service%20%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E8%B0%83%E7%94%A8%E5%88%B0AMS.jpg)

## AMS与ActivityThread通信启动Service过程

- 由上一小节的分析，ContextImpl最终调用的是AMS的startService方法，接着往下看

>frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java
```
@Override
    public ComponentName startService(IApplicationThread caller, Intent service,
            String resolvedType, boolean requireForeground, String callingPackage, int userId)
            throws TransactionTooLargeException {
        enforceNotIsolatedCaller("startService");
        // Refuse possible leaked file descriptors
        ........
        synchronized(this) {
            final int callingPid = Binder.getCallingPid();
            final int callingUid = Binder.getCallingUid();
            final long origId = Binder.clearCallingIdentity();
            ComponentName res;
            try {
                res = mServices.startServiceLocked(caller, service,
                        resolvedType, callingPid, callingUid,
                        requireForeground, callingPackage, userId);//1
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
            return res;
        }
    }
```
- 由以上源码，AMS实现了IActivityManager，并且实现了startService方法，看到该方法注释1处，调用了ActiveServices的
startServiceLocked方法，ActiveServices是用来管理Service的类，接着往下看

> frameworks/base/services/core/java/com/android/server/am/ActiveServices.java


```
ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType,
            int callingPid, int callingUid, boolean fgRequired, String callingPackage,
            final int userId, boolean allowBackgroundActivityStarts)
            throws TransactionTooLargeException {
        .......

        ServiceLookupResult res =
            retrieveServiceLocked(service, null, resolvedType, callingPackage,
                    callingPid, callingUid, userId, true, callerFg, false, false);//1
        .......

        ServiceRecord r = res.record;//2

        .......

        ComponentName cmp = startServiceInnerLocked(smap, service, r, callerFg, addToStarting);//3
        return cmp;
    }
```

- 由以上源码，首先需要明白一点就是在AMS中用**ServiceRecord**描述每一个Service组件，就像描述Activity为ActivityRecord,接着startServiceLocked逻辑分为三步:
1. 首先调用retrieveServiceLocked方法查找是否存要启动的ServiceRecord，如果存在则将其封装到ServiceLookupResult返回，如果不存在则会与PMS（PackageManangerService）通信获取对应的Service组件，这里则不再跟进方法进行展开
2. 在注释2处，根据ServiceLookupResult获取对应的ServiceRecord
3. 注释3处则继续调用ActiveServices的startServiceInnerLocked，接着往下看

> frameworks/base/core/java/android/app/ActivityManager.java

```
ComponentName startServiceInnerLocked(ServiceMap smap, Intent service, ServiceRecord r,
            boolean callerFg, boolean addToStarting) throws TransactionTooLargeException {
       
        String error = bringUpServiceLocked(r, service.getFlags(), callerFg, false, false);//1
        if (error != null) {
            return new ComponentName("!!", error);
        }

       .....

        return r.name;
    }
```
- 由以上源码，只是继续调用了ActiveServices的bringUpServiceLocked方法，继续跟进看看

> frameworks/base/services/core/java/com/android/server/am/ActiveServices.java

```
private String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg,
            boolean whileRestarting, boolean permissionsReviewRequired)
            throws TransactionTooLargeException {
        ......
        
        ProcessRecord app;

        if (!isolated) {
            app = mAm.getProcessRecordLocked(procName, r.appInfo.uid, false); //1
            .....
            if (app != null && app.thread != null) {//2
                try {
                    app.addPackage(r.appInfo.packageName, r.appInfo.longVersionCode, mAm.mProcessStats);
                    realStartServiceLocked(r, app, execInFg);//3
                    return null;
                } catch (TransactionTooLargeException e) {
                    throw e;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception when starting service " + r.shortInstanceName, e);
                }

                // If a dead object exception was thrown -- fall through to
                // restart the application.
            }
        }
        ......
        }
       ........

        return null;
    }
```
- 由以上源码，注释1处通过mAm（也就是AMS对象）查找对应Service组件的进程ProcessRecord对象，注释2处如果进程存在并且与进程通信的**IApplicationThread**对象（上一小节传入）也存在则调用注释3处 ActiveServices 的 realStartServiceLocked 方法，继续跟进该方法

>frameworks/base/services/core/java/com/android/server/am/ActiveServices.java

```
/**
     * Note the name of this method should not be confused with the started services concept.
     * The "start" here means bring up the instance in the client, and this method is called
     * from bindService() as well.
     */
    private final void realStartServiceLocked(ServiceRecord r,
            ProcessRecord app, boolean execInFg) throws RemoteException {
        ......
        r.setProcess(app);//1
        
        ......
        try {
            ......
            
            app.thread.scheduleCreateService(r, r.serviceInfo,
                    mAm.compatibilityInfoForPackage(r.serviceInfo.applicationInfo),
                    app.getReportedProcState());//2
            r.postNotification();
            created = true;
        } catch (DeadObjectException e) {
           .....
        } finally {
            ........
    }
```
- 由以上源码，注释1处将获取的应用程序进程设置给ServiceRecord也就是Service组件，代表它将在哪个应用程序进程启动，然后注释2处则调用app.thread.scheduleCreateService，app.thread代表是谁已经不用多说了吧，就是上一步骤强调过的IApplicationThread，它的实现是应用程序进程的ActivityThread的 ApplicationThread，接下来转入到应用程序进程的ActivityThread类。

### ActivityThread 创建启动Service

- 通过上一节的分析，继续看到ApplicationThread的scheduleCreateService方法

>frameworks/base/core/java/android/app/ActivityThread.java

```

private class ApplicationThread extends IApplicationThread.Stub {

.......
public final void scheduleCreateService(IBinder token,
                ServiceInfo info, CompatibilityInfo compatInfo, int processState) {
            updateProcessState(processState, false);
            CreateServiceData s = new CreateServiceData();
            s.token = token;
            s.info = info;
            s.compatInfo = compatInfo;//1

            sendMessage(H.CREATE_SERVICE, s);//2
        }
.......
}        
```
- 由以上源码，注释一处将要启动的Service组件信息封装成CreateServiceData对象，并在注释而出调用ActivityThread的H将线程切换到了主线程来操作如下所示代码

>frameworks/base/core/java/android/app/ActivityThread.java

```
class H extends Handler {
.....
public void handleMessage(Message msg) {
            .....
            switch (msg.what) {
                
                case CREATE_SERVICE:
                 ......    
                 handleCreateService((CreateServiceData)msg.obj);//1
                    ....
                    break;
               ....
    }
    
}
```
- 由以上源码，注释1处继续调用了ActivityThread的handleCreateService方法，接着往下看

>frameworks/base/core/java/android/app/ActivityThread.java


```
private void handleCreateService(CreateServiceData data) {
        // If we are getting ready to gc after going to the background, well
        // we are back active so skip it.
        unscheduleGcIdler();

        LoadedApk packageInfo = getPackageInfoNoCheck(
                data.info.applicationInfo, data.compatInfo);//1
        Service service = null;
        try {
            java.lang.ClassLoader cl = packageInfo.getClassLoader();
            service = packageInfo.getAppFactory()
                    .instantiateService(cl, data.info.name, data.intent);//2
        } catch (Exception e) {
            ......
        }

        try {
            ......

            ContextImpl context = ContextImpl.createAppContext(this, packageInfo);//3
            context.setOuterContext(service);

            Application app = packageInfo.makeApplication(false, mInstrumentation);
            service.attach(context, this, data.info.name, data.token, app,
                    ActivityManager.getService());//4
            service.onCreate();//5
            mServices.put(data.token, service);//6
            
            .....
        } catch (Exception e) {
            if (!mInstrumentation.onException(service, e)) {
                throw new RuntimeException(
                    "Unable to create service " + data.info.name
                    + ": " + e.toString(), e);
            }
        }
    }
```
- 由以上以上源码，handleCreateService则为正在创建Service组件的方法，主要分为以下几步：

1. 注释1处获取了应用程序的描述对象LoadedApk
2. 注释2处则首先通过第一步获取应用程序的描述对象packageInfo获取应该程序的类加载器，然后通过类加载器根据前面获取的CreateServiceData对象保存的Service组件信息创建了Service组件的实例
3. 看到注释3处源码，这里根据应用程序的描述对象packageInfo来创建了ContextImpl对象，也就是Service组件的上下文，，并在注释4处将与Service组件进行关联，这样创建的Service组件就能够通过ContextImpl来获取应用程序的对应资源啦，这时候回头看看本文开始的分析，会有一种豁然开朗的感觉，Service也继承ContextWrapper，而ContextWrapper的功能都基于ContextImpl，而ContextImpl则通过LoadedApk来获取对应的应用程序资源，同时也表明Service也是一个Context。
4. 接着注释5处调用了Service的onCreate方法，这样开发者就可以在Service的onCreate方法进行一些自定义的初始化工作啦，最后注释6将实例化好的Service组件保存到ActivityThread的mServices中，它是ArrayMap对象。

### AMS与ActivityThread通信启动Service过程时序图
- 本小节最后还是通过时序图来对上面的步骤进行回顾

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/Service/startService/ActivityThread%E5%90%AF%E5%8A%A8Service%E7%BB%84%E4%BB%B6(startService).jpg)

### 参考
- 书籍《Android 系统情景源代码分析》第三版
- [Android 10 源码地址](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:)
