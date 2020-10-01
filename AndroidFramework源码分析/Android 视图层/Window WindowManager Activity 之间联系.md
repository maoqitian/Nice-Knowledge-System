# Window WindowManager Activity 之间联系
## Window 三种类型
- 承载 Activity
- 承载当个窗口(Dialog)
- 系统Window(Toast)

## window 与 Activity之间联系
### Activity.setContentView 方法
- 创建一个加载布局首先调用了它的setContentView方法
> appcompat/appcompat/src/main/java/androidx/appcompat/app/AppCompatActivity.java
```
@Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

 /**
     * @return The {@link AppCompatDelegate} being used by this Activity.
     */
    @NonNull
    public AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, this);
        }
        return mDelegate;
    }
    
  @NonNull
    public static AppCompatDelegate create(@NonNull Dialog dialog,
            @Nullable AppCompatCallback callback) {
        return new AppCompatDelegateImpl(dialog, callback);
    }    
```
- 调用了委托类 AppCompatDelegate 的 setContentView 方法，AppCompatDelegate 是抽象类，它的实现为AppCompatDelegateImpl，看到它的 setContentView 方法
>appcompat/appcompat/src/main/java/androidx/appcompat/app/AppCompatDelegateImpl.java
```
@Override
    public void setContentView(int resId) {
        ensureSubDecor();
        .....
    }
```
- AppCompatDelegateImpl 的 setContentView 方法又调用了 ensureSubDecor 方法

>appcompat/appcompat/src/main/java/androidx/appcompat/app/AppCompatDelegateImpl.java

```
private ViewGroup createSubDecor() {
        
        ......

        ViewGroup subDecor = null;

        // Now set the Window's content view with the decor
        mWindow.setContentView(subDecor);
        
        ...

        return subDecor;
    }
```
- subDecor 是一个 ViewGroup，inflate 各种布局配置，接着调用了 Window 对象的 setContentView 方法 ，接着探索这个 Window 对象代表什么。

## Window 实例化

### Activity.attach
- ActivityThread初始启动应用创建Activity 的方法handleLaunchActivity方法中调用了Activity的attach方法([深入理解Android 之 Activity启动流程](https://github.com/maoqitian/Nice-Knowledge-System/blob/master/AndroidFramework%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/Activity%E5%90%AF%E5%8A%A8/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3Android%20%E4%B9%8B%20Activity%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B%EF%BC%88Android%2010%EF%BC%89.md))，也就就是如下源码注释1处，创建了Window对象为PhoneWindow（Window对象是其唯一实现类PhoneWindow）
- 还需注意如下源码注释2，**Window的 Callback 对象指向的是当前 Activity对象**，方便事件分发传递回Activity
- 注释3处设置了WindowManager，设置方法中实际上创建的是WindowManagerImpl，接着看WindowManager 实例化
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
        attachBaseContext(context);

        .....
        mWindow = new PhoneWindow(this, window, activityConfigCallback);//1
        ......
        mWindow.setCallback(this);//2

        .......
        mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);//3
        if (mParent != null) {
            mWindow.setContainer(mParent.getWindow());
        }
        mWindowManager = mWindow.getWindowManager();
        
    }
```
### WindowManager 实例化

> frameworks/base/core/java/android/view/Window.java

```
 public void setWindowManager(WindowManager wm, IBinder appToken, String appName,
            boolean hardwareAccelerated) {
       .......
        mWindowManager = ((WindowManagerImpl)wm).createLocalWindowManager(this);//1
    }
```
> frameworks/base/core/java/android/view/WindowManagerImpl.java

```
public WindowManagerImpl createLocalWindowManager(Window parentWindow) {
        return new WindowManagerImpl(mContext, parentWindow);
    }
```

- 由以上源码，可以知道最终实例化的WindowManager的对象为WindowManagerImpl对象，继续回头看PhoneWindow得setContentView方法。

## 界面根布局 DecorView 创建

###  PhoneWindow.setContentView

> frameworks/base/core/java/com/android/internal/policy/PhoneWindow.java
```
protected DecorView generateDecor(int featureId) {
        // System process doesn't have application context and in that case we need to directly use
        // the context we have. Otherwise we want the application context, so we don't cling to the
        // activity.
        Context context;
        if (mUseDecorContext) {
            Context applicationContext = getContext().getApplicationContext();
            if (applicationContext == null) {
                context = getContext();
            } else {
                context = new DecorContext(applicationContext, getContext());
                if (mTheme != -1) {
                    context.setTheme(mTheme);
                }
            }
        } else {
            context = getContext();
        }
        return new DecorView(context, featureId, this, getAttributes());
    }
```

- Activity的 setContentView方法调用的是PhoneWindow的setContentView方法，PhoneWindow的setContentView方法会创建DecorView，最终将加载根布局到DecorView(FrameLayout)

### Activity 布局加载时序图
![Activity布局加载时序图](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f4aee4be3d0e44c88cef08dc3b3784ae~tplv-k3u1fbpfcp-zoom-1.image)

## Window绘制

### Window 与 WindowManager 之间联系

- 前文提到Activity的 attach 方法，其中调用了Window 的 setWindowManager方法，创建了WindowManager，WindowManager是一个接口，创建的是它的实现类 WindowManagerImpl 对象

### WindowManagerImpl.addView

- Activity启动流程中还会调用 ActivityThread的 handleResumeActivity 方法如下所示
>frameworks/base/core/java/android/app/ActivityThread.java
```
 @Override
    public void handleResumeActivity(IBinder token, boolean finalStateRequest, boolean isForward,
            String reason) {
        // If we are getting ready to gc after going to the background, well
        // we are back active so skip it.
        .......

        // TODO Push resumeArgs into the activity for consideration
        final ActivityClientRecord r = performResumeActivity(token, finalStateRequest, reason);//1
        .....

        final Activity a = r.activity;

       .....
        
        if (r.window == null && !a.mFinished && willBeVisible) {
            r.window = r.activity.getWindow();
            View decor = r.window.getDecorView();
            decor.setVisibility(View.INVISIBLE);
            ViewManager wm = a.getWindowManager();//2
            WindowManager.LayoutParams l = r.window.getAttributes();
            a.mDecor = decor;
            l.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
            l.softInputMode |= forwardBit;
            if (r.mPreserveWindow) {
                a.mWindowAdded = true;
                r.mPreserveWindow = false;
                // Normally the ViewRoot sets up callbacks with the Activity
                // in addView->ViewRootImpl#setView. If we are instead reusing
                // the decor view we have to notify the view root that the
                // callbacks may have changed.
                ViewRootImpl impl = decor.getViewRootImpl();
                ....
            }
            if (a.mVisibleFromClient) {
                if (!a.mWindowAdded) {
                    a.mWindowAdded = true;
                    wm.addView(decor, l);//3
                }
                ....
            }

           .....
        } 
        ......
        Looper.myQueue().addIdleHandler(new Idler());
    }
```
- 注释1处performResumeActivity最终会出发Activity生命周期方法 onResume 方法回调这里就不进行展开了
- 注释2处 获取了前面实例化的 WindowManagerImpl对象
- 注释3处通过WindowManager的addview方法 将 DecorView 与WindowManager 建立联系，也就是从addview方法触发了绘制界面，实际上调用的是WindowManagerImpl的 addview 方法，接着往下看

>frameworks/base/core/java/android/view/WindowManagerImpl.java
```
private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();
.....
  @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }
```
- WindowManagerImpl的addView方法调用了单例WindowManagerGlobal的addView方法

>frameworks/base/core/java/android/view/WindowManagerGlobal.java

```
private final ArrayList<View> mViews = new ArrayList<View>();
@UnsupportedAppUsage
private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
@UnsupportedAppUsage
private final ArrayList<WindowManager.LayoutParams> mParams = new ArrayList<WindowManager.LayoutParams>();
private final ArraySet<View> mDyingViews = new ArraySet<View>();
    
public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {
        ViewRootImpl root;
        View panelParentView = null;

           .......
           
           final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;//1


            root = new ViewRootImpl(view.getContext(), display); //2

            view.setLayoutParams(wparams);
            
            mViews.add(view);//3
            mRoots.add(root);//4
            mParams.add(wparams);//5

            // do this last because it fires off messages to start doing things
            try {
                root.setView(view, wparams, panelParentView);//6
            } catch (RuntimeException e) {
                // BadTokenException or InvalidDisplayException, clean up.
                if (index >= 0) {
                    removeViewLocked(index, true);
                }
                throw e;
            }
        }
    }
```
- 由以上源码，注释1 处获取布局参数对象
- 注释2处直接创建了ViewRootImpl对象，这里我们需要理解一下ViewRootImpl对象的作用，既然前面已经有了DecorView，为何还需要ViewRootImpl？我的理解是 **ViewRootImpl可以说是 WindowManager 管理 Window的中间人，每个ViewRootImpl对应一个Window，而WindowManager的实现最终都由单例WindowManagerGlobal来实现，而WindowManagerService管理WindowManagerGlobal，最终达到系统管理Window 的目的**
- 接下来注释3、4、5则是分别保存所有 Window 对应的View 集合，所有Window 对应的ViewRootImpl 集合，所有Window 对应的布局参数集合
- 接下来注释6 调用 ViewRootImpl 的 setView 将 DecorView 对应 Window 的绘制流程绑定继续执行，接着看ViewRootImpl得setView方法

## ViewRootImpl.setView方法
- setView方法中通过Binder对象mWindowSession与WindowManagerService IPC通信调用它的addToDisplay方进行页面绘制,目前先不进行展开（如下源码注释2）
>frameworks/base/core/java/android/view/ViewRootImpl.java
```
    /**
     * We have one child
     */
    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        synchronized (this) {
            if (mView == null) {
                mView = view;
            ......
            
                // Schedule the first layout -before- adding to the window
                // manager, to make sure we do the relayout before receiving
                // any other events from the system.
                requestLayout();//1
            .......        
                try {
                    mOrigWindowType = mWindowAttributes.type;
                    mAttachInfo.mRecomputeGlobalAttributes = true;
                    collectViewAttributes();
                    res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
                            getHostVisibility(), mDisplay.getDisplayId(), mTmpFrame,
                            mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
                            mAttachInfo.mOutsets, mAttachInfo.mDisplayCutout, mInputChannel,
                            mTempInsets);//2
                    setFrame(mTmpFrame);
                } catch (RemoteException e) {
                    ......
                } finally {
                    if (restore) {
                        attrs.restore();
                    }
                }
            ......
    }
```
- 而如上源码注释1，requestLayout方法为真正的 View 的的绘制入口
### ViewRootImpl.requestLayout

- requestLayout方法注释1处首先检查当前线程是否为UI线程，接着注释2处调用了scheduleTraversals方法，接着往下看

>frameworks/base/core/java/android/view/ViewRootImpl.java
```
    @Override
    public void requestLayout() {
        if (!mHandlingLayoutInLayoutRequest) {
            checkThread();//1
            mLayoutRequested = true;
            scheduleTraversals();//2
        }
    }
     void checkThread() {
        if (mThread != Thread.currentThread()) {
            throw new CalledFromWrongThreadException(
                    "Only the original thread that created a view hierarchy can touch its views.");
        }
    }
```
- 如下源码，ViewRootImpl的scheduleTraversals方法注释1处在主线程Handler 插入一个异步空消息，等到Vsync信号来时，Handler会优先执行这个异步消息，保证绘制的优先级
- 注释2执行了Choreographer.postCallback方法，Choreographer能够监听Vsync信号，当Vsync信号到来时执行callback，也就是执行mTraversalRunnable，它是Runnable对象
>frameworks/base/core/java/android/view/ViewRootImpl.java
```
//Choreographer.CALLBACK_INPUT       输入事件，比如键盘
//Choreographer.CALLBACK_ANIMATION   动画
//Choreographer.CALLBACK_TRAVERSAL   比如`ViewRootImpl.scheduleTraversals, layout or draw`
@UnsupportedAppUsage
    void scheduleTraversals() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true;
            mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();//1
            mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);//2
            if (!mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    }
```

- 接着看到TraversalRunnable的实现如下源码所示，它的run 方法调用了doTraversal方法如下

>frameworks/base/core/java/android/view/ViewRootImpl.java
```
final class TraversalRunnable implements Runnable {
        @Override
        public void run() {
            doTraversal();
        }
    }
```

>frameworks/base/core/java/android/view/ViewRootImpl.java
```
  void doTraversal() {
        if (mTraversalScheduled) {
            mTraversalScheduled = false;
            mHandler.getLooper().getQueue().removeSyncBarrier(mTraversalBarrier);//1
            .....
            performTraversals();//2
            ....
        }
    }
```
- ViewRootImpl的doTraversal方法如上所示，注释1处移除之前插入的异步消息，然后注释2处继续执行performTraversals方法

>frameworks/base/core/java/android/view/ViewRootImpl.java
```
  private void performTraversals() {
       .....
        // Ask host how big it wants to be
            windowSizeMayChange |= measureHierarchy(host, lp, res,
                    desiredWindowWidth, desiredWindowHeight);
       .....
       // Ask host how big it wants to be
       performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
      .....
      performLayout(lp, mWidth, mHeight);
      .....
      performDraw();
      .....
  }
```
- ViewRootImpl的 performTraversals 方法源码很长，这里精简一下如上所示依次调用 performMeasure、performLayout、performDraw完成View 的绘制逻辑，具体就先不展开了，本文先注重流程。

### Window绘制流程时序图

![Window绘制流程时序图](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5fb48129452d41558a2d1b9fb78347f1~tplv-k3u1fbpfcp-zoom-1.image)

## 总结
![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/73a93c611b804c7090ce58c834b082cb~tplv-k3u1fbpfcp-zoom-1.image) 
