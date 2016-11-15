package com.ldm.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.ldm.basic.utils.SystemTool;
import com.ldm.basic.utils.ThreadPool;

/**
 * Created by ldm on 14-1-3. Service在SystemTool初始化信息时被初始化，提供了一些基础的常用方法
 * <p/>
 */
public class BasicService extends Service {

    /**
     * service实例
     */
    private static BasicService service;

    /**
     * 返回一个静态的BasicService, service有时会为null，可以通过activity注册该service然后在通过该
     *
     * @return BasicService
     */
    public static BasicService getInstance() {
        return service;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        ThreadPool.shutdownNow();
        super.onDestroy();
    }

    /**
     * 创建一个异步任务，可以在Activity关闭后仍保证正常运行, 如果tag已存在任务列表中，本次创建将无效
     * createAsyncTask创建任务时如果BasicService没有启动
     * ，将会自动调用SystemTool.startBasicService(activity)进行启动服务，并延时三秒处理任务
     *
     * @param tag  唯一标识
     * @param task 任务
     */
    public static void createAsyncTask(final Context context, final String tag, final ThreadPool.AsyncTask task) {
        if (BasicService.getInstance() == null) {
            if (context != null) {
                // 使用SystemTool启动服务
                SystemTool.startBasicService(context);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ThreadPool.addTask(tag, task);
                    }
                }, 3000);
            }
        } else {
            ThreadPool.addTask(tag, task);
        }
    }
}
