package com.ldm.basic.res;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.ldm.basic.utils.Base64;
import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.FileType;
import com.ldm.basic.utils.SystemTool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Created by ldm on 13-1-5. Bitmap的一些常用方法
 */
public class BitmapHelper {

    /**
     * 按比例返回图片适合的BitmapFactory.Options，但不强制宽高度
     *
     * @param path              绝对路径
     * @param width             宽度
     * @param inPreferredConfig 图片输入使用的模式
     * @return BitmapFactory.Options
     */
    public static BitmapFactory.Options getOptions(final String path, final int width, final int height, final Bitmap.Config inPreferredConfig) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;// 允许可清除
        options.inInputShareable = true;
        BitmapFactory.decodeFile(path, options);
        options.inPreferredConfig = inPreferredConfig;
        int scale;
        if (width == options.outWidth) {
            scale = 1;
        } else {
            if (height > 0) {
                scale = (options.outHeight / height + options.outWidth / width) / 2;
            } else {
                scale = options.outWidth / width;
            }
        }
        options.inSampleSize = scale <= 0 ? 1 : scale;
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * 根据给定的最小直径对图片进行缩放
     *
     * @param path     路径
     * @param diameter 直径长度
     * @return Bitmap
     */
    public static Bitmap getBitmapMinDiameter(final String path, final int diameter) {
        BitmapFactory.Options options = getOptionsMinDiameter(path, diameter);
        Bitmap bit2 = null;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap == null) {
                return null;
            }
            int degree = readPictureDegree(path);
            Matrix m = new Matrix();
            m.reset();
            int w = bitmap.getWidth(), h = bitmap.getHeight();
            if (degree != 0) {
                if (degree / 90 % 2 == 1) {
                    h = bitmap.getWidth();
                    w = bitmap.getHeight();
                }
                m.postRotate(degree);
            }
            float s = diameter * 1.0f / Math.min(w, h);
            m.postScale(s, s);
            bit2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            if (bit2 != bitmap && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bit2;
    }

    /**
     * 根据最小的直径截取一个正方形图像（返回图片较大时不建议使用）
     *
     * @param path     地址
     * @param diameter 直径长度
     * @return Bitmap
     */
    public static Bitmap getBitmapMinDiameterToSquare(final String path, final int diameter) {
        Bitmap bitmap = getBitmapMinDiameter(path, diameter);
        if (bitmap == null || bitmap.getWidth() == bitmap.getHeight()) {
            return bitmap;
        }
        int l = 0, t = 0;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            l = (bitmap.getWidth() - bitmap.getHeight()) / 2;
        } else if (bitmap.getWidth() < bitmap.getHeight()) {
            t = (bitmap.getHeight() - bitmap.getWidth()) / 2;
        }
        Bitmap bit2 = Bitmap.createBitmap(bitmap, l, t, diameter, diameter);
        if (bit2 != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return bit2;
    }

    /**
     * 根据给定的最小直径对图片进行缩放,返回适合的BitmapFactory.Options
     *
     * @param path     路径
     * @param diameter 直径长度
     * @return BitmapFactory.Options
     */
    public static BitmapFactory.Options getOptionsMinDiameter(String path, int diameter) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Config.ARGB_8888;
        if (diameter > 0) {
            options.inJustDecodeBounds = true;
            options.inPurgeable = true;// 允许可清除
            options.inInputShareable = true;
            BitmapFactory.decodeFile(path, options);
            int scale = (int) (diameter * 1.0f / Math.min(options.outWidth, options.outHeight));
            scale = scale <= 0 ? 1 : scale;
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
        }
        return options;
    }

    /**
     * 返回图像的宽高
     *
     * @param path 图像地址
     * @return int[]
     */
    public static int[] getBitmapSize(String path) {
        int[] is = new int[2];
        try {
            if (SystemTool.SYS_SDK_INT <= Build.VERSION_CODES.JELLY_BEAN && "webp".equals(FileType.getFileType(path))) {
                Bitmap bit = BitmapHelper.decodeWebp(FileTool.openFile(path));

                byte[] buffer = new byte[1024];
                InputStream fis = FileTool.openFile(path);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    while ((fis.read(buffer, 0, buffer.length)) > 0) {
                        baos.write(buffer);
                    }
                    fis.close();
                    byte[] data = baos.toByteArray();
                    int[] width = new int[]{0};
                    int[] height = new int[]{0};
                    LibWebp.WebPDecodeARGB(data, data.length, width, height);
                    is[0] = width[0];
                    is[1] = height[0];
                } catch (Exception e) {
                    e.printStackTrace();
                }
                is[0] = bit.getWidth();
                is[1] = bit.getHeight();
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                is[0] = options.outWidth;
                is[1] = options.outHeight;
                int degree = readPictureDegree(path);
                if (degree != 0) {
                    if (degree / 90 % 2 == 1) {
                        is[0] = options.outHeight;
                        is[1] = options.outWidth;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            is[0] = -1;
            is[1] = -1;
        }
        return is;
    }

    /**
     * 创建一个纯色Bitmap
     *
     * @param color  颜色
     * @param width  宽度
     * @param height 高度
     * @param config Config
     * @return Bitmap
     */
    public static Bitmap createPureBitmap(int color, int width, int height, Config config) {
        Bitmap bit = Bitmap.createBitmap(width, height, config);
        Canvas c = new Canvas(bit);
        Paint p = new Paint();
        p.setColor(color);
        p.setStrokeWidth(2);
        p.setStyle(Style.FILL);
        c.drawRect(0, 0, width, height, p);
        return bit;
    }

    /**
     * 创建一个纯色Bitmap
     *
     * @param color  颜色 #000000
     * @param width  宽度
     * @param height 高度
     * @param config Config
     * @return Bitmap
     */
    public static Bitmap createPureBitmap(String color, int width, int height, Config config) {
        return createPureBitmap(Color.parseColor(color), width, height, config);
    }

    /**
     * 将Drawable进行旋转后返回Drawable
     *
     * @param res      activity.getResources
     * @param drawable Drawable
     * @param degrees  角度
     * @return Drawable
     */
    public static Drawable drawableRotate(Resources res, Drawable drawable, int degrees) {
        if (drawable == null)
            return null;
        return bitmapRotate(res, Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565), degrees);
    }

    /**
     * 根据路径返回相近大小的图片
     *
     * @param path   路径
     * @param size   目标byte字节数
     * @param config Bitmap.Config
     * @return Bitmap
     */
    public static Bitmap getBitmapToFixedSize(String path, int size, Bitmap.Config config) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = config;
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;// 允许可清除
        options.inInputShareable = true;
        BitmapFactory.decodeFile(path, options);
        int scale = (int) (options.outWidth * options.outHeight * 1.0f / size) / 2;
        scale = scale <= 0 ? 1 : scale;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        Bitmap result;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap == null) {
            return null;
        }
        int configScale;
        if (bitmap.getConfig() == Bitmap.Config.ARGB_4444 || bitmap.getConfig() == Bitmap.Config.RGB_565) {
            configScale = 2;
        } else {
            configScale = 4;
        }
        int nSize = bitmap.getWidth() * bitmap.getHeight() * configScale;
        if (nSize > size) {
            Matrix m = new Matrix();
            m.reset();
            float s = (float) (Math.sqrt((size * 1.0d / nSize))) - 0.001f;
            m.postScale(s, s);

            result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            if (bitmap != result && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } else {
            result = bitmap;
        }
        return result;
    }

    /**
     * 将Bitmap进行旋转后返回Drawable
     *
     * @param res     activity.getResources
     * @param bmp     Bitmap
     * @param degrees 角度
     * @return Drawable
     */
    public static Drawable bitmapRotate(Resources res, Bitmap bmp, int degrees) {
        if (bmp == null)
            return null;
        try {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bmp.getWidth() / 2, (float) bmp.getHeight() / 2);
            Bitmap b2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
            if (bmp != b2 && !bmp.isRecycled()) {
                bmp.recycle();
            }
            return new BitmapDrawable(res, b2);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过path与需要的width 返回用户一个Bitmap (这个比例是模糊的，出图的宽度不一定会与width相同，如果需要精确出图可以使用getBitmapAccurateSize)
     *
     * @param path  地址
     * @param width 目标宽 （与高度成缩放比例进行缩放）
     * @return Bitmap
     */
    public static Bitmap getBitmapProportional(final String path, final int width) {
        return getBitmap(path, width, -1, false);
    }

    public static Bitmap getBitmapProportionalRGB565(final String path, final int width) {
        return getBitmap(path, width, -1, false, Config.RGB_565);
    }

    /**
     * 通过path与需要的width 返回用户一个Bitmap (这个比例是模糊的，出图的宽度不一定会与width相同，如果需要精确出图可以使用getBitmapAccurateSize)
     *
     * @param path     地址
     * @param width    目标宽 （与高度成缩放比例进行缩放）
     * @param inDither 是否开启抖动解码模式，true开启，但将消耗更大的性能
     * @return Bitmap
     */
    public static Bitmap getBitmapProportional(final String path, final int width, boolean inDither, final Bitmap.Config inPreferredConfig) {
        return getBitmap(path, width, -1, inDither, inPreferredConfig);
    }

    /**
     * 通过path与需要的width 返回用户一个Bitmap (这个比例是模糊的，出图的宽度不一定会与width相同，如果需要精确出图可以使用getBitmapAccurateSize)
     *
     * @param path     地址
     * @param width    目标宽 （与高度成缩放比例进行缩放）
     * @param inDither 是否开启抖动解码模式，true开启，但将消耗更大的性能
     * @return Bitmap
     */
    public static Bitmap getBitmapProportional(final String path, final int width, boolean inDither) {
        return getBitmap(path, width, -1, inDither);
    }

    /**
     * 根据宽高对给定路径的图片进行缩放
     *
     * @param path   绝对路径
     * @param width  目标宽 如果为0将使用原始宽度 （如果这个宽度大于原图宽度，返回原图宽度）
     * @param height 目标高 小余0使用宽度的缩放比例
     * @return Bitmap
     */
    public static Bitmap getBitmap(final String path, final int width, final int height, boolean inDither) {
        return getBitmap(path, width, height, inDither, Config.ARGB_8888);
    }

    /**
     * 根据宽高对给定路径的图片进行缩放
     *
     * @param path              绝对路径
     * @param width             目标宽 如果为0将使用原始宽度 （如果这个宽度大于原图宽度，返回原图宽度）
     * @param height            目标高 小余0使用宽度的缩放比例
     * @param inPreferredConfig 图片输入使用的模式
     * @return Bitmap
     */
    public static Bitmap getBitmap(final String path, final int width, final int height, boolean inDither, final Bitmap.Config inPreferredConfig) {
        BitmapFactory.Options options = null;
        if (width > 0) {
            options = getOptions(path, width, height, inPreferredConfig);
        }
        if (options == null) {
            options = new BitmapFactory.Options();
            options.inPreferredConfig = Config.ARGB_8888;
        }
        Bitmap result = null;
        try {
            options.inDither = inDither;
            Bitmap bm = BitmapFactory.decodeFile(path, options);
            if (bm == null) {// 图片读取失败，重试一次
                bm = BitmapFactory.decodeFile(path, options);
                if (bm == null) {
                    return null;
                }
            }
            Matrix m = new Matrix();
            m.reset();
            // 计算是否有旋转
            int degree = readPictureDegree(path);
            int w = bm.getWidth(), h = bm.getHeight();
            if (degree != 0) {
                if (degree / 90 % 2 == 1) {// 当处于基数时表示宽高需要交换
                    h = bm.getWidth();
                    w = bm.getHeight();
                }
                m.postRotate(degree);
            }

            // 如果没有设置宽度 使用原图宽度
            float targetWidth = width <= 0 ? w : width;
            float targetScaleX = targetWidth * 1.0f / w;
            float targetScaleY = height <= 0 ? targetScaleX : height * 1.0f * h;
            m.postScale(targetScaleX, targetScaleY);
            if (targetScaleX != 1.0f || targetScaleY != 1.0f || degree != 0) {
                result = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                if (bm != result && !bm.isRecycled()) {
                    bm.recycle();
                }
            } else {
                result = bm;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * LazyImageDownloader2列表中使用
     *
     * @param path   绝对路径
     * @param width  目标宽 如果为0将使用原始宽度 （如果这个宽度大于原图宽度，返回原图宽度）
     * @param height 目标高 小余0使用宽度的缩放比例
     * @return Bitmap
     * @throws OutOfMemoryError
     */
    public static Bitmap getBitmapThrowsOutOfMemoryError(final String path, final int width, final int height, final Bitmap.Config inPreferredConfig) throws OutOfMemoryError {
        BitmapFactory.Options options;
        if (width > 0) {
            options = getOptions(path, width, height, inPreferredConfig);
        }else{
            options = new BitmapFactory.Options();
            options.inPreferredConfig = inPreferredConfig;
        }
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        Matrix m = new Matrix();
        m.reset();
        // 计算是否有旋转
        int degree = readPictureDegree(path);
        int w = bm.getWidth(), h = bm.getHeight();
        if (degree != 0) {
            if (degree / 90 % 2 == 1) {// 当处于基数时表示宽高需要交换
                h = bm.getWidth();
                w = bm.getHeight();
            }
            m.postRotate(degree);
        }
        // 如果没有设置宽度 使用原图宽度
        float targetWidth = width <= 0 ? w : width;
        float targetScaleX = targetWidth * 1.0f / w;
        float targetScaleY = height <= 0 ? targetScaleX : height * 1.0f * h;
        m.postScale(targetScaleX, targetScaleY);
        Bitmap result;
        if (targetScaleX != 1.0f || targetScaleY != 1.0f || degree != 0) {
            result = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            if (bm != result && !bm.isRecycled()) {
                bm.recycle();
            }
        } else {
            result = bm;
        }
        return result;
    }

    /**
     * 根据宽高重置给定Bitmap的大小
     *
     * @param bm     Bitmap
     * @param path   源路径（用来读取相片是否有旋转）
     * @param width  宽度 使用原图宽度给-1
     * @param height 高度根据宽度自动缩放给-1
     * @return Bitmap
     * @throws OutOfMemoryError
     */
    public static Bitmap resetBitmapThrowsOutOfMemoryError(Bitmap bm, String path, final int width, final int height) throws OutOfMemoryError {
        Matrix m = new Matrix();
        m.reset();
        // 计算是否有旋转
        int degree = readPictureDegree(path);
        int w = bm.getWidth(), h = bm.getHeight();
        if (degree != 0) {
            if (degree / 90 % 2 == 1) {// 当处于基数时表示宽高需要交换
                h = bm.getWidth();
                w = bm.getHeight();
            }
            m.postRotate(degree);
        }
        // 如果没有设置宽度 使用原图宽度
        float targetWidth = width <= 0 ? w : width;
        float targetScaleX = targetWidth * 1.0f / w;
        float targetScaleY = height <= 0 ? targetScaleX : height * 1.0f * h;
        m.postScale(targetScaleX, targetScaleY);
        Bitmap result;
        if (targetScaleX != 1.0f || targetScaleY != 1.0f || degree != 0) {
            result = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            if (bm != result && !bm.isRecycled()) {
                bm.recycle();
            }
        } else {
            result = bm;
        }
        return result;
    }

    /**
     * 速度比getBitmap快一些，但某些国产手机不好用，放大时会出现四周增加透明度而不是拉伸图片
     *
     * @param path   路径
     * @param width  目标宽 如果为0将使用原始宽度 （如果这个宽度大于原图宽度，返回原图宽度）
     * @param height 目标高 小余0使用宽度的缩放比例
     * @return Bitmap
     */
    public static Bitmap getBitmapAccurateSize(final String path, final int width, final int height) {
        Bitmap bit = getBitmap(path, width, height, false);
        Bitmap bit2 = null;
        if (bit != null) {
            int _w = width;
            if (_w <= 0) {
                _w = bit.getWidth();
            }
            float _h = height;
            if (height <= 0) {
                float s = _w * 1.0f / bit.getWidth();
                _h = bit.getHeight() * s;
            }
            bit2 = ThumbnailUtils.extractThumbnail(bit, _w, (int) _h);
            int degree = readPictureDegree(path);
            if (degree != 0) {
                bit2 = rotateBitmap(degree, bit2);
            }
        }
        if (bit != null && bit2 != null && bit != bit2 && !bit.isRecycled()) {
            bit.recycle();
        }
        return bit2;
    }

    /**
     * 通过path与需要的width 返回用户一个Drawable
     *
     * @param res   activity.getResources
     * @param path  地址
     * @param width 目标宽
     * @return Drawable
     */
    public static Drawable getDrawable(Resources res, final String path, final int width) {
        Bitmap bm = getBitmapProportional(path, width);
        if (bm == null) {
            return null;
        }
        return new BitmapDrawable(res, bm);
    }

    /**
     * 通过path、width 和 height 返回一个Drawable
     *
     * @param res    activity.getResources()
     * @param path   绝对路径
     * @param width  目标宽
     * @param height 目标高
     * @return Drawable
     */
    public static Drawable getDrawable(Resources res, final String path, final int width, final int height) {
        Bitmap bm = getBitmap(path, width, height, false);
        if (bm == null) {
            return null;
        }
        return new BitmapDrawable(res, bm);
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap Bitmap
     * @return Bitmap
     */
    public static Bitmap getRoundBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    /**
     * 创建圆角图片
     *
     * @param bitmap Bitmap
     * @param pixels 圆角像素
     * @return Bitmap
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        if (bitmap == null || bitmap.isRecycled())
            return null;
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, pixels, pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); // Mode.SRC_IN
        canvas.drawBitmap(bitmap, rect, rect, paint);
        if (bitmap != output && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return output;
    }

    /**
     * 返回一个六边形的图片
     *
     * @param src          Bitmap
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return Bitmap
     */
    public static Bitmap getHexagonBitmap(Bitmap src, int targetWidth, int targetHeight) throws OutOfMemoryError {
        if (src == null || src.isRecycled()) {
            return null;
        }
        final int radius = targetWidth / 2;
        final double radian30 = 30 * Math.PI / 180;
        final float a = (float) (radius * Math.sin(radian30));
        final float b = (float) (radius * Math.cos(radian30));
        final float c = (targetHeight - 2 * b) / 2;
        Path path = new Path();
        path.reset();
        path.moveTo(targetWidth, targetHeight / 2);
        path.lineTo(targetWidth - a, targetHeight - c);
        path.lineTo(targetWidth - a - radius, targetHeight - c);
        path.lineTo(0, targetHeight / 2);
        path.lineTo(a, c);
        path.lineTo(targetWidth - a, c);
        path.close();
        return clipBitmap(src, targetWidth, targetHeight, path);
    }

    /**
     * 根据给定的Path裁剪图片
     *
     * @param src          Bitmap 使用完成后将被触发recycle
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @param path         用来切个图片的路径
     * @return Bitmap
     */
    public static Bitmap clipBitmap(Bitmap src, int targetWidth, int targetHeight, Path path) throws OutOfMemoryError {
        //创建一个空的画布，用来存储切割后的图片
        Bitmap result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(result);
        //清空画布
        c.drawARGB(0, 0, 0, 0);
        //初始化画笔
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#FFFFFF"));
        //绘制路径
        c.drawPath(path, paint);
        //使用SRC_IN模式绘制Bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); // Mode.SRC_IN
        c.drawBitmap(src, 0, 0, paint);
        if (!src.isRecycled()) {
            src.recycle();
        }
        return result;
    }

    /**
     * 根据给定的Path裁剪图片
     *
     * @param src  Bitmap 使用完成后将被触发recycle
     * @param mask Bitmap 用来做扣图的蒙版
     * @return Bitmap
     */
    public static Bitmap clipBitmap(Bitmap src, Bitmap mask) {
        return clipBitmap(src, mask, src.getWidth(), src.getHeight());
    }

    /**
     * 根据给定的Path裁剪图片
     *
     * @param src          Bitmap 使用完成后将被触发recycle
     * @param mask         Bitmap 用来做扣图的蒙版
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return Bitmap
     */
    public static Bitmap clipBitmap(Bitmap src, Bitmap mask, int targetWidth, int targetHeight) throws OutOfMemoryError {
        if (src == null || src.isRecycled() || mask == null || mask.isRecycled()) {
            return null;
        }
        Bitmap result = null;
        //创建一个空的画布，用来存储切割后的图片
        result = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
        Canvas c = new Canvas(result);
        //清空画布
        c.drawARGB(0, 0, 0, 0);
        //初始化画笔
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        paint.setColor(Color.parseColor("#FFFFFF"));

        /**
         * 绘制模板，宽度相同
         */
        Bitmap maskNew;
        if (mask.getWidth() != targetWidth) {
            Matrix m = new Matrix();
            m.reset();
            float s = targetWidth * 1.0f / mask.getWidth();
            m.postScale(s, s);
            c.drawBitmap(mask, m, paint);
            maskNew = Bitmap.createBitmap(mask, 0, 0, mask.getWidth(), mask.getHeight(), m, true);
        } else {
            maskNew = mask;
        }
        int offTop = (targetHeight - mask.getHeight()) / 2;
        c.drawBitmap(maskNew, 0, offTop, paint);
        //使用SRC_IN模式绘制Bitmap
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); // Mode.SRC_IN
        c.drawBitmap(src, 0, 0, paint);
        if (!src.isRecycled()) {
            src.recycle();
        }
        return result;
    }

    /**
     * 释放view中设置的Background内存，如果view为ImageView则同时尝试释放getDrawable中持有的图像
     *
     * @param view View
     */
    public static void recycle(View view) {
        if (view != null) {
            if (view instanceof ImageView) {
                ImageView iv = ((ImageView) view);
                if (iv.getDrawable() != null) {
                    recycleDrawable(iv.getDrawable());
                }
            }
            if (view.getBackground() != null) {
                recycleDrawable(view.getBackground());
            }
        }
    }

    /**
     * 释放Drawable内存
     *
     * @param drawable Drawable
     */
    public static void recycleDrawable(Drawable drawable) {
        if (drawable != null) {
            Bitmap bmp = null;
            try {
                bmp = ((BitmapDrawable) drawable).getBitmap();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null != bmp && !bmp.isRecycled()) {
                bmp.recycle();
            }
        }
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片旋转N角度后返回 (转换后将释放"原Bitmap"的内存)
     *
     * @param degree 角度
     * @param bitmap 原图
     * @return Bitmap
     */
    public static Bitmap rotateBitmap(int degree, Bitmap bitmap) {
        return rotateBitmap(degree, bitmap, true);
    }

    /**
     * 将图片旋转N角度后返回
     *
     * @param degree  角度
     * @param bitmap  原图
     * @param recycle true转换后将释放"原Bitmap"的内存
     * @return Bitmap
     */
    public static Bitmap rotateBitmap(int degree, final Bitmap bitmap, boolean recycle) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        // 创建新的图片
        Bitmap b = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (recycle && b != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return b;
    }

    /**
     * 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
     *
     * @param path 地址
     * @return base64String
     */
    public static String imageToBase64Str(String path) {
        InputStream in;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream(path);
            data = new byte[in.available()];
            int len = in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    /**
     * 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
     *
     * @param bitmap Bitmap
     * @return base64String
     */
    public static String imageToBase64Str(Bitmap bitmap) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, bao);// 得到输出流
        return Base64.encodeToString(bao.toByteArray(), Base64.DEFAULT);
    }

    /**
     * 根据给定的drawableId读取一下Bitmap
     *
     * @param context Context
     * @param id      drawableId
     * @return Bitmap
     */
    public Bitmap getBitmapToResources(Context context, final int id) {
        Resources res = context.getResources();
        Drawable draw;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            draw = res.getDrawable(id, context.getTheme());
        } else {
            draw = res.getDrawable(id);
        }
        if (draw == null) {
            return null;
        }
        BitmapDrawable bd = (BitmapDrawable) draw;
        return bd.getBitmap();
    }

    /**
     * 将bitmap转存到指定目录下 JPEG格式
     *
     * @param directory 目录
     * @param bitmap    Bitmap
     * @return 路径
     */
    public static String saveBitmap(String directory, Bitmap bitmap, CompressFormat format) {
        FileTool.createDirectory(directory);
        String suffix;
        switch (format) {
            case PNG:
                suffix = ".png";
                break;
            case WEBP:
                suffix = ".webp";
                break;
            default:
                suffix = ".jpg";
                break;
        }
        String _path = directory + "/" + new Date().getTime() + suffix;
        try {
            bitmap.compress(format, 90, new FileOutputStream(new File(_path)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            _path = null;
        }
        return _path;
    }

    /**
     * 返回Bitmap的 byte[]形式, 读取完成后释放源Bitmap
     *
     * @param bmp Bitmap
     * @return byte[]
     */
    public static byte[] bitmapToByteArray(final Bitmap bmp) {
        return bitmapToByteArray(bmp, true);
    }

    /**
     * 返回Bitmap的 byte[]形式
     *
     * @param bmp         Bitmap
     * @param needRecycle 读取完成后是否释放源Bitmap
     * @return byte[]
     */
    public static byte[] bitmapToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 解码WEBP图片
     *
     * @param is InputStream
     * @return Bitmap
     */
    public static Bitmap decodeWebp(InputStream is) {
        Bitmap bitmap = null;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            while ((is.read(buffer, 0, buffer.length)) > 0) {
                baos.write(buffer);
            }
            is.close();
            byte[] data = baos.toByteArray();

            int[] width = new int[]{0};
            int[] height = new int[]{0};
            byte[] decodedData = LibWebp.WebPDecodeARGB(data, data.length, width, height);
            int[] pixels = new int[decodedData.length / 4];
            ByteBuffer.wrap(decodedData).asIntBuffer().get(pixels);
            bitmap = Bitmap.createBitmap(pixels, width[0], height[0], Bitmap.Config.ARGB_8888);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 将Bitmap存储成WEBP格式
     *
     * @param bitmap  Bitmap
     * @param quality 质量
     * @param os      OutputStream
     */
    public static void encodeWebp(Bitmap bitmap, int quality, OutputStream os) {
        Config config = bitmap.getConfig();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        byte[] sourceByteArray;
        byte[] encodedData = null;
        if (config.equals(Config.ARGB_8888)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
            bitmap.copyPixelsToBuffer(byteBuffer);
            sourceByteArray = byteBuffer.array();
            encodedData = LibWebp.WebPEncodeRGBA(sourceByteArray, width, height, width * 4, quality);
        } else {
            sourceByteArray = new byte[width * height * 4];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int pixel = bitmap.getPixel(i, j);
                    int index = (j * width + i) * 4;
                    sourceByteArray[index] = (byte) (pixel & 0xff);
                    sourceByteArray[index + 1] = (byte) (pixel >> 8 & 0xff);
                    sourceByteArray[index + 2] = (byte) (pixel >> 16 & 0xff);
                    sourceByteArray[index + 3] = (byte) (pixel >> 24 & 0xff);
                }
            }
            encodedData = LibWebp.WebPEncodeRGBA(sourceByteArray, width, height, width * 4, quality);
        }
        try {
            os.write(encodedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 裁剪掉图片四周的透明部分
     *
     * @param src 源图片
     * @return 新的图片
     */
    public static Bitmap clipBitmapTransparent(Bitmap src) {
        return clipBitmap(src, Color.TRANSPARENT);
    }

    /**
     * 裁剪掉图片四周的白色部分
     *
     * @param src 源图片
     * @return 新的图片
     */
    public static Bitmap clipBitmapWhite(Bitmap src) {
        return clipBitmap(src, Color.WHITE);
    }

    /**
     * 裁剪掉图片四周的黑色部分
     *
     * @param src 源图片
     * @return 新的图片
     */
    public static Bitmap clipBitmapBlack(Bitmap src) {
        return clipBitmap(src, Color.BLACK);
    }

    /**
     * 根据给定的眼色，在置顶的src四边裁剪
     *
     * @param src         Bitmap源图片
     * @param targetColor 裁剪颜色
     * @return 新的图片
     */
    public static Bitmap clipBitmap(Bitmap src, int targetColor) {
        Bitmap out;
        final int w = src.getWidth();
        final int h = src.getHeight();
        final int[] ss = new int[w * h];
        src.getPixels(ss, 0, w, 0, 0, w, h);
        int top = -1, bottom = -1, left = -1, right = -1;
        // 找top距离
        for (int r = 0; r < h; r++) {
            if (top != -1) {
                break;
            }
            int off = r * w;
            for (int c = 0; c < w; c++) {
                if (ss[off + c] != targetColor) {
                    top = r;
                    break;
                }
            }
        }

        // 找bottom
        for (int r = h - 1; r >= 0; r--) {
            if (bottom != -1) {
                break;
            }
            int off = r * w;
            for (int c = 0; c < w; c++) {
                if (ss[off + c] != targetColor) {
                    bottom = r;
                    break;
                }
            }
        }

        // 找left
        for (int c = 0; c < w; c++) {
            if (left != -1) {
                break;
            }
            for (int r = top; r <= bottom; r++) {
                if (ss[r * w + c] != targetColor) {
                    left = c;
                    break;
                }
            }
        }

        // 找right
        for (int c = w - 1; c >= 0; c--) {
            if (right != -1) {
                break;
            }
            for (int r = top; r <= bottom; r++) {
                if (ss[r * w + c] != targetColor) {
                    right = c;
                    break;
                }
            }
        }

        try {
            top = top == -1 ? 0 : top;
            left = left == -1 ? 0 : left;
            right = right == -1 ? 0 : right;
            bottom = bottom == -1 ? 0 : bottom;
            out = Bitmap.createBitmap(src, left, top, right - left, bottom - top);
            return out;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
