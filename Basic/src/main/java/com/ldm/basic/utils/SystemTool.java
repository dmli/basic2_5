package com.ldm.basic.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ldm.basic.BasicService;
import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.db.BasicSQLiteOpenHelper;
import com.ldm.basic.res.ResourcesUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ldm on 10-6-11.
 * 提供系统所需基本数据及功能
 */
public class SystemTool {

    /**
     * 系统窗口宽高 STATUS_BAR_HEIGHT 状态栏高度
     */

    public static int SYS_SCREEN_WIDTH, SYS_SCREEN_HEIGHT, SYS_REAL_SCREEN_WIDTH, SYS_REAL_SCREEN_HEIGHT, STATUS_BAR_HEIGHT;

    /**
     * 这个尺寸用来做适配时的基础尺寸
     */
    public static double SYS_ADAPTER_BASE_WIDTH;
    public static double SYS_ADAPTER_BASE_HEIGHT;

    /**
     * 系统版本 如 2.3.3
     */
    public static String SYS_VERSION_RELEASE;

    /**
     * 系统版本数值 2.1 == 7
     */
    public static int SYS_SDK_INT;

    /**
     * 像素密度（像素比例）
     */
    public static float DENSITY;

    /**
     * 手机唯一码
     */
    public static String DEVICE_ID;

    /**
     * 像素密度（每寸像素）
     */
    public static int DENSITY_DPI;

    // 系统退出标记
    private static boolean isQuit = false;

    private static Gson gson;

    /**
     * Activity的集合，用于记录所有初始过的Activity
     */
    public static Map<String, WeakReference<Activity>> activitySet;

    static {
        activitySet = new HashMap<String, WeakReference<Activity>>();
        STATUS_BAR_HEIGHT = 0;
    }

    public static void initSysInfo(final Activity activity) {
        initSysInfo(activity, false);
    }

    /**
     * 获取系统所有参数（进入系统需第一时间执行）
     *
     * @param activity Activity
     */
    public static void initSysInfo(final Activity activity, boolean debug) {
        // 获取手机唯一码
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        SYS_VERSION_RELEASE = Build.VERSION.RELEASE;
        SYS_SDK_INT = Build.VERSION.SDK_INT;
        SYS_SCREEN_WIDTH = dm.widthPixels;
        SYS_SCREEN_HEIGHT = dm.heightPixels;
        DEVICE_ID = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        DENSITY = dm.density;
        DENSITY_DPI = dm.densityDpi;

        try {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Method method = Display.class.getMethod("getRealMetrics", DisplayMetrics.class);
            DisplayMetrics dm1 = new DisplayMetrics();
            method.invoke(display, dm1);
            SYS_REAL_SCREEN_WIDTH = dm1.widthPixels;
            SYS_REAL_SCREEN_HEIGHT = dm1.heightPixels;
        } catch (Exception e) {
            SYS_REAL_SCREEN_WIDTH = SYS_SCREEN_WIDTH;
            SYS_REAL_SCREEN_HEIGHT = SYS_SCREEN_HEIGHT;
        }

        BasicApplication.setModeToDebug(debug);
        BasicApplication.CLIENT_START_STATE = true;
        getStatusBarHeight(activity);// 初始化一次系统通知栏高度
    }

    /**
     * 获取状态栏的高度
     *
     * @param activity Activity
     * @return int
     */
    public static int getStatusBarHeight(final Activity activity) {
        View root = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        if (root != null) {
            int[] ii = new int[2];
            root.getLocationInWindow(ii);
            STATUS_BAR_HEIGHT = ii[1];
        }
        return STATUS_BAR_HEIGHT;
    }

    /**
     * 根据工程基础宽度自动适应手机分辨率（需要设置SYS_ADAPTER_BASE_WIDTH后才能使用）
     *
     * @param width 基础宽度
     * @return double 新宽度
     */
    public static int autoWidthSize(double width) {
        return (int) calculate(width, SYS_ADAPTER_BASE_WIDTH, SYS_SCREEN_WIDTH);
    }

    /**
     * 根据工程基础高度自动适应手机分辨率（需要设置SYS_ADAPTER_BASE_HEIGHT后才能使用）
     *
     * @param height 基础高度
     * @return double 新高度
     */
    public static int autoHeightSize(double height) {
        return (int) calculate(height, SYS_ADAPTER_BASE_HEIGHT, SYS_SCREEN_HEIGHT);
    }

    /**
     * 获取手机屏幕宽度
     *
     * @param activity 设置这个参数，当内存丢失时可以重新初始化数据
     * @return SYS_SCREEN_WIDTH
     */
    public static int getSysScreenWidth(Activity activity) {
        if (SYS_SCREEN_WIDTH <= 0 && activity != null) {
            initSysInfo(activity);
        }
        return SYS_SCREEN_WIDTH;
    }

    /**
     * 获取手机屏幕高度
     *
     * @param activity 设置这个参数，当内存丢失时可以重新初始化数据
     * @return 设置这个参数，当内存丢失时可以重新初始化数据
     */
    public static int getSysScreenHeight(Activity activity) {
        if (SYS_SCREEN_HEIGHT <= 0 && activity != null) {
            initSysInfo(activity);
        }
        return SYS_SCREEN_HEIGHT;
    }

    /**
     * 获取手机屏幕的宽度
     *
     * @param activity 设置这个参数，当内存丢失时可以重新初始化数据
     * @return SYS_REAL_SCREEN_WIDTH
     */
    public static int getSysRealScreenWidth(Activity activity) {
        if (SYS_REAL_SCREEN_WIDTH <= 0 && activity != null) {
            initSysInfo(activity);
        }
        return SYS_REAL_SCREEN_WIDTH <= 0 ? SYS_SCREEN_WIDTH : SYS_REAL_SCREEN_WIDTH;
    }

    /**
     * 获取手机屏幕的真实高度（这里将包括虚拟键盘）
     *
     * @param activity 设置这个参数，当内存丢失时可以重新初始化数据
     * @return SYS_REAL_SCREEN_HEIGHT
     */
    public static int getSysRealScreenHeight(Activity activity) {
        if (SYS_REAL_SCREEN_HEIGHT <= 0 && activity != null) {
            initSysInfo(activity);
        }
        return SYS_REAL_SCREEN_HEIGHT <= 0 ? SYS_SCREEN_HEIGHT : SYS_REAL_SCREEN_HEIGHT;
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    /**
     * 返回当前手机是否有虚拟按键
     *
     * @param activity Activity
     * @return true有虚拟按键
     */
    public static boolean isVirtualKey(Activity activity) {
        if (SYS_REAL_SCREEN_HEIGHT <= 0 && activity != null) {
            initSysInfo(activity);
        }
        return SYS_REAL_SCREEN_HEIGHT > 0 && SYS_REAL_SCREEN_HEIGHT != SYS_SCREEN_HEIGHT;
    }

    /**
     * 返回当前手机的虚拟键盘高度
     *
     * @param activity Activity
     * @return 0表示没有虚拟按键
     */
    public static int getVirtualKeyHeight(Activity activity) {
        if (SYS_REAL_SCREEN_HEIGHT <= 0 && activity != null) {
            initSysInfo(activity);
        }
        return SYS_REAL_SCREEN_HEIGHT > 0 ? SYS_REAL_SCREEN_HEIGHT - SYS_SCREEN_HEIGHT : 0;
    }

    /**
     * 全屏操作
     *
     * @param activity Activity
     */
    @SuppressLint("NewApi")
    public static void fullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 这个方法可以根据给定的基础分辨率与数值计算出适合当前分辨率使用的大小
     *
     * @param value     数值
     * @param baseValue 数值所在的分辨率
     * @param nowValue  当前分辨率
     * @return double 新数值
     */
    public static double calculate(double value, double baseValue, double nowValue) {
        double cardinal = value / baseValue;
        return Math.round(cardinal * nowValue);
    }

    /**
     * 启动BasicService，启动后可以通过BasicService getInstance()拿到实例
     *
     * @param context Context
     */
    public static void startBasicService(final Context context) {
        if (BasicService.getInstance() == null) {
            try {
                context.startService(new Intent(context, BasicService.class).setFlags(32));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取SIM卡中的电话号码
     *
     * @return phoneNumber 号码可能为空
     */
    public static String localReadSIM(final Context context) {
        String result = "";
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        result = tm.getLine1Number();
        if (result != null && !"".equals(result) && result.length() >= 11) {
            result = result.trim().replace("+86", "");
        }
        return result;
    }

    /**
     * 返回软盘是否开启状态
     *
     * @param ac Activity
     * @return true开启 false没有开启
     */
    public static boolean isSoftInputOpen(Activity ac) {
        final Rect r = new Rect();
        View rootNode = ac.getWindow().getDecorView();
        rootNode.getWindowVisibleDisplayFrame(r);
        final int screenHeight = rootNode.getRootView().getHeight();
        return screenHeight != r.bottom;
    }

    /**
     * 获取软键盘高度(这个方法需要在软键盘完全出现后调用有效,建议在OnGlobalLayoutListener监听中使用)
     * *rootNode.getViewTreeObserver().addOnGlobalLayoutListener*
     *
     * @param ac Activity
     * @return int
     */
    public static int getSoftInputHeight(Activity ac) {
        final Rect r = new Rect();
        View rootNode = ac.getWindow().getDecorView();
        rootNode.getWindowVisibleDisplayFrame(r);
        final int screenHeight = rootNode.getRootView().getHeight();
        return screenHeight - r.bottom;
    }

    /**
     * 获取手机网络状态
     *
     * @param context Context
     * @return true为打开状态
     */
    public static boolean getNetworkStatus(final Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                NetworkInfo networkinfo = manager.getActiveNetworkInfo();
                return networkinfo != null && networkinfo.isAvailable();
            }
        }
        return true;
    }

    /**
     * 验证当前是否使用使用wifi上网
     *
     * @param context Context
     * @return true=wifi
     */
    public static boolean isWifiNetwork(final Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        return networkinfo != null && networkinfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 返回当前是否使用使用手机流量
     *
     * @param context Context
     * @return true=手机流量
     */
    public static boolean isMobileNetwork(final Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        return networkinfo != null && networkinfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 安装APK文件
     *
     * @param activity Activity
     * @param filePath 路径
     */
    public static void install(final Activity activity, final String filePath) {
        install(activity, new File(filePath));
    }

    /**
     * 安装APK
     *
     * @param activity Activity
     * @param file     文件
     */
    public static void install(final Activity activity, final File file) {
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, "没有找到安装文件，安装失败！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 进入裁剪界面
     *
     * @param activity     Activity
     * @param uri          图片的Uri
     * @param expectWidth  期望宽度
     * @param expectHeight 期望高度
     * @param requestCode  请求代码，在onActivityResult中可以得到
     */
    public static void cropPhoto(final Activity activity, Uri uri, int expectWidth, int expectHeight, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", expectWidth);
        intent.putExtra("outputY", expectHeight);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 将图库返回的Uri转换成可用的绝对路径
     *
     * @param context Context
     * @param uri     Uri
     * @return 绝对地址
     */
    public static String imageUriToPath(final Context context, Uri uri) {
        String path = null;
        try {
            if (SystemTool.SYS_SDK_INT >= 19) {
                path = ResourcesUtils.getPath(context, uri);
            } else {
                ContentResolver resolver = context.getContentResolver();
                if (uri != null) {
                    Cursor cursor = resolver.query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                    if (cursor != null) {
                        int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        if (cursor.moveToFirst()) {
                            path = cursor.getString(column_index);
                        }
                        cursor.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * 获取状态栏高度
     *
     * @return px
     */
    public static int getStatusHeight() {
        Resources sys = Resources.getSystem();
        if (sys == null) {
            return 38;
        }
        return sys.getDimensionPixelSize(sys.getIdentifier("status_bar_height", "dimen", "android"));
    }

    /**
     * 强制关闭APP，需要申请root权限
     *
     * @param pkgName 包路径
     */
    public static void forceStopAPP(String pkgName) {
        Process sh = null;
        DataOutputStream os = null;
        try {
            sh = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(sh.getOutputStream());
            final String Command = "am force-stop " + pkgName + "\n";
            os.writeBytes(Command);
            os.flush();
            sh.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放掉驻留在系统后台的进程
     *
     * @param context Context
     */
    @SuppressWarnings("deprecation")
    public static void killBackgroundProcesses(Context context) {
        ActivityManager activityManger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List list = activityManger.getRunningAppProcesses();
        if (list != null) {
            for (int i = 0; i < list.size(); ++i) {
                ActivityManager.RunningAppProcessInfo amInfo = (ActivityManager.RunningAppProcessInfo) list.get(i);
                if (amInfo.pkgList != null && amInfo.pkgList.length > 0) {
                    String[] pkgList = new String[amInfo.pkgList.length];
                    System.arraycopy(amInfo.pkgList, 0, pkgList, 0, amInfo.pkgList.length);
                    for (String s : pkgList) {
                        try {
                            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(s, 0);
                            if (!((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                                    activityManger.killBackgroundProcesses(s);
                                } else {
                                    activityManger.restartPackage(s);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 申请root权限，返回状态：  -1这个手机没有root   1申请被拒绝  0申请成功
     *
     * @param command 通过 getPackageCodePath获得
     * @return -1这个手机没有root | 1申请被拒绝 | 0申请成功
     */

    public static int applyRootPermission(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            return process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * dip转px
     *
     * @param value dip值
     * @return px值
     */
    public static float dipToPx(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param value px值
     * @return px值
     */
    public static float spToPx(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 模拟系统HOME键
     *
     * @param activity Activity
     */
    public static void sysHome(final Activity activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        activity.startActivity(intent);
    }

    /**
     * 打开软键盘
     *
     * @param context Context
     */
    public static void showSoftInput(final Context context) {
        showSoftInput(context, null);
    }

    /**
     * 打开软键盘
     *
     * @param context Context
     * @param view    要使用软盘的view
     */
    public static void showSoftInput(final Context context, final View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        view.setFocusable(true);
        view.requestFocus();
        boolean bool = imm.showSoftInput(view, 0);
        if (!bool) {
            bool = imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
        if (!bool) {
            imm.showSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     * 弹出软盘
     *
     * @param context Context
     * @param view    要使用软盘的view
     * @link 请使用showSoftInput(Context, View)
     * @deprecated
     */
    public static void showSoftInputFromInputMethod(final Context context, final View view) {
        showSoftInput(context, view);
    }

    /**
     * 关闭软盘
     *
     * @param context Context
     */
    public static void hideSoftInput(final Context context) {
        hideSoftInput(context, null);
    }

    /**
     * 关闭软盘
     *
     * @param context Context
     * @param binder  IBinder
     */
    public static boolean hideSoftInput(final Context context, final IBinder binder) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean bool = false;
        try {
            if (imm.isActive()) {
                if (binder != null) {
                    bool = imm.hideSoftInputFromWindow(binder, 0);
                }
                if (!bool && context instanceof Activity) {
                    Activity a = (Activity) context;
                    if (a.getCurrentFocus() != null) {
                        bool = imm.hideSoftInputFromWindow(a.getCurrentFocus().getWindowToken(), 0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    /**
     * 软盘开启则关闭，软盘关闭则开启
     *
     * @param activity Activity
     */
    public static void toggleSoftInput(final Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 根据手机版本获取设置界面的ACTION
     *
     * @return ACTION_SETTINGS
     */
    public static String getSettingsAction() {
        if (SYS_SDK_INT <= 10) {
            return Settings.ACTION_WIRELESS_SETTINGS;
        } else {
            return Settings.ACTION_SETTINGS;
        }
    }

    /**
     * 获取指定packageName对应的PackageInfo
     *
     * @param context     Context
     * @param packageName 包路径
     * @return null or PackageInfo
     */
    public static PackageInfo getAppPackageInfo(Context context, String packageName) {
        PackageInfo version = null;
        try {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                version = pm.getPackageInfo(packageName, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 延时启动Activity
     *
     * @param context     Context
     * @param intent      Intent
     * @param millisecond 延时毫秒数
     */
    public static void delayStart(final Context context, final Intent intent, final long millisecond) {
        PendingIntent restartIntent = PendingIntent.getActivity(context, 0, intent, Intent.FILL_IN_ACTION);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + millisecond, restartIntent);
        exit(true);
    }

    /**
     * 2秒内连续2次返回键退出系统 退出所有的activity，保留service
     *
     * @param context Context
     */
    public static void quit(final Context context) {
        quit(context, null, false);
    }

    /**
     * 2秒内连续2次返回键退出系统 退出所有的activity，保留service
     *
     * @param context  Context
     * @param callback 回调接口
     */
    public static void quit(final Context context, ExitCallback callback) {
        quit(context, callback, false);
    }

    /**
     * 2秒内连续2次返回键退出系统 退出所有的activity，保留service
     *
     * @param context Context
     * @param kill    是否kill进程
     */
    public static void quit(final Context context, boolean kill) {
        quit(context, null, kill);
    }

    /**
     * 2秒内连续2次返回键退出系统
     *
     * @param context Context
     * @param kill    是否kill进程
     */
    public static void quit(final Context context, ExitCallback callback, boolean kill) {
        if (isQuit) {
            SystemTool.exit(callback, kill);
        } else {
            Toast.makeText(context, BasicApplication.CONSTANTS.EXIT_TEXT, Toast.LENGTH_SHORT).show();
            isQuit = true;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isQuit = false;
                }
            }, 2000);
        }
    }

    /**
     * 退出带有标记的本系统中的所有Activity
     *
     * @param kill true 结束进程，将结束后台的service
     */
    public static void exit(boolean kill) {
        exit(null, kill);
    }

    /**
     * 退出带有标记的本系统中的所有Activity
     *
     * @param callback 回调接口 接口中不允许使用更新UI的代码
     * @param kill     true 结束进程，将结束后台的service
     */
    public static void exit(ExitCallback callback, boolean kill) {
        finishAllActivity(null);
        try {
            if (callback != null) {
                callback.exit();
            }
            BasicApplication.CLIENT_START_STATE = false;
            BasicSQLiteOpenHelper.closeDB();
            if (kill) {
                // 关闭没有记录到 activitySet 中的Activity 并结束所有服务
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 销毁所有继承BasicActivity的Activity
     */
    public static void finishAllActivity() {
        finishAllActivity(null);
    }

    /**
     * 销毁所有继承BasicActivity的Activity,但保留调用者
     *
     * @param keep 保留着
     */
    public static void finishAllActivity(Activity keep) {
        final Map<String, WeakReference<Activity>> acs = activitySet;
        if (acs != null && acs.size() > 0) {
            String[] names = null;
            synchronized (acs) {
                names = acs.keySet().toArray(new String[acs.size()]);
            }
            for (String s : names) {
                WeakReference<Activity> wr = acs.get(s);
                if (wr != null) {
                    Activity a = wr.get();
                    if (a != null && !a.equals(keep)) {
                        a.finish();
                    }
                }
            }
        }
    }

    /**
     * 删除对应的cla的Activity （删除并不执行finish()），模糊查询
     *
     * @param cla Class<? extends BasicActivity>
     */
    public static void removeActivity(Class<? extends Activity> cla) {
        final Map<String, WeakReference<Activity>> acs = activitySet;
        if (acs != null && acs.size() > 0) {
            String[] names;
            synchronized (acs) {
                names = acs.keySet().toArray(new String[acs.size()]);
            }
            String name = cla.getName();
            synchronized (activitySet) {
                for (String str : names) {
                    if (str.startsWith(name)) {
                        acs.remove(str);
                    }
                }
            }
        }
    }

    /**
     * 删除对应activityKey的activity，（删除并不执行finish()）精确匹配
     *
     * @param activityKey activityKey
     */
    public static void removeActivity(String activityKey) {
        if (activitySet != null && activitySet.size() > 0) {
            synchronized (activitySet) {
                if (activitySet.containsKey(activityKey)) {
                    activitySet.remove(activityKey);
                }
            }
        }
    }

    /**
     * 返回这个activity的存活数量
     *
     * @param cla Class<? extends BasicActivity>
     * @return 数量
     */
    public static int isActivityAliveNumber(Class<? extends Activity> cla) {
        int count = 0;
        final Map<String, WeakReference<Activity>> acs = activitySet;
        if (acs != null && acs.size() > 0) {
            String[] names;
            synchronized (acs) {
                names = acs.keySet().toArray(new String[acs.size()]);
            }
            String name = cla.getName();
            for (String s : names) {
                if (s.startsWith(name)) {
                    WeakReference<Activity> wa1 = acs.get(s);
                    if (wa1 != null && wa1.get() != null) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 检查指定的activity是否处于激活状态
     *
     * @param cla Class<? extends BasicActivity>
     * @return true激活状态
     */
    public static boolean isActivityAlive(Class<? extends Activity> cla) {
        return isActivityAliveNumber(cla) > 0;
    }

    /**
     * 根据指定的Class 结束Activity
     *
     * @param cla ? extends Activity
     */
    public static void finishActivity(Class<? extends Activity> cla) {
        final Map<String, WeakReference<Activity>> acs = activitySet;
        if (acs != null && acs.size() > 0) {
            String name = cla.getName();
            String[] names;
            synchronized (acs) {
                names = acs.keySet().toArray(new String[acs.size()]);
            }
            for (String s : names) {
                if (s.startsWith(name)) {
                    WeakReference<Activity> wa1 = acs.get(s);
                    if (wa1 != null && wa1.get() != null) {
                        wa1.get().finish();
                    }
                }
            }
        }
    }

    /**
     * 结束程序接口，该接口内不允许出现任何更新UI的代码
     *
     * @author ldm
     */
    public interface ExitCallback {
        void exit();
    }
}
