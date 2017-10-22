package com.iboalali.sysnotifsnooze;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by alali on 27-Aug-17.
 */

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    private NotificationListenerBroadcastReceiver notificationListenerBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationListenerBroadcastReceiver = new NotificationListenerBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.string_filter_intent));
        registerReceiver(notificationListenerBroadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationListenerBroadcastReceiver != null) {
            unregisterReceiver(notificationListenerBroadcastReceiver);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null)
            return;
        checkAndSnoozeNotification(sbn);

    }

    private void checkAndSnoozeNotification(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals("android")) {
            //Log.d(TAG, sbn.getPackageName() + ": " + sbn.getNotification().extras.getString(getString(R.string.notification_intent_key), ""));



            String[] svcs = sbn.getNotification().extras.getStringArray("android.foregroundApps");
            Log.d(TAG, sbn.getNotification().extras.getString(getString(R.string.notification_intent_key), ""));
            if (svcs != null) {
                for (String svc : svcs) {
                    Log.d(TAG, svc + " is running in the background");
                }
            }else{
                Log.d(TAG, "Not the notification you're looking for");
            }

/*
            String key = sbn.getNotification().extras.getString(getString(R.string.notification_intent_key));
            if (key == null) return;

            String nc = getString(R.string.notification_content_singular);
            String ncp = getString(R.string.notification_content_plural);

            if (key.contains(nc) || key.contains(ncp)) {
                NotificationListener.this.snoozeNotification(sbn.getKey(), 10000000000000L);
                Log.d(TAG, sbn.getPackageName() + ": " + key + ", snoozed");

            } else if (Build.MODEL.equals("Pixel 2") || Build.MODEL.equals("Pixel 2 XL")) {
                if (key.contains(getString(R.string.notification_content_plural_pixel2)) || key.contains(getString(R.string.notification_content_singular_pixel2))) {
                    NotificationListener.this.snoozeNotification(sbn.getKey(), 10000000000000L);
                    Log.d(TAG, sbn.getPackageName() + ": " + key + ", snoozed");
                }
            }
            //Long.MAX_VALUE = 9223372036854775807 = 292.5 million years -> not working
            //10000000000000 = 317.09792 years -> working
*/

        }
    }
    class NotificationListenerBroadcastReceiver extends BroadcastReceiver{
    private static final String TAG = "NL Broadcast Receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received Broadcast");

            if(intent.getStringExtra("command").equals("hide")){
                for(StatusBarNotification sbn: NotificationListener.this.getActiveNotifications()){
                    checkAndSnoozeNotification(sbn);

                }
            }


        }
    }

}