# 深入理解Android之 ContentProvider
> 内容提供程序有助于应用管理其自身和其他应用所存储数据的访问，并提供与其他应用共享数据的方法。它们会封装数据，并提供用于定义数据安全性的机制。内容提供程序是一种标准接口，可将一个进程中的数据与另一个进程中运行的代码进行连。实现内容提供程序大有好处。最重要的是，通过配置内容提供程序，您可以使其他应用安全地访问和修改您的应用数据(摘自官方文档)。本文源码基于 [Android 10](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:) 

## ContentProvider 知识回顾

![ContentProvider 知识回顾](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/ContentProvider/ContentProvider.png)

## ContentProvider 的启动

- 本文不会去讨论ContentProvider如何使用。在应用程序中使用 getContentResolver.query 来查询内容提供者数据，直接分析背后的逻辑。

###  getContentResolver.query方法调用到AMS
>本文 AMS 指代 ActivityManagerService
- 源码分析从getContentResolver.query开始。首先要搞清楚getContentResolver获取的是啥。要获取ContentProvider的数据，直接使用Context的getContentResolver方法，毫无疑问，该方法的最终实现在ContextImpl中。

>frameworks/base/core/java/android/app/ContextImpl.java

```
 @Override
    public ContentResolver getContentResolver() {
        return mContentResolver;
    }
```
- 由以上源码只是返回了mContentResolver，它对于的是ApplicationContentResolver对象，该对象创建在ContextImpl构造方法中
>frameworks/base/core/java/android/app/ContextImpl.java

```
 private ContextImpl(@Nullable ContextImpl container, @NonNull ActivityThread mainThread,
            @NonNull LoadedApk packageInfo, @Nullable String splitName,
            @Nullable IBinder activityToken, @Nullable UserHandle user, int flags,
            @Nullable ClassLoader classLoader, @Nullable String overrideOpPackageName) {
       .......
        mContentResolver = new ApplicationContentResolver(this, mainThread);
    }


private static final class ApplicationContentResolver extends ContentResolver {
     .....
}
```

- 由以上源码，可以清楚了解到getContentResolver方法最终获取的是继承了ContentResolver类的ApplicationContentResolver对象，而这个对象创建在应用程序进程启动的过程就已经创建了，并且持有**ActivityThread**对象（ContextImpl构造方法）。ContentResolver类为应用程序提供了对内容模型的访问。所以query方法实际为ContentResolver类的query方法。

> frameworks/base/core/java/android/content/ContentResolver.java
    
```
public final @Nullable Cursor query(final @RequiresPermission.Read @NonNull Uri uri,
            @Nullable String[] projection, @Nullable Bundle queryArgs,
            @Nullable CancellationSignal cancellationSignal) {
        Preconditions.checkNotNull(uri, "uri");

        ......

        IContentProvider unstableProvider = acquireUnstableProvider(uri);//1
        if (unstableProvider == null) {
            return null;
        }
        .....
}

 public static final String SCHEME_CONTENT = "content";

/**
     * Returns the content provider for the given content URI.
     *
     * @param uri The URI to a content provider
     * @return The ContentProvider for the given URI, or null if no content provider is found.
     * @hide
     */
    public final IContentProvider acquireUnstableProvider(Uri uri) {
        if (!SCHEME_CONTENT.equals(uri.getScheme())) {//2
            return null;
        }
        String auth = uri.getAuthority();
        if (auth != null) {
            return acquireUnstableProvider(mContext, uri.getAuthority());//3
        }
        return null;
    }

/**
 * The ipc interface to talk to a content provider.
 * @hide
 */
public interface IContentProvider extends IInterface {
    ......
}


```
1. 通过以上源码，注释1处调用的acquireUnstableProvider方法最终调用的是ContentResolver的抽象方法acquireUnstableProvider(注释3处)，他的实现在ApplicationContentResolver类
2. 注释2处会判断传入的 uri是否包含**content**，表明当前请求是否是访问ContentProvider
3. acquireUnstableProvider要返回的是**IContentProvider**对象，看到IInterface就可以明白它就是AIDL接口，可实现进程间程通信，也就是Binder通信。接着看到ApplicationContentResolver的acquireUnstableProvider方法

> frameworks/base/core/java/android/app/ContextImpl.java
```
private static final class ApplicationContentResolver extends ContentResolver {
        @UnsupportedAppUsage
        private final ActivityThread mMainThread;

        ......
        @Override
        protected IContentProvider acquireUnstableProvider(Context c, String auth) {
            return mMainThread.acquireProvider(c,
                    ContentProvider.getAuthorityWithoutUserId(auth),
                    resolveUserIdFromAuthority(auth), false);//1
        }

        ......
    }
```
- 由以上ApplicationContentResolver的acquireUnstableProvider方法，没啥好说的调用了ActivityThread的acquireProvider方法，接着往下看

> frameworks/base/core/java/android/app/ActivityThread.java
```
@UnsupportedAppUsage
    public final IContentProvider acquireProvider(
            Context c, String auth, int userId, boolean stable) {
        final IContentProvider provider = acquireExistingProvider(c, auth, userId, stable); //1
        if (provider != null) {
            return provider;
        }
        .......
        ContentProviderHolder holder = null;
        try {
            synchronized (getGetProviderLock(auth, userId)) {
                holder = ActivityManager.getService().getContentProvider(
                        getApplicationThread(), c.getOpPackageName(), auth, userId, stable);//2
            }
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
        
        .......

        // Install provider will increment the reference count for us, and break
        // any ties in the race.
        holder = installProvider(c, holder, holder.info,
                true /*noisy*/, holder.noReleaseNeeded, stable);//3
        return holder.provider;
    }
```
- 由以上主线程管理类ActivityThread的acquireProvider方法源码
1. 注释1处会根据 auth 和 userId组成的key 在 ActivityThread 类的ArrayMap类型的**mProviderMap** 中查找是否已经有对应IContentProvider，也就是ContentProvider是否被当前进程访问过，创建则直接返回IContentProvider
2. 注释2处当前进程没有访问过目标ContentProvider则会调用AMS的getContentProvider来进一步处理，同样的也传入了**ApplicationThread**，方便后续通信，稍后再细说。
3. 注释2处如果经过AMS处理之后ContentProvider代理对象为ContentProviderHolder并返回IContentProvider对象（ContentProviderHolder说白了就是一个可以进程间传输的bean对象，继承   Parcelable 接口，它包含了IContentProvider对象，ContentProvider在清单文件注册信息ProviderInfo 等）

### Activity调用ContentProvider到AMS时序图

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/ContentProvider/Activity%E8%B0%83%E7%94%A8ContentProvider%E5%88%B0AMS%E6%97%B6%E5%BA%8F%E5%9B%BE.jpg)


- 接下来继续看到AMS的getContentProvider方法

> frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java
```
  @Override
    public final ContentProviderHolder getContentProvider(
            IApplicationThread caller, String callingPackage, String name, int userId,
            boolean stable) {
        .....
        
        final int callingUid = Binder.getCallingUid();
        .....
        if (callingPackage != null && 
        return getContentProviderImpl(caller, name, null, callingUid, callingPackage,
                null, stable, userId);//1
    }
```
- 由以上源码，只是获取了调用进程的uid，接着继续调用getContentProviderImpl方法，返回的正是ContentProvider代理对象ContentProviderHolder；该方法比较长，接下来分块了解


### 应用程序当前进程运行ContentProvider

> frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java

```
private ContentProviderHolder getContentProviderImpl(IApplicationThread caller,
            String name, IBinder token, int callingUid, String callingPackage, String callingTag,
            boolean stable, int userId) {
        ContentProviderRecord cpr; //1
        ContentProviderConnection conn = null; //2
        ProviderInfo cpi = null; //3
        boolean providerRunning = false; 
        
        .....
        // First check if this content provider has been published...
            cpr = mProviderMap.getProviderByName(name, userId);//4
        
        if (providerRunning) {
                cpi = cpr.info;
                String msg;

                if (r != null && cpr.canRunHere(r)) {//5
                    ......
                    // This provider has been published or is in the process
                    // of being published...  but it is also allowed to run
                    // in the caller's process, so don't make a connection
                    // and just let the caller instantiate its own instance.
                    ContentProviderHolder holder = cpr.newHolder(null); //5
                    // don't give caller the provider object, it needs
                    // to make its own.
                    holder.provider = null;
                    return holder;
                }
        .....
}                
```
- 如上所示，首先明确注释1、2、3变量含义

 1. ContentProviderRecord 代表一个ContentProvider在AMS中的描述，包含 ContentProvider 相关信息；ContentProviderConnection是个Binder对象，表示AMS和客户端之间的连接描述，客户端指示App 进程，也可以指代provider启动运行后的进程；ProviderInfo则代表ContentProvider在清单文件注册的信息，比如provider的唯一标识符authorities等；

 2. 注释4处mProviderMap(ProviderMap)中保存是ContentProvider相关信息，通过名称和userId为key来获取对应的ContentProviderRecord，主要 name 对应的是 ContentProvider 的清单文件注册唯一表示信息 android:authorities
 
 3. 注释5处canRunHere函数会检查当前 ContentProvider 清单注册文件 multiprocess 属性是否在访问它的应用程序进程启动，接着注释6
 
 4. 注释6处，AMS运行到此，说明要获取的ContentProvider已经发布过了或者正在发布，并且它可以运行在调用者的进程，也就是App进程，直接返回ContentProviderHolder 让App进程去实例化ContentProvider，调用的是**ContentProviderRecord的newHolder方法**

- 接着回到ActivityThread的acquireProvider方法
> frameworks/base/core/java/android/app/ActivityThread.java
```
@UnsupportedAppUsage
    public final IContentProvider acquireProvider(
            Context c, String auth, int userId, boolean stable) {
        .......
        
        ContentProviderHolder holder = null;
        try {
            synchronized (getGetProviderLock(auth, userId)) {
                holder = ActivityManager.getService().getContentProvider(
                        getApplicationThread(), c.getOpPackageName(), auth, userId, stable);
            }
        .....

        // Install provider will increment the reference count for us, and break
        // any ties in the race.
        holder = installProvider(c, holder, holder.info,
                true /*noisy*/, holder.noReleaseNeeded, stable);//1
        return holder.provider;
    }
```
- 由前面分析，结合以上源码，AMS立即返回了ContentProviderHolder对象，该对象是通过ContentProviderRecord的newHolder方法获得的，也就包含ContentProvider的信息，接着调用installProvider来实例化ContentProvider，该方法留到后文在继续分析。


### 新建进程启动ContentProvider

- 上一小节只是大概分析了本地ContentProvider启动过程，而如果ContentProvider在新的进程启动又是怎样一个过程呢？接着回到 AMS 的 getContentProviderImpl 方法
> frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java

```
private ContentProviderHolder getContentProviderImpl(IApplicationThread caller,
            String name, IBinder token, int callingUid, String callingPackage, String callingTag,
            boolean stable, int userId) {
        ContentProviderRecord cpr; 
        ContentProviderConnection conn = null; 
        ProviderInfo cpi = null;       
    ........
    if (!providerRunning) {
                try {
                   .....
                    cpi = AppGlobals.getPackageManager().
                        resolveContentProvider(name,
                            STOCK_PM_FLAGS | PackageManager.GET_URI_PERMISSION_PATTERNS, userId);//1
                    .....
                } catch (RemoteException ex) {
                }
                ......
                ComponentName comp = new ComponentName(cpi.packageName, cpi.name);
                .......
                cpr = mProviderMap.getProviderByClass(comp, userId);
                .......
                final boolean firstClass = cpr == null;
                if (firstClass) {
                   ......
                        cpr = new ContentProviderRecord(this, cpi, ai, comp, singleton);//2
                    } catch (RemoteException ex) {
                       .....
                }
                
                final int N = mLaunchingProviders.size();//3
                int i;
                for (i = 0; i < N; i++) {
                    if (mLaunchingProviders.get(i) == cpr) {4
                        break;
                    }
                }
                
                // If the provider is not already being launched, then get it
                // started.
                if (i >= N) { 
                   
                    try {
                        // Content provider is now in use, its package can't be stopped.
                        ......

                        // Use existing process if already started
                        ......
                        ProcessRecord proc = getProcessRecordLocked(
                                cpi.processName, cpr.appInfo.uid, false);
                        if (proc != null && proc.thread != null && !proc.killed) {
                            .....
                            if (!proc.pubProviders.containsKey(cpi.name)) {
                               
                                proc.pubProviders.put(cpi.name, cpr);
                                try {
                                    proc.thread.scheduleInstallProvider(cpi);//5
                                } catch (RemoteException e) {
                                }
                            }
                        } else {
                           .....
                            proc = startProcessLocked(cpi.processName,
                                    cpr.appInfo, false, 0,
                                    new HostingRecord("content provider",
                                    new ComponentName(cpi.applicationInfo.packageName,
                                            cpi.name)), false, false, false);//6
                            .......
                            }
                        }
                        cpr.launchingApp = proc;
                        mLaunchingProviders.add(cpr);//7
                    
                    .......
                    
                    if (firstClass) {
                    mProviderMap.putProviderByClass(comp, cpr);//8
                }

                mProviderMap.putProviderByName(name, cpr);
                conn = incProviderCountLocked(r, cpr, token, callingUid, callingPackage, callingTag,
                        stable);//9
                        
                        
                synchronized (cpr) {
            while (cpr.provider == null) {
               .......
                try {
                    .....
                    cpr.wait(wait); //10
                   ....
                } catch (InterruptedException ex) {
                } 
                ....
            }
        }        
                
            return cpr.newHolder(conn);    
                        
}
```
1. 由以上源码，注释1处通过PMS（PackageManagerService）获取ContentProvider在清单文件注册信息，注释2处新建了ContentProviderRecord来在AMS描述ContentProvider对象
2. 结合注释3和注释4 如果ContentProvider已经启动了，则不会再继续执行后面的逻辑
3. 如果ContentProvider需要运行的进程已经启动，则注释5处会调用该进程ActivityThread 的 scheduleInstallProvider 方法，否则调用AMS的 startProcessLocked 来启动 ContentProvider 需要运行的应用程序进程。当前分析第一次启动，肯定是走注释6来启动应用程序进程，由我前面的分析文章 [深入理解Android之应用程序进程启动流程](https://juejin.im/post/5e8de292e51d4546e64c6646)，应用程序进程启动后会首先执行 **ActivityThread 的 main方法**。
4. 注释7处将正在启动的 ContentProvider 描述 ContentProviderRecord 保存到 mLaunchingProviders 
5. 最后注释8和9如果第一次运行则将其保存，并构造新启动的provider与AMS自己连接的描述对象ContentProviderConnection，最后返回ContentProviderHolder。
6. 注释10处使用一个while循环等待应用程序进程启动，实际就是等待进程启动之后启动运行其中的ContentProvider启动，而后AMS将对应ContentProviderRecord 描述生成对象**ContentProviderHolder**返回。 

- 接下来继续看到在新的应用程序进程ContentProvider是如何启动的，继续看到 ActivityThread 的 main 方法。

> frameworks/base/core/java/android/app/ActivityThread.java
```
 public static void main(String[] args) {
       ......
        ActivityThread thread = new ActivityThread();
        thread.attach(false, startSeq);
       ......
    }
```
- main 方法中只关注这两行代码，一个新建了ActivityThread对象，其次是调用了它的attach方法

> frameworks/base/core/java/android/app/ActivityThread.java
```
@UnsupportedAppUsage
    private void attach(boolean system, long startSeq) {
         .....
            final IActivityManager mgr = ActivityManager.getService();
            try {
                mgr.attachApplication(mAppThread, startSeq);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
          .....
    }
```
- attach 方也不用关注过多的逻辑，这里直接有调用了 AMS 的 attachApplication 的方法，并传入当前 provider 运行进程的 **ApplicationThread**，接着往下看

> frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java

```
    @Override
    public final void attachApplication(IApplicationThread thread, long startSeq) {
        synchronized (this) {
            .....
            attachApplicationLocked(thread, callingPid, callingUid, startSeq);
            .....
        }
    }
    
     @GuardedBy("this")
    private final boolean attachApplicationLocked(IApplicationThread thread,
            int pid, int callingUid, long startSeq) {
           .....
           
           List<ProviderInfo> providers = normalMode ? generateApplicationProvidersLocked(app) : null;//1
           .....
           thread.bindApplication(processName, appInfo, providers, null, profilerInfo,
                        null, null, null, testMode,
                        mBinderTransactionTrackingEnabled, enableTrackAllocation,
                        isRestrictedBackupMode || !normalMode, app.isPersistent(),
                        new Configuration(app.getWindowProcessController().getConfiguration()),
                        app.compat, getCommonServicesLocked(app.isolated),
                        mCoreSettingsObserver.getCoreSettingsLocked(),
                        buildSerial, autofillOptions, contentCaptureOptions);//2
           
           .....
                
        }
```
> frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java

```
// =========================================================
// CONTENT PROVIDERS
// =========================================================

    private final List<ProviderInfo> generateApplicationProvidersLocked(ProcessRecord app) {
        List<ProviderInfo> providers = null;
        try {
            providers = AppGlobals.getPackageManager()
                    .queryContentProviders(app.processName, app.uid,
                            STOCK_PM_FLAGS | PackageManager.GET_URI_PERMISSION_PATTERNS
                                    | MATCH_DEBUG_TRIAGED_MISSING, /*metadastaKey=*/ null)
                    .getList();
        } catch (RemoteException ex) {
        }
        .....
        int userId = app.userId;
        if (providers != null) {
            int N = providers.size();
            app.pubProviders.ensureCapacity(N + app.pubProviders.size());
            for (int i=0; i<N; i++) {
                // TODO: keep logic in sync with installEncryptionUnawareProviders
                ProviderInfo cpi =
                    (ProviderInfo)providers.get(i);
               .......

                ComponentName comp = new ComponentName(cpi.packageName, cpi.name);
                ContentProviderRecord cpr = mProviderMap.getProviderByClass(comp, userId);
                if (cpr == null) {
                    cpr = new ContentProviderRecord(this, cpi, app.info, comp, singleton);
                    mProviderMap.putProviderByClass(comp, cpr);
                }
                
                app.pubProviders.put(cpi.name, cpr);
                if (!cpi.multiprocess || !"android".equals(cpi.packageName)) {
                    // Don't add this if it is a platform component that is marked
                    // to run in multiple processes, because this is actually
                    // part of the framework so doesn't make sense to track as a
                    // separate apk in the process.
                    app.addPackage(cpi.applicationInfo.packageName,
                            cpi.applicationInfo.longVersionCode, mProcessStats);
                }
                notifyPackageUse(cpi.applicationInfo.packageName,
                                 PackageManager.NOTIFY_PACKAGE_USE_CONTENT_PROVIDER);
            }
        }
        return providers;
    }
```

- 由以上源码，AMS 的 attachApplication又调用了 attachApplicationLocked 方法，
1. 注释1处generateApplicationProvidersLocked如上所示，首先由PMS获取ContentProvider组件信息并检查是否已经创建ContentProviderRecord，并放入集以类名为key的 mProviderMap中，一个应用程序进程启动时会将该进程所有的 ContenProvider 组件启动起来。
2. 回到AMS的 attachApplicationLocked 方法，接着AMS通过 进程的 ApplicationThread 调用到对应进程的 ActivityThread 的 bindApplication 方法
- frameworks/base/core/java/android/app/ActivityThread.java
```
 public final void bindApplication(String processName, ApplicationInfo appInfo,
                List<ProviderInfo> providers, ComponentName instrumentationName,
                ProfilerInfo profilerInfo, Bundle instrumentationArgs,
                IInstrumentationWatcher instrumentationWatcher,
                IUiAutomationConnection instrumentationUiConnection, int debugMode,
                boolean enableBinderTracking, boolean trackAllocation,
                boolean isRestrictedBackupMode, boolean persistent, Configuration config,
                CompatibilityInfo compatInfo, Map services, Bundle coreSettings,
                String buildSerial, AutofillOptions autofillOptions,
                ContentCaptureOptions contentCaptureOptions) {
           ......

            AppBindData data = new AppBindData();
            data.processName = processName;
            data.appInfo = appInfo;
            data.providers = providers;
            data.instrumentationName = instrumentationName;
            data.instrumentationArgs = instrumentationArgs;
            data.instrumentationWatcher = instrumentationWatcher;
            data.instrumentationUiAutomationConnection = instrumentationUiConnection;
            data.debugMode = debugMode;
            data.enableBinderTracking = enableBinderTracking;
            data.trackAllocation = trackAllocation;
            data.restrictedBackupMode = isRestrictedBackupMode;
            data.persistent = persistent;
            data.config = config;
            data.compatInfo = compatInfo;
            data.initProfilerInfo = profilerInfo;
            data.buildSerial = buildSerial;
            data.autofillOptions = autofillOptions;
            data.contentCaptureOptions = contentCaptureOptions;
            sendMessage(H.BIND_APPLICATION, data);//1 
        }
```
- AppBindData 是ActivityThread的静态内部类，由以上 bindApplication 方法源码注释1处调用了主线程 Handler 发送消息，将接下来的逻辑运行在线程，并且将 AppBindData 数据绑定的消息BIND_APPLICATION发送到主线Handler的消息队列中处理。

> frameworks/base/core/java/android/app/ActivityThread.java

```
class H extends Handler {
    
       .....
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BIND_APPLICATION:
                    .....
                    AppBindData data = (AppBindData)msg.obj;
                    handleBindApplication(data);
                    .....
                    break;}
                    .......
        }
    }
```
- 显然，解析出 AppBindData 的数据后继续调用handleBindApplication方法
> frameworks/base/core/java/android/app/ActivityThread.java

```
 private void handleBindApplication(AppBindData data) {
 .....
        Application app;
        ......
        try {
            // If the app is being launched for full backup or restore, bring it up in
            // a restricted environment with the base application class.
            app = data.info.makeApplication(data.restrictedBackupMode, null); //1

            

            // don't bring up providers in restricted mode; they may depend on the
            // app's custom Application class
            if (!data.restrictedBackupMode) {
                if (!ArrayUtils.isEmpty(data.providers)) {
                    installContentProviders(app, data.providers);//2
                }
            }

            // Do this after providers, since instrumentation tests generally start their
            // test thread at this point, and we don't want that racing.
            ......
            try {
                mInstrumentation.callApplicationOnCreate(app);//3
            } catch (Exception e) {
                
            }
        }         
 .....
```
- 由以上 handleBindApplication 方法源码
1. 注释1处创建应用程序进程的 Application
2. 注释2处，获取 AppBindData 中的 providers ，它保存了当前应用程序所需要启动的所有 ContentProvider 组件，接着调用了 ActivityThread 的 installContentProviders 方法。
> frameworks/base/core/java/android/app/ActivityThread.java

```
@UnsupportedAppUsage
    private void installContentProviders(
            Context context, List<ProviderInfo> providers) {
        final ArrayList<ContentProviderHolder> results = new ArrayList<>();

        for (ProviderInfo cpi : providers) {
            .......
            ContentProviderHolder cph = installProvider(context, null, cpi,
                    false /*noisy*/, true /*noReleaseNeeded*/, true /*stable*/);//1
            if (cph != null) {
                cph.noReleaseNeeded = true;
                results.add(cph);//2
            }
        }

        try {
            ActivityManager.getService().publishContentProviders(
                getApplicationThread(), results);//3
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }
```
- 由以上源码，注释1处遍历 providers 并调用 installProvider 方法来启动每个 ContentProvider 组件，接着注释2将包含启动 ContentProvider 组件信息 ContentProviderHolder 保存并在注释3处调用 AMS 的 publishContentProviders 的放传递给 AMS，方便 AMS 能够访问启动的  ContentProvider 组件。
- 接着看到 installProvider 方法逻辑

 > frameworks/base/core/java/android/app/ActivityThread.java
```
private ContentProviderHolder installProvider(Context context,
            ContentProviderHolder holder, ProviderInfo info,
            boolean noisy, boolean noReleaseNeeded, boolean stable) {

        ContentProvider localProvider = null;
        IContentProvider provider;
        if (holder == null || holder.provider == null) {//1
           .......
            Context c = null;
            ApplicationInfo ai = info.applicationInfo;
            if (context.getPackageName().equals(ai.packageName)) {
                c = context;
            }
           ......
            try {
                final java.lang.ClassLoader cl = c.getClassLoader();
                LoadedApk packageInfo = peekPackageInfo(ai.packageName, true);
                
                .......
                
                localProvider = packageInfo.getAppFactory()
                        .instantiateProvider(cl, info.name);//2
                provider = localProvider.getIContentProvider();//3
                ......
                // XXX Need to create the correct context for this provider.
                localProvider.attachInfo(c, info);//4
            } catch (java.lang.Exception e) {
                ......
            }
        }
        
        ......
        synchronized (mProviderMap) {
        IBinder jBinder = provider.asBinder();
        if (localProvider != null) {
        .......
        ProviderClientRecord pr = mLocalProvidersByName.get(cname);
              .......
                    holder = new ContentProviderHolder(info);
                    holder.provider = provider;
                    holder.noReleaseNeeded = true;
                    pr = installProviderAuthoritiesLocked(provider, localProvider, holder);
                   
                    mLocalProviders.put(jBinder, pr);//5
                    mLocalProvidersByName.put(cname, pr);
                }
                retHolder = pr.mHolder;
                
        ......
        }
    }        
```
> frameworks/base/core/java/android/content/ContentProvider.java

```
    private void attachInfo(Context context, ProviderInfo info, boolean testing) {
        mNoPerms = testing;
        mCallingPackage = new ThreadLocal<>();

        if (mContext == null) {
            mContext = context;
            if (context != null && mTransport != null) 
           ........
            ContentProvider.this.onCreate();
        }
    }

```


- 由以上installProvider方法源码

1. 注释1处 installProvider 方法被调用时传递就是 null ，所以注释2处通过ClassLoder反射创建了ContentProvider对象实例，并在注释3处获取了IContentProvider对象，注释4处则回调了ContentProvider的 **OnCreate** 方法(如上代码所示)，这样自定义 ContentProvider 组件初始化一下工作就可以在OnCreate方法执行，但是需要注意是运行主线程，避免耗时操作；

2. 注释5处则保存当前进程运行的 ContentProvider封装 的对象 ProviderClientRecord，注释3获取的 IContentProvider，之后会将其发布到 AMS 方便其它需要访问该 ContentProvider 组件调用。
3. 回看到 handleBindApplication 的 注释3 会回调 Application 的 OnCreate 方法
- 接着会看到 installContentProviders 方法注释3 将 ContentProvider 组件创建完成之后调用 AMS 的publishContentProviders 方法

> frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java
```
public final void publishContentProviders(IApplicationThread caller,
            List<ContentProviderHolder> providers) {
       ........
        synchronized (this) {
            final ProcessRecord r = getRecordForAppLocked(caller);
            ........

            final int N = providers.size();
            for (int i = 0; i < N; i++) {
                ContentProviderHolder src = providers.get(i);
               
                ContentProviderRecord dst = r.pubProviders.get(src.info.name);
                
                if (dst != null) {
                    ComponentName comp = new ComponentName(dst.info.packageName, dst.info.name);
                    mProviderMap.putProviderByClass(comp, dst);
                    String names[] = dst.info.authority.split(";");
                    for (int j = 0; j < names.length; j++) {
                        mProviderMap.putProviderByName(names[j], dst);
                    }

                    int launchingCount = mLaunchingProviders.size();
                    int j;
                    boolean wasInLaunchingProviders = false;
                    for (j = 0; j < launchingCount; j++) {
                        if (mLaunchingProviders.get(j) == dst) {
                            mLaunchingProviders.remove(j);
                            wasInLaunchingProviders = true;
                            j--;
                            launchingCount--;
                        }
                    }
                   
                    synchronized (dst) {
                        dst.provider = src.provider;//1
                        dst.setProcess(r);
                        dst.notifyAll();
                    }
                    ......
                }
            }

            Binder.restoreCallingIdentity(origId);
        }
    }
```
- 根据前面分析，mLaunchingProviders保存是正在启动中的 ContentProvider 组件，之前在AMS 被描述为 ContentProviderRecord，而启动完成的组件描述为ContentProviderHolder，根据前面的分析，启动组件进程过程正在等待，这是判断对应要启动provider组件则注释1处将启动好组件对应 IContentProvider 赋值，这样AMS 则将链接对应 IContentProvider的**ContentProviderHolder**对象 返回到调用者的这边，也就是 ActivityThread 的 acquireProvider 方法

> frameworks/base/core/java/android/app/ActivityThread.java
```
@UnsupportedAppUsage
    public final IContentProvider acquireProvider(
            Context c, String auth, int userId, boolean stable) {
        final IContentProvider provider = acquireExistingProvider(c, auth, userId, stable);
        .......
        ContentProviderHolder holder = null;
        try {
            synchronized (getGetProviderLock(auth, userId)) {
                holder = ActivityManager.getService().getContentProvider(
                        getApplicationThread(), c.getOpPackageName(), auth, userId, stable);//1
            }
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
        ......
        
        holder = installProvider(c, holder, holder.info,
                true /*noisy*/, holder.noReleaseNeeded, stable);//2
        return holder.provider;
    }
```
- 由以上源码注释1获取到了ContentProviderHolder，注释2处installProvider前面分析过，ContentProviderHolder不为空，所以IContentProvider也不为空，最后返回的也就是 IContentProvider 对象，也就是说调用的 query 方法也就是 IContentProvider 对象指向的对象的 query 方法，接着往下看
- 最后再来看看 IContentProvider 获取
>
```
public abstract class ContentProvider implements ContentInterface, ComponentCallbacks2 {
    .....

    private Transport mTransport = new Transport();


      /**
     * Binder object that deals with remoting.
     *
     * @hide
     */
    class Transport extends ContentProviderNative {
    volatile ContentInterface mInterface = ContentProvider.this;
        .......
         @Override
        public Cursor query(String callingPkg, Uri uri, @Nullable String[] projection,
                @Nullable Bundle queryArgs, @Nullable ICancellationSignal cancellationSignal) {
                ......
                // ContentProvider.this.query
                cursor = mInterface.query(
                            uri, projection, queryArgs,
                            CancellationSignal.fromTransport(cancellationSignal));
            .....
           }
           ......
    }
    .......
    @UnsupportedAppUsage
    public IContentProvider getIContentProvider() {
        return mTransport;
    }
```
- 到此应该很清晰了，在 ContentProvider 中 IContentProvider 指向的是 mTransport，它就是 ContentProvider 的 Binder 代理对象，显然调用者通过Binder机制调用到了ContentProvider.Transport 的 query方法，它内部调用了 ContentProvider 本身的 query 方法，最终执行结果返回也是通过Binder机制返回。这里也清楚了解到**通过 AMS 获取的不是 ContentProvider 本身，而是能够与它进程间通信的 IContentProvider 对象**。

- 剩余的 insert、delete、update 等方法基本也是一样的，这里就不再进行展开了。

### AMS 启动ContentProvider结合最终调用query方法时序图 

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/ContentProvider/AMS%E5%90%AF%E5%8A%A8ContentProvider%E6%97%B6%E5%BA%8F%E5%9B%BE.jpg)

### 回顾

- ContentProvider能实现进程间通信实际上是通过对 Binder 的封装
- ContentProvider启动过程的 OnCreate 方法调用会先于 Application方法的 OnCreate 方法调用
- getContentResolver.query 实际获取不是 ContentProvider 组件本身，而是支持 binder 通信的代理对象 IContentProvider
- ContentProvider 启动支持在当前进程启动，也支持新建一个进程启动
- 不管调用ContentProvider的哪个方法，ContentProvider没有启动都会先启动对应的内容提供者组件



