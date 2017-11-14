package com.iboalali.sysnotifsnooze;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by alali on 27-Aug-17.
 */

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    private NotificationListenerBroadcastReceiver notificationListenerBroadcastReceiver;
    SharedPreferences sharedPreferencesPackageNames;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    long snoozeDurationMs = 5000L;

    /**
     * This is set on the notification shown by the activity manager about all apps
     * running in the background.  It indicates that the notification should be shown
     * only if any of the given apps do not already have a {@link Notification#FLAG_FOREGROUND_SERVICE}
     * notification currently visible to the user.  This is a string array of all
     * package names of the apps.
     */
    public static final String EXTRA_FOREGROUND_APPS = "android.foregroundApps";

    @Override
    public void onCreate() {
        super.onCreate();
        notificationListenerBroadcastReceiver = new NotificationListenerBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.string_filter_intent));
        registerReceiver(notificationListenerBroadcastReceiver, filter);
        sharedPreferencesPackageNames = getSharedPreferences("myPackageNames", MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("mySettingsPref", MODE_PRIVATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationListenerBroadcastReceiver != null) {
            unregisterReceiver(notificationListenerBroadcastReceiver);
        }
    }

    private void snoozeSystemNotification(StatusBarNotification sbn){
        Log.d(TAG, "in snoozeNotification");
        if (sbn.getPackageName().equals("android") && sbn.getNotification().extras.containsKey(EXTRA_FOREGROUND_APPS)) {
            Log.d(TAG, "found the notification");

            String key = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
            if (key == null) return;

            String[] svcs = sbn.getNotification().extras.getStringArray(EXTRA_FOREGROUND_APPS);

            boolean areAllSelected = false;

            Set<String> selected = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_selected), null);
            if (selected != null && svcs != null){
                if (selected.contains(getString(R.string.string_all_key))){
                    areAllSelected = true;
                }else {
                    for (String s : svcs) {
                        if (selected.contains(s)) {
                            areAllSelected = true;
                        } else {
                            areAllSelected = false;
                            break;
                        }
                    }
                }

                if (areAllSelected){
                    // TODO: test for the best combination of duration and battery life
                    snoozeNotification(sbn.getKey(), snoozeDurationMs);
                    Log.d(TAG, sbn.getPackageName() + ": " + key + ", snoozed for " + snoozeDurationMs);
                }
            }
        }
    }

    private void checkForSystemNotification(StatusBarNotification sbn){
        Log.d(TAG, "in checkForSystemNotification");

        if (sbn.getPackageName().equals("android") && sbn.getNotification().extras.containsKey(EXTRA_FOREGROUND_APPS)) {
            Log.d(TAG, "found the notification");

            String key = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
            if (key == null) return;

            String[] svcs = sbn.getNotification().extras.getStringArray(EXTRA_FOREGROUND_APPS);

            // checking for null, just to avoid a potential null exception
            if (svcs != null){
                // if key exist, add new package names to the list and put in shared preferences
                Set<String> pns = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_all), null);
                if (pns != null){
                    List<String> newList = new ArrayList<>(pns);
                    for (String s : svcs) {
                        Log.d(TAG, "EXTRA_FOREGROUND_APPS: " + s);
                        if(!pns.contains(s)){
                            Log.d(TAG, s + " is not on the list. Added it");
                            newList.add(s);
                        }
                    }

                    editor = sharedPreferencesPackageNames.edit();
                    editor.putStringSet(getString(R.string.shared_pref_key_package_name_all), new HashSet<>(newList));
                    editor.putStringSet(getString(R.string.shared_pref_key_package_name_current), new HashSet<String>(Arrays.asList(svcs)));
                    editor.apply();

                }else{ // if key doesn't exist, just put the whole list in shared preferences

                    Log.d(TAG, "Add all EXTRA_FOREGROUND_APPS to the list");
                    editor = sharedPreferencesPackageNames.edit();
                    editor.putStringSet(getString(R.string.shared_pref_key_package_name_all), new HashSet<>(Arrays.asList(svcs)));
                    editor.putStringSet(getString(R.string.shared_pref_key_package_name_current), new HashSet<String>(Arrays.asList(svcs)));
                    editor.apply();
                }
            }
        }
    }

    private void snoozeSystemNotificationTheOldWay(StatusBarNotification sbn){
        if (sbn.getPackageName().equals("android") && sbn.getNotification().extras.containsKey(EXTRA_FOREGROUND_APPS)) {
            String key = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
            if (key == null) return;

            snoozeNotification(sbn.getKey(), 10000000000000L);
            //Long.MAX_VALUE = 9223372036854775807 = 292.5 million years -> not working
            //10000000000000 = 317.09792 years -> working

            Log.d(TAG, sbn.getPackageName() + ": " + key + ", snoozed the old way");
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null)
            return;

        if (!sharedPreferencesPackageNames.contains(getString(R.string.shared_pref_key_package_name_selected))){
            SharedPreferences.Editor editor = sharedPreferencesPackageNames.edit();
            List<String> list = new ArrayList<>();
            list.add(getString(R.string.string_all_key));
            editor.putStringSet(getString(R.string.shared_pref_key_package_name_selected), new HashSet<String>(list));
            editor.apply();
        }

        if(sharedPreferences.getBoolean(getString(R.string.shared_pref_key_isOldWay), false )) {
            snoozeSystemNotificationTheOldWay(sbn);
        }else{
            checkForSystemNotification(sbn);
            snoozeSystemNotification(sbn);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn == null)
            return;

        checkForSystemNotification(sbn);
        //snoozeSystemNotification(sbn);
    }

    class NotificationListenerBroadcastReceiver extends BroadcastReceiver{
        private static final String TAG = "NL Broadcast Receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received Broadcast");

            if (intent.getStringExtra("command").equals("hide")) {
                for (StatusBarNotification sbn : NotificationListener.this.getActiveNotifications()) {
                    if(sharedPreferences.getBoolean(getString(R.string.shared_pref_key_isOldWay), false )) {
                        snoozeSystemNotificationTheOldWay(sbn);
                    }else{
                        checkForSystemNotification(sbn);
                        snoozeSystemNotification(sbn);
                    }
                }
            }
            // Debug code, not used in production
            else if(intent.getStringExtra("command").equals("extra_hide")){
                for (StatusBarNotification sbn : NotificationListener.this.getActiveNotifications()) {
                    //checkAndSnoozeNotification(sbn);
                    if (sbn.getPackageName().equals("android") && sbn.getNotification().extras.containsKey(EXTRA_FOREGROUND_APPS)) {
                        NotificationListener.this.snoozeNotification(sbn.getKey(), snoozeDurationMs);

                        String key = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
                        Log.d(TAG, sbn.getPackageName() + ": snoozed");
                    }
                }
            }
            // Debug code, not used in production
            else if (intent.getStringExtra("command").equals("extra_show")){
                for (StatusBarNotification sbn : NotificationListener.this.getSnoozedNotifications()) {
                    if (sbn.getPackageName().equals("android") && sbn.getNotification().extras.containsKey(EXTRA_FOREGROUND_APPS)) {
                        NotificationListener.this.snoozeNotification(sbn.getKey(), -100000000000000L);
                        NotificationListener.this.cancelNotification(sbn.getKey());

                        String key = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
                        Log.d(TAG, sbn.getPackageName() + ": un-snoozed");
                    }
                }
            }



        }
    }
}


