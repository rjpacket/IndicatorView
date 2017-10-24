### 一、先看效果

![viewpager指示器.gif](http://upload-images.jianshu.io/upload_images/5994029-178321f2b401f9a6.gif?imageMogr2/auto-orient/strip)


### 二、分析
这个效果用动画来写，并不是很好实现。可以考虑用自定义view，自定义view只需要不断的重绘view，看起来和动画没有区别。
自定义view的话，我们来分析view的属性：
1. 圆点的半径 (知道半径，高度也就知道了)
2. 圆之间的padding
3. 圆点个数
4. 圆形view
5. 滚动view (下面称呼为BarView)
大概就这么多，其他的不需要了。再来看3,4两个view的属性：
> 3.1 圆心
> 3.2 半径
> 3.3 是否选中 (涉及到填充颜色)

BarView我们可以看成首尾两个圆，中间一个矩形的组合体：
> 4.1 左边圆的圆心和半径
> 4.2 右边圆的圆心和半径 (半径和左边的圆一致)
> 4.3 矩形的宽度高度 (这个并不需要，已知左右圆，宽高都是可以求出来的)

全部需要的条件就这么多，接下来是代码分析了。

### 三、代码实现
首先建立两个对象，一个圆点对象，一个滑块对象：
```

/**
 * 圆点对象
 */
public class PointView {
    private int x;
    private int y;
    private int radius;

    private boolean isChecked;
}

/**
 * 滑块对象
 */
public class BarView {
    private int leftX;
    private int leftY;

    private int rightX;
    private int rightY;

    private int radius;
}

```
可以说，这两个对象建立起来，这个view已经完成一半了。(这里不一定一开始想的很清楚，后面可以逐渐的完善这两个类)

首先当然继承自view， 初始化工作：
```
    public class IndicatorView extends View {
        private Context mContext;
        private Paint pointPaint;
        private int childCount;
        private Paint selectPointPaint;
        private int selectPosition;
        private int scrollPosition;  //这个是滑动的时候 要到达的position
        private float ratio;
        private int pointSpace = 80; // 球之间的空隙
        private int radius = 40; // 球半径
        private List<PointView> pointViews;
        private BarView barView;
    
        public IndicatorView(Context context) {
            this(context, null);
        }
    
        public IndicatorView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init(context, attrs);
        }
    
        private void init(Context context, AttributeSet attrs) {
            mContext = context;
            pointPaint = new Paint();
            pointPaint.setAntiAlias(true);
            pointPaint.setColor(Color.GRAY);
    
            selectPointPaint = new Paint();
            selectPointPaint.setAntiAlias(true);
            selectPointPaint.setColor(Color.parseColor("#eb1c42"));
        }
    }
```
这个时候需要绘制了，但是从哪里绘制呢？我们这个view是配合viewpager使用的，所以需要暴漏一个方法，设置一个viewpager进来：
```
/**
     * 关联viewpager
     * @param viewPager
     */
    public void setViewPager(ViewPager viewPager) {
        childCount = viewPager.getAdapter().getCount();
        initPoints();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                scrollPosition = position;
                Log.d("------->", "position:" + position + ", positionOffset:" + positionOffset + ", positionOffsetPixels:" + positionOffsetPixels);
                ratio = positionOffset;
                if(ratio >= 1 || ratio <= 0){
                    return;
                }
                compute();
                invalidate();
            }

            @Override
            public void onPageSelected(int position) {
                selectPosition = position;
                ratio = 0;
                compute();
                invalidate();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ratio = 0;
        compute();
        invalidate();
    }
```
当viewpager设置进来的时候，我们第一步拿到页面的个数，去initPoints()初始化小圆点：
```
/**
     * 初始化点
     */
    private void initPoints() {
        pointViews = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            PointView pointView = new PointView();
            pointViews.add(pointView);
        }
        //顺便初始化bar
        barView = new BarView();
    }
```
然后监听viewpager的滚动，在滚动里面去计算BarView的位置和设置圆点的选中状态。
在监听方法里面，需要注意 scrollPosition 和 selectPosition 的区别，这个 scrollPosition 是页面将要到达的页面的 index,而 selectPosition 是记录当前选中圆点的index。两者的区别比较大，用处也不一样。这个 scrollPosition 我们在后面还需要用到。
监听方法里面有个 compute() 计算方法：
```

    /**
     * 绘制之前准备， 这个计算一定要在canvas外面计算
     */
    private void compute() {
        //计算球位置
        for (int i = 0; i < pointViews.size(); i++) {
            PointView pointView = pointViews.get(i);
            pointView.setRadius(radius);
            pointView.setX(radius * (2 * i + 1) + pointSpace* i);
            pointView.setY(radius);
            pointView.setChecked(selectPosition == i);
        }
        //计算bar位置
        PointView selectPointView = pointViews.get(selectPosition);
        int selectX = selectPointView.getX();
        int selectY = selectPointView.getY();
        if(selectPosition <= scrollPosition){
            //往右是增加右边圆的圆心
            barView.setLeftX(selectX);
            barView.setLeftY(selectY);
            barView.setRightX((int) (selectX + (2 * radius + pointSpace) * ratio));
            barView.setRightY(selectY);
            barView.setRadius(radius);
        }else{
            //往左是减少左边圆的圆心
            barView.setRightX(selectX);
            barView.setRightY(selectY);
            barView.setLeftX((int) (selectX - (2 * radius + pointSpace) * (1 - ratio)));
            barView.setLeftY(selectY);
            barView.setRadius(radius);
        }
    }
    
```
圆点（也就是小球）的位置很好计算，半径圆心和选中状态，几乎计算一遍就行。主要是BarView的位置，它是随着viewpager滚动而改变的，viewpager滚动有向左和向右两个
方向，这个时候 scrollPosition 就起到关键性的作用了，往右，这个 index 是大于当前 selectPosition 的，往左，这个 index 是小于或者等于(经过打印是等于，严谨点，小于也判断了) selectPosition 的，
那就有两种计算方法，往右，左边圆心等于选中圆的圆心，右边圆心等于选中圆心加上偏移量，偏移量可以计算得出，代码如上，往左，右边圆心是等于选中圆的圆心，左边圆心是选中圆心减去偏移量。
计算完调用 invalidate()，自动重绘页面，调用 view 的 onDraw() 方法。接下来就是重点的onDraw()方法：
```

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画圆点
        for (PointView pointView : pointViews) {
            canvas.drawCircle(pointView.getX(), pointView.getY(), pointView.getRadius(), pointView.isChecked()?selectPointPaint:pointPaint);
        }
        //画bar的头尾圆
        canvas.drawCircle(barView.getLeftX(), barView.getLeftY(), barView.getRadius(), selectPointPaint);
        canvas.drawCircle(barView.getRightX(), barView.getRightY(), barView.getRadius(), selectPointPaint);
        //画bar的中间rect
        canvas.drawRect(new RectF(barView.getLeftX(), 0, barView.getRightX(), radius * 2), selectPointPaint);
    }
    
```
是不是很简单？为什么重点部分这么少代码，因为计算已经在外面计算过了，这里只需要把数据拿来绘制一下就好了。
到这是不是结束了，不，自定义view除了 onDraw() 这个关键方法还有 onMeaure() 没有用到，如果不计算view的高度，默认是充满屏幕的，就无法动态放置 view，所以最后：
```
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置view宽高 不设置就是默认全屏view，没办法改变位置
        if(pointViews != null && pointViews.size() > 0) {
            int width = pointViews.get(pointViews.size() - 1).getX() + radius;
            int height = radius * 2;
            setMeasuredDimension(width, height);
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
```
到这就结束了。当然还可以添加属性，直接在xml文件就可以设置圆点 padding 和圆点半径，这些就交给你去拓展了。附上 [简书地址](http://www.jianshu.com/p/1235c5d37d73)


### 一、先看效果

![viewpager指示器-Bezier.gif](http://upload-images.jianshu.io/upload_images/5994029-8b3a736b36722387.gif?imageMogr2/auto-orient/strip)

### 二、分析
结合[前一篇](http://www.jianshu.com/p/1235c5d37d73) 写法，其他的内容不变，只需要改变BarView的绘制就可以:

>  1. 绘制左边圆
>  2. 绘制右边圆
>  3. 绘制贝塞尔曲线合成的图像

从上面gif图中分析，需要两条二阶贝塞尔曲线，一条曲线向下凹a线，一条向上凸b线。
绘制贝塞尔曲线需要一个起点、一个终点和一个控制点。
a线起始点Sa是左边圆顶点，终点Ea右边圆顶点，控制点Pa我们可以固定，也可以线性变化（我这里复杂些，线性变化，这样看起来会有粘性，更弹）；
b线起始点Sb是左边圆底点，终点Eb右边圆底点，控制点Pb同理。

### 三、示意图

![示意图.png](http://upload-images.jianshu.io/upload_images/5994029-61ad4a9b1c507d78.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 四、代码修改
在前一篇代码的基础上，只需要在BarView里记录控制点就可以了：
```
    /**
     * 滑块对象
     */
    public class BarView {
        private int leftX;
        private int leftY;
    
        private int rightX;
        private int rightY;
    
        private int radius;
    
        private int bezierTopX;
    
        private int bezierTopY;
        private int bezierBottomY;
    }
```
增加三个变量记录控制点的坐标，上下两点的X轴坐标一样。然后是计算这个控制点：
```
    /**
     * 绘制之前准备， 这个计算一定要在canvas外面计算
     */
    private void compute() {
        //计算球位置
        for (int i = 0; i < pointViews.size(); i++) {
            PointView pointView = pointViews.get(i);
            pointView.setRadius(radius);
            pointView.setX(radius * (2 * i + 1) + pointSpace * i);
            pointView.setY(radius);
            pointView.setChecked(selectPosition == i);
        }
        //计算bar位置
        PointView selectPointView = pointViews.get(selectPosition);
        int selectX = selectPointView.getX();
        int selectY = selectPointView.getY();
        if (selectPosition <= scrollPosition) {
            //往右是增加右边圆的圆心
            if (ratio <= 0.5) {
                barView.setLeftX(selectX);
                barView.setLeftY(selectY);
                barView.setRightX((int) (selectX + (2 * radius + pointSpace) * ratio));
                barView.setRightY(selectY);
                barView.setRadius(radius);
                barView.setBezierTopX(((barView.getLeftX() + barView.getRightX()) / 2));
                barView.setBezierTopY((int) (radius * 1.0 / 4 + 3 * ratio * radius / 2));
                barView.setBezierBottomY((int) (radius * 1.0 * 7 / 4 - 3 * ratio * radius / 2));
            } else {
                barView.setLeftX((int) (selectX + (2 * radius + pointSpace) * ratio));
                barView.setLeftY(selectY);
                PointView pointView = pointViews.get(selectPosition + 1);
                barView.setRightX(pointView.getX());
                barView.setRightY(pointView.getY());
                barView.setRadius(radius);
                barView.setBezierTopX(((barView.getLeftX() + barView.getRightX()) / 2));
                barView.setBezierTopY((int) (radius * 1.0 * 7 / 4 - 3 * ratio * radius / 2));
                barView.setBezierBottomY((int) (radius * 1.0 / 4 + 3 * ratio * radius / 2));
            }
        } else {
            //往左是减少左边圆的圆心
            if (ratio >= 0.5) {
                barView.setRightX(selectX);
                barView.setRightY(selectY);
                barView.setLeftX((int) (selectX - (2 * radius + pointSpace) * (1 - ratio)));
                barView.setLeftY(selectY);
                barView.setRadius(radius);
                barView.setBezierTopX(((barView.getLeftX() + barView.getRightX()) / 2));
                barView.setBezierTopY((int) (radius * 1.0 * 7 / 4 - 3 * ratio * radius / 2));
                barView.setBezierBottomY((int) (radius * 1.0 / 4 + 3 * ratio * radius / 2));
            } else {
                barView.setRightX((int) (selectX - (2 * radius + pointSpace) * (1 - ratio)));
                barView.setRightY(selectY);
                PointView pointView = pointViews.get(selectPosition - 1);
                barView.setLeftX(pointView.getX());
                barView.setLeftY(pointView.getY());
                barView.setRadius(radius);
                barView.setBezierTopX(((barView.getLeftX() + barView.getRightX()) / 2));
                barView.setBezierTopY((int) (radius * 1.0 / 4 + 3 * ratio * radius / 2));
                barView.setBezierBottomY((int) (radius * 1.0 * 7 / 4 - 3 * ratio * radius / 2));
            }
        }
    }
```
可以看到，我们只修改了compute()方法，在四种情况下，左滑到一半，一半滑到右，右滑到一半，一半滑到左，分别计算BezierTopX，BezierTopY和BezierBottomY。
BezierTopX很好计算，就是首尾圆心X坐标加起来的一半，后面两个比较难计算，为了清晰，建一个表格：

![计算规则.png](http://upload-images.jianshu.io/upload_images/5994029-6ce382863e90219b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


从表格上可以看到，ratio 从 0 ~ 1/2 ~ 1 的变化过程中，我们让控制点也在一个范围内线性变化。计算得出的公式就在上面的代码中了。
最后，我们还要去绘制这些线，修改 onDraw() 方法：
```
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画圆点
        for (PointView pointView : pointViews) {
            canvas.drawCircle(pointView.getX(), pointView.getY(), pointView.getRadius(), pointView.isChecked() ? selectPointPaint : pointPaint);
        }
        //画bar的头尾圆
        canvas.drawCircle(barView.getLeftX(), barView.getLeftY(), barView.getRadius(), selectPointPaint);
        canvas.drawCircle(barView.getRightX(), barView.getRightY(), barView.getRadius(), selectPointPaint);
        //画bar的中间rect  贝塞尔画法
        bezierPath = new Path();
        bezierPath.moveTo(barView.getLeftX(), 0);
        bezierPath.quadTo(barView.getBezierTopX(), barView.getBezierTopY(), barView.getRightX(), 0);
        bezierPath.lineTo(barView.getRightX(), 2 * radius);
        bezierPath.quadTo(barView.getBezierTopX(), barView.getBezierBottomY(), barView.getLeftX(), 2 * radius);
        bezierPath.lineTo(barView.getLeftX(), 0);
        canvas.drawPath(bezierPath, selectPointPaint);
    }
```
我们需要每次新建一个Path，因为不新建的话会绘制全部的路径，然后moveTo()起点，quadTo()绘制a线，再lineTo()底部点，绘制的路径要闭合，不然颜色没办法填充，只是单纯的线，
再quadTo()绘制b线，最后lineTo()起点。

到这，我们的贝塞尔就绘制完毕了，运行看看效果吧。

附上 [简书地址](http://www.jianshu.com/p/0c55a71c7676)