package com.iboalali.sysnotifsnooze;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

/**
 * Created by alali on 02-Sep-17.
 */

public class Utils {

    private static final String TAG = "Utils";

    public static boolean hasAccessGranted(Context CONTEXT) {
        ContentResolver contentResolver = CONTEXT.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = CONTEXT.getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    public static String getAppName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;

        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }

        return ai != null ? (String) pm.getApplicationLabel(ai) : null;
    }

    public static String getAppVersionName(Context context) {
        PackageManager manager = context.getPackageManager();

        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static int getAppVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();

        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }

}
