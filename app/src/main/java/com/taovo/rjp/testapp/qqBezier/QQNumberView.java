package com.taovo.rjp.testapp.qqBezier;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Gimpo create on 2017/10/25 13:05
 * @email : jimbo922@163.com
 */

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = radius * 2;
        setMeasuredDimension(width, width);
    }

    public void setNumber(int number) {
        this.number = number;
        invalidate();
    }

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

    public class NumberEvaluator implements TypeEvaluator<Point> {

        @Override
        public Point evaluate(float fraction, Point startValue, Point endValue) {
            Point p = new Point();
            p.x = (int) (startValue.x + (endValue.x - startValue.x) * fraction);
            p.y = (int) (startValue.y + (endValue.y - startValue.y) * fraction);
            return p;
        }
    }
}
