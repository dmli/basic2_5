package com.ldm.basic.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by ldm on 12-1-17.
 * NotificationTool
 */
public class NotificationTool {

    private static NotificationManager mNotificationManager = null;
    private static Notification notification = null;

    /**
     * notification 不播放提示音
     *
     * @param context   上下文
     * @param title     标题
     * @param shot      描述语
     * @param appicon   图标
     * @param largeIcon 大图标
     * @param id        id
     * @param intent    Intent
     */
    public static void notification(final Context context, final String title, final String shot, final int appicon, final Bitmap largeIcon, final int id,
                                    final Intent intent) {
        notification(context, title, shot, appicon, largeIcon, null, id, intent);
    }

    /**
     * notification 播放手机默认信息提示音
     *
     * @param context   上下文
     * @param title     标题
     * @param shot      描述语
     * @param appicon   图标
     * @param largeIcon 大图标
     * @param id        id
     * @param intent    Intent
     */
    public static void notificationToDefaultSound(final Context context, final String title, final String shot, final int appicon, final Bitmap largeIcon,
                                                  final int id, final Intent intent) {
        notification(context, title, shot, appicon, largeIcon, "-1", id, intent);
    }

    /**
     * notification
     *
     * @param context   上下文
     * @param title     标题
     * @param shot      描述语
     * @param appicon   图标
     * @param largeIcon 大图标
     * @param soundUri  音效文件路径   null不播放，“-1”播放手机默认提示音，如果是有效的URI则播放URI对应的音频文件
     * @param id        id
     * @param intent    Intent
     */
    public static void notification(final Context context, final String title, final String shot, final int appicon, final Bitmap largeIcon,
                                    final String soundUri, final int id, final Intent intent) {
        mNotificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title).setContentText(shot).setSmallIcon(appicon);
        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon);
        }
        notification = builder.build();
        if (intent != null) {
            notification.contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        if (soundUri != null) {
            if ("-1".equals(soundUri)) {
                notification.defaults |= Notification.DEFAULT_SOUND;
            } else {
                notification.sound = Uri.parse(soundUri);
            }
        }
        mNotificationManager.notify(id, notification);
    }

    public static void closeNotification(int id) {
        if (null != mNotificationManager) {
            mNotificationManager.cancel(id);
        }
    }
}
