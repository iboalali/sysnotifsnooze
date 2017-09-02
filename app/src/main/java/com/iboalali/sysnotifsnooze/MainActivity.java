package com.iboalali.sysnotifsnooze;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.iboalali.sysnotifsnooze.util.IabHelper;
import com.iboalali.sysnotifsnooze.util.IabResult;

public class MainActivity extends AppCompatActivity {

    public static Context CONTEXT;
    IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CONTEXT = this;

        String base64EncodedPublicKey = ""; // TODO: add public licence key before publishing

        mHelper = new IabHelper(CONTEXT, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d("MyActivity", "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Utils.hasAccessGranted()) {
            Log.d("MyActivity:", "No Notification Access");

            AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT.getApplicationContext());
            builder.setMessage(R.string.permission_request_msg); //TODO: change this text
            builder.setTitle(R.string.permission_request_title); // TODO: change this title, maybe?
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.Allow, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                }
            });

            AlertDialog alertDialog = builder.create();
            //alertDialog.show(); //TODO: solve this problem

        }else{
            Log.d("MyActivity:", "Has Notification Access");

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener{
        private static final String KEY_NOTIFICATION_PERMISSION = "notification_permission";
        private static final String KEY_NOTIFICATION = "notification";
        private static final String KEY_SMALL_TIP = "small_tip";
        private static final String KEY_LARGE_TIP = "large_tip";

        private Preference notification_permission;
        //private Preference notification;
        private Preference small_tip;
        private Preference large_tip;
        private boolean isNotificationAccessPermissionGranted;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);

            notification_permission = findPreference(KEY_NOTIFICATION_PERMISSION);
            //notification = findPreference(KEY_NOTIFICATION);
            small_tip = findPreference(KEY_SMALL_TIP);
            large_tip = findPreference(KEY_LARGE_TIP);

            notification_permission.setOnPreferenceClickListener(this);
            small_tip.setOnPreferenceClickListener(this);
            large_tip.setOnPreferenceClickListener(this);

        }


        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()){
                case KEY_NOTIFICATION_PERMISSION:
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    /*
                    if (isNotificationAccessPermissionGranted){
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT.getApplicationContext());
                        builder.setMessage(R.string.permission_request_msg); //TODO: change this text
                        builder.setTitle(R.string.permission_request_title); // TODO: change this title, maybe?
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.Allow, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                    */
                    break;

                case KEY_SMALL_TIP:
                    // IAP for a small tip around 2 €/$
                    break;

                case KEY_LARGE_TIP:
                    // IAP for a small tip around 5 €/$
                    break;
            }
            return false;
        }

        @Override
        public void onStart() {
            super.onStart();

            isNotificationAccessPermissionGranted = Utils.hasAccessGranted();
            notification_permission.setSummary(isNotificationAccessPermissionGranted ? getString(R.string.granted) : getString(R.string.not_granted));
/*
            if (isNotificationAccessPermissionGranted){
                NotificationListener nl = new NotificationListener();
                for (StatusBarNotification sbn: nl.getSnoozedNotifications()){
                    if (sbn.getPackageName().equals("android")){
                        if (sbn.getNotification().extras.getString(getString(R.string.notification_intent_key)).contains(getString(R.string.notification_content))){
                            notification.setSummary(R.string.hidden_success);
                        }
                    }

                }
            }else{

            }

            */
        }
    }

}