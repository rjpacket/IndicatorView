### 一、先看效果

![viewpager指示器.gif](http://upload-images.jianshu.io/upload_images/5994029-178321f2b401f9a6.gif?imageMogr2/auto-orient/strip)


### 二、分析
[博客](https://juejin.im/post/59ed6453f265da43200265fb) 上看到这个动画效果很不错，决定自己来写试试。
动画拆开来看就是Touch的时候不断的绘制竖线和数字，很简单的自定义View，相信如果你看了前面的几篇博客，这个手到擒来，但是也还有很多细节需要注意。

### 三、代码
因为全部是 for 循环的绘制，抽成对象反而多余了，直接继承View：
```
    /**
     * 尺图
     */
    public class RulerView extends View {
        private Context mContext;
        private Paint centerLinePaint;
        private Paint grayLinePaint;
        private Paint txtPaint;
        private int space = 20; // 竖线间空隙
        private int startValue = 40; // 起始刻度
        private int endValue = 100; // 结束刻度
        private int width;
        private int height;
        private int currentOffset; // 当前操作偏移量
        private int totalOffset; // 之前操作偏移量
        private float downX;
        private float lastMoveX;
        private int touchSlop;
    
        public RulerView(Context context) {
            this(context, null);
        }
    
        public RulerView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init(context, attrs);
        }
    
        private void init(Context context, AttributeSet attrs) {
            mContext = context;
            centerLinePaint = new Paint();
            centerLinePaint.setAntiAlias(true);
            centerLinePaint.setColor(Color.parseColor("#49BA72"));
            centerLinePaint.setStrokeWidth(3);
    
            grayLinePaint = new Paint();
            grayLinePaint.setAntiAlias(true);
            grayLinePaint.setColor(Color.parseColor("#66666666"));
            grayLinePaint.setStrokeWidth(3);
    
            txtPaint = new Paint();
            txtPaint.setAntiAlias(true);
            txtPaint.setColor(Color.parseColor("#333333"));
            txtPaint.setTextSize(30);
    
            touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        }
    }
```
定义一些属性。有两个属性需要注意，currentOffset 和 totalOffset ！这是什么意思呢？当前的偏移和总共的偏移，因为你在触摸往右移动的时候，尺子要从一个偏移量的位置开始绘制，然后下一次再触摸向右移动的时候，不但要加上当前的偏移，还要加上之前的偏移，这样你的尺子才是随着手指滑动的。
再看 onMeasure() 方法：
```
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if(widthMode == MeasureSpec.EXACTLY){
            width = MeasureSpec.getSize(widthMeasureSpec);
        }else{
            width = mContext.getResources().getDisplayMetrics().widthPixels;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if(heightMode == MeasureSpec.EXACTLY){
            height = MeasureSpec.getSize(heightMeasureSpec);
        }else{
            height = (int) (mContext.getResources().getDisplayMetrics().density * 60 + 0.5);
        }
        setMeasuredDimension(width, height);
    }
```
这段代码很简单，如果你 xml 设置了 MeasureSpec.EXACTLY 的模式，也就是宽度是 match_parent 或者固定的值，那么我们就取这个值；如果是 wrap_content ，我们通过测量值一直是0，这个时候让它的宽度等于手机屏幕。同理高度我们也设置一个固定的值。然后就是绘制了：
```
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = startValue * 10; i < endValue * 10 + 1; i++) {
            int lineHeight = 60;
            if(i % 5 == 0){
                if(i % 10 == 0){
                    lineHeight = 80;
                    int x = (i - startValue * 10) * space + currentOffset + totalOffset;
                    if(x > 0 || x < width) {
                        canvas.drawText(String.valueOf(i / 10), x, lineHeight + 30, txtPaint);
                    }
                }
            }else{
                lineHeight = 30;
            }
            int startX = (i - startValue * 10) * space + currentOffset + totalOffset;
            if(startX > 0 || startX < width) {
                canvas.drawLine(startX, 0, startX, lineHeight, grayLinePaint);
            }
        }
        canvas.drawLine(width / 2, 0, width / 2, 120, centerLinePaint);
    }
```
代码不多，我们一行行的来看。我们只知道起始值和终止值，但是中间还有小刻度，比如0.1，所以我们需要将起始值乘以10，这样就能进行循环了。
循环里面我们判断一下，如果当前 
> index % 10 == 0 

代表是大刻度，将这个线的高度设置最高 80，如果 
> index % 5 == 0 

代表当前是中间刻度，我们将高度设置为 60，否则都是小刻度，高度就设置为 30 好了。（这里说明一下，这个刻度可以写成属性，自己定制）。
然后得到高度之后，我们下面的代码就是绘制刻度线：
```
    int startX = (i - startValue * 10) * space + currentOffset + totalOffset;
    if(startX > 0 || startX < width) {
        canvas.drawLine(startX, 0, startX, lineHeight, grayLinePaint);
    }
```

这里有个细节，绘制刻度并不是所有的都要绘制，超出屏幕的就不需要处理了。

刻度绘制的过程中，顺便绘制数字：
```
    int x = (i - startValue * 10) * space + currentOffset + totalOffset;
    if(x > 0 || x < width) {
        canvas.drawText(String.valueOf(i / 10), x, lineHeight + 30, txtPaint);
    }
```
同样的道理，超出屏幕的数字不去管它。

绘制的部分写完了，接下来就是 onTouchEvent() 的处理了：
```
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                currentOffset = (int) (moveX - downX);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                currentOffset = 0;
                totalOffset += event.getX() - downX;
                if(totalOffset > width / 2){
                    totalOffset = width / 2;
                }else if(totalOffset < width / 2 - (endValue - startValue) * space * 10){
                    totalOffset = width / 2 - (endValue - startValue) * space * 10;
                }
                if(totalOffset % space != 0){
                    totalOffset -= totalOffset % space;
                }
                invalidate();
                break;
        }
        return true;
    }
```
得到当前偏移，不断的 invalidate() 重新绘制。有一点需要注意手指抬起的时候，一定要执行 

> currentOffset = 0; 

这句话，这句话不写，会有很大的问题，刻度闪烁，原因我还不清楚，但是加上没有问题。

再看 ACTION_UP 和 ACTION_CANCEL 里面的其他代码，1~6行都是处理边界问题的，滑到看不到刻度的时候自动回弹。7 8 9行代码是让正中间的蓝色刻度可以正好和尺子上的刻度对齐。

到这基本结束。

附上 [github地址](https://github.com/rjpacket/IndicatorView)
