package com.iboalali.sysnotifsnooze;

import android.app.Notification;
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

    /**
     * This is set on the notification shown by the activity manager about all apps
     * running in the background.  It indicates that the notification should be shown
     * only if any of the given apps do not already have a {@link Notification#FLAG_FOREGROUND_SERVICE}
     * notification currently visible to the user.  This is a string array of all
     * package names of the apps.
     * @hide
     */
    public static final String EXTRA_FOREGROUND_APPS = "android.foregroundApps";

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

    private void checkAndSnoozeNotification(StatusBarNotification sbn)
    {
        if (sbn.getPackageName().equals("android") && sbn.getNotification().extras.containsKey(EXTRA_FOREGROUND_APPS)) {
            String key = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
            if (key == null) return;

            snoozeNotification(sbn.getKey(), 10000000000000L);
            //Long.MAX_VALUE = 9223372036854775807 = 292.5 million years -> not working
            //10000000000000 = 317.09792 years -> working

            Log.d(TAG, sbn.getPackageName() + ": " + key + ", snoozed");
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null)
            return;

        checkAndSnoozeNotification(sbn);
    }

    class NotificationListenerBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "NL Broadcast Receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received Broadcast");

            if (intent.getStringExtra("command").equals("hide")) {
                for (StatusBarNotification sbn : NotificationListener.this.getActiveNotifications()) {
                    checkAndSnoozeNotification(sbn);
                }
            }
        }
    }
}
