package com.iboalali.sysnotifsnooze;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

/**
 * Created by alali on 02-Sep-17.
 */

public class Utils {

    public static boolean hasAccessGranted(Context CONTEXT) {
        ContentResolver contentResolver = CONTEXT.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = CONTEXT.getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }


}
