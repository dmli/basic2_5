package com.ldm.basic.utils;

import android.graphics.Color;

/**
 * Created by ldm on 15/10/14.
 * 颜色过度器
 */
public class ArgbEvaluator {


    /**
     * 开始颜色
     */
    private final int sA, sR, sG, sB;
    private final int eA, eR, eG, eB;
    private final int dA, dR, dG, dB;

    /**
     * 结束颜色
     */
    private int[] eARGB = new int[3];

    /**
     * 色差数值
     */
    private int[] dARGB = new int[3];

    /**
     * 创建一个颜色过度器
     *
     * @param sARGB 开始颜色
     * @param eARGB 结束颜色
     */
    public ArgbEvaluator(int[] sARGB, int[] eARGB) {

        this.sA = sARGB[0];
        this.sR = sARGB[1];
        this.sG = sARGB[2];
        this.sB = sARGB[3];

        this.eA = eARGB[0];
        this.eR = eARGB[1];
        this.eG = eARGB[2];
        this.eB = eARGB[3];

        this.dA = this.eA - this.sA;
        this.dR = this.eR - this.sR;
        this.dG = this.eG - this.sG;
        this.dB = this.eB - this.sB;
    }

    /**
     * 创建一个颜色过度器
     *
     * @param sARGB 开始颜色
     * @param eARGB 结束颜色
     */
    public ArgbEvaluator(int sARGB, int eARGB) {

        this.sA = sARGB >>> 24;
        this.sR = (sARGB >> 16) & 0xFF;
        this.sG = (sARGB >> 8) & 0xFF;
        this.sB = sARGB & 0xFF;

        this.eA = eARGB >>> 24;
        this.eR = (eARGB >> 16) & 0xFF;
        this.eG = (eARGB >> 8) & 0xFF;
        this.eB = eARGB & 0xFF;

        this.dA = this.eA - this.sA;
        this.dR = this.eR - this.sR;
        this.dG = this.eG - this.sG;
        this.dB = this.eB - this.sB;
    }

    /**
     * 根据给定的progress生成对应的颜色值
     *
     * @param progress 进度 取值0-1
     */
    public int evaluator(float progress) {
        return Color.argb(sA + (int) (dA * progress), sR + (int) (dR * progress), sG + (int) (dG * progress), sB + (int) (dB * progress));
    }
}
