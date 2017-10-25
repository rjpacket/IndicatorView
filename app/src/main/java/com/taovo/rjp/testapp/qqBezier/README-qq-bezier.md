### 一、先看效果

![viewpager指示器.gif](http://upload-images.jianshu.io/upload_images/5994029-178321f2b401f9a6.gif?imageMogr2/auto-orient/strip)


### 二、分析
昨天写了贝塞尔的动画效果，非常好玩。今天突然想到qq的消息气泡，点击拖拽有粘性，也能用贝塞尔曲线实现。知道思路，但是不知道从哪下手，百度了一篇博客，大致了解了整个过程。
感谢 [猴菇同学的博客](http://blog.csdn.net/qq_31715429/article/details/54386934)的分享，借鉴了他的思路，代码就自己敲了一遍，计算方法有点不一样，他是利用角度计算，我利用相似三角形计算，路子不同但最终目的都是一样的，看代码。

### 三、代码分析
结合[猴菇同学的博客](http://blog.csdn.net/qq_31715429/article/details/54386934)的思路，我发现就是计算所有的点，然后绘制这些点，那么我们可以建立一个类来记录这些点的坐标，首先是 ControllerView 类：
```
    /**
     * 控制器类
     */
    public class ControllerView {
        private int x;
        private int y;
        private int radius;
    
        private int saX;
        private int saY;
        private int eaX;
        private int eaY;
    
        private int sbX;
        private int sbY;
        private int ebX;
        private int ebY;
    
        private int px;
        private int py;
    }
```
接下来自定义view：
```
    /**
     * qq 气泡view
     */
    public class QQNumberView extends View {
        private Context mContext;
        private int number;
        private int radius = 40; // 红点的半径
        private int minWidth = 10; // 红点最小半径
        private int maxDistance = 200; // 最长能扯多远
        private Paint controlPaint;
        private Paint numberPaint;
        private float flexRatio = 0.6f; // 弹性系数
        private ControllerView controllerView;
        private Path bezierPath;
        private boolean mIsDrag = false; // 是否正在拖拽
        private boolean mIsMoreDrag = false; // 是否过度拖拽
    
        public QQNumberView(Context context) {
            this(context, null);
        }
    
        public QQNumberView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init(context, attrs);
        }
    
        private void init(Context context, AttributeSet attrs) {
            mContext = context;
            controlPaint = new Paint();
            controlPaint.setAntiAlias(true);
            controlPaint.setColor(Color.parseColor("#eb1c42"));
    
            numberPaint = new Paint();
            numberPaint.setAntiAlias(true);
            numberPaint.setColor(Color.WHITE);
            numberPaint.setTextSize(32);
    
            controllerView = new ControllerView();
        }
    }
```
定义一些必要的属性和初始化工作。然后我们需要修改 onTouchEvent() 方法：
```
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(!mIsMoreDrag) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mIsDrag = true;
                }else{
                    return super.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                compute(event.getX() * flexRatio, event.getY() * flexRatio);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                mIsDrag = false;
                resetView(event.getX() * flexRatio, event.getY() * flexRatio);
                break;
        }
        return true;
    }
```
ACTION_DOWN 的时候，如果已经拖拽过了（用 mIsMoreDrag 标记），直接

> return super.onTouchEvent(event);

表示不再处理 touch 事件，否则首次 touch 的时候，我们需要禁止父类拦截并且将拖拽标记 mIsDrag 置为 true ：

> getParent().requestDisallowInterceptTouchEvent(true);
> mIsDrag = true;

这样我们在 ACTION_MOVE 里面可以直接处理手指的移动事件，而外面包裹的 listview 拿不到滑动事件。我们再看看 compute() 方法怎么处理的：
```
    /**
     * 计算所有需要的位置坐标
     * @param x
     * @param y
     */
    private void compute(float x, float y) {
        // 记录控制点圆心坐标
        controllerView.setX((int) x);
        controllerView.setY((int) y);
        float d = (float) Math.sqrt((x - radius) * (x - radius) + (y - radius) * (y - radius));
        if (!mIsMoreDrag && d >= maxDistance) {
            controllerView.reset();
            mIsMoreDrag = true;
            invalidate();
            return;
        }
        // 锚点的半径 线性变化
        int anchorRadius = (int) (radius - d / radius);
        if (anchorRadius <= minWidth) {
            anchorRadius = minWidth;
        }
        // 记录锚点的半径
        controllerView.setRadius(anchorRadius);
        // 计算a线起点终点
        float saX = x - (y - radius) * radius / d;
        float saY = y - (radius - x) * radius / d;
        float eaX = radius - (y - radius) * anchorRadius / d;
        float eaY = radius - (radius - x) * anchorRadius / d;
        // 计算b线起点终点
        float sbX = x + (y - radius) * radius / d;
        float sbY = y + (radius - x) * radius / d;
        float ebX = radius + (y - radius) * anchorRadius / d;
        float ebY = radius + (radius - x) * anchorRadius / d;
        // 记录a线起点终点坐标
        controllerView.setSaX((int) saX);
        controllerView.setSaY((int) saY);
        controllerView.setEaX((int) eaX);
        controllerView.setEaY((int) eaY);
        // 记录b线起点终点坐标
        controllerView.setSbX((int) sbX);
        controllerView.setSbY((int) sbY);
        controllerView.setEbX((int) ebX);
        controllerView.setEbY((int) ebY);
        // 计算控制点的半径
        float pX = (x + radius) / 2;
        float pY = (y + radius) / 2;
        // 记录控制点的坐标
        controllerView.setPx((int) pX);
        controllerView.setPy((int) pY);
        // 刷新
        invalidate();
    }
```
注释已经写的很清楚了，但是需要注意里面的一段代码：
```
        if (!mIsMoreDrag && d >= maxDistance) {
            controllerView.reset();
            mIsMoreDrag = true;
            invalidate();
            return;
        }
```
这个表示如果之前没有拖拽过并且拖拽距离超过了设定的值，我们需要将记录的数据清掉，mIsMoreDrag 置为 true，然后刷新。reset() 的方法写在了控制器类里面：
```
    public void reset() {
        radius = 0;

        saX = 0;
        saY = 0;
        eaX = 0;
        eaY = 0;

        sbX = 0;
        sbY = 0;
        ebX = 0;
        ebY = 0;

        px = 0;
        py = 0;
    }
```

回到 onTouchEvent() 方法中，我们在 ACTION_UP 的时候需要允许父类拦截事件，外面的 listview 可以继续滑动：
```
    getParent().requestDisallowInterceptTouchEvent(false);
    mIsDrag = false;
    resetView(event.getX() * flexRatio, event.getY() * flexRatio);
    
```
同时重置页面的状态，比如拉到一半我们抬起手指，可以让球回到原始位置，或者拉到最远位置，这个时候我们认为用户想清掉球，那么就让他消失。消失我们可以不让 canvas 继续绘制，但是拉到一半怎么让它平滑的滚回起始位置呢？看 reset() 方法：
```
    /**
     * 手指抬起，view恢复 
     */
    private void resetView(float x, float y) {
        Point startPoint = new Point((int) x, (int) y);
        Point endPoint = new Point(radius, radius);
        ValueAnimator numberAnim = ValueAnimator.ofObject(new NumberEvaluator(), startPoint, endPoint);
        numberAnim.setDuration(3000);
        numberAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point p = (Point) animation.getAnimatedValue();
                compute(p.x, p.y);
            }
        });
    }
```
对，就是使用的估值器，因为你手指抬起的时候，已经不能产生连续的点坐标去给 canvas 绘制了，这个时候怎么办呢？估值器去帮我们做这件事，告诉估值器起点和终点，它就会一直返回我们中间值。

所有的事情都准备的差不多了，来看看重点 onDraw() 是怎么绘制的吧：
```
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsMoreDrag) {
            if (mIsDrag) {
                canvas.drawCircle(controllerView.getX(), controllerView.getY(), radius, controlPaint);
                canvas.drawText(String.valueOf(number), controllerView.getX(), controllerView.getY(), numberPaint);
            }
        } else {
            if (mIsDrag) {
                bezierPath = new Path();
                bezierPath.moveTo(controllerView.getSaX(), controllerView.getSaY());
                bezierPath.quadTo(controllerView.getPx(), controllerView.getPy(), controllerView.getEaX(), controllerView.getEaY());
                bezierPath.lineTo(controllerView.getEbX(), controllerView.getEbY());
                bezierPath.quadTo(controllerView.getPx(), controllerView.getPy(), controllerView.getSbX(), controllerView.getSbY());
                bezierPath.lineTo(controllerView.getSaX(), controllerView.getSaY());
                canvas.drawPath(bezierPath, controlPaint);
                canvas.drawCircle(radius, radius, controllerView.getRadius(), controlPaint);
                canvas.drawCircle(controllerView.getX(), controllerView.getY(), radius, controlPaint);
                canvas.drawText(String.valueOf(number), controllerView.getX(), controllerView.getY(), numberPaint);
            } else {
                canvas.drawCircle(radius, radius, radius, controlPaint);
                canvas.drawText(String.valueOf(number), radius, radius, numberPaint);
            }
        }
    }
```
这里我们需要分开考虑，在没有过度拖拽的情况下，我们走的是外层的 else 下面的代码，如果没有拖拽就是

> canvas.drawCircle(radius, radius, radius, controlPaint);
> canvas.drawText(String.valueOf(number), radius, radius, numberPaint);

想当于初始化小球。然后拖拽的过程中我们需要绘制贝塞尔曲线、拖拽的球、锚点的球和文字：

> canvas.drawPath(bezierPath, controlPaint);
> canvas.drawCircle(radius, radius, controllerView.getRadius(), controlPaint);
> canvas.drawCircle(controllerView.getX(), controllerView.getY(), radius, controlPaint);
> canvas.drawText(String.valueOf(number), controllerView.getX(), controllerView.getY(), numberPaint);

如果是过度拖拽，也有两种情况，一种是手指松开了，一种是手指还在屏幕移动。手指还没松开的时候我们还要去绘制拖拽的球，但是松开的话，else 里面什么都不需要干了。

到这就全部结束了。

附上 [github地址](https://github.com/rjpacket/IndicatorView/tree/qq-bezier-master)
