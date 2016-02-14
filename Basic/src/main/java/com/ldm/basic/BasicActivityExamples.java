package com.ldm.basic;

import android.os.Bundle;
import android.view.View;

import com.ldm.basic.utils.Log;

/**
 * Created by ldm on 15/12/3.
 * 演示 Thread及handler的使用
 */
public class BasicActivityExamples extends BasicActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this));

        /**
         * 使用异步任务需要调用setAsynchronous(a)方法加入一个任务
         */
        setAsynchronous(a);


        /**
         * 开始执行第一个任务
         */
        startAsyncTask(1, null);

        /**
         * 开始执行第二个任务
         */
        startAsyncTask(2, null);


    }


    @Override
    protected void handleMessage(int tag, Object obj) {
        if (tag == 1) {
            /**
             * Asynchronous的async方法执行完成时会执行这里
             */
            Log.e("第一个任务开始完成 = " + String.valueOf(obj));

        } else if (tag == 2) {
            Log.e("第二个任务开始完成 = " + String.valueOf(obj));
        } else if (tag == 3) {
            Log.e("这是由securityHandler发送出的一个任务");
        }
    }

    /**
     * 创建一个任务，使用setAsynchronous(a)方法加入到任务列表中
     */
    private Asynchronous a = new Asynchronous() {
        @Override
        public Object async(int tag, Object obj) {
            if (tag == 1) {
                /**
                 * 这是第一个异步任务
                 */
                Log.e("第一个任务开始执行");
                /**
                 * 如果需要将任务返回到
                 */
                return "ok";
            } else if (tag == 2) {
                /**
                 * 这是第二个异步任务
                 */
                Log.e("第二个任务开始执行");
            }
            return null;
        }
    };
}
