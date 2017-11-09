package com.iboalali.sysnotifsnooze;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.iboalali.sysnotifsnooze.util.IabHelper;
import com.iboalali.sysnotifsnooze.util.IabResult;
import com.iboalali.sysnotifsnooze.util.Inventory;
import com.iboalali.sysnotifsnooze.util.Purchase;
import com.iboalali.sysnotifsnooze.util.SkuDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static String SKU_SMALL_TIP_2 = "small_tip_2";
    public static String SKU_LARGE_TIP_5 = "large_tip_5";
    private static final String TAG = "MainActivity";

    private SharedPreferences sharedPreferencesPackageNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView_app_name);
        String title = textView.getText().toString();

        if (Build.VERSION.SDK_INT >= 27 || Build.MODEL.equals("Pixel 2") || Build.MODEL.equals("Pixel 2 XL")){
            title = title.replace("%s", getString(R.string.string_app_name_replace_using_battery));
        }else{
            title = title.replace("%s", getString(R.string.string_app_name_replace_running_in_the_background));
        }

        textView.setText(title);

        sharedPreferencesPackageNames = getSharedPreferences("myPackageNames", MODE_PRIVATE);
        if(!sharedPreferencesPackageNames.contains(getString(R.string.shared_pref_key_package_name_selected))){
            SharedPreferences.Editor editor = sharedPreferencesPackageNames.edit();
            List<String> list = new ArrayList<>();
            list.add(getString(R.string.string_all_key));
            editor.putStringSet(getString(R.string.shared_pref_key_package_name_selected), new HashSet<String>(list));
            editor.apply();
        }

    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener{
        private static final String KEY_NOTIFICATION_PERMISSION = "notification_permission";
        private static final String KEY_BACKGROUND_APPS = "background_app";
        private static final String KEY_SETTINGS_HIDE_ICON = "hide_icon";
        private static final String KEY_SMALL_TIP = "small_tip";
        private static final String KEY_LARGE_TIP = "large_tip";
        private static final String TAG = "SettingsFragment";

        private boolean isSwitchSet;

        IabHelper mHelper;

        private Preference notification_permission;
        private SwitchPreference settings_hide_icon;
        private Preference background_app;
        private Preference small_tip;
        private Preference large_tip;
        private boolean isNotificationAccessPermissionGranted;

        private SharedPreferences sharedPreferencesPackageNames;
        private SharedPreferences sharedPreferences;


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            // setup shared preferences
            sharedPreferences = getActivity().getSharedPreferences("mySettingsPref", MODE_PRIVATE);
            sharedPreferencesPackageNames = getActivity().getSharedPreferences("myPackageNames", MODE_PRIVATE);

            sharedPreferencesPackageNames.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

            // license key
            String base64EncodedPublicKey = getContext().getString(R.string.public_license_key);

            // setup In-app billing
            mHelper = new IabHelper(getActivity().getApplicationContext(), base64EncodedPublicKey);
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        Log.d(TAG, "Problem setting up In-app Billing: " + result);
                        return;
                    }
                    // Hooray, IAB is fully set up!
                    ArrayList<String> skus = new ArrayList<String>();
                    skus.add(MainActivity.SKU_SMALL_TIP_2);
                    skus.add(MainActivity.SKU_LARGE_TIP_5);

                    mHelper.queryInventoryAsync(true, skus, queryInventoryFinishedListener);

                }
            });

            // find Preferences
            settings_hide_icon = (SwitchPreference)findPreference(KEY_SETTINGS_HIDE_ICON);
            notification_permission = findPreference(KEY_NOTIFICATION_PERMISSION);
            background_app = findPreference(KEY_BACKGROUND_APPS);

            small_tip = findPreference(KEY_SMALL_TIP);
            large_tip = findPreference(KEY_LARGE_TIP);

            // setup click listener
            notification_permission.setOnPreferenceClickListener(this);
            settings_hide_icon.setOnPreferenceClickListener(this);
            background_app.setOnPreferenceClickListener(this);
            small_tip.setOnPreferenceClickListener(this);
            large_tip.setOnPreferenceClickListener(this);

            settings_hide_icon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    Log.d(TAG, "in onPreferenceChange");

                    if (!settings_hide_icon.isChecked()){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(R.string.string_hide_icon_alert_dialog_msg);
                        builder.setTitle(R.string.string_hide_icon_alert_dialog_title);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.string_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "set switch to TRUE");

                                PackageManager pkg = getContext().getPackageManager();
                                pkg.setComponentEnabledSetting(new ComponentName(getContext(), MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                        PackageManager.DONT_KILL_APP);

                                settings_hide_icon.setChecked(true);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(getContext().getString(R.string.string_sharedPreferences_isIconHidden), true);
                                editor.apply();

                                Log.d(TAG, "switch is: " + String.valueOf(settings_hide_icon.isChecked()));
                            }
                        });

                        builder.setNegativeButton(getContext().getString(R.string.string_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                settings_hide_icon.setChecked(false);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(getContext().getString(R.string.string_sharedPreferences_isIconHidden), true);
                                editor.apply();
                            }
                        });

                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                Log.d(TAG, "set switch to FALSE (1)");

                                PackageManager pkg = getContext().getPackageManager();
                                pkg.setComponentEnabledSetting(new ComponentName(getContext(), MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                        PackageManager.DONT_KILL_APP);

                                settings_hide_icon.setChecked(false);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(getContext().getString(R.string.string_sharedPreferences_isIconHidden), false);
                                editor.apply();
                                Log.d(TAG, "switch is: " + String.valueOf(settings_hide_icon.isChecked()));
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    }else{
                        Log.d(TAG, "set switch to FALSE (2)");

                        PackageManager pkg = getContext().getPackageManager();
                        pkg.setComponentEnabledSetting(new ComponentName(getContext(), MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);

                        settings_hide_icon.setChecked(false);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getContext().getString(R.string.string_sharedPreferences_isIconHidden), false);
                        editor.apply();
                        Log.d(TAG, "switch is: " + String.valueOf(settings_hide_icon.isChecked()));
                    }


                    return true;
                }
            });

        } // end of onCreate

        @Override
        public void onResume() {
            super.onResume();

            if (!Utils.hasAccessGranted(getContext())) {
                Log.d(TAG, "No Notification Access");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getContext().getString(R.string.string_sharedPref_granted), false);
                editor.apply();

                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                builder.setMessage(R.string.permission_request_msg);
                builder.setTitle(R.string.permission_request_title);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }else{
                Log.d(TAG, "Has Notification Access");

                StringBuilder stringBuilder = new StringBuilder();
                Set<String> l = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_current), null);
                if (l != null) {
                    for (String str : l) {
                        stringBuilder.append("- ").append(Utils.getAppName(getContext(), str)).append("\n");
                    }

                    stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());

                    if (background_app != null) {
                        background_app.setSummary(stringBuilder.toString());
                    }
                }

                if (!sharedPreferences.getBoolean(getContext().getString(R.string.string_sharedPref_granted), false)) {
                    Log.d(TAG, "sending broadcast");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getContext().getString(R.string.string_sharedPref_granted), true);
                    editor.apply();

                    Intent intent = new Intent(getString(R.string.string_filter_intent));
                    intent.putExtra("command", "hide");
                    getContext().sendBroadcast(intent);
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mHelper != null) mHelper.dispose();
            mHelper = null;

            sharedPreferencesPackageNames.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }

        SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (s.equals(getString(R.string.shared_pref_key_package_name_current))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    Set<String> l = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_current), null);
                    if (l != null) {
                        for (String str : l) {
                            stringBuilder.append("- ").append(Utils.getAppName(getContext(), str)).append("\n");
                        }

                        stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());

                        if (background_app != null) {
                            background_app.setSummary(stringBuilder.toString());
                        }
                    }
                }
            }
        };

        IabHelper.QueryInventoryFinishedListener queryInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                if (mHelper == null) return;

                if (result.isFailure()) {
                    Log.d(TAG, "Failed to query inventory: " + result);
                    return;
                }

                Log.d(TAG, "Query inventory was successful.");

                SkuDetails sku_small_tip_2 = inv.getSkuDetails(SKU_SMALL_TIP_2);
                Log.d(TAG, sku_small_tip_2.getTitle() + ": " + sku_small_tip_2.getPrice());
                Preference preference_small_tip_2 = findPreference(KEY_SMALL_TIP);
                preference_small_tip_2.setSummary(sku_small_tip_2.getPrice());
                String title = sku_small_tip_2.getTitle();

                SkuDetails sku_large_tip_5 = inv.getSkuDetails(SKU_LARGE_TIP_5);
                Log.d(TAG, sku_large_tip_5.getTitle() + ": " + sku_large_tip_5.getPrice());
                Preference preference_large_tip_5 = findPreference(KEY_LARGE_TIP);
                preference_large_tip_5.setSummary(sku_large_tip_5.getPrice());
                title = sku_large_tip_5.getTitle();

                try{
                    preference_small_tip_2.setTitle(title.substring(0, title.indexOf("(")));
                    preference_large_tip_5.setTitle(title.substring(0, title.indexOf("(")));
                }catch (StringIndexOutOfBoundsException e){
                    e.printStackTrace();
                    preference_small_tip_2.setTitle(getString(R.string.string_settings_small_donation));
                    preference_large_tip_5.setTitle(getString(R.string.string_settings_large_donation));
                }

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

                    if (isNotificationAccessPermissionGranted){
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                        builder.setMessage(R.string.permission_request_msg);
                        builder.setTitle(R.string.permission_request_title);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }


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

                case KEY_BACKGROUND_APPS:
                    Intent i = new Intent(getContext(), AppSelectorActivity.class);
                    startActivity(i);
                    break;

            }
            return false;
        }

        @Override
        public void onStart() {
            super.onStart();

            isNotificationAccessPermissionGranted = Utils.hasAccessGranted(getContext());
            notification_permission.setSummary(isNotificationAccessPermissionGranted ? getString(R.string.granted) : getString(R.string.not_granted));

            isSwitchSet = sharedPreferences.getBoolean(getContext().getString(R.string.string_sharedPreferences_isIconHidden), false);
            settings_hide_icon.setChecked(isSwitchSet);

        }

    }

}