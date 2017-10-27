package com.taovo.rjp.testapp.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

/**
 * @author Gimpo create on 2017/10/26 14:24
 * @email : jimbo922@163.com
 */

/**
 * 尺图
 */
public class RulerView extends View {
    private Context mContext;
    private Paint centerLinePaint;
    private Paint grayLinePaint;
    private Paint txtPaint;
    private int space = 20;
    private int startValue = 40;
    private int endValue = 100;
    private int width;
    private int height;
    private float mLastX;
    private int touchSlop;
    private Scroller mScroller;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int maxScrollX = 1000; // 最大允许滑出范围
    private int currentOffset; // 当前偏移
    private VelocityTracker mVelocityTracker;
    private boolean isFastScroll;
    private RulerCallback mListener;
    private int number;
    private int BASELINE_OFFSET;

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
        centerLinePaint.setStrokeWidth(5);

        grayLinePaint = new Paint();
        grayLinePaint.setAntiAlias(true);
        grayLinePaint.setColor(Color.parseColor("#66666666"));
        grayLinePaint.setStrokeWidth(5);

        txtPaint = new Paint();
        txtPaint.setAntiAlias(true);
        txtPaint.setColor(Color.parseColor("#333333"));
        txtPaint.setTextSize(50);

        // 新增部分 start
        ViewConfiguration viewConfiguration = ViewConfiguration.get(mContext);
        touchSlop = viewConfiguration.getScaledTouchSlop();
        mScroller = new Scroller(mContext);
        mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        // 新增部分 end
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            width = mContext.getResources().getDisplayMetrics().widthPixels;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = (int) (mContext.getResources().getDisplayMetrics().density * 60 + 0.5);
        }
        setMeasuredDimension(width, height);

        BASELINE_OFFSET = width / 2;
        int x = (number - startValue) * space * 10 - BASELINE_OFFSET + BASELINE_OFFSET % space;
        if (x % space != 0) {
            x -= x % space;
        }
        scrollTo(x, 0);
        computeAndCallback(x);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = startValue * 10; i < endValue * 10 + 1; i++) {
            int lineHeight = 80;
            if (i % 5 == 0) {
                if (i % 10 == 0) {
                    lineHeight = 120;
                    int x = (i - startValue * 10) * space;
                    if (x > 0 || x < width) {
                        canvas.drawText(String.valueOf(i / 10), x, lineHeight + 50, txtPaint);
                    }
                }
            } else {
                lineHeight = 50;
            }
            int startX = (i - startValue * 10) * space;
            if (startX > 0 || startX < width) {
                canvas.drawLine(startX, 0, startX, lineHeight, grayLinePaint);
            }
        }
        int startX = BASELINE_OFFSET + getScrollX() - BASELINE_OFFSET % space;
        canvas.drawLine(startX, 0, startX, 180, centerLinePaint);
    }

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

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            scrollTo(x, 0);
            computeAndCallback(x);
            postInvalidate();
        } else {
            if (isFastScroll) {
                int x = mScroller.getCurrX() + BASELINE_OFFSET % space;
                if (x % space != 0) {
                    x -= x % space;
                }
                scrollTo(x, 0);
                computeAndCallback(x);
                postInvalidate();
            }
        }
    }

    /**
     * 计算并回调位置信息
     *
     * @param scrollX
     */
    private void computeAndCallback(int scrollX) {
        if (mListener != null) {
            int finalX = BASELINE_OFFSET + scrollX;
            if (finalX % space != 0) {
                finalX -= finalX % space;
            }
            mListener.onRulerSelected((endValue - startValue) * 10, startValue * 10 + finalX / space);
        }
    }

    /**
     * 初始化 速度追踪器
     */
    private void obtainVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    /**
     * 释放 速度追踪器
     */
    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void setmListener(RulerCallback mListener) {
        this.mListener = mListener;
    }

    /**
     * 设置number的值
     * @param number
     */
    public void setNumber(int number) {
        this.number = number;
    }
}
