### 一、先看效果

![ruler-view.gif](http://upload-images.jianshu.io/upload_images/5994029-49b1efeedfe2de4f.gif?imageMogr2/auto-orient/strip)


### 二、分析
[上一篇博客](http://www.jianshu.com/p/4a497e875928) 我们绘制了薄荷健康的直尺效果，可以说只是简单的绘制，并没有交互的操作，例如手势滑动，数值回调。这一篇我们来完善一下。

首先是手势滑动，如果还用上一篇的写法，不好处理，惯性滑动的话我们想到的是 Scroller 这个辅助类以及速度追踪器。 Scroller 很熟悉，自定义 View 的滑动经常用到，就是计算一系列的数值，然后调用 scrollTo() 这个方法将 View 滚动到确定的位置，写法都是固定的，参考百度。

重点说说速度追踪器 VelocityTracker，这个类干嘛的？我也不清楚，找了 [一篇博客](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2012/1114/558.html) 观察一下。用法很详细，大致了解了一下，但是博客里有几个重要的参数没有说明，后面重点提。

上面的 gif 图中间有一条绿线，这个绿线认为是基准线，代码里用偏移量表示。

### 三、代码
相比较上一篇的代码，我们需要修改几个地方，首先是初始化：
```
    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        centerLinePaint = new Paint();
        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setColor(Color.parseColor("#49BA72"));
        centerLinePaint.setStrokeWidth(5);

        grayLinePaint = new Paint();
        grayLinePaint.setAntiAlias(true);
        grayLinePaint.setColor(Color.parseColor("#66666666"));
        grayLinePaint.setStrokeWidth(5);

        txtPaint = new Paint();
        txtPaint.setAntiAlias(true);
        txtPaint.setColor(Color.parseColor("#333333"));
        txtPaint.setTextSize(50);
        
        // 新增部分
        ViewConfiguration viewConfiguration = ViewConfiguration.get(mContext);
        // 最小响应距离
        touchSlop = viewConfiguration.getScaledTouchSlop();
        mScroller = new Scroller(mContext);
        // 惯性滑动最低速度要求 低于这个速度认为是触摸
        mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        // 惯性滑动的最大速度  触摸速度不会超过这个值 
        mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
    }
```
新增的部分标注出来了。然后是触摸部分 onTouchEvent()：
```
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        obtainVelocityTracker();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                isFastScroll = false;
                float moveX = event.getX();
                currentOffset = (int) (moveX - mLastX);
                scrollTo(getScrollX() - currentOffset, 0);
                computeAndCallback(getScrollX());
                mLastX = moveX;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) mVelocityTracker.getXVelocity();
                if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                    isFastScroll = true;
                    flingX(-initialVelocity);
                } else {
                    int x = getScrollX();
                    if (x % space != 0) {
                        x -= x % space;
                    }
                    if (x < -BASELINE_OFFSET) {
                        x = -BASELINE_OFFSET + BASELINE_OFFSET % space;
                    } else if (x > (endValue - startValue) * space * 10 - BASELINE_OFFSET) {
                        x = (endValue - startValue) * space * 10 - BASELINE_OFFSET + BASELINE_OFFSET % space;
                    }
                    scrollTo(x, 0);
                    computeAndCallback(x);
                }
                releaseVelocityTracker();
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }
        return true;
    }
```
很长，一行行分析。
```
    case MotionEvent.ACTION_DOWN:
         mLastX = event.getX();
         if (!mScroller.isFinished()) {
             mScroller.abortAnimation();
         }
         break;
         
```
记录按下的位置，然后如果上一次的动画还在继续，立即停止。
再看 MOVE 里面：
```
    case MotionEvent.ACTION_MOVE:
         isFastScroll = false;
         float moveX = event.getX();
         currentOffset = (int) (moveX - mLastX);
         scrollTo(getScrollX() - currentOffset, 0);
         computeAndCallback(getScrollX());
         mLastX = moveX;
         break;
```
第一个布尔值是标记是否正在惯性滑动，在后面会用到。为什么在这里置为false？因为触摸的时候不可能在惯性滑动。然后计算每一次触摸的偏移，调用 scrollTo() 不断的让自己（View 本身）滚动。
后面的 computeAndCallback() 方法暂时可以不看。最后还要记下每一次 MOVE 的坐标，因为是计算每一次的偏移的，不是总的偏移。

最后看 UP 和 CANCEL 事件：
```
     case MotionEvent.ACTION_CANCEL:
         mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
         int initialVelocity = (int) mVelocityTracker.getXVelocity();
         if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
             isFastScroll = true;
             flingX(-initialVelocity);
         } else {
             int x = getScrollX();
             if (x % space != 0) {
                 x -= x % space;
             }
             if (x < -BASELINE_OFFSET) {
                 x = -BASELINE_OFFSET + BASELINE_OFFSET % space;
             } else if (x > (endValue - startValue) * space * 10 - BASELINE_OFFSET) {
                 x = (endValue - startValue) * space * 10 - BASELINE_OFFSET + BASELINE_OFFSET % space;
             }
             scrollTo(x, 0);
             computeAndCallback(x);
         }
         releaseVelocityTracker();
         break;
```
第1行 computeCurrentVelocity() 方法是手指离开屏幕的瞬间去计算 View 在手机 x-y 方向的速度值；
第2行 getXVelocity() 方法获得 X 轴方向的速度值 initialVelocity；
第3行 判断速度是否大于最低 mMinimumVelocity 要求，满足的话，认为需要惯性滑动，调用方法 flingX()：
```
    /**
     * 惯性滑动
     *
     * @param velocityX
     */
    public void flingX(int velocityX) {
        mScroller.fling(getScrollX(), getScrollY(), velocityX, 0, -BASELINE_OFFSET, (endValue - startValue) * space * 10 - BASELINE_OFFSET, 0, 0);
        awakenScrollBars(mScroller.getDuration());
        invalidate();
    }
    
```
上面这个方法就是惯性滑动的重点所在。有了初速度，调用 mScroller.fling() 方法交给 Scroller 处理。这里要注意 fling() 方法的8个参数分别代表什么，12参数代表滚动开始的位置，34参数代表这个方向上的初速度，56参数代表X滚动的范围，78参数代表Y滚动的范围。

第6行 也就是 else 不满足最小滚动速度的时候，认为是触摸事件的抬起，这个时候我们需要手动的将 View 的刻度线滚动到基准线的位置，因为滚动的时候可能基准线位于两根刻度线之间，这个时候需要校准：
```
    int x = getScrollX();
    if (x % space != 0) {
        x -= x % space;
    }
    if (x < -BASELINE_OFFSET) {
        x = -BASELINE_OFFSET + BASELINE_OFFSET % space;
    } else if (x > (endValue - startValue) * space * 10 - BASELINE_OFFSET) {
        x = (endValue - startValue) * space * 10 - BASELINE_OFFSET + BASELINE_OFFSET % space;
    }
    scrollTo(x, 0);
    computeAndCallback(x);
```
首先获取滚动的长度，如果对 space 取余有余，说明基准线在两个刻度之间，需要减去

附上 [简书地址](http://www.jianshu.com/p/4a497e875928)
