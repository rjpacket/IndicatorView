package com.taovo.rjp.testapp.raceDog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.taovo.rjp.testapp.R;

import java.util.LinkedList;
import java.util.Random;

/**
 * @author Gimpo create on 2017/10/20 17:45
 * @email : jimbo922@163.com
 */

public class DogSurface extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private Context mContext;

    Bitmap dog1;
    Bitmap dog2;
    Bitmap dog3;
    Bitmap dog4;
    Bitmap dog5;
    Floor floor;
    private boolean stop;
    private int screenHeight;
    private int screenWidth;
    private int topBarHeight = 120;
    private int bottomBarHeight = 120;
    private int dogSpace = 10;
    private LinkedList<Dog> dogs = new LinkedList<>();
    SurfaceHolder holder;
    private int waitTime = 3;  // 等待30s
    private Paint bgPaint;
    private Random random;

    public DogSurface(Context context) {
        this(context, null);
    }

    public DogSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        random = new Random();
        holder = getHolder();
        holder.addCallback(this);

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.GRAY);
        bgPaint.setTextSize(80);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        dog1 = dog2 = dog3 = dog4 = dog5 = BitmapFactory.decodeResource(getResources(), R.mipmap.dog);
        screenHeight = getHeight();
        screenWidth = getWidth();
        floor = new Floor(0, topBarHeight, 3000, screenHeight - bottomBarHeight);
        initDog();
        stop = false;
        new Thread(this).start();
    }

    private void initDog() {
        int perHeight = (screenHeight - topBarHeight - bottomBarHeight) / 5;
        int dogWidth = perHeight - dogSpace * 2;
        Dog d1 = new Dog(1, dog1, dogSpace, topBarHeight + dogSpace, dogWidth + dogSpace, topBarHeight + dogSpace + dogWidth);
        Dog d2 = new Dog(2, dog2, dogSpace, d1.getBottom() + dogSpace * 2, dogWidth + dogSpace, d1.getBottom() + dogSpace * 2 + dogWidth);
        Dog d3 = new Dog(3, dog3, dogSpace, d2.getBottom() + dogSpace * 2, dogWidth + dogSpace, d2.getBottom() + dogSpace * 2 + dogWidth);
        Dog d4 = new Dog(4, dog4, dogSpace, d3.getBottom() + dogSpace * 2, dogWidth + dogSpace, d3.getBottom() + dogSpace * 2 + dogWidth);
        Dog d5 = new Dog(5, dog5, dogSpace, d4.getBottom() + dogSpace * 2, dogWidth + dogSpace, d4.getBottom() + dogSpace * 2 + dogWidth);
        dogs.add(d1);
        dogs.add(d2);
        dogs.add(d3);
        dogs.add(d4);
        dogs.add(d5);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop = true;
    }

    @Override
    public void run() {
        while (waitTime > 0){
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas(null);
                if (canvas == null)
                    break;
                drawTime(canvas);
                drawFloor(canvas);
                drawDogs(canvas);
            } catch (Exception e) {
                Log.i("Unit", e.toString());
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }
            try {
                Thread.sleep(1000);
                waitTime--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        M:while (!stop){
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas(null);
                canvas.drawColor(Color.BLACK);
                if (canvas == null)
                    break;
                int firstDogLeft = 0;
                for (Dog dog : dogs) {
                    if(dog.getRight() - floor.getLeft() >= 3000){
                        stop = true;
                        canvas.drawText(dog.getId() + "号获胜", screenWidth / 2, screenHeight / 2, bgPaint);
                        break M;
                    }
                    if(dog.getLeft() > screenWidth / 2){
                        if(dog.getLeft() > firstDogLeft){
                            firstDogLeft = dog.getLeft();
                        }
                    }
                    dog.setSpeed(random.nextInt(10));
                }
                if(firstDogLeft != 0) {
                    for (Dog dog : dogs) {
                        dog.setOffset(firstDogLeft - screenWidth / 2);
                    }
                    floor.setOffset(firstDogLeft - screenWidth / 2);
                }
                drawFloor(canvas);
                drawDogs(canvas);
            } catch (Exception e) {
                Log.i("Unit", e.toString());
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void drawTime(Canvas canvas) {

    }

    /**
     * 画出dog
     * @param canvas
     */
    private void drawDogs(Canvas canvas) {
        for (Dog dog : dogs) {
            canvas.drawBitmap(dog.getBitmap(), null, new RectF(dog.getLeft(), dog.getTop(), dog.getRight(), dog.getBottom()), bgPaint);
        }
    }

    /**
     * 画出地板
     * @param canvas
     */
    private void drawFloor(Canvas canvas) {
        canvas.drawRect(new RectF(floor.getLeft(), floor.getTop(), floor.getRight(), floor.getBottom()), bgPaint);
    }

}
