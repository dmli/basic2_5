package com.ldm.basic.intent;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by ldm on 11-12-11. intent请求工具，提供了默认的进出动画及自定义动画等
 */
public class IntentUtil {

    /**
     * IntentUtil默认的跳转动画 使用前需要预先设置
     */
    public static int INTENT_DEFAULT_ENTER_ANIM;
    public static int INTENT_DEFAULT_EXIT_ANIM;

    /**
     * 设置返回键触发时的动画效果 使用前需要预先设置
     */
    public static int FINISH_DEFAULT_ENTER_ANIM;
    public static int FINISH_DEFAULT_EXIT_ANIM;

    private static Intent intent;

    /**
     * 自定义动画 使用 DEFAULT_ENTER_ANIM 和 DEFAULT_EXIT_ANIM 作为动画效果
     *
     * @param activity Activity
     * @param classes  目标类
     */
    public static void intentDIY(Activity activity, Class<?> classes) {
        intentDIY(activity, classes, null);
    }

    /**
     * 自定义动画 使用 DEFAULT_ENTER_ANIM 和 DEFAULT_EXIT_ANIM 作为动画效果
     *
     * @param activity Activity
     * @param classes  目标类
     * @param paramMap 传入参数
     */
    public static void intentDIY(Activity activity, Class<?> classes, Map<String, Object> paramMap) {
        intentDIY(activity, classes, paramMap, INTENT_DEFAULT_ENTER_ANIM, INTENT_DEFAULT_EXIT_ANIM);
    }

    /**
     * 自定义动画
     *
     * @param activity  Activity
     * @param classes   目标类
     * @param enterAnim enter资源ID
     * @param exitAnim  exit资源ID
     */
    public static void intentDIY(Activity activity, Class<?> classes, int enterAnim, int exitAnim) {
        intentDIY(activity, classes, null, enterAnim, exitAnim);
    }

    /**
     * 自定义动画
     *
     * @param activity  Activity
     * @param classes   目标类
     * @param paramMap  传入参数
     * @param enterAnim enter资源ID
     * @param exitAnim  exit资源ID
     */
    public static void intentDIY(final Activity activity, final Class<?> classes, final Map<String, Object> paramMap, final int enterAnim, final int exitAnim) {
        intent = new Intent(activity, classes);
        organize(intent, paramMap);
        if (enterAnim != 0 && exitAnim != 0) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
        start(activity);
    }

    /**
     * 关闭Activity时产生的动画
     *
     * @param activity Activity
     */
    public static void finishDIY(final Activity activity) {
        finishDIY(activity, FINISH_DEFAULT_ENTER_ANIM, FINISH_DEFAULT_EXIT_ANIM);
    }

    /**
     * 关闭Activity时产生的动画
     *
     * @param activity  Activity
     * @param enterAnim 进入动画
     * @param exitAnim  退出动画
     */
    public static void finishDIY(final Activity activity, final int enterAnim, final int exitAnim) {
        if (enterAnim != 0 && exitAnim != 0) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
        activity.finish();
    }

    /**
     * 不执行任何动画
     *
     * @param activity Activity
     * @param classes  目标
     * @param paramMap 参数
     */
    public static void intentNotAnimation(final Activity activity, final Class<?> classes, final Map<String, Object> paramMap) {
        intent = new Intent(activity, classes);
        organize(intent, paramMap);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.overridePendingTransition(0, 0);
        start(activity);
    }

    /**
     * 整理参数
     *
     * @param intent Intent
     * @param pm     参数
     */
    private static void organize(final Intent intent, final Map<String, Object> pm) {
        if (pm == null)
            return;
        Set<String> set = pm.keySet();
        for (String key : set) {
            intent.putExtra(key, (Serializable) pm.get(key));
        }
    }

    private static void start(final Activity activity) {
        activity.startActivity(intent);
    }
}
