# 深入理解Android 之 Activity启动流程（Android 10）
> 在进阶Android的路上，了解理解一个应用根Activity启动流程可以作为一个切入点，由此展开进阶之路。平时我们开发的应用都是展示在Android系统桌面上，这个系统桌面其实也是一个Android应用，它叫Launcher。所以本文通过源码层面从Launcher调用ATMS，ATMS调用ApplicationThread，最后ActivityThread启动Activity三个过程了解Activity启动流程（文中源码基于Android 10 ）。

- [Android源码地址](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:)

- 首先来个脑图，对于整体模块在大脑中形成一个整体印象

![Activity启动流程](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/Activity/Android%20Activity%E5%90%AF%E5%8A%A8%E8%BF%87%E7%A8%8B.png)

## Launcher到ActivityTaskManagerService

### Launcher 调用 Activity

- 至于Launcher如何加载展示应用程序到界面这里先略过（与PMS相关），本文先关注Activity启动过程。当我们点击系统桌面的应用图标，直接响应的则是Launcher这个应用程序，会调用它的startActivitySafely方法
>packages/apps/Launcher3/src/com/android/launcher3/Launcher.java

```
public boolean startActivitySafely(View v, Intent intent, ItemInfo item,
            @Nullable String sourceContainer) {
        .....

        boolean success = super.startActivitySafely(v, intent, item, sourceContainer); // 1
        if (success && v instanceof BubbleTextView) {
            // This is set to the view that launched the activity that navigated the user away
            // from launcher. Since there is no callback for when the activity has finished
            // launching, enable the press state and keep this reference to reset the press
            // state when we return to launcher.
            BubbleTextView btv = (BubbleTextView) v;
            btv.setStayPressed(true);
            addOnResumeCallback(btv);
        }
        return success;
    }
```
- 通过以上源码，在注释1调用的是父类的startActivitySafely方法，Launcher类本身就是Activity,它的父类为BaseDraggingActivity，接着看到它的startActivitySafely方法
>packages/apps/Launcher3/src/com/android/launcher3/BaseDraggingActivity.java

```
public boolean startActivitySafely(View v, Intent intent, @Nullable ItemInfo item,
            @Nullable String sourceContainer) {
        .......

        // Prepare intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //1
        if (v != null) {
            intent.setSourceBounds(getViewBounds(v));
        }
        try {
            ......
            if (isShortcut) {
                // Shortcuts need some special checks due to legacy reasons.
                startShortcutIntentSafely(intent, optsBundle, item, sourceContainer);
            } else if (user == null || user.equals(Process.myUserHandle())) {
                // Could be launching some bookkeeping activity
                startActivity(intent, optsBundle);//2
                AppLaunchTracker.INSTANCE.get(this).onStartApp(intent.getComponent(),
                        Process.myUserHandle(), sourceContainer);
            } else {
                .......
            }
            getUserEventDispatcher().logAppLaunch(v, intent);
            getStatsLogManager().logAppLaunch(v, intent);
            return true;
        } catch (NullPointerException|ActivityNotFoundException|SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + item + " intent=" + intent, e);
        }
        return false;
    }
```
- 以上源码看到注释1，设置启动Activity的Flag为**FLAG_ACTIVITY_NEW_TASK**，设置这个Flag则Activity的启动就会在新的任务栈中启动，后面还会遇到它；接着看到注释2，调用了startActivity的方法，显然这就是调用了Activity类的startActivity方法。继续探究Activity类的startActivity方法

>frameworks/base/core/java/android/app/Activity.java

```
@Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        if (options != null) {
            startActivityForResult(intent, -1, options);//1
        } else {
            // Note we want to go through this call for compatibility with
            // applications that may have overridden the method.
            startActivityForResult(intent, -1);
        }
    }
```
- 有以上源码看到注释1，Activity类的startActivity方法调用的是startActivityForResult方法，这个方法日常开发启动Activity有参数回调也会使用，这里参数传入-1，表明Launcher启动Activity并不管它成功与否。接着看startActivityForResult方法
>frameworks/base/core/java/android/app/Activity.java

```
Activity mParent;

public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
            @Nullable Bundle options) {
        if (mParent == null) { //1
            options = transferSpringboardActivityOptions(options);
            Instrumentation.ActivityResult ar =
                mInstrumentation.execStartActivity(
                    this, mMainThread.getApplicationThread(), mToken, this,
                    intent, requestCode, options);//2
            ......
        } else {
            ......
        }
    }
```

- 通过以上源码看到注释1，mParent的声明类型为Activity，当前还是正在起Activity，mParent == null成立，看到注释2调用了Instrumentation类的execStartActivity方法，Instrumentation允许您监视系统与应用程序之间的所有交互（Instrumentation注释：allowing you to monitor all of the interaction the system has with the application.），接着看到它的execStartActivity方法

### Instrumentation 调用到ATMS

>frameworks/base/core/java/android/app/Instrumentation.java

```
@UnsupportedAppUsage
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;//1
        ......
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            int result = ActivityTaskManager.getService()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options); //2
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }
```
- 通过以上源码看到注释1，这里获取了IApplicationThread，如果你了解Binder，第一反应就应该很清晰，目前处于Launcher应用程序进程，要启动Activity则需要请求系统服务进程（SystemServer）,而Android进程间通信则可以使用Binder，而这里实现方式为AIDL，它的实现类为ActivityThread的内部类ApplicationThread，而**ApplicationThread**作用则为应用程序进程和系统服务进程通信的桥梁，后面还会继续提到；接着看到注释2，这里调用ActivityTaskManager.getService则可以获取ActivityTaskManagerService的代理对象，看看他的实现
>frameworks/base/core/java/android/app/ActivityTaskManager.java
```
 public static IActivityTaskManager getService() {
        return IActivityTaskManagerSingleton.get();
    }

    @UnsupportedAppUsage(trackingBug = 129726065)
    private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton =
            new Singleton<IActivityTaskManager>() {
                @Override
                protected IActivityTaskManager create() {
                    final IBinder b = ServiceManager.getService(Context.ACTIVITY_TASK_SERVICE);//1
                    return IActivityTaskManager.Stub.asInterface(b); //2
                }
            };
```
- 由以上源码注释1，通过ServiceManager来获取远程服务ActivityTaskManagerService，ServiceManager底层最终调用的还是c++层的ServiceManager，它是Binder的守护服务，通过它能够获取在Android系统启动时注册的系统服务，这其中就包含这里提到的ATMS；接着回到注释2建立 Launcher与 ATMS的连接，这样回到execStartActivity方法，Launcher就通过调用ATMS的startActivity方法将启动Activity的数据交给ATMS服务来处理了。

- 为了更好理解，看看Launcher调用到ActivityTaskManagerService时序图来对上面的步骤进行回顾

![Launcher调用到ActivityTaskManagerService时序图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/Activity/Launcher%E8%B0%83%E7%94%A8%E5%88%B0ActivityTaskManagerService%E6%97%B6%E5%BA%8F%E5%9B%BE.jpg)

## ActivityTaskManagerService 调用ApplicationThread

### ATMS处理启动Activity请求
- 通过上一小节，启动应用程序Activity已经走到ActivityTaskManagerService中，如果你熟悉前以往版本的Android源码，你肯定会知道ActivityManagerService，而在Android 10 中则将AMS用于管理Activity及其容器（任务，堆栈，显示等）的系统服务分离出来放到ATMS中，也许是谷歌不想让AMS的代码越来越膨胀吧(Android 10中AMS代码有一万九千行)。好了，接着看到ATMS的startActivity方法
>frameworks/base/services/core/java/com/android/server/wm/ActivityTaskManagerService.java
```
@Override
    public final int startActivity(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,
                resultWho, requestCode, startFlags, profilerInfo, bOptions,
                UserHandle.getCallingUserId());//1
    }
```
- 由以上代码，继续调用了startActivityAsUser方法，该方法多传入了用户的ID，接着会判断是否有权限调用，没有权限调用则抛出异常，否则获取用户id用于后续进程间Binder通信。接着继续看startActivityAsUser方法
>frameworks/base/services/core/java/com/android/server/wm/ActivityTaskManagerService.java

```
@Override
    public int startActivityAsUser(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,
                resultWho, requestCode, startFlags, profilerInfo, bOptions, userId,
                true /*validateIncomingUser*/);//1
    }

    int startActivityAsUser(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId,
            boolean validateIncomingUser) {
        enforceNotIsolatedCaller("startActivityAsUser");

        userId = getActivityStartController().checkTargetUser(userId, validateIncomingUser,
                Binder.getCallingPid(), Binder.getCallingUid(), "startActivityAsUser");

        // TODO: Switch to user app stacks here.
        return getActivityStartController().obtainStarter(intent, "startActivityAsUser")
                .setCaller(caller)
                .setCallingPackage(callingPackage)
                .setResolvedType(resolvedType)
                .setResultTo(resultTo)
                .setResultWho(resultWho)
                .setRequestCode(requestCode)
                .setStartFlags(startFlags)
                .setProfilerInfo(profilerInfo)
                .setActivityOptions(bOptions)
                .setMayWait(userId)
                .execute();//2

    }
```
- 由以上代码，注释1调用了ATMS自己实现的startActivityAsUser方法，在注释而2处构造了ActivityStarter，此类收集了用于确定将意图和标志如何转换为活动以及关联的任务和堆栈的所有逻辑，obtainStarter方法第二个参数代表启动Activity的意图，接着调用了execute方法，
>frameworks/base/services/core/java/com/android/server/wm/ActivityStarter.java

```
int execute() {
        try {
            // TODO(b/64750076): Look into passing request directly to these methods to allow
            // for transactional diffs and preprocessing.
            if (mRequest.mayWait) { //1
                return startActivityMayWait(mRequest.caller, mRequest.callingUid,
                        mRequest.callingPackage, mRequest.realCallingPid, mRequest.realCallingUid,
                        mRequest.intent, mRequest.resolvedType,
                        mRequest.voiceSession, mRequest.voiceInteractor, mRequest.resultTo,
                        mRequest.resultWho, mRequest.requestCode, mRequest.startFlags,
                        mRequest.profilerInfo, mRequest.waitResult, mRequest.globalConfig,
                        mRequest.activityOptions, mRequest.ignoreTargetSecurity, mRequest.userId,
                        mRequest.inTask, mRequest.reason,
                        mRequest.allowPendingRemoteAnimationRegistryLookup,
                        mRequest.originatingPendingIntent, mRequest.allowBackgroundActivityStart);//2
            } 
          ......  
        } 
        .......
    }
```
- 由以上代码看到注释1，前面构造ActivityStarter已经传入了用户id，所以这里判断条件成立，则继续调用startActivityMayWait方法
>frameworks/base/services/core/java/com/android/server/wm/ActivityStarter.java

```
private int startActivityMayWait(IApplicationThread caller, int callingUid,
            String callingPackage, int requestRealCallingPid, int requestRealCallingUid,
            Intent intent, String resolvedType, IVoiceInteractionSession voiceSession,
            IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, WaitResult outResult,
            Configuration globalConfig, SafeActivityOptions options, boolean ignoreTargetSecurity,
            int userId, TaskRecord inTask, String reason,
            boolean allowPendingRemoteAnimationRegistryLookup,
            PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
            ......

            final ActivityRecord[] outRecord = new ActivityRecord[1];//1
            int res = startActivity(caller, intent, ephemeralIntent, resolvedType, aInfo, rInfo,
                    voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid,
                    callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options,
                    ignoreTargetSecurity, componentSpecified, outRecord, inTask, reason,
                    allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent,
                    allowBackgroundActivityStart);//2
          ......
            return res;
        }
    }
```
- 由以上代码，可以看到注释1处创建了一个ActivityRecord数组，ActivityRecord代表一个Activity,接着调用了startActivity方法，
>frameworks/base/services/core/java/com/android/server/wm/ActivityStarter.java
```
   private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent,
            String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid,
            String callingPackage, int realCallingPid, int realCallingUid, int startFlags,
            SafeActivityOptions options,
            boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity,
            TaskRecord inTask, boolean allowPendingRemoteAnimationRegistryLookup,
            PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        mSupervisor.getActivityMetricsLogger().notifyActivityLaunching(intent);
        int err = ActivityManager.START_SUCCESS;
        // Pull the optional Ephemeral Installer-only bundle out of the options early.
        final Bundle verificationBundle
                = options != null ? options.popAppVerificationBundle() : null;

        WindowProcessController callerApp = null;
        if (caller != null) {//1
            callerApp = mService.getProcessController(caller);//2
            if (callerApp != null) {
                callingPid = callerApp.getPid();
                callingUid = callerApp.mInfo.uid;
            } else {
                Slog.w(TAG, "Unable to find app for caller " + caller
                        + " (pid=" + callingPid + ") when starting: "
                        + intent.toString());
                err = ActivityManager.START_PERMISSION_DENIED;
            }
        }
       .......

        ActivityRecord r = new ActivityRecord(mService, callerApp, callingPid, callingUid,
                callingPackage, intent, resolvedType, aInfo, mService.getGlobalConfiguration(),
                resultRecord, resultWho, requestCode, componentSpecified, voiceSession != null,
                mSupervisor, checkedOptions, sourceRecord);
        if (outActivity != null) {
            outActivity[0] = r;//3
        }

       ......

        final int res = startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags,
                true /* doResume */, checkedOptions, inTask, outActivity, restrictedBgActivity);//4
        .....
        return res;
    }
```
- 由以上代码，startActivity里面有很多的逻辑代码，这里只看一些重点的逻辑代码，主要做了两个事情：
（1）注释1处判断IApplicationThread是否为空，前面第一小节我们就已经提到过，它代表的就是Launcher进程的ApplicationThread，注释2通过与即将要启动的应用程序进程建立联系，应用程序进程的是fork到Zyote进程，这里先不进行展开了,先专注Activity启动流程。接着注释3创建ActivityRecord代表即将要启动的Activity，包含了Activity的所有信息，并赋值给上一步骤中创建的ActivityRecord类型的outActivity，注释4则继续调用startActivity方法
>frameworks/base/services/core/java/com/android/server/wm/ActivityStarter.java

```
private int startActivity(final ActivityRecord r, ActivityRecord sourceRecord,
                IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
                int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
                ActivityRecord[] outActivity, boolean restrictedBgActivity) {
        int result = START_CANCELED;
        final ActivityStack startedActivityStack;
        try {
            mService.mWindowManager.deferSurfaceLayout();
            result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor,
                    startFlags, doResume, options, inTask, outActivity, restrictedBgActivity);//1
        } 
        ........

        return result;
    }
```
- 由以上代码，注释1处startActivity又调用了startActivityUnchecked方法
>frameworks/base/services/core/java/com/android/server/wm/ActivityStarter.java

```
// Note: This method should only be called from {@link startActivity}.
    private int startActivityUnchecked(final ActivityRecord r, ActivityRecord sourceRecord,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
            ActivityRecord[] outActivity, boolean restrictedBgActivity) {
        ......    
        final TaskRecord taskToAffiliate = (mLaunchTaskBehind && mSourceRecord != null)
                ? mSourceRecord.getTaskRecord() : null;
        // Should this be considered a new task?
        int result = START_SUCCESS;
        if (mStartActivity.resultTo == null && mInTask == null && !mAddingToTask
                && (mLaunchFlags & FLAG_ACTIVITY_NEW_TASK) != 0) { //1
            newTask = true;
            result = setTaskFromReuseOrCreateNewTask(taskToAffiliate); //2
        } 
        ........
        if (mDoResume) {
            final ActivityRecord topTaskActivity =
                    mStartActivity.getTaskRecord().topRunningActivityLocked();
            if (!mTargetStack.isFocusable()
                    || (topTaskActivity != null && topTaskActivity.mTaskOverlay
                    && mStartActivity != topTaskActivity)) {
                
              mTargetStack.ensureActivitiesVisibleLocked(mStartActivity, 0, !PRESERVE_WINDOWS);
                mTargetStack.getDisplay().mDisplayContent.executeAppTransition();
            } else {
                // If the target stack was not previously focusable (previous top running activity
                // on that stack was not visible) then any prior calls to move the stack to the
                // will not update the focused stack.  If starting the new activity now allows the
                // task stack to be focusable, then ensure that we now update the focused stack
                // accordingly.
                if (mTargetStack.isFocusable()
                        && !mRootActivityContainer.isTopDisplayFocusedStack(mTargetStack)) {
                    mTargetStack.moveToFront("startActivityUnchecked");
                }
                mRootActivityContainer.resumeFocusedStacksTopActivities(
                        mTargetStack, mStartActivity, mOptions);//3
            }
        }
```
- 由上代码注释1，在前面第一节Launcher部分中有提到过设置了Flag为**FLAG_ACTIVITY_NEW_TASK**，所以注意判断条件成立，则调用setTaskFromReuseOrCreateNewTask，它内部会创建的TaskRecord（代表Activity的任务栈），并将传入的TaskRecord对象设置给代表启动的Activity的ActivityRecord，接着在注释3调用了RootActivityContainer的resumeFocusedStacksTopActivities方法，RootActivityContainer 将一些东西从ActivityStackSupervisor中分离出来。目的是将其与RootWindowContainer合并，作为统一层次结构的一部分，接着看它的resumeFocusedStacksTopActivities方法

> frameworks/base/services/core/java/com/android/server/wm/RootActivityContainer.java

```
boolean resumeFocusedStacksTopActivities(
            ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {

        ......

        boolean result = false;
        if (targetStack != null && (targetStack.isTopStackOnDisplay()
                || getTopDisplayFocusedStack() == targetStack)) { 
            result = targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);//1
        }

        .......

        return result;
    }
```
- 由以上代码注释1处，又调用了ActivityStack的resumeTopActivityUncheckedLocked方法，ActivityStack应该算是任务栈的描述，它管理者一个应用的所有TaskRecord和他们的状态，接着看到它的resumeTopActivityUncheckedLocked方法
>frameworks/base/services/core/java/com/android/server/wm/ActivityStack.java

```
//确保栈顶 activity 为Resume
boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
        if (mInResumeTopActivity) {
            // Don't even start recursing.
            return false;
        }

        boolean result = false;
        try {
            // 防止递归
            mInResumeTopActivity = true;
            result = resumeTopActivityInnerLocked(prev, options); //1
            ........
        } finally {
            mInResumeTopActivity = false;
        }

        return result;
    }
```
- 由以上代码，在注释1处接着又调用ActivityStack的resumeTopActivityInnerLocked方法

> frameworks/base/services/core/java/com/android/server/wm/ActivityStack.java

```
@GuardedBy("mService")
    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
    
    ....
    // Whoops, need to restart this activity!
            
            ........
       mStackSupervisor.startSpecificActivityLocked(next, true, true);//1
            ........
        
}
```
- 由以上代码看到注释1，resumeTopActivityInnerLocked方法中逻辑非常多，这里直接精简到这一句关键代码，调用了ActivityStackSupervisor的startSpecificActivityLocked方法

### ActivityStackSupervisor 启动Activity

>frameworks/base/services/core/java/com/android/server/wm/ActivityStackSupervisor.java

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
            // ATMS lock held.
            final Message msg = PooledLambda.obtainMessage(
                    ActivityManagerInternal::startProcess, mService.mAmInternal, r.processName,
                    r.info.applicationInfo, knownToBeDead, "activity", r.intent.getComponent());//3
            mService.mH.sendMessage(msg);
        }
        ........
    }

```
- 如上代码所示注释1，判断要启动的应用程序进程是否已经准备好，hasThread则是确定应用程序进程的**IApplicationThread**是否存在，如果存在则调用ActivityStackSupervisor的realStartActivityLocked方法启动Activity；如果是第一次启动，则应用程序进程没有准备好，则会走到注释3处启动应用程序进程，本文先跳过，留到下篇文章在探究。接下来继续看到realStartActivityLocked方法
> frameworks/base/services/core/java/com/android/server/wm/ActivityStackSupervisor.java

```
boolean realStartActivityLocked(ActivityRecord r, WindowProcessController proc,
            boolean andResume, boolean checkConfig) throws RemoteException {
           .......

                // Create activity launch transaction.
                final ClientTransaction clientTransaction = ClientTransaction.obtain(
                        proc.getThread(), r.appToken);//1

                final DisplayContent dc = r.getDisplay().mDisplayContent;
                clientTransaction.addCallback(LaunchActivityItem.obtain(new Intent(r.intent),
                        System.identityHashCode(r), r.info,
                        // TODO: Have this take the merged configuration instead of separate global
                        // and override configs.
                        mergedConfiguration.getGlobalConfiguration(),
                        mergedConfiguration.getOverrideConfiguration(), r.compat,
                        r.launchedFromPackage, task.voiceInteractor, proc.getReportedProcState(),
                        r.icicle, r.persistentState, results, newIntents,
                        dc.isNextTransitionForward(), proc.createProfilerInfoIfNeeded(),
                                r.assistToken));//2

                // Set desired final state.
                final ActivityLifecycleItem lifecycleItem;
                if (andResume) {
                    lifecycleItem = ResumeActivityItem.obtain(dc.isNextTransitionForward());
                } else {
                    lifecycleItem = PauseActivityItem.obtain();
                }
                clientTransaction.setLifecycleStateRequest(lifecycleItem);

                // Schedule transaction.
                mService.getLifecycleManager().scheduleTransaction(clientTransaction);//3

                .......

        return true;
    }
```
- 由以上代码注释1处，创建了ClientTransaction对象，它是包含一系列消息的容器，可以将其发送到客户端，这个客户端就我们要启动的应用程序Activity，注释2处将前面一路传递进来的启动Activity参数封装成了LaunchActivityItem请求request对象，接着我们看到注释3，这里调用了ClientLifecycleManager的scheduleTransaction方法，它的初始化在AMTS构造方法中，并传入了ClientTransaction参数，接着看到ClientLifecycleManager的scheduleTransaction方法

### ClientLifecycleManager（ActivityThread）处理ClientTransaction 

> frameworks/base/services/core/java/com/android/server/wm/ClientLifecycleManager.java

```
 void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
        final IApplicationThread client = transaction.getClient();//1
        transaction.schedule();//2
        if (!(client instanceof Binder)) {
            // If client is not an instance of Binder - it's a remote call and at this point it is
            // safe to recycle the object. All objects used for local calls will be recycled after
            // the transaction is executed on client in ActivityThread.
            transaction.recycle();
        }
    }
```
- 到此，基本上已经比较清晰了，注释1处获取了要启动的应用程序进程的IApplicationThread，上一步中创建ClientTransaction对象时已经将其赋值给ClientTransaction的变量mClient，随后scheduleTransaction判断是否支持进程间通信；注释二处则调用了ClientTransaction的schedule方法，
> frameworks/base/core/java/android/app/servertransaction/ClientTransaction.java
```
/** Target client. */
    private IApplicationThread mClient;
    
     /** Schedule the transaction after it was initialized. It will be send to client and all its
     * individual parts will be applied in the following sequence:
     * 1. The client calls {@link #preExecute(ClientTransactionHandler)}, which triggers all work
     *    that needs to be done before actually scheduling the transaction for callbacks and
     *    lifecycle state request.
     * 2. The transaction message is scheduled.
     * 3. The client calls {@link TransactionExecutor#execute(ClientTransaction)}, which executes
     *    all callbacks and necessary lifecycle transitions.
     */
    public void schedule() throws RemoteException {
        mClient.scheduleTransaction(this); //1 
    }

```
- 通过以上代码，注释1处mClient则代表要启动的应用程序进程的IApplicationThread，而当前还处于ATMS服务的进程，也就是SystemServer进程，这时ATMS要与即将启动的应用程序进程通信则通过IApplicationThread来执行AIDL，IApplicationThread实现为ApplicationThread，它是ActivityThread的内部类，所以前面也说过ApplicationThread为进程间通信的桥梁，注释1处则相当于是IApplicationThread.scheduleTransaction，并将包含要启动Activity信息的ClientTransaction传递到了应用程序进程，下一节就从IApplicationThread讲起。

- 为了更好理解，看看AMTS调用到ApplicationThread时序图来对上面的步骤进行回顾

![AMTS调用到ApplicationThread时序图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/Activity/ActivityTaskManagerService%E8%B0%83%E7%94%A8ApplicationThread%E6%97%B6%E5%BA%8F%E5%9B%BE.jpg)

## ActivityThread启动Activity

### ApplicationThread 处理进程间数据通信
- 接着上一节的内容，我们从ApplicationThread的scheduleTransaction方法开始
>frameworks/base/core/java/android/app/ActivityThread.java

```
private class ApplicationThread extends IApplicationThread.Stub {
     @Override
        public void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
            ActivityThread.this.scheduleTransaction(transaction);//1
        }
}
```
- 由以上代码，注释1处调用了ActivityThread的scheduleTransaction方法，ActivityThread继承了ClientTransactionHandler，scheduleTransaction在里面实现
>frameworks/base/core/java/android/app/ClientTransactionHandler.java

```
 /** Prepare and schedule transaction for execution. */
    void scheduleTransaction(ClientTransaction transaction) {
        transaction.preExecute(this);
        sendMessage(ActivityThread.H.EXECUTE_TRANSACTION, transaction);
    }
```
### ActivityThread.H 线程间消息处理

- 可以看到这里发送了一个Handler消息，而ActivityThread.H则是ActivityThread的内部Handler，它是整个应用程序的主线程Handler，这里为什么需要切换线程呢？**其原因为前面ATMS进程间通信则是运行在Binder线程，而Android更新UI则需要在主线程**，接着看到ActivityThread.H的消息处理
>

```
 class H extends Handler {
 
  public void handleMessage(Message msg) {
            if (DEBUG_MESSAGES) Slog.v(TAG, ">>> handling: " + codeToString(msg.what));
            switch (msg.what) {
            case EXECUTE_TRANSACTION:
                    final ClientTransaction transaction = (ClientTransaction) msg.obj;//1
                    mTransactionExecutor.execute(transaction);//2
                    ......
                    break;
                    
             }       
}                    
```
### TransactionExecutor

- 由以上代码，看到注释1处，获取了由ATMS传递过来的启动Activity进程的数据，注释2处调用了TransactionExecutor的来处理ClientTransaction的数据，接着看到它的execute方法
>frameworks/base/core/java/android/app/servertransaction/TransactionExecutor.java

```
public void execute(ClientTransaction transaction) {
        if (DEBUG_RESOLVER) Slog.d(TAG, tId(transaction) + "Start resolving transaction");

        .......

        executeCallbacks(transaction); //

        executeLifecycleState(transaction);
        mPendingActions.clear();
        if (DEBUG_RESOLVER) Slog.d(TAG, tId(transaction) + "End resolving transaction");
    }
```
- 由以上代码注释1处接着调用了TransactionExecutor的executeCallbacks方法
>frameworks/base/core/java/android/app/servertransaction/TransactionExecutor.java

```
/** Cycle through all states requested by callbacks and execute them at proper times. */
    @VisibleForTesting
    public void executeCallbacks(ClientTransaction transaction) {
        final List<ClientTransactionItem> callbacks = transaction.getCallbacks();
        if (callbacks == null || callbacks.isEmpty()) {
            // No callbacks to execute, return early.
            return;
        }
        if (DEBUG_RESOLVER) Slog.d(TAG, tId(transaction) + "Resolving callbacks in transaction");

        final IBinder token = transaction.getActivityToken();
        ActivityClientRecord r = mTransactionHandler.getActivityClient(token);

        // In case when post-execution state of the last callback matches the final state requested
        // for the activity in this transaction, we won't do the last transition here and do it when
        // moving to final state instead (because it may contain additional parameters from server).
        final ActivityLifecycleItem finalStateRequest = transaction.getLifecycleStateRequest();
        final int finalState = finalStateRequest != null ? finalStateRequest.getTargetState()
                : UNDEFINED;
        // Index of the last callback that requests some post-execution state.
        final int lastCallbackRequestingState = lastCallbackRequestingState(transaction);

        final int size = callbacks.size();
        for (int i = 0; i < size; ++i) {
            final ClientTransactionItem item = callbacks.get(i);//1
            if (DEBUG_RESOLVER) Slog.d(TAG, tId(transaction) + "Resolving callback: " + item);
            final int postExecutionState = item.getPostExecutionState();
            final int closestPreExecutionState = mHelper.getClosestPreExecutionState(r,
                    item.getPostExecutionState());
            if (closestPreExecutionState != UNDEFINED) {
                cycleToPath(r, closestPreExecutionState, transaction);
            }

            item.execute(mTransactionHandler, token, mPendingActions);//2
            ........
        }
    }
```
### LaunchActivityItem 
- 由以上代码注释1处，获取的ClientTransactionItem则为第二小节中提到过的LaunchActivityItem对象，它继承了ClientTransactionItem，并保存这需要启动的Activity数据，接着看到注释2 LaunchActivityItem的execute方法。

> frameworks/base/core/java/android/app/servertransaction/LaunchActivityItem.java

```
  @Override
    public void execute(ClientTransactionHandler client, IBinder token,
            PendingTransactionActions pendingActions) {
        Trace.traceBegin(TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
        ActivityClientRecord r = new ActivityClientRecord(token, mIntent, mIdent, mInfo,
                mOverrideConfig, mCompatInfo, mReferrer, mVoiceInteractor, mState, mPersistentState,
                mPendingResults, mPendingNewIntents, mIsForward,
                mProfilerInfo, client, mAssistToken);//1
        client.handleLaunchActivity(r, pendingActions, null /* customIntent */);//2
        Trace.traceEnd(TRACE_TAG_ACTIVITY_MANAGER);
    }
```
- 由以上代码，注释1处恢复了要启动的Activity的数据，ActivityClientRecord是ActivityThread的内部类，这里的client为ClientTransactionHandler，而前面已经说过ActivityThread继承ClientTransactionHandler，所以这里的注释2处调用的就是ActivityThread的handleLaunchActivity方法
>frameworks/base/core/java/android/app/ActivityThread.java

```
 /**
     * Extended implementation of activity launch. Used when server requests a launch or relaunch.
     */
    @Override
    public Activity handleLaunchActivity(ActivityClientRecord r,
            PendingTransactionActions pendingActions, Intent customIntent) {
        .......

        final Activity a = performLaunchActivity(r, customIntent);//1

        .......

        return a;
    }
```
- 由以上代码注释1处，继续调用了ActivityThread的performLaunchActivity方法来启动Activity，返回的也是Activity实例。所以performLaunchActivity方法才是启动Activity实例的核心代码。

### Core Activity Launch

>frameworks/base/core/java/android/app/ActivityThread.java
```
/**  Core implementation of activity launch. */
    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
    
        ActivityInfo aInfo = r.activityInfo;//1
        if (r.packageInfo == null) {
            r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo,
                    Context.CONTEXT_INCLUDE_CODE);//2
        }

        ComponentName component = r.intent.getComponent();//3
        if (component == null) {
            component = r.intent.resolveActivity(
                mInitialApplication.getPackageManager());
            r.intent.setComponent(component);
        }

        if (r.activityInfo.targetActivity != null) {
            component = new ComponentName(r.activityInfo.packageName,
                    r.activityInfo.targetActivity);
        }

        //应用程序Context的创建
        ContextImpl appContext = createBaseContextForActivity(r);//4
        Activity activity = null;
        try {
        
            java.lang.ClassLoader cl = appContext.getClassLoader();
            //创建Activity的实例
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);//5
            StrictMode.incrementExpectedActivityCount(activity.getClass());
            r.intent.setExtrasClassLoader(cl);
            r.intent.prepareToEnterProcess();
            if (r.state != null) {
                r.state.setClassLoader(cl);
            }
        } catch (Exception e) {
            if (!mInstrumentation.onException(activity, e)) {
                throw new RuntimeException(
                    "Unable to instantiate activity " + component
                    + ": " + e.toString(), e);
            }
        }

        try {
        
        //应用程序Application的创建
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);//6

           ......

            if (activity != null) {
                CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
                Configuration config = new Configuration(mCompatConfiguration);
                if (r.overrideConfig != null) {
                    config.updateFrom(r.overrideConfig);
                }
                if (DEBUG_CONFIGURATION) Slog.v(TAG, "Launching activity "
                        + r.activityInfo.name + " with config " + config);
                Window window = null;
                if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                    window = r.mPendingRemoveWindow;
                    r.mPendingRemoveWindow = null;
                    r.mPendingRemoveWindowManager = null;
                }
                appContext.setOuterContext(activity);
                // 通过Activity的 attach 方法将 context等各种数据与Activity绑定，初始化Activity
                activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback,
                        r.assistToken); //7

                ......
                if (r.isPersistable()) {
                    mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);//8
                } else {
                    mInstrumentation.callActivityOnCreate(activity, r.state);
                }
                ......
                r.activity = activity;
            }
            r.setState(ON_CREATE);

           .......
        }

        return activity;
    }
```
- 由以上代码，注释1处获取了前面保存启动应用程序信息的ActivityClientRecord中的应用程序信息，包括应用程序在清单文件中注册了哪些四大组件，启动的根Activity是什么，并在注释2处通过getPackageInfo方法获取LoadedApk描述对应Apk文件资源，注释3处的ComponentName类获取则对应启动Activity的包名和类名，注释4处则生成了启动应用程序的Base上下文环境Context，注释5处通过注释3获取的类名，通过类加载器和Intent对象实例化了Activity对象，注释6则根据注释2处获取Apk描述对象LoadedApk创建了应用程序的Application对象，并在makeApplication方法中调用了它的OnCreate方法，所以应用程序最新启动的是Application才到根Activity，注释7处则前面创建的Context、Application、Window对象与Activity关联来初始化Activity，最后注释8处还继续调用了Instrumentation对象的callActivityOnCreate方法。接着往下看

### Activity的 OnCreate方法调用
>frameworks/base/core/java/android/app/Instrumentation.java

```
public void callActivityOnCreate(Activity activity, Bundle icicle,PersistableBundle persistentState) {
        prePerformCreate(activity); 
        activity.performCreate(icicle, persistentState);//1
        postPerformCreate(activity);
    }
```
- 由以上代码，注释1处又调用了Activity的performCreate方法，继续往下看
>frameworks/base/core/java/android/app/Activity.java

```
final void performCreate(Bundle icicle, PersistableBundle persistentState) {
        dispatchActivityPreCreated(icicle);
        mCanEnterPictureInPicture = true;
        restoreHasCurrentPermissionRequest(icicle);
        if (persistentState != null) {
            onCreate(icicle, persistentState);//1
        } else {
            onCreate(icicle);
        }
        .......
    }
```
- 最终，在已经实例初始化好的Activity调用它的performCreate方法中又掉用了onCreate方法（注释1）。至此，也就是整个应用程序的Activity启动过程我们已经走完了。
- 为了更好理解，看看ActivityThread启动Activity的时序图来对上面的步骤进行回顾

![ActivityThread启动Activity的时序图](https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%20%E5%9B%9B%E5%A4%A7%E7%BB%84%E4%BB%B6%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B/Activity/ActivityThread%E5%90%AF%E5%8A%A8Activity%E6%97%B6%E5%BA%8F%E5%9B%BE.jpg)

## 最后

- 通过本文，基本上将引用程序启动根的Activity启动流程走了一遍，但是其中还有一点没说展开的就是应用程序进程的启动过程，这一部分内容将通过后续文章继续探究。。如果文章中有写得不对的地方，欢迎在留言区留言大家一起讨论，共同学习进步。如果觉得我的文章给予你帮助，也请给我一个喜欢和关注。
### 参考
- 书籍《Android 进阶解密》
- [Android源码地址](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:)
