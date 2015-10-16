package com.ldm.basic.anim;

/**
 * Created by ldm on 14-8-22.
 * （摘取网络代码）
 * 网格
 */
public abstract class Mesh {

    private static final int HORIZONTAL_SPLIT = 40;
    private static final int VERTICAL_SPLIT = 40;
    /**
     * splitting partitions in horizontal and vertical
     */
    protected int mHorizontalSplit;
    protected int mVerticalSplit;

    protected int mBmpWidth = -1;
    protected int mBmpHeight = -1;
    protected final float[] mVertices;

    public Mesh() {
        mHorizontalSplit = HORIZONTAL_SPLIT;
        mVerticalSplit = VERTICAL_SPLIT;
        mVertices = new float[(mHorizontalSplit + 1) * (mVerticalSplit + 1) * 2];
    }

    public Mesh(int rowNum, int colNum) {
        mHorizontalSplit = rowNum;
        mVerticalSplit = colNum;
        mVertices = new float[(mHorizontalSplit + 1) * (mVerticalSplit + 1) * 2];
    }

    public float[] getVertices() {
        return mVertices;
    }

    public int getWidth() {
        return mHorizontalSplit;
    }

    public int getHeight() {
        return mVerticalSplit;
    }

    public static void setXY(float[] array, int index, float x, float y) {
        array[index * 2] = x;
        array[index * 2 + 1] = y;
    }

    public void setBitmapSize(int w, int h) {
        mBmpWidth = w;
        mBmpHeight = h;
    }

    public abstract void buildPaths(float endX, float endY);

    public abstract void buildMeshes(float interpolatedTime);

    public void buildMeshes(float w, float h) {
        final int row = mVerticalSplit;
        final int col = mHorizontalSplit;
        int index = 0;
        for (int y = 0; y <= row; y++) {
            float fy = y * h / row;
            for (int x = 0; x <= col; x++) {
                float fx = x * w / col;
                setXY(mVertices, index, fx, fy);
                index++;
            }
        }
    }
}