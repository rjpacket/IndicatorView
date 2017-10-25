package com.taovo.rjp.testapp.qqBezier;

/**
 * @author Gimpo create on 2017/10/25 14:25
 * @email : jimbo922@163.com
 */

public class ControllerView {
    private int x;
    private int y;
    private int radius;

    private int saX;
    private int saY;
    private int eaX;
    private int eaY;

    private int sbX;
    private int sbY;
    private int ebX;
    private int ebY;

    private int px;
    private int py;


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

    public int getSaX() {
        return saX;
    }

    public void setSaX(int saX) {
        this.saX = saX;
    }

    public int getSaY() {
        return saY;
    }

    public void setSaY(int saY) {
        this.saY = saY;
    }

    public int getEaX() {
        return eaX;
    }

    public void setEaX(int eaX) {
        this.eaX = eaX;
    }

    public int getEaY() {
        return eaY;
    }

    public void setEaY(int eaY) {
        this.eaY = eaY;
    }

    public int getSbX() {
        return sbX;
    }

    public void setSbX(int sbX) {
        this.sbX = sbX;
    }

    public int getSbY() {
        return sbY;
    }

    public void setSbY(int sbY) {
        this.sbY = sbY;
    }

    public int getEbX() {
        return ebX;
    }

    public void setEbX(int ebX) {
        this.ebX = ebX;
    }

    public int getEbY() {
        return ebY;
    }

    public void setEbY(int ebY) {
        this.ebY = ebY;
    }

    public int getPx() {
        return px;
    }

    public void setPx(int px) {
        this.px = px;
    }

    public int getPy() {
        return py;
    }

    public void setPy(int py) {
        this.py = py;
    }

    public void reset() {
        radius = 0;

        saX = 0;
        saY = 0;
        eaX = 0;
        eaY = 0;

        sbX = 0;
        sbY = 0;
        ebX = 0;
        ebY = 0;

        px = 0;
        py = 0;
    }
}
