package com.taovo.rjp.testapp.raceDog;

import android.graphics.Bitmap;

/**
 * @author Gimpo create on 2017/10/20 17:49
 * @email : jimbo922@163.com
 */

public class Dog {
    private int id;
    private Bitmap bitmap;
    private int speed;

    private int left;
    private int top;
    private int right;
    private int bottom;

    private int offset;

    public Dog(int id, Bitmap bitmap, int left, int top, int right, int bottom){
        this.setId(id);
        this.bitmap = bitmap;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        this.left += speed;
        this.right += speed;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        this.left -= offset;
        this.right -= offset;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOffset() {
        return offset;
    }
}
