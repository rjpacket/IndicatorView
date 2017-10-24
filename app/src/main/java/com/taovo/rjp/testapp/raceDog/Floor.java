package com.taovo.rjp.testapp.raceDog;

/**
 * @author Gimpo create on 2017/10/20 18:25
 * @email : jimbo922@163.com
 */

public class Floor {
    private int left;
    private int top;
    private int right;
    private int bottom;

    private int endLeft;
    private int endTop;
    private int endRright;
    private int endBottom;

    private int offset;

    public Floor(int left, int top, int right, int bottom){
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

        this.setEndLeft(right);
        this.setEndTop(top - 20);
        this.setRight(right + 20);
        this.setBottom(bottom + 20);
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

    public int getEndLeft() {
        return endLeft;
    }

    public void setEndLeft(int endLeft) {
        this.endLeft = endLeft;
    }

    public int getEndTop() {
        return endTop;
    }

    public void setEndTop(int endTop) {
        this.endTop = endTop;
    }

    public int getEndRright() {
        return endRright;
    }

    public void setEndRright(int endRright) {
        this.endRright = endRright;
    }

    public int getEndBottom() {
        return endBottom;
    }

    public void setEndBottom(int endBottom) {
        this.endBottom = endBottom;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        this.left -= offset;
        this.right -= offset;
        this.endLeft -= offset;
        this.endRright -= offset;
    }

    public int getOffset() {
        return offset;
    }
}
