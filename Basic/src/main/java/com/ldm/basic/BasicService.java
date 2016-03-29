package com.ldm.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.ldm.basic.conn.BasicHttpGet;
import com.ldm.basic.conn.BasicHttpPost;
import com.ldm.basic.utils.SystemTool;
import com.ldm.basic.utils.TaskThreadToMultiService;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldm on 14-1-3. Service在SystemTool初始化信息时被初始化，提供了一些基础的常用方法
 * <p/>
 * 1/CMD_SEND_CONTENT 向指定服务器发送一个post或get求情，但没有返回值
 */
public class BasicService extends Service {

    /**
     * 异步任务的唯一标识将会被存储到该队列中
     */
    public static final Map<String, AsyncTaskCallback> ASYNC_TASK_QUEUE = new HashMap<>();

    /**
     * 异步任务
     */
    private static TaskThreadToMultiService taskThreadToMultiService;

    /**
     * service实例
     */
    private static WeakReference<BasicService> service;

    /**
     * 任务进入
     */
    public static final int RESULT_CHILD_ENTER = 101;

    /**
     * 任务退出
     */
    public static final int RESULT_CHILD_EXIT = 102;

    /**
     * 返回一个静态的BasicService, service有时会为null，可以通过activity注册该service然后在通过该
     *
     * @return BasicService
     */
    public static BasicService getInstance() {
        if (service == null) {
            return null;
        }
        return service.get();
    }

    @Override
    public void onCreate() {
        // 静态化Service
        service = new WeakReference<BasicService>(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        ASYNC_TASK_QUEUE.clear();
        super.onDestroy();
    }

    /**
     * 向指定服务器发送日志
     *
     * @param url     URL
     * @param type    “0”Post "1"GET
     * @param content 内容
     */
    public void sendContent(String url, String type, String content) {
        if ("0".equals(type)) {
            new BasicHttpPost(url).execute(content);
        } else {
            new BasicHttpGet(url).execute(content);
        }
    }

    /**
     * 创建一个异步任务，可以在Activity关闭后仍保证正常运行, 如果tag已存在任务列表中，本次创建将无效
     * createAsyncTask创建任务时如果BasicService没有启动
     * ，将会自动调用SystemTool.startBasicService(activity)进行启动服务，并延时三秒处理任务
     *
     * @param tag  唯一标识
     * @param task 任务
     * @return true任务生效
     */
    public static boolean createAsyncTask(final Context context, final String tag, final AsyncTaskCallback task) {
        boolean result = false;
        if (!ASYNC_TASK_QUEUE.containsKey(tag) && task != null) {
            task._tag = tag;
            ASYNC_TASK_QUEUE.put(tag, task);
            if (BasicService.getInstance() == null) {
                if (context != null) {
                    // 使用SystemTool启动服务
                    SystemTool.startBasicService(context);
                    handler.sendMessageDelayed(handler.obtainMessage(300, tag), 3000);// 延时三秒创建任务
                }
            } else {
                // 创建任务
                handler.sendMessage(handler.obtainMessage(300, tag));
            }
            result = true;
        }
        return result;
    }


    /**
     * 创建一个异步任务，可以在Activity关闭后仍保证正常运行, 如果tag已存在任务列表中，本次创建将无效
     * createAsyncTask创建任务时如果BasicService没有启动
     * ，将会自动调用SystemTool.startBasicService(activity)进行启动服务，并延时三秒处理任务
     *
     * @param tag         唯一标识
     * @param delayedTime 延时时间
     * @param task        任务
     * @return true任务生效
     */
    public static boolean createAsyncDelayedTask(final Context context, final String tag, int delayedTime, final AsyncTaskCallback task) {
        boolean result = false;
        if (!ASYNC_TASK_QUEUE.containsKey(tag) && task != null) {
            task._tag = tag;
            ASYNC_TASK_QUEUE.put(tag, task);
            if (BasicService.getInstance() == null) {
                if (context != null) {
                    // 使用SystemTool启动服务
                    SystemTool.startBasicService(context);
                    handler.sendMessageDelayed(handler.obtainMessage(300, tag), delayedTime + 3000);// 延时三秒创建任务
                }
            } else {
                // 创建任务
                handler.sendMessageDelayed(handler.obtainMessage(300, tag), delayedTime);
            }
            result = true;
        }
        return result;
    }

    /**
     * 创建一个延时任务，在service主线程中调用
     *
     * @param task        AsyncTaskCallback
     * @param delayedTime 延时时间
     */
    public static void createAsyncDelayedTask(Context context, AsyncTaskCallback task, int delayedTime) {
        if (BasicService.getInstance() == null) {
            if (context != null) {
                // 使用SystemTool启动服务
                SystemTool.startBasicService(context);
                handler.sendMessageDelayed(handler.obtainMessage(301, task), delayedTime + 3000);// 延时三秒创建任务
            }
        } else {
            handler.sendMessageDelayed(handler.obtainMessage(301, task), delayedTime);// 延时创建任务
        }
    }

    /**
     * 这个方法将使用TaskThreadToMultiService作为线程池进行处理，最大线程数量为20，且任务均为异步操作，没有主线程
     * 如果这个满足不了可以使用createAsyncTask方法创建任务，createAsyncTask将不受数量限制且不需要在任务队列中等待
     *
     * @param tag  唯一标识
     * @param task 任务
     * @return true任务生效
     */
    public static boolean createAsyncTaskToThreadPool(final Context context, final String tag, final AsyncTaskCallback task) {
        boolean result = false;
        if (taskThreadToMultiService == null) {
            taskThreadToMultiService = new TaskThreadToMultiService(10);
            taskThreadToMultiService.setMaxRetainFreeThreadNumber(0);//设置最大空闲线程保留数量为0
        }
        if (!ASYNC_TASK_QUEUE.containsKey(tag) && task != null) {
            task._tag = tag;
            ASYNC_TASK_QUEUE.put(tag, task);
            if (BasicService.getInstance() == null) {
                if (context != null) {
                    // 使用SystemTool启动服务
                    SystemTool.startBasicService(context);
                    handler.sendMessageDelayed(handler.obtainMessage(30, tag), 3000);// 延时三秒创建任务
                }
            } else {
                // 创建任务
                handler.sendMessage(handler.obtainMessage(30, tag));
            }
            result = true;
        }
        return result;
    }

    /**
     * 处理异步任务的各种回调方法
     */
    private static Handler handler = new BasicSecurityHandler();

    private static class BasicSecurityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESULT_CHILD_ENTER: {
                    if (msg.obj != null && ASYNC_TASK_QUEUE.containsKey(msg.obj.toString())) {
                        ASYNC_TASK_QUEUE.get(msg.obj.toString()).enter(BasicService.getInstance());
                    }
                }
                break;
                case RESULT_CHILD_EXIT: {
                    if (msg.obj != null && ASYNC_TASK_QUEUE.containsKey(msg.obj.toString())) {
                        synchronized (ASYNC_TASK_QUEUE) {
                            ASYNC_TASK_QUEUE.remove(msg.obj.toString()).exit(BasicService.getInstance(), msg.arg1);
                        }
                    }
                }
                break;
                case 300: {
                    createAsyncThreadTask(msg);
                }
                break;
                case 301: {
                    AsyncTaskCallback task = (AsyncTaskCallback) msg.obj;
                    if (task != null) {
                        task.asynchronous(getInstance());
                    }
                }
                break;
                case 30: {// 这个是线程池的任务创建模块
                    createAsyncTaskToThreadPool(msg);
                    break;
                }
                default:
                    break;
            }
        }

        /**
         * 创建一个基于线程池的任务
         *
         * @param msg Message
         */
        private void createAsyncTaskToThreadPool(Message msg) {
            taskThreadToMultiService.addTask(new TaskThreadToMultiService.Task(msg.obj) {
                @Override
                public void taskStart(Object... obj) {
                    handler.sendMessage(handler.obtainMessage(RESULT_CHILD_ENTER, obj[0]));
                    AsyncTaskCallback _task = ASYNC_TASK_QUEUE.get(String.valueOf(obj[0]));
                    if (_task != null) {
                        handler.sendMessage(handler.obtainMessage(RESULT_CHILD_EXIT, _task.asynchronous(BasicService.getInstance()), -1, obj[0]));
                    }
                }
            });
        }

        /**
         * 创建一个异步线程任务
         *
         * @param msg Message
         */
        private void createAsyncThreadTask(Message msg) {
            new AsyncTaskThread(String.valueOf(msg.obj)) {
                @Override
                public void run() {
                    handler.sendMessage(handler.obtainMessage(RESULT_CHILD_ENTER, _tag));
                    AsyncTaskCallback _task = ASYNC_TASK_QUEUE.get(_tag);
                    if (_task != null) {
                        handler.sendMessage(handler.obtainMessage(RESULT_CHILD_EXIT, _task.asynchronous(BasicService.getInstance()), -1, _tag));
                    }
                }
            }.start();
        }
    }

    /**
     * 异步任务
     *
     * @return 异步任务数量
     */
    public static int getAsyncTaskNumber() {
        return ASYNC_TASK_QUEUE.size();
    }

    /**
     * 异步任务回调接口
     *
     * @author ldm
     */
    public static abstract class AsyncTaskCallback {
        public String _tag;
        public Object[] _obj;

        /**
         * 创建时需要指定一个唯一标识
         *
         * @param obj 用户自定义参数，该参数可以在AsyncTaskCallback中的obj中得到
         */
        public AsyncTaskCallback(Object... obj) {
            this._obj = obj;
        }

        /**
         * 当该任务开始处理时调用(主线程中被调用)
         */
        public void enter(Context context) {
        }

        /**
         * 当该任务处理完成后被调用(主线程中被调用)
         *
         * @param context Context
         * @param state   用户接口来自asynchronous(Context)的返回信息
         */
        public void exit(Context context, int state) {
        }

        /**
         * 该方法为异步操作
         *
         * @param context Context
         * @return 任意状态 将在exit方法中接收
         */
        public int asynchronous(Context context) {
            return -1;
        }

    }

    /**
     * 异步任务线程
     */
    private abstract static class AsyncTaskThread extends Thread {

        public String _tag;

        private AsyncTaskThread(String tag) {
            this._tag = tag;
        }
    }
}
