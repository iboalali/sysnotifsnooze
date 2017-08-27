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
    Context context;
    private NotificationReceiver notificationReceiver;

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();
        notificationReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.iboalali.sysnotifsnooze.NOTIFICATION_LISTENER_SERVICE");
        registerReceiver(notificationReceiver, filter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("My Notif (onPosted):", sbn.getPackageName());
        Log.d("My Notif (onPosted)2:", sbn.getNotification().extras.getString("android.title"));

        if (sbn.getPackageName().equals("android")) {
            if (sbn.getNotification().extras.getString("android.title").contains("running in the background")) {
                NotificationListener.this.snoozeNotification(sbn.getKey(), 3000);
            }

        }

    }


    class NotificationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            for(StatusBarNotification sbn : NotificationListener.this.getActiveNotifications()){
                Log.d("My Notif (onReceive):", sbn.getPackageName());
                //sbn.getPackageName()
            }
        }
    }



}