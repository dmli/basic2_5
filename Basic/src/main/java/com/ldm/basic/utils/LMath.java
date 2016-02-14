package com.ldm.basic.utils;

import android.graphics.PointF;

/**
 * Created by ldm on 12-4-17.
 * 根据提供当前时间及给定的格式，返回时间字符串
 */
public class LMath {

    /**
     * 根据给定x,y及半径，计算角度
     *
     * @param x x坐标点
     * @param y y坐标点
     * @param c 中心点
     * @return 角度
     */
    public static double getAngle(float x, float y, PointF c) {
        double radians = Math.atan2(x - c.x, y - c.y);
        return Math.toDegrees(radians);
    }


    /**
     * @param x1  第一个点x
     * @param y1  第一个点y
     * @param x2  第二个点x
     * @param y2  第二个点y
     * @param cx0 圆心x
     * @param cy0 圆心y
     * @return 角度
     */
    public static double getAngle(float x1, float y1, float x2, float y2, float cx0, float cy0) {
        float xx1 = x1 - cx0, xx2 = x2 - cx0;
        float yy1 = y1 - cy0, yy2 = y2 - cy0;
        double angle1 = Math.atan(yy1 / xx1) * (180 / Math.PI);
        double angle2 = Math.atan(yy2 / xx2) * (180 / Math.PI);
        return angle2 - angle1;
    }

    /**
     * 返回两点间的角度
     *
     * @param x1 点1x
     * @param y1 点1y
     * @param x2 点2x
     * @param y2 点2y
     * @return 角度
     */
    public static double getAngle(float x1, float y1, float x2, float y2) {
        double radians = Math.atan2(x1 - x2, y1 - y2);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 根据给定x,y及半径，计算弧度
     *
     * @param x x坐标点
     * @param y y坐标点
     * @param c 中心点
     * @return 弧度
     */
    public static double getRadians(float x, float y, PointF c) {
        return Math.atan2(x - c.x, y - c.y);
    }

    /**
     * 根据给定的x、y、半径及角度计算另一个的坐标
     *
     * @param x      x坐标点
     * @param y      y坐标点
     * @param radius 半径
     * @param angle  角度
     * @return PointF
     */
    public static PointF getPoint(float x, float y, float radius, float angle) {
        double x1 = x + radius * Math.cos(Math.toRadians(angle));
        double y1 = y + radius * Math.sin(Math.toRadians(angle));
        return new PointF((float) x1, (float) y1);
    }

    /**
     * 计算两点间的距离
     *
     * @param p1 点1
     * @param p2 点2
     * @return double类型 距离
     */
    public static double getDistance(PointF p1, PointF p2) {
        double _x = Math.abs(p1.x - p2.x);
        double _y = Math.abs(p1.y - p2.y);
        return Math.sqrt(_x * _x + _y * _y);
    }

    /**
     * 计算两点间的距离
     *
     * @param x1 点1x
     * @param y1 点1y
     * @param x2 点2x
     * @param y2 点2y
     * @return double类型 距离
     */
    public static double getDistance(float x1, float y1, float x2, float y2) {
        double _x = Math.abs(x1 - x2);
        double _y = Math.abs(y1 - y2);
        return Math.sqrt(_x * _x + _y * _y);
    }

    /**
     * 获取当前的百分比在给定范围内的值
     *
     * @param percentage 取值范围0-100
     * @param start      开始范围
     * @param end        结束范围
     * @return float
     */
    public static float getRange(float percentage, float start, float end) {
        return getRange(percentage, start, end, 100.0f);
    }

    /**
     * 获取t值在给定范围内的值
     *
     * @param t     取值范围0-N
     * @param start 开始范围
     * @param end   结束范围
     * @param maxT  最大t值，将于t计算百分比
     * @return float
     */
    public static float getRange(float t, float start, float end, float maxT) {
        return (end - start) * t / maxT + start;
    }

}
