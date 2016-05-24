package com.ldm.basic.utils;

import com.ldm.basic.app.BasicApplication;

public class LLog {

    public static String DEF_TAG = "BASIC_FRAME_LOG";

    // *************************v******************************//
    public static void v(String str) {
        v(DEF_TAG, str);
    }

    public static void v(String tag, String str) {
        if (BasicApplication.IS_DEBUG) {
            android.util.Log.v(tag, str);
        }
    }

    // **************************v*****************************//
    public static void d(String str) {
        d(DEF_TAG, str);
    }

    public static void d(String tag, String str) {
        if (BasicApplication.IS_DEBUG) {
            android.util.Log.d(tag, str);
        }
    }

    // **************************i*****************************//
    public static void i(String str) {
        i(DEF_TAG, str);
    }

    public static void i(String tag, String str) {
        if (BasicApplication.IS_DEBUG) {
            android.util.Log.i(tag, str);
        }
    }

    // **************************w*****************************//
    public static void w(String str) {
        w(DEF_TAG, str, null);
    }

    public static void w(String tag, String str) {
        w(tag, str, null);
    }

    public static void w(String tag, String str, Throwable tr) {
        if (BasicApplication.IS_DEBUG) {
            if(tr == null){
                android.util.Log.w(tag, str);
            }else{
                android.util.Log.w(tag, str, tr);
            }
        }
    }

    // ***************************e****************************//
    public static void e(String str) {
        e(DEF_TAG, str);
    }

    public static void e(String tag, String str) {
        if (BasicApplication.IS_DEBUG) {
            android.util.Log.e(tag, str);
        }
    }
}
