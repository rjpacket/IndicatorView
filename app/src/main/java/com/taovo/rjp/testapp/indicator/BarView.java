package com.taovo.rjp.testapp.indicator;

/**
 * @author Gimpo create on 2017/10/24 11:29
 * @email : jimbo922@163.com
 */

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

    public int getLeftX() {
        return leftX;
    }

    public void setLeftX(int leftX) {
        this.leftX = leftX;
    }

    public int getLeftY() {
        return leftY;
    }

    public void setLeftY(int leftY) {
        this.leftY = leftY;
    }

    public int getRightX() {
        return rightX;
    }

    public void setRightX(int rightX) {
        this.rightX = rightX;
    }

    public int getRightY() {
        return rightY;
    }

    public void setRightY(int rightY) {
        this.rightY = rightY;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getBezierTopY() {
        return bezierTopY;
    }

    public void setBezierTopY(int bezierTopY) {
        this.bezierTopY = bezierTopY;
    }

    public int getBezierTopX() {
        return bezierTopX;
    }

    public void setBezierTopX(int bezierTopX) {
        this.bezierTopX = bezierTopX;
    }

    public int getBezierBottomY() {
        return bezierBottomY;
    }

    public void setBezierBottomY(int bezierBottomY) {
        this.bezierBottomY = bezierBottomY;
    }
}
