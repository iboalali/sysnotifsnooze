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


        Log.d(TAG, sbn.getPackageName() + ": " + sbn.getNotification().extras.getString(getString(R.string.notification_intent_key), ""));

        if (sbn.getPackageName().equals("android")) {
            String key = sbn.getNotification().extras.getString(getString(R.string.notification_intent_key));
            if (key == null) return;

            String nc = getString(R.string.notification_content_singular);
            String ncp = getString(R.string.notification_content_plural);

            if (key.contains(nc) || key.contains(ncp)) {
                NotificationListener.this.snoozeNotification(sbn.getKey(), 10000000000000L);
                Log.d(TAG, sbn.getPackageName() + ": " + key + ", snoozed");

            }
            //Long.MAX_VALUE = 9223372036854775807 = 292.5 million years -> not working
            //10000000000000 = 317.09792 years -> working

        }
    }

    class NotificationListenerBroadcastReceiver extends BroadcastReceiver{
    private static final String TAG = "NL Broadcast Receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received Broadcast");


            if(intent.getStringExtra("command").equals("hide")){
                for(StatusBarNotification sbn: NotificationListener.this.getActiveNotifications()){
                    Log.d(TAG, "List: " + sbn.getPackageName() + ": " + sbn.getNotification().extras.getString(getString(R.string.notification_intent_key), ""));

                    String key = sbn.getNotification().extras.getString(getString(R.string.notification_intent_key));
                    if (key == null) return;

                    String nc = getString(R.string.notification_content_singular);
                    String ncp = getString(R.string.notification_content_plural);
                    String ncd = getString(R.string.notification_content_display_over);

                    if (key.contains(nc) || key.contains(ncp) || key.contains(ncd)) {
                        NotificationListener.this.snoozeNotification(sbn.getKey(), 10000000000000L);
                        Log.d(TAG, sbn.getPackageName() + ": " + key + ", snoozed");

                    }
                }
            }
        }
    }




}