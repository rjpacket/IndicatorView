package com.taovo.rjp.testapp.indicator;

/**
 * @author Gimpo create on 2017/10/24 11:01
 * @email : jimbo922@163.com
 */

public class PointView {
    private int x;
    private int y;
    private int radius;

    private boolean isChecked;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
