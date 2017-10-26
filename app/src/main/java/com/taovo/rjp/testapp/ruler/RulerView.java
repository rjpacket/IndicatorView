package com.taovo.rjp.testapp.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

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
}
