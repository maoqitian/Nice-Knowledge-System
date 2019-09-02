## 自定义CoordinatorLayout.Behavior
```
/**
 * @author maoqitian
 * @Description: 自定义 FloatingActionButton 滑动显示与隐藏 结合 RecyclerView
 * 使用前提是 fab 包含在 CoordinatorLayout中
 * @date 2019/6/21 0021 9:17
 */
public class ScrollAwareFABBehavior2 extends CoordinatorLayout.Behavior<FloatingActionButton>  {

    private static final String TAG = "ScrollAwareFABBehavior2";

    private boolean fabAnimationStarted = false; //是否开始 fab 动画
    private boolean flingHappened = false;

    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    private boolean mIsAnimatingOut = false;
    
    public ScrollAwareFABBehavior2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 如果是 RecyclerView 拦截嵌套滑动
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {

        if (target instanceof RecyclerView) {
            return true;
        }
        return false;
    }
    //滑动停止
    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull final FloatingActionButton child, @NonNull View target, int type) {

        // If animation didn't start, we don't need to care about running the restore animation.
        // i.e.: when the user swipes to another tab in a viewpager. The onNestedPreScroll is never called.
        if (!fabAnimationStarted) {
            return;
        }

        // Animate back when the fling ended (TYPE_NON_TOUCH：非手指触碰手势输入类型，通常是手指离开屏幕后的惯性滑动事件)
        // or if the user made the touch up (TYPE_TOUCH：正常的屏幕触控驱动事件) but the fling didn't happen.
        if (type == ViewCompat.TYPE_NON_TOUCH || (type == ViewCompat.TYPE_TOUCH && !flingHappened)) {
            //ViewCompat.animate(child).translationY(0).start();
            fabAnimationStarted = false;
        }
    }

    //当进行快速滑动
    @Override
    public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, float velocityX, float velocityY, boolean consumed) {

        // We got a fling. Flag it.
        flingHappened = true;
        return false;

    }
    //进行滑动事件处理

    /**
     * 接收子View处理完滑动后的滑动距离信息, 在这里父控件可以选择是否处理剩余的滑动距离。如果想要该方法得到回调，先前的onStartNestedScroll(View, View, int, int)必须返回true
     * @param coordinatorLayout
     * @param child
     * @param target 触发嵌套滑动的 view
     * @param dxConsumed 表示 view 消费了 x 方向的距离
     * @param dyConsumed 表示 view 消费了 y 方向的距离
     * @param dxUnconsumed 表示 view 剩余未消费 x 方向距离
     * @param dyUnconsumed 表示 view 剩余未消费 y 方向距离
     * @param type 触发滑动事件的类型：其值有
     * ViewCompat. TYPE_TOUCH
     * ViewCompat. TYPE_NON_TOUCH
     */
    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    }

    /**
     * 在子View消费滑动事件前，优先响应滑动操作，消费部分或全部滑动距离,提前处理执行动画
     * @param coordinatorLayout
     * @param child
     * @param target 触发嵌套滑动的 view
     * @param dx 表示 view 本次 x 方向的滚动的总距离，单位：像素
     * @param dy 表示 view 本次 y 方向的滚动的总距离，单位：像素
     * @param consumed
     * @param type 触发滑动事件的类型：其值有
     * ViewCompat. TYPE_TOUCH
     * ViewCompat. TYPE_NON_TOUCH
     */
    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (!fabAnimationStarted) {
            Log.d(TAG, "onStartNestedScroll: animation is starting");
            fabAnimationStarted = true;
            flingHappened = false;
            /*CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

            ViewCompat.animate(child).translationY(child.getHeight() + lp.bottomMargin).start();*/

            if (dy > 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
                // 不显示FAB
                animateOut(child);

            }
            else if (dy < 0 && child.getVisibility() != View.VISIBLE) {
                // 显示FAB
                animateIn(child);

            }
            if(target instanceof RecyclerView && child.getVisibility() == View.VISIBLE){
                //RecyclerView 滑动到顶部隐藏 fab
                RecyclerView recyclerView = (RecyclerView) target;
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        //newState分 0,1,2三个状态,2是滚动状态,0是停止
                        super.onScrollStateChanged(recyclerView, newState);
                        //-1代表顶部,返回true表示没到顶,还可以滑
                        //1代表底部,返回true表示没到底部,还可以滑
                        boolean b = recyclerView.canScrollVertically(-1);
                        if(!b){
                            // 不显示FAB
                            animateOut(child);
                        }
                    }
                });

            }
        }
    }

    // 定义滑动时的属性动画效果 隐藏
    @SuppressLint("RestrictedApi")
    private void animateOut(final FloatingActionButton button) {
        ViewCompat.animate(button).scaleX(0.0F).scaleY(0.0F).alpha(0.0F).setInterpolator(INTERPOLATOR).withLayer()
                .setListener(new ViewPropertyAnimatorListener() {
                    public void onAnimationStart(View view) {
                        ScrollAwareFABBehavior2.this.mIsAnimatingOut = true;
                    }

                    public void onAnimationCancel(View view) {
                        ScrollAwareFABBehavior2.this.mIsAnimatingOut = false;
                    }

                    public void onAnimationEnd(View view) {
                        ScrollAwareFABBehavior2.this.mIsAnimatingOut = false;
                        view.setVisibility(View.INVISIBLE);
                    }
                }).start();

    }

    //显示
    @SuppressLint("RestrictedApi")
    private void animateIn(FloatingActionButton button) {
        button.setVisibility(View.VISIBLE);

        ViewCompat.animate(button).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                .start();

    }
}
```
## 使用

```
    <string name="scroll_aware_fab_behavior" >该类路径.ScrollAwareFABBehavior2</string>

    布局文件中直接引用 
    app:layout_behavior="@string/scroll_aware_fab_behavior"
```

## 参考链接

[关于CoordinatorLayout与Behavior的一点分析](https://www.jianshu.com/p/a506ee4afecb)

[Material Design系列教程（5） - NestedScrollView](https://www.jianshu.com/p/f55abc60a879)