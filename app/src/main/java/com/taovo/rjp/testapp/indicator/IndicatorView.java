package com.taovo.rjp.testapp.indicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gimpo create on 2017/10/24 10:33
 * @email : jimbo922@163.com
 */

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
}
