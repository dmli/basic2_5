package com.ldm.basic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.ldm.basic.utils.BasicSecurityHandler;
import com.ldm.basic.utils.BasicSecurityHandler.SecurityHandlerInterface;
import com.ldm.basic.utils.Log;
import com.ldm.basic.utils.SystemTool;

/**
 * Created by ldm on 15/11/11.
 * BasicSecurityHandler演示类
 */
public class BasicSecurityHandlerExamples extends View implements SecurityHandlerInterface {


    public BasicSecurityHandlerExamples(Context context) {
        super(context);
    }

    public BasicSecurityHandlerExamples(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 这个方法是实现SecurityHandlerInterface的，用来做securityHandler的任务处理
     * 例子：
     * securityHandler.sendEmptyMessage(10， "abc")
     *
     * @param what Message的what
     * @param obj  Message的obj
     */
    @Override
    public void handleMessage(int what, Object obj) {
        switch (what) {
            case 10:
                Log.e("data = " + String.valueOf(obj));
                break;
            default:
                break;
        }
    }

    /**
     * 创建一个BasicSecurityHandler实体
     */
    private BasicSecurityHandler securityHandler = new BasicSecurityHandler<>(this);
}
