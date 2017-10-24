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