package com.iboalali.sysnotifsnooze;

import android.app.Application;
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
import com.iboalali.sysnotifsnooze.util.Inventory;
import com.iboalali.sysnotifsnooze.util.Purchase;

public class MainActivity extends AppCompatActivity {

    public static String SKU_SMALL_TIP_2 = "small_tip_2";
    //public static String SKU_SMALL_TIP_2 = "android.test.purchased";
    public static String SKU_LARGE_TIP_5 = "large_tip_5";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener{
        private static final String KEY_NOTIFICATION_PERMISSION = "notification_permission";
        private static final String KEY_NOTIFICATION = "notification";
        private static final String KEY_SMALL_TIP = "small_tip";
        private static final String KEY_LARGE_TIP = "large_tip";
        //private static final String KEY_RESTORE_PURCHASES = "restore_purchases";
        private static final String TAG = "SettingsFragment";

        IabHelper mHelper;

        Context CONTEXT;

        private Preference notification_permission;
        //private Preference notification;
        private Preference small_tip;
        private Preference large_tip;
        private boolean isNotificationAccessPermissionGranted;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            CONTEXT = getActivity().getApplicationContext();
            addPreferencesFromResource(R.xml.settings);

            String base64EncodedPublicKey = "";

            mHelper = new IabHelper(CONTEXT, base64EncodedPublicKey);
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        Log.d(TAG, "Problem setting up In-app Billing: " + result);
                        return;
                    }
                    // Hooray, IAB is fully set up!
                    mHelper.queryInventoryAsync(queryInventoryFinishedListener);
                }
            });

            notification_permission = findPreference(KEY_NOTIFICATION_PERMISSION);
            small_tip = findPreference(KEY_SMALL_TIP);
            large_tip = findPreference(KEY_LARGE_TIP);

            notification_permission.setOnPreferenceClickListener(this);
            small_tip.setOnPreferenceClickListener(this);
            large_tip.setOnPreferenceClickListener(this);

        }

        @Override
        public void onResume() {
            super.onResume();

            if (!Utils.hasAccessGranted(CONTEXT)) {
                Log.d(TAG, "No Notification Access");

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
                Log.d(TAG, "Has Notification Access");

            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mHelper != null) mHelper.dispose();
            mHelper = null;
        }

        IabHelper.QueryInventoryFinishedListener queryInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                if (mHelper == null) return;

                if (result.isFailure()) {
                    Log.d(TAG, "Failed to query inventory: " + result);
                    return;
                }

                Log.d(TAG, "Query inventory was successful.");

                // check for un-consumed purchases, and consume them
                Purchase small_tip_2_purchase = inv.getPurchase(SKU_SMALL_TIP_2);
                if (small_tip_2_purchase != null){
                    mHelper.consumeAsync(small_tip_2_purchase, onConsumeFinishedListener);
                }
                Purchase large_tip_5_purchase = inv.getPurchase(SKU_LARGE_TIP_5);
                if (large_tip_5_purchase != null){
                    mHelper.consumeAsync(large_tip_5_purchase, onConsumeFinishedListener);
                }
            }
        };

        IabHelper.OnIabPurchaseFinishedListener onIabPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase info) {
                if (result.isFailure()) {
                    Log.d(TAG, "Error purchasing: " + result);

                }else if (info.getSku().equals(SKU_SMALL_TIP_2) || info.getSku().equals(SKU_LARGE_TIP_5)){
                    // consume purchase
                    Log.d(TAG, "item purchased: " + result);
                    mHelper.consumeAsync(info, onConsumeFinishedListener);
                }
            }
        };

        IabHelper.OnConsumeFinishedListener onConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                if (result.isSuccess()) {
                    // provision the in-app purchase to the user
                    Log.d(TAG, "item consumed: " + result);
                }
                else {
                    // handle error
                    Log.d(TAG, "Error consuming: " + result);
                }
            }
        };

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
            if (mHelper == null) return;

            // Pass on the activity result to the helper for handling
            if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                // not handled, so handle it ourselves (here's where you'd
                // perform any handling of activity results not related to in-app
                // billing...
                super.onActivityResult(requestCode, resultCode, data);
            }
            else {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
            }
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
                    if (mHelper != null) mHelper.flagEndAsync();
                    mHelper.launchPurchaseFlow(getActivity(), MainActivity.SKU_SMALL_TIP_2, 1001, onIabPurchaseFinishedListener, "");
                    break;

                case KEY_LARGE_TIP:
                    // IAP for a small tip around 5 €/$
                    if (mHelper != null) mHelper.flagEndAsync();
                    mHelper.launchPurchaseFlow(getActivity(), MainActivity.SKU_LARGE_TIP_5, 1001, onIabPurchaseFinishedListener, "");
                    break;
/*
                case KEY_RESTORE_PURCHASES:
                    mHelper.queryInventoryAsync(queryInventoryFinishedListener);
                    break;
                    */
            }
            return false;
        }

        @Override
        public void onStart() {
            super.onStart();

            isNotificationAccessPermissionGranted = Utils.hasAccessGranted(CONTEXT);
            notification_permission.setSummary(isNotificationAccessPermissionGranted ? getString(R.string.granted) : getString(R.string.not_granted));
        }
    }

}