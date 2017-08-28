package com.iboalali.sysnotifsnooze;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by alali on 27-Aug-17.
 */

public class NotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null)
            return;


        Log.d("My Notif (onPosted):", sbn.getPackageName());

        if (sbn.getPackageName().equals("android")) {
            if (sbn.getNotification().extras.getString("android.title").contains("running in the background")) {
                NotificationListener.this.snoozeNotification(sbn.getKey(), 10000000000000L);
                Log.d("My Notif (onPosted):", sbn.getPackageName() + " snoozed");

            }
            //Long.MAX_VALUE = 9223372036854775807 = 292.5 million years
            //10000000000000 = 317.09792 years

        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }
}