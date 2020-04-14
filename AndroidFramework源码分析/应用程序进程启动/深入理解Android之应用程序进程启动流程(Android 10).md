> 在上一篇文章[深入理解Android 之 Activity启动流程](https://juejin.im/post/5e8407d251882573be11b63c)中，应用程序根Activity的启动过程通过系统源码梳理了一遍，其中还有一个细节便是Android每个应用都是都是一个应用进程，而应用进程不会凭空产生，本文则通过系统源码继续探究Android应用程序进程启动流程（文中源码基于Android 10 ）。

- [Android源码地址](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:)

- 首先照例还是先看一个脑图，在自己大脑中能产生初步印象

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%B3%BB%E7%BB%9F%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F%E8%BF%9B%E7%A8%8B%E5%90%AF%E5%8A%A8%E8%BF%87%E7%A8%8B.png) 


## ActivityManagerService请求Zygote启动应用程序进程

### AMS处理启动应用程序基本数据

- 上一篇文章第二小节分析中提到了ActivityStackSupervisor的startSpecificActivityLocked的方法

> frameworks/base/services/core/java/com/android/server/wm/ActivityStackSupervisor.java

```
void startSpecificActivityLocked(ActivityRecord r, boolean andResume, boolean checkConfig) {
        // Activity应用程序进程是否已经准备好
        final WindowProcessController wpc =
                mService.getProcessController(r.processName, r.info.applicationInfo.uid);

        boolean knownToBeDead = false;
        if (wpc != null && wpc.hasThread()) { //1
            try {
                realStartActivityLocked(r, wpc, andResume, checkConfig); //2
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception when starting activity "
                        + r.intent.getComponent().flattenToShortString(), e);
            }
        .......
        }

        .......
        try {
           .......
            // Post message to start process to avoid possible deadlock of calling into AMS with the
            // ATMS lock held.  Hnadler发消息 避免死锁
            final Message msg = PooledLambda.obtainMessage(
                    ActivityManagerInternal::startProcess, mService.mAmInternal, r.processName,
                    r.info.applicationInfo, knownToBeDead, "activity", r.intent.getComponent());//3
            mService.mH.sendMessage(msg);
        }
        ........
    }
```
- 由以前源码注释1和2如果应用程序进程已经存在，则继续启动Activity逻辑，否则来到注释3出，我们看到关键部分ActivityManagerInternal::startProcess，"::"是Java 8 新增特性，相当于调用了ActivityManagerInternal的startProcess方法，而ActivityManagerInternal（frameworks/base/core/java/android/app/ActivityManagerInternal.java）是一个抽象类，它是Activity管理器本地服务接口，它的实现为AMS的内部类LocalService，它注册在AMS启动的过程，通过LocalServices（frameworks/base/core/java/com/android/server/LocalServices.java）注册，此类的使用方式与ServiceManager相似，不同之处在于，此处注册的服务不是Binder对象，并且只能在同一进程（SystemServer进程）中使用。也就是说**ActivityManagerInternal实现类LocalService是SystemServer进程的本地服务Service**，通过本地服务注册到LocalServices中，而AMS也是运行在SystemServer进程，则可以直接使用LocalService。所以注释3处调用了LocalService的startProcess方法

> frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java

```
 @Override
        public void startProcess(String processName, ApplicationInfo info,
                boolean knownToBeDead, String hostingType, ComponentName hostingName) {
            try {
                if (Trace.isTagEnabled(Trace.TRACE_TAG_ACTIVITY_MANAGER)) {
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "startProcess:"
                            + processName);
                }
                synchronized (ActivityManagerService.this) {
                    startProcessLocked(processName, info, knownToBeDead, 0 /* intentFlags */,
                            new HostingRecord(hostingType, hostingName),
                            false /* allowWhileBooting */, false /* isolated */,
                            true /* keepIfLarge */);//
                }
            } 
            .......
        }
        
final ProcessRecord startProcessLocked(String processName,
            ApplicationInfo info, boolean knownToBeDead, int intentFlags,
            HostingRecord hostingRecord, boolean allowWhileBooting,
            boolean isolated, boolean keepIfLarge) {
        return mProcessList.startProcessLocked(processName, info, knownToBeDead, intentFlags,
                hostingRecord, allowWhileBooting, isolated, 0 /* isolatedUid */, keepIfLarge,
                null /* ABI override */, null /* entryPoint */, null /* entryPointArgs */,
                null /* crashHandler */); //2 
    }        

```
- 由以上源码注释1处调用了startProcessLocked方法，由注释2处则调用了 ProcessList的 startProcessLocked方法，ProcessList是处理Activity进程的管理类，接着往下看
>frameworks/base/services/core/java/com/android/server/am/ProcessList.java
```
@GuardedBy("mService")
    final ProcessRecord startProcessLocked(String processName, ApplicationInfo info,
            boolean knownToBeDead, int intentFlags, HostingRecord hostingRecord,
            boolean allowWhileBooting, boolean isolated, int isolatedUid, boolean keepIfLarge,
            String abiOverride, String entryPoint, String[] entryPointArgs, Runnable crashHandler) {
        long startTime = SystemClock.elapsedRealtime();
        ProcessRecord app;
        ..........

        if (app == null) {
            checkSlow(startTime, "startProcess: creating new process record");
            app = newProcessRecordLocked(info, processName, isolated, isolatedUid, hostingRecord);//1
            if (app == null) {
                Slog.w(TAG, "Failed making new process record for "
                        + processName + "/" + info.uid + " isolated=" + isolated);
                return null;
            }
            app.crashHandler = crashHandler;
            app.isolatedEntryPoint = entryPoint;
            app.isolatedEntryPointArgs = entryPointArgs;
            checkSlow(startTime, "startProcess: done creating new process record");
        }

        .....
        
        final boolean success = startProcessLocked(app, hostingRecord, abiOverride);//2
        checkSlow(startTime, "startProcess: done starting proc!");
        return success ? app : null;
    }
```
- 由以上代码，注释1处创建了ProcessRecord对象，它保存了当前正在运行的特定进程的完整信息，也就是需要启动的应用程序进程，接着注释2继续调用startProcessLocked方法
>frameworks/base/services/core/java/com/android/server/am/ProcessList.java
 
```
 /**
     * @return {@code true} if process start is successful, false otherwise.
     * @param app
     * @param hostingRecord
     * @param disableHiddenApiChecks
     * @param abiOverride
     */
    @GuardedBy("mService")
    boolean startProcessLocked(ProcessRecord app, HostingRecord hostingRecord,
            boolean disableHiddenApiChecks, boolean mountExtStorageFull,
            String abiOverride) {
        
        try {
            
            ....
            int uid = app.uid;//1
            int[] gids = null;
            int mountExternal = Zygote.MOUNT_EXTERNAL_NONE;
            if (!app.isolated) {
               ......

                /*
                 *添加共享的应用程序和配置文件GID，以便应用程序可以共享 某些资源（如共享库）并访问用户范围的资源
                 */
                if (ArrayUtils.isEmpty(permGids)) {
                    gids = new int[3];
                } else {
                    gids = new int[permGids.length + 3];
                    System.arraycopy(permGids, 0, gids, 3, permGids.length);
                }
                gids[0] = UserHandle.getSharedAppGid(UserHandle.getAppId(uid));
                gids[1] = UserHandle.getCacheAppGid(UserHandle.getAppId(uid));
                gids[2] = UserHandle.getUserGid(UserHandle.getUserId(uid)); //2
                .......
            }
            ......
            final String entryPoint = "android.app.ActivityThread";//3

            return startProcessLocked(hostingRecord, entryPoint, app, uid, gids,
                    runtimeFlags, mountExternal, seInfo, requiredAbi, instructionSet, invokeWith,
                    startTime);//4
        } catch (RuntimeException e) {
           .......
        }
    }
    
    
    boolean startProcessLocked(HostingRecord hostingRecord,
            String entryPoint,
            ProcessRecord app, int uid, int[] gids, int runtimeFlags, int mountExternal,
            String seInfo, String requiredAbi, String instructionSet, String invokeWith,
            long startTime) {
        
        .......

        if (mService.mConstants.FLAG_PROCESS_START_ASYNC) {
            if (DEBUG_PROCESSES) Slog.i(TAG_PROCESSES,
                    "Posting procStart msg for " + app.toShortString());
            mService.mProcStartHandler.post(() -> {
                try {
                    final Process.ProcessStartResult startResult = startProcess(app.hostingRecord,
                            entryPoint, app, app.startUid, gids, runtimeFlags, mountExternal,
                            app.seInfo, requiredAbi, instructionSet, invokeWith, app.startTime);//5
                    synchronized (mService) {
                        handleProcessStartedLocked(app, startResult, startSeq);
                    }
                } catch (RuntimeException e) {
                   .....
            });
            return true;
        } else {
            try {
                final Process.ProcessStartResult startResult = startProcess(hostingRecord,
                        entryPoint, app,
                        uid, gids, runtimeFlags, mountExternal, seInfo, requiredAbi, instructionSet,
                        invokeWith, startTime);//6
                handleProcessStartedLocked(app, startResult.pid, startResult.usingWrapper,
                        startSeq, false);
            } catch (RuntimeException e) {
              ........  
            }
            return app.pid > 0;
        }
    }
```
- 由以上代码注释1处获取了应用程序的用户id，注释2出获取了用户组ID，为应用程序进程提供用户组资源访问权限，注释3处给entryPoint赋值为**android.app.ActivityThread**，先记住这个值，后续分析还会继续提起它；注释4处接着调用了startProcessLocked方法，在该方法中注释5和6最终都是调用了ProcessList类的startProcess方法，接着往下看

> frameworks/base/services/core/java/com/android/server/am/ProcessList.java

```

/**
     * The currently running application zygotes.
     */
    final ProcessMap<AppZygote> mAppZygotes = new ProcessMap<AppZygote>();

    /**
     * The processes that are forked off an application zygote.
     */
    final ArrayMap<AppZygote, ArrayList<ProcessRecord>> mAppZygoteProcesses =
            new ArrayMap<AppZygote, ArrayList<ProcessRecord>>();

 private Process.ProcessStartResult startProcess(HostingRecord hostingRecord, String entryPoint,
            ProcessRecord app, int uid, int[] gids, int runtimeFlags, int mountExternal,
            String seInfo, String requiredAbi, String instructionSet, String invokeWith,
            long startTime) {
        try {
           .......
            final Process.ProcessStartResult startResult;
            if (hostingRecord.usesWebviewZygote()) {
                startResult = startWebView(entryPoint,
                        app.processName, uid, uid, gids, runtimeFlags, mountExternal,
                        app.info.targetSdkVersion, seInfo, requiredAbi, instructionSet,
                        app.info.dataDir, null, app.info.packageName,
                        new String[] {PROC_START_SEQ_IDENT + app.startSeq});//1
            } else if (hostingRecord.usesAppZygote()) {
                final AppZygote appZygote = createAppZygoteForProcessIfNeeded(app);

                startResult = appZygote.getProcess().start(entryPoint,
                        app.processName, uid, uid, gids, runtimeFlags, mountExternal,
                        app.info.targetSdkVersion, seInfo, requiredAbi, instructionSet,
                        app.info.dataDir, null, app.info.packageName,
                        /*useUsapPool=*/ false,
                        new String[] {PROC_START_SEQ_IDENT + app.startSeq});//2
            } else {
                startResult = Process.start(entryPoint,
                        app.processName, uid, uid, gids, runtimeFlags, mountExternal,
                        app.info.targetSdkVersion, seInfo, requiredAbi, instructionSet,
                        app.info.dataDir, invokeWith, app.info.packageName,
                        new String[] {PROC_START_SEQ_IDENT + app.startSeq});//3
            }
            ....
            return startResult;
        } 
        ......
    }

```
- 由以上代码，startProcess方法会根据当前启动的是什么进程处理不同的逻辑，注释1是启动webview进程，注释2处理用户AppZygote，而注释3处才是出路新创建的应用程序进程，也就是调用了Process类的start方法，Process是
用于管理操作系统进程的工具类，接着看它的start方法

### Process 处理启动参数到Zygote进程

>frameworks/base/core/java/android/os/Process.java

```
/**
     * State associated with the zygote process.
     * @hide
     */
    public static final ZygoteProcess ZYGOTE_PROCESS = new ZygoteProcess(); //1

    public static ProcessStartResult start(@NonNull final String processClass,
                                           @Nullable final String niceName,
                                           int uid, int gid, @Nullable int[] gids,
                                           int runtimeFlags,
                                           int mountExternal,
                                           int targetSdkVersion,
                                           @Nullable String seInfo,
                                           @NonNull String abi,
                                           @Nullable String instructionSet,
                                           @Nullable String appDataDir,
                                           @Nullable String invokeWith,
                                           @Nullable String packageName,
                                           @Nullable String[] zygoteArgs) {
        return ZYGOTE_PROCESS.start(processClass, niceName, uid, gid, gids,
                    runtimeFlags, mountExternal, targetSdkVersion, seInfo,
                    abi, instructionSet, appDataDir, invokeWith, packageName,
                    /*useUsapPool=*/ true, zygoteArgs);//2
    }

```
- 由以上代码，结合注释1和注释2则调用了ZygoteProcess类的start方法，ZygoteProcess保持与Zygote 的通信状态。 此类负责对Zygote 打开套接字，并代表Porcess启动进程。接着看到ZygoteProcess类的start方法

>frameworks/base/core/java/android/os/ZygoteProcess.java

```
 public final Process.ProcessStartResult start(@NonNull final String processClass,
                                                  final String niceName,
                                                  int uid, int gid, @Nullable int[] gids,
                                                  int runtimeFlags, int mountExternal,
                                                  int targetSdkVersion,
                                                  @Nullable String seInfo,
                                                  @NonNull String abi,
                                                  @Nullable String instructionSet,
                                                  @Nullable String appDataDir,
                                                  @Nullable String invokeWith,
                                                  @Nullable String packageName,
                                                  boolean useUsapPool,
                                                  @Nullable String[] zygoteArgs) {
        .....
        try {
            return startViaZygote(processClass, niceName, uid, gid, gids,
                    runtimeFlags, mountExternal, targetSdkVersion, seInfo,
                    abi, instructionSet, appDataDir, invokeWith, /*startChildZygote=*/ false,
                    packageName, useUsapPool, zygoteArgs);//1
        }
        .....
    }
```
- 由以上代码注释1，又调用了ZygoteProcess类的start方法，接着往下看
> frameworks/base/core/java/android/os/ZygoteProcess.java

```
private Process.ProcessStartResult startViaZygote(@NonNull final String processClass,
                                                      @Nullable final String niceName,
                                                      final int uid, final int gid,
                                                      @Nullable final int[] gids,
                                                      int runtimeFlags, int mountExternal,
                                                      int targetSdkVersion,
                                                      @Nullable String seInfo,
                                                      @NonNull String abi,
                                                      @Nullable String instructionSet,
                                                      @Nullable String appDataDir,
                                                      @Nullable String invokeWith,
                                                      boolean startChildZygote,
                                                      @Nullable String packageName,
                                                      boolean useUsapPool,
                                                      @Nullable String[] extraArgs)
                                                      throws ZygoteStartFailedEx {
        ArrayList<String> argsForZygote = new ArrayList<>();//1

        // --runtime-args, --setuid=, --setgid=,
        // and --setgroups= must go first
        argsForZygote.add("--runtime-args");
        argsForZygote.add("--setuid=" + uid);
        argsForZygote.add("--setgid=" + gid);
        argsForZygote.add("--runtime-flags=" + runtimeFlags);
        if (mountExternal == Zygote.MOUNT_EXTERNAL_DEFAULT) {
            argsForZygote.add("--mount-external-default");
        } else if (mountExternal == Zygote.MOUNT_EXTERNAL_READ) {
            argsForZygote.add("--mount-external-read");
        } else if (mountExternal == Zygote.MOUNT_EXTERNAL_WRITE) {
            argsForZygote.add("--mount-external-write");
        } else if (mountExternal == Zygote.MOUNT_EXTERNAL_FULL) {
            argsForZygote.add("--mount-external-full");
        }

        .......
        synchronized(mLock) {
            // The USAP pool can not be used if the application will not use the systems graphics
            // driver.  If that driver is requested use the Zygote application start path.
            return zygoteSendArgsAndGetResult(openZygoteSocketIfNeeded(abi),
                                              useUsapPool,
                                              argsForZygote);//2
        }
    }
```
- 由以上代码，注释1处创建了一个集合argsForZygote用来保存启动应用程序进程参数，接着又调用了zygoteSendArgsAndGetResult方法，而该方法第一个参数又调用了openZygoteSocketIfNeeded方法
>frameworks/base/core/java/android/os/ZygoteProcess.java

```
private Process.ProcessStartResult zygoteSendArgsAndGetResult(
            ZygoteState zygoteState, boolean useUsapPool, @NonNull ArrayList<String> args)
            throws ZygoteStartFailedEx {
       .......

        if (useUsapPool && mUsapPoolEnabled && canAttemptUsap(args)) {
            try {
                return attemptUsapSendArgsAndGetResult(zygoteState, msgStr);//1
            } catch (IOException ex) {
                // If there was an IOException using the USAP pool we will log the error and
                // attempt to start the process through the Zygote.
                Log.e(LOG_TAG, "IO Exception while communicating with USAP pool - "
                        + ex.getMessage());
            }
        }

        return attemptZygoteSendArgsAndGetResult(zygoteState, msgStr);//2
    }
    
    private ZygoteState openZygoteSocketIfNeeded(String abi) throws ZygoteStartFailedEx {
        try {
            attemptConnectionToPrimaryZygote();//3

            if (primaryZygoteState.matches(abi)) {
                return primaryZygoteState;
            }

            if (mZygoteSecondarySocketAddress != null) {
                // The primary zygote didn't match. Try the secondary.
                attemptConnectionToSecondaryZygote();//4

                if (secondaryZygoteState.matches(abi)) {
                    return secondaryZygoteState;
                }
            }
        } catch (IOException ioe) {
            throw new ZygoteStartFailedEx("Error connecting to zygote", ioe);
        }

        throw new ZygoteStartFailedEx("Unsupported zygote ABI: " + abi);
    }
```
- 由以上代码，注释1处和注释2都传入了zygoteState，它代表的是ZygoteState类，它是ZygoteProcess静态内部类，保存了与 Zygote 进行通信的状态，而它的返回是由openZygoteSocketIfNeeded方法处理的，所以看到注释3和注释4分别调用了attemptConnectionToPrimaryZygote方法和attemptConnectionToSecondaryZygote方法，这和Zygote进程启动加载的配置文件有关，这里就不展开了，两个方法逻辑基本相同，直接看到其中一个attemptConnectionToPrimaryZygote方法
>frameworks/base/core/java/android/os/ZygoteProcess.java

```
 /**
     * Creates a ZygoteState for the primary zygote if it doesn't exist or has been disconnected.
     */
    @GuardedBy("mLock")
    private void attemptConnectionToPrimaryZygote() throws IOException {
        if (primaryZygoteState == null || primaryZygoteState.isClosed()) {
            primaryZygoteState =
                    ZygoteState.connect(mZygoteSocketAddress, mUsapPoolSocketAddress);//1

            maybeSetApiBlacklistExemptions(primaryZygoteState, false);
            maybeSetHiddenApiAccessLogSampleRate(primaryZygoteState);
            maybeSetHiddenApiAccessStatslogSampleRate(primaryZygoteState);
        }
    }
```
- 由注释1调用ZygoteState的connect方法来打开Socket连接，mZygoteSocketAddress则是**名称为zygote的 Socket 服务**
 
### ActivityManagerService请求Zygote启动应用程序进程时序图

- 本小节最后还是通过时序图来对上面的步骤进行回顾

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%B3%BB%E7%BB%9F%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/AMS%E8%AF%B7%E6%B1%82Zygote%E5%90%AF%E5%8A%A8%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F%E8%BF%9B%E7%A8%8B.jpg)

## Zygote启动应用程序进程

- 经过上一小节分析，AMS最终是通过Socket方式进程间通信请求到了Zygote进程。Zygote英文为受精卵的意思，它启动过程创建了Android 虚拟机，其他应用程序进程则通过fork复制Zygote来创建应用程序进程，比如AMS服务所在的SystemServer进程，关于Zygote进程进程的启动过程细节又是另一部分内容了，本文则先只分析应用程序进程启动的过程，接下来从ZygoteInit的main方法开始

>frameworks/base/core/java/com/android/internal/os/ZygoteInit.java

```
 @UnsupportedAppUsage
    public static void main(String argv[]) {
        .....
        Runnable caller;//1
        try {
            ........

            boolean startSystemServer = false;//2
            String zygoteSocketName = "zygote";//3
            String abiList = null;
            boolean enableLazyPreload = false;
            for (int i = 1; i < argv.length; i++) {
                if ("start-system-server".equals(argv[i])) {//4
                    startSystemServer = true;
                } 
                .......
            }

            final boolean isPrimaryZygote = zygoteSocketName.equals(Zygote.PRIMARY_SOCKET_NAME);
            
            ......
            Zygote.initNativeState(isPrimaryZygote);//5
            
            .....
            zygoteServer = new ZygoteServer(isPrimaryZygote);//6

            if (startSystemServer) { 
                Runnable r = forkSystemServer(abiList, zygoteSocketName, zygoteServer); //7
            }

            // The select loop returns early in the child process after a fork and
            // loops forever in the zygote.
            caller = zygoteServer.runSelectLoop(abiList);//8
        } 
        
        .......

        // We're in the child process and have exited the select loop. Proceed to execute the
        // command.
        if (caller != null) {
            caller.run(); //9
        }
    }
```
由以上是缩短精简的ZygoteInit的main方法，注释1处创建了一个Runnable类型的请求对象代表此次请求Zygote获取应用进程的对象；结合注释2、4和7如果是Android系统启动过程，则会启动核心进程SystemServer，否则是启动其他应用程序进程，注释3出设置**Socket连接名称为zygote**，注释5出则初始化了Zygote的状态环境，包括支持套接字的环境，安全环境等,同时也包括Socket连接的名称为**zygote**；注释6创建了ZygoteServer对象，它可以理解为Zygote支持Socket进程通信的服务端，而在注释8的 runSelectLoop 方法则是Zygote进程等待接收AMS请求启动应用程序进程的关键方法，注释9先放着，接着往下看

>frameworks/base/core/java/com/android/internal/os/ZygoteServer.java

```
Runnable runSelectLoop(String abiList) {
        ......
        while (true) {

            while (--pollIndex >= 0) {
                if ((pollFDs[pollIndex].revents & POLLIN) == 0) {
                    continue;
                }

                if (pollIndex == 0) {
                    // Zygote server socket

                    ZygoteConnection newPeer = acceptCommandPeer(abiList);
                    peers.add(newPeer);
                    socketFDs.add(newPeer.getFileDescriptor());

                } else if (pollIndex < usapPoolEventFDIndex) {
                    // Session socket accepted from the Zygote server socket  从Zygote服务器套接字接受会话套接字

                    try {
                        ZygoteConnection connection = peers.get(pollIndex);
                        final Runnable command = connection.processOneCommand(this); //1

                        ......
                            return command;
                        } 
        .......                
    }
```
- 由以上代码，通过一个死循环等待接收请求，注释1处则调用了ZygoteConnection的processOneCommand方法，接着往下看

### 获取应用程序进程 

>frameworks/base/core/java/com/android/internal/os/ZygoteConnection.java
```
/**
     * Reads one start command from the command socket. If successful, a child is forked and a
     * {@code Runnable} that calls the childs main method (or equivalent) is returned in the child
     * process. {@code null} is always returned in the parent process (the zygote).
     *
     * If the client closes the socket, an {@code EOF} condition is set, which callers can test
     * for by calling {@code ZygoteConnection.isClosedByPeer}.
     */
    Runnable processOneCommand(ZygoteServer zygoteServer) {
        String args[];
        ZygoteArguments parsedArgs = null;
        FileDescriptor[] descriptors;

        try {
            args = Zygote.readArgumentList(mSocketReader);//1
           ....
        } 
        
        ......

        pid = Zygote.forkAndSpecialize(parsedArgs.mUid, parsedArgs.mGid, parsedArgs.mGids,
                parsedArgs.mRuntimeFlags, rlimits, parsedArgs.mMountExternal, parsedArgs.mSeInfo,
                parsedArgs.mNiceName, fdsToClose, fdsToIgnore, parsedArgs.mStartChildZygote,
                parsedArgs.mInstructionSet, parsedArgs.mAppDataDir, parsedArgs.mTargetSdkVersion); //2

        try {
            if (pid == 0) { //3
                // in child
                zygoteServer.setForkChild();

                zygoteServer.closeServerSocket();
                IoUtils.closeQuietly(serverPipeFd);
                serverPipeFd = null;

                return handleChildProc(parsedArgs, descriptors, childPipeFd,
                        parsedArgs.mStartChildZygote);
            } else {
                // In the parent. A pid < 0 indicates a failure and will be handled in
                // handleParentProc.
                IoUtils.closeQuietly(childPipeFd);
                childPipeFd = null;
                handleParentProc(pid, descriptors, serverPipeFd);
                return null;
            }
        } 
        ......
    }
```
- 由以上代码注释1处获取了启动应用程序进程的参数，看到注释2处，直接调用了Zygote类的forkAndSpecialize方法
>frameworks/base/core/java/com/android/internal/os/Zygote.java
```
    /**
     * Forks a new VM instance.  The current VM must have been started
     * @return 0 if this is the child, pid of the child
     * if this is the parent, or -1 on error.
     */
    public static int forkAndSpecialize(int uid, int gid, int[] gids, int runtimeFlags,
            int[][] rlimits, int mountExternal, String seInfo, String niceName, int[] fdsToClose,
            int[] fdsToIgnore, boolean startChildZygote, String instructionSet, String appDataDir,
            int targetSdkVersion) {
        ZygoteHooks.preFork();
        // Resets nice priority for zygote process.
        resetNicePriority();
        int pid = nativeForkAndSpecialize(
                uid, gid, gids, runtimeFlags, rlimits, mountExternal, seInfo, niceName, fdsToClose,
                fdsToIgnore, startChildZygote, instructionSet, appDataDir);//1
        return pid;
    }
```
- 由以上代码注释1处，通过Native方法nativeForkAndSpecialize与底层通信复制fork出应用即将要启动的应用程序进程，本文则关注大致流程，底层Native实现则不再继续展开，有兴趣朋友可以继续跟进frameworks/base/core/jni/com_android_internal_os_Zygote.cpp查看。根据forkAndSpecialize方法的注释也可以明白，该方法返回0则为child，也就是应用程序进程，-1则为父进程，所以这里再次回到ZygoteConnection的processOneCommand方法，启动的是应用程序进程，则在注释3出pid=0 此时已经处于应用程序进程了，接着继续调用ZygoteConnection的
handleChildProc方法

>frameworks/base/core/java/com/android/internal/os/ZygoteConnection.java

```
    private Runnable handleChildProc(ZygoteArguments parsedArgs, FileDescriptor[] descriptors,
            FileDescriptor pipeFd, boolean isZygote) {
        ..........
        } else {
            if (!isZygote) {
                return ZygoteInit.zygoteInit(parsedArgs.mTargetSdkVersion,
                        parsedArgs.mRemainingArgs, null /* classLoader */); //1
            } else {
                return ZygoteInit.childZygoteInit(parsedArgs.mTargetSdkVersion,
                        parsedArgs.mRemainingArgs, null /* classLoader */);
            }
        }
    }
```
- 由以上源码，启动的是应用程序进程则会走到注释1处调用ZygoteInit类的zygoteInit方法

### 创建应用程序的ActivityThread

> frameworks/base/core/java/com/android/internal/os/ZygoteInit.java

```
 public static final Runnable zygoteInit(int targetSdkVersion, String[] argv,
            ClassLoader classLoader) {
        if (RuntimeInit.DEBUG) {
            Slog.d(RuntimeInit.TAG, "RuntimeInit: Starting application from zygote");
        }

        Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "ZygoteInit");
        RuntimeInit.redirectLogStreams();

        RuntimeInit.commonInit();
        ZygoteInit.nativeZygoteInit();//1
        return RuntimeInit.applicationInit(targetSdkVersion, argv, classLoader); //2
    }
```
- 由以上源码，注释1出的Native方法会创建当前应用程序进程的Binder线程池，则当前应用程序就拥有了Binder通信的能力，注释2处接着调用了RuntimeInit的applicationInit方法

> frameworks/base/core/java/com/android/internal/os/RuntimeInit.java    
```
protected static Runnable applicationInit(int targetSdkVersion, String[] argv,
            ClassLoader classLoader) {
        ......

        final Arguments args = new Arguments(argv); //1
        ......
        return findStaticMain(args.startClass, args.startArgs, classLoader);2
    }
```
- 由以上代码，注释1处处理AMS请求中传递过来的参数，这其中就包括**android.app.ActivityThread**，也就是注释2处args.startClass代表的值，接着继续看findStaticMain方法

> frameworks/base/core/java/com/android/internal/os/RuntimeInit.java

```
 protected static Runnable findStaticMain(String className, String[] argv,
            ClassLoader classLoader) {
        Class<?> cl;

        try {
            cl = Class.forName(className, true, classLoader);//1
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(
                    "Missing class when invoking static main " + className,
                    ex);
        }

        Method m;
        try {
            m = cl.getMethod("main", new Class[] { String[].class });//2
        } 
        ......

        /*
         * This throw gets caught in ZygoteInit.main(), which responds
         * by invoking the exception's run() method. This arrangement
         * clears up all the stack frames that were required in setting
         * up the process.
         */
        return new MethodAndArgsCaller(m, argv);
    }
```
- 由以上代码，注释1处反射获取了**android.app.ActivityThread**，也就是应用程序进程的**ActivityThread**，注释2处获取了**ActivityThread**的main方法，然后返回了Runnable对象为MethodAndArgsCaller

> frameworks/base/core/java/com/android/internal/os/RuntimeInit.java

```
/**
     * Helper class which holds a method and arguments and can call them. This is used as part of
     * a trampoline to get rid of the initial process setup stack frames.
     */
    static class MethodAndArgsCaller implements Runnable {
        /** method to call */
        private final Method mMethod;

        /** argument array */
        private final String[] mArgs;

        public MethodAndArgsCaller(Method method, String[] args) {
            mMethod = method;
            mArgs = args;
        }

        public void run() {
            try {
                mMethod.invoke(null, new Object[] { mArgs }); //1
            } 
            ...
        }
    }
```
- 由以上代码，注释1处代表应用程序进程ActivityThread的main方法，结合本小节开头的从ZygoteInit的main方法的注释9，目前已经处于应用程序进程，则会调用Runnable类型caller的run方法，也就是MethodAndArgsCaller的run方法，由以上代码注释1处，则会调用ActivityThread的main方法，而ActivityThread作为每个应程序主线程的管理类，到此，应用程序进程启动完成，接着上一篇文章，当ATMS与应用程序Binder通信通过IApplicationThread为ActivityThread的内部类，然后**调用ActivityThread的performLaunchActivity方法来启动Activity**。

### Zygote启动应用程序进程时序图

- 本小节最后还是通过时序图来对上面的步骤进行回顾

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%B3%BB%E7%BB%9F%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/Zygote%E5%90%AF%E5%8A%A8%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F%E8%BF%9B%E7%A8%8B.jpg)

## 总结

### 启动过程涉及的进程

- 结合上一篇文章[深入理解Android 之 Activity启动流程](https://juejin.im/post/5e8407d251882573be11b63c)，总结出如下应用程序进程启动涉及的几个进程间调用的关系图

![image](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%B3%BB%E7%BB%9F%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F%E5%90%AF%E5%8A%A8%E8%BF%87%E7%A8%8B%E5%90%84%E4%B8%AA%E8%BF%9B%E7%A8%8B%E9%97%B4%E8%B0%83%E7%94%A8%E5%85%B3%E7%B3%BB%E5%9B%BE.jpg)

### 参考
- 书籍《Android 进阶解密》
- [Android源码地址](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:)