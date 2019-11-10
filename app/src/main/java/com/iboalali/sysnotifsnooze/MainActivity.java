package com.iboalali.sysnotifsnooze;

import android.content.ComponentName;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView_app_name);
        String title = textView.getText().toString();

        if (Build.VERSION.SDK_INT >= 27 || Build.MODEL.equals("Pixel 2") || Build.MODEL.equals("Pixel 2 XL")) {
            title = title.replace("%s", "\"" + getString(R.string.string_app_name_replace_using_battery) + "\"");
        } else {
            title = title.replace("%s", "\"" + getString(R.string.string_app_name_replace_running_in_the_background) + "\"");
        }

        textView.setText(title);

        SharedPreferences sharedPreferencesPackageNames = getSharedPreferences("myPackageNames", MODE_PRIVATE);
        if (!sharedPreferencesPackageNames.contains(getString(R.string.shared_pref_key_package_name_selected))) {
            SharedPreferences.Editor editor = sharedPreferencesPackageNames.edit();
            List<String> list = new ArrayList<>();
            list.add(getString(R.string.string_all_key));
            editor.putStringSet(getString(R.string.shared_pref_key_package_name_selected), new HashSet<String>(list));
            editor.apply();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("mySettingsPref", MODE_PRIVATE);
        if (!sharedPreferences.contains(getString(R.string.shared_pref_key_version_code))) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.shared_pref_key_version_code), Utils.getAppVersionCode(getApplicationContext()));
            editor.apply();
        }

    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private static final String KEY_NOTIFICATION_PERMISSION = "notification_permission";
        private static final String KEY_BACKGROUND_APPS = "background_app";
        private static final String KEY_SETTINGS_HIDE_ICON = "hide_icon";
        private static final String KEY_SETTINGS_OLD_WAY = "old_way";
        private static final String TAG = "SettingsFragment";

        private boolean isSwitchSet_isIconHidden;
        private boolean isSwitchSet_isOldWay;

        private Preference notification_permission;
        private SwitchPreference settings_hide_icon;
        private SwitchPreference settings_old_way;
        private Preference background_app;
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

            // find Preferences
            settings_hide_icon = (SwitchPreference) findPreference(KEY_SETTINGS_HIDE_ICON);
            settings_old_way = (SwitchPreference) findPreference(KEY_SETTINGS_OLD_WAY);
            notification_permission = findPreference(KEY_NOTIFICATION_PERMISSION);
            background_app = findPreference(KEY_BACKGROUND_APPS);

            // setup click listener
            notification_permission.setOnPreferenceClickListener(this);
            settings_hide_icon.setOnPreferenceClickListener(this);
            background_app.setOnPreferenceClickListener(this);

            settings_hide_icon.setOnPreferenceChangeListener((preference, o) -> {
                Log.d(TAG, "in settings_hide_icon onPreferenceChange");

                if (!settings_hide_icon.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.string_hide_icon_alert_dialog_msg);
                    builder.setTitle(R.string.string_hide_icon_alert_dialog_title);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.string_yes, (dialogInterface, i) -> {
                        Log.d(TAG, "set switch to TRUE");

                        PackageManager pkg = getContext().getPackageManager();
                        pkg.setComponentEnabledSetting(new ComponentName(getContext(), MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);

                        settings_hide_icon.setChecked(true);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getContext().getString(R.string.shared_pref_key_isIconHidden), true);
                        editor.apply();

                        Log.d(TAG, "switch is: " + settings_hide_icon.isChecked());
                    });

                    builder.setNegativeButton(getContext().getString(R.string.string_no), (dialogInterface, i) -> {
                        settings_hide_icon.setChecked(false);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getContext().getString(R.string.shared_pref_key_isIconHidden), false);
                        editor.apply();
                    });

                    builder.setOnCancelListener(dialogInterface -> {
                        Log.d(TAG, "set switch to FALSE (1)");

                        PackageManager pkg = getContext().getPackageManager();
                        pkg.setComponentEnabledSetting(new ComponentName(getContext(), MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);

                        settings_hide_icon.setChecked(false);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getContext().getString(R.string.shared_pref_key_isIconHidden), false);
                        editor.apply();
                        Log.d(TAG, "switch is: " + settings_hide_icon.isChecked());
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                } else {
                    Log.d(TAG, "set switch to FALSE (2)");

                    PackageManager pkg = getContext().getPackageManager();
                    pkg.setComponentEnabledSetting(new ComponentName(getContext(), MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);

                    settings_hide_icon.setChecked(false);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getContext().getString(R.string.shared_pref_key_isIconHidden), false);
                    editor.apply();
                    Log.d(TAG, "switch is: " + settings_hide_icon.isChecked());
                }


                return true;
            });

            settings_old_way.setOnPreferenceChangeListener((preference, o) -> {
                Log.d(TAG, "in settings_old_way onPreferenceChange");

                if (!settings_old_way.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.string_old_way_alert_dialog_msg);
                    builder.setTitle(R.string.string_old_way_alert_dialog_title);
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                        Log.d(TAG, "set switch to TRUE");

                        background_app.setEnabled(false);

                        settings_old_way.setChecked(true);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getContext().getString(R.string.shared_pref_key_isOldWay), true);
                        editor.apply();
                        Log.d(TAG, "switch is: " + settings_old_way.isChecked());

                        Intent intent = new Intent(getString(R.string.string_filter_intent));
                        intent.putExtra("command", "hide");
                        getContext().sendBroadcast(intent);
                    });

                    builder.setNegativeButton("No", (dialogInterface, i) -> {
                        Log.d(TAG, "keep switch on FALSE");
                        background_app.setEnabled(true);

                        settings_old_way.setChecked(false);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getContext().getString(R.string.shared_pref_key_isOldWay), false);
                        editor.apply();
                        Log.d(TAG, "switch is: " + settings_old_way.isChecked());
                    });

                    builder.setOnCancelListener(dialogInterface -> {
                        Log.d(TAG, "keep switch on FALSE");
                        background_app.setEnabled(true);

                        settings_old_way.setChecked(false);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getContext().getString(R.string.shared_pref_key_isOldWay), false);
                        editor.apply();
                        Log.d(TAG, "switch is: " + settings_old_way.isChecked());
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.string_new_way_alert_dialog_msg);
                    builder.setTitle(R.string.string_new_way_alert_dialog_title);
                    builder.setPositiveButton("OK", null);
                    background_app.setEnabled(true);

                    settings_old_way.setChecked(false);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getContext().getString(R.string.shared_pref_key_isOldWay), false);
                    editor.apply();
                    Log.d(TAG, "switch is: " + settings_old_way.isChecked());

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();


                }

                return true;
            });

        } // end of onCreate

        @Override
        public void onResume() {
            super.onResume();

            if (!Utils.hasAccessGranted(getContext())) {
                Log.d(TAG, "No Notification Access");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getContext().getString(R.string.shared_pref_key_granted), false);
                editor.apply();

                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());

                String msg = getString(R.string.permission_request_msg);
                if (Build.VERSION.SDK_INT >= 27 || Build.MODEL.equals("Pixel 2") || Build.MODEL.equals("Pixel 2 XL")) {
                    msg = msg.replace("%s", "\"" + getString(R.string.string_app_name_replace_using_battery) + "\"");
                } else {
                    msg = msg.replace("%s", "\"" + getString(R.string.string_app_name_replace_running_in_the_background) + "\"");
                }

                builder.setMessage(msg);

                builder.setTitle(R.string.permission_request_title);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.OK, (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            } else {
                Log.d(TAG, "Has Notification Access");

                StringBuilder stringBuilder = new StringBuilder();
                Set<String> l = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_current), null);
                if (l != null) {
                    stringBuilder.append(getString(R.string.string_settings_running_in_the_background)).append(":").append("\n");
                    for (String str : l) {
                        stringBuilder.append("- ").append(Utils.getAppName(getContext(), str)).append("\n");
                    }

                    stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());

                    if (background_app != null) {
                        background_app.setSummary(stringBuilder.toString());
                    }
                }

                if (!sharedPreferences.getBoolean(getContext().getString(R.string.shared_pref_key_granted), false)) {
                    Log.d(TAG, "sending broadcast");
                    Log.i(TAG, "sending broadcast");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getContext().getString(R.string.shared_pref_key_granted), true);
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
            sharedPreferencesPackageNames.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }

        SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (s.equals(getString(R.string.shared_pref_key_package_name_current))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    Set<String> l = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_current), null);
                    if (l != null) {
                        stringBuilder.append(getString(R.string.string_settings_running_in_the_background)).append(":").append("\n");
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

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case KEY_NOTIFICATION_PERMISSION:

                    if (isNotificationAccessPermissionGranted) {
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                        builder.setMessage(R.string.permission_request_msg);
                        builder.setTitle(R.string.permission_request_title);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.OK, (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                    break;
            }
            return false;
        }

        @Override
        public void onStart() {
            super.onStart();

            isNotificationAccessPermissionGranted = Utils.hasAccessGranted(getContext());
            notification_permission.setSummary(isNotificationAccessPermissionGranted ? getString(R.string.granted) : getString(R.string.not_granted));

            isSwitchSet_isIconHidden = sharedPreferences.getBoolean(getContext().getString(R.string.shared_pref_key_isIconHidden), false);
            isSwitchSet_isOldWay = sharedPreferences.getBoolean(getContext().getString(R.string.shared_pref_key_isOldWay), false);
            settings_hide_icon.setChecked(isSwitchSet_isIconHidden);
            settings_old_way.setChecked(isSwitchSet_isOldWay);
            background_app.setEnabled(!isSwitchSet_isOldWay);
        }
    }
}