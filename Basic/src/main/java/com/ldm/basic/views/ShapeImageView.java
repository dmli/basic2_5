package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ldm on 16/4/6.
 * 简易的
 */
public class ShapeImageView extends LImageView {


    public static class Shape {
        public static final int CIRCLE = 1; // 圆形

        public static final int RECTANGLE = 2; // 矩形

        public static final int ROUND_RECTANGLE = 4;// 圆角矩形

        public static final int POLYGON = 5; // 六边形

        public static final int SQUARE = 6; // 正方形
    }

    private float radiusX, radiusY;

    /**
     * 默认正方形
     */
    protected int currentShape = Shape.RECTANGLE; // 默认为圆形
    private BaseShape shape = new RectangleShape();

    //画布抗锯齿
    private PaintFlagsDrawFilter paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    public ShapeImageView(Context context) {
        super(context);
    }

    public ShapeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShapeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 如果使用圆角
     *
     * @param radius 圆角
     */
    public void setRadius(float radius) {
        this.radiusX = radius;
        this.radiusY = radius;
        invalidate();
    }

    /**
     * 设置圆角
     *
     * @param radiusX 圆角X
     * @param radiusY 圆角Y
     */
    public void setRadius(float radiusX, float radiusY) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        invalidate();
    }

    /**
     * 切换状态
     *
     * @param type 类型 取值 Shape中属性
     */
    public void changeShapeType(int type) {
        if (currentShape == type) {
            return;
        }
        currentShape = type;
        switch (type) {
            case Shape.SQUARE:
                shape = new SquareShape();
                break;
            case Shape.CIRCLE:
                shape = new CircleShape();
                break;
            case Shape.POLYGON:
                shape = new PolygonImageView();
                break;
            case Shape.ROUND_RECTANGLE:
                shape = new RoundRectangleShare();
                break;
            default://默认矩形，不添加任何
                shape = new RectangleShape();
                break;
        }
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas.isHardwareAccelerated() && getLayerType() != View.LAYER_TYPE_HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        if (!isInEditMode() && shape != null) {
            canvas.setDrawFilter(paintFlagsDrawFilter);
            canvas.save();
            shape.drawPath(this, canvas);
            super.onDraw(canvas);
            canvas.restore();
            shape.adorn(this, canvas);
        } else {
            super.onDraw(canvas);
        }

    }

    /**
     * 多边形，目前仅提供六边形
     */
    private class PolygonImageView implements BaseShape {


        @Override
        public void adorn(ShapeImageView view, Canvas canvas) {

        }

        @Override
        public void drawPath(ShapeImageView view, Canvas canvas) {
            canvas.clipPath(getPolygonPath(getPolygonPoints(view.getWidth(), view.getHeight(), 6, 0)));
        }

        /**
         * 返回六边形的点集合
         *
         * @param width       宽度
         * @param height      高度
         * @param sides       多边形的边数量
         * @param offsetAngle 偏移量
         * @return PointF[]
         */
        private PointF[] getPolygonPoints(int width, int height, int sides, float offsetAngle) {
            PointF[] points = new PointF[sides];
            float centerX = width / 2f;
            float centerY = height / 2f;
            float radius = Math.min(width, height) / 2f;
            sides = Math.max(0, Math.abs(sides));

            offsetAngle = (float) (Math.PI * offsetAngle / 180);
            for (int i = 0; i < sides; i++) {
                float x = (float) (centerX + radius * Math.cos(offsetAngle));
                float y = (float) (centerY + radius * Math.sin(offsetAngle));
                points[i] = new PointF(x, y);
                offsetAngle += 2 * Math.PI / sides;
            }
            return points;
        }

        private Path getPolygonPath(PointF[] points) {
            if (points == null || points.length == 0) {
                return null;
            }
            Path path = new Path();
            path.moveTo(points[0].x, points[0].y);
            for (PointF p : points) {
                path.lineTo(p.x, p.y);
            }
            return path;
        }
    }


    /**
     * 圆角矩形实现
     */
    private class RoundRectangleShare implements BaseShape {

        @Override
        public void adorn(ShapeImageView view, Canvas canvas) {

        }

        @Override
        public void drawPath(ShapeImageView view, Canvas canvas) {
            Path path = new Path();
            path.addRoundRect(new RectF(0.0f, 0.0f, view.getWidth(), view.getHeight()), view.radiusX, view.radiusY, Path.Direction.CW);
            canvas.clipPath(path);
        }
    }

    /**
     * 矩形，根据ImageView的自身宽高显示
     */
    private class RectangleShape implements BaseShape {


        @Override
        public void adorn(ShapeImageView view, Canvas canvas) {

        }

        @Override
        public void drawPath(ShapeImageView view, Canvas canvas) {
            Path path = new Path();
            path.addRect(new RectF(0.0f, 0.0f, view.getWidth(), view.getHeight()), Path.Direction.CW);
            canvas.clipPath(path);
        }
    }


    /**
     * 圆形实现
     */
    private class CircleShape implements BaseShape {

        @Override
        public void adorn(ShapeImageView view, Canvas canvas) {

        }

        @Override
        public void drawPath(ShapeImageView view, Canvas canvas) {
            Path path = new Path();
            int width = view.getWidth();
            int height = view.getHeight();
            path.addCircle(width / 2f, height / 2f, width / 2f, Path.Direction.CW);
            canvas.clipPath(path);
        }
    }

    /**
     * 正方形
     */
    private class SquareShape implements BaseShape {

        public void adorn(ShapeImageView view, Canvas canvas) {
        }

        @Override
        public void drawPath(ShapeImageView view, Canvas canvas) {
            Path path = new Path();
            int width = view.getWidth();
            int height = view.getHeight();
            int offset = Math.abs((width - height) / 2);
            if (width >= height) {
                path.addRect(new RectF(offset, 0.0f, offset + view.getHeight(), view.getWidth()), Path.Direction.CW);
            } else {
                path.addRect(new RectF(0.0f, offset, view.getHeight(), offset + view.getWidth()), Path.Direction.CW);
            }
            canvas.clipPath(path);
        }
    }


    private interface BaseShape {

        void adorn(ShapeImageView view, Canvas canvas);

        void drawPath(ShapeImageView view, Canvas canvas);
    }

    private class FrameStrokeRef {


    }
}
