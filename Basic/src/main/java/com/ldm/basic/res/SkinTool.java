package com.ldm.basic.res;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.LLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by ldm on 14-5-3.
 * 换肤工具
 */
public class SkinTool {

    private static SkinTool skinTool;

    private static String FILES_DIR;//本地缓存文件路径

    /**
     * filesDir 下面对应的第一个文件夹名（存储皮肤的文件夹），
     * 也是skinFilePath.zip目录中的第一个文件夹名字，必需保持一致
     */
    private final String skinRootDirectory;

    /**
     * 实例化换肤工具
     */
    private SkinTool() {
        this.skinRootDirectory = "skin";
    }

    /**
     * 获取SkinTool实例，使用前需要
     *
     * @return SkinTool
     */
    public static SkinTool getInstance() {
        if (FILES_DIR == null) {
            LLog.e("FILES_DIR 为空，请使用setFilesDir(path)设置皮肤存储路径！");
            return null;
        }
        if (skinTool == null) {
            skinTool = new SkinTool();
        }
        return skinTool;
    }

    /**
     * 获取SkinTool实例并指定filesDir目录
     *
     * @param filesDir 皮肤目录
     * @return SkinTool
     */
    public static SkinTool getInstance(String filesDir) {
        setFilesDir(filesDir);
        return getInstance();
    }

    /**
     * 将制定的皮肤文件加载到filesDir目录下
     *
     * @param skinFilePath 文件路径
     * @return true加载完成
     */
    public boolean loadingSkin(String skinFilePath) {
        boolean result = false;
        ZipFile skinZip = null;
        try {
            skinZip = new ZipFile(skinFilePath);
            FileTool ft = new FileTool();
            for (Enumeration<? extends ZipEntry> entries = skinZip.entries(); entries.hasMoreElements(); ) {
                ZipEntry z = entries.nextElement();
                if (z.isDirectory()) {
                    new File(FILES_DIR + "/" + z.getName()).mkdirs();
                } else {
                    File outFile = new File(FILES_DIR + "/" + z.getName());
                    if (outFile.exists()) {
                        if (!FileTool.deleteToLinux(FILES_DIR + "/" + z.getName())) {
                            FileTool.deleteAllFiles(FILES_DIR + "/" + z.getName());
                        }
                    }
                    if (outFile.createNewFile()) {
                        ft.write(skinZip.getInputStream(z), new FileOutputStream(outFile));
                    }
                }
            }
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
        	if (skinZip != null) {
        		try {
					skinZip.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        }
        return result;
    }

    /**
     * 根据指定名称获取Drawable，后缀默认png
     *
     * @param res  Resources
     * @param name 文件名，不包含后缀
     * @return 没有找到时返回null
     */
    public Drawable getDrawable(Resources res, String name) {
        Bitmap bitmap = getBitmap(name);
        return bitmap == null ? null : new BitmapDrawable(res, bitmap);
    }

    /**
     * 根据指定名称获取Drawable
     *
     * @param res    Resources
     * @param name   文件名，不包含后缀
     * @param suffix 文件后缀
     * @return Drawable
     */
    public Drawable getDrawable(Resources res, String name, String suffix) {
        Bitmap bitmap = getBitmap(name, suffix);
        return bitmap == null ? null : new BitmapDrawable(res, bitmap);
    }

    /**
     * 根据名称获取.9图像，后缀仅支持png
     *
     * @param res  Resources
     * @param name 文件名 如：image.9.png 仅指定image
     * @return Drawable
     */
    public Drawable getNinePatchDrawable(Resources res, String name) {
        Bitmap bm = getBitmap(name, "9.png");
        return bm == null ? null : new NinePatchDrawable(res, bm, bm.getNinePatchChunk(), new Rect(0, 0, bm.getWidth(), bm.getHeight()), null);
    }

    /**
     * 根据name获取Bitmap，后缀默认为png
     *
     * @param name 文件名 不包含后缀
     * @return 没有找到返回null
     */
    public Bitmap getBitmap(String name) {
        return getBitmap(name, "png");
    }

    /**
     * 根据指定name与suffix获取Bitmap
     *
     * @param name   文件名
     * @param suffix 后缀
     * @return 没有找到返回null
     */
    public Bitmap getBitmap(String name, String suffix) {
        Bitmap bitmap = null;
        File f;
        if ((f = exists(name, suffix)) != null) {
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 根据名称与后缀在skinRootDirectory目录中检索文件是否存在
     *
     * @param name   名称
     * @param suffix 后缀
     * @return 如果存在返回File，不存在返回null
     */
    public File exists(String name, String suffix) {
        File f = new File(FILES_DIR + "/" + skinRootDirectory + "/" + name + "." + suffix);
        return f.exists() ? f : null;
    }

    /**
     * 删除当前加载的皮肤
     *
     * @return true删除成功
     */
    public boolean removeSkin() {
        if (!FileTool.deleteToLinux(FILES_DIR + "/" + skinRootDirectory)) {
            FileTool.deleteAllFiles(FILES_DIR + "/" + skinRootDirectory);
        }
        return true;
    }

    /**
     * 设置缓存路径
     *
     * @param filesDir 设置皮肤加载路径
     */
    public static void setFilesDir(String filesDir) {
        SkinTool.FILES_DIR = filesDir;
    }
}
