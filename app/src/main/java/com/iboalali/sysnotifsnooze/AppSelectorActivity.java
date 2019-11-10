package com.iboalali.sysnotifsnooze;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by iboalali on 02-Nov-17.
 */

public class AppSelectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selector);
    }


    public static class AppSelectorScreen extends PreferenceFragment implements Preference.OnPreferenceClickListener {

        private static final String TAG = "AppSelectorScreen";
        private SharedPreferences sharedPreferencesPackageNames;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_selector_screen);

            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("apps");
            sharedPreferencesPackageNames = getActivity().getSharedPreferences("myPackageNames", MODE_PRIVATE);

            Set<String> all = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_all), null);
            Set<String> selected = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_selected), null);

            CheckBoxPreference a = new CheckBoxPreference(getContext());
            a.setKey(getContext().getString(R.string.string_all_key));
            a.setTitle(R.string.string_all_apps);

            if (selected != null) {
                if (selected.contains(getContext().getString(R.string.string_all_key))) {
                    a.setChecked(true);
                }
            }

            a.setOnPreferenceClickListener(this);
            preferenceCategory.addPreference(a);

            if (all != null) {
                for (String s : all) {
                    CheckBoxPreference p = new CheckBoxPreference(getContext());
                    p.setKey(s);
                    p.setSummary(s);
                    p.setEnabled(!a.isChecked());

                    if (selected != null) {
                        if (selected.contains(s)) {
                            p.setChecked(true);
                        }
                    }

                    String appName = Utils.getAppName(getContext(), s);

                    if (appName == null) {
                        continue;
                    }

                    p.setTitle(appName);
                    p.setOnPreferenceClickListener(this);
                    preferenceCategory.addPreference(p);
                }
            }

            String title = preferenceCategory.getTitle().toString();

            // all devices with android 8.1 and Pixel 2 (XL) on 8.0 and onward are using "using battery" instead of "running in the background"
            if (Build.VERSION.SDK_INT >= 27 || Build.MODEL.equals("Pixel 2") || Build.MODEL.equals("Pixel 2 XL")) {
                title = title.replace("%s", "\"" + getString(R.string.string_app_name_replace_using_battery) + "\"");
            } else {
                title = title.replace("%s", "\"" + getString(R.string.string_app_name_replace_running_in_the_background) + "\"");
            }

            preferenceCategory.setTitle(title);

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Intent intent = new Intent(getString(R.string.string_filter_intent));
            intent.putExtra("command", "hide");
            getContext().sendBroadcast(intent);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("apps");

            if (preference.getKey().equals(getString(R.string.string_all_key))) {
                Set<String> l = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_all), null);
                if (l != null) {
                    for (String s : l) {
                        CheckBoxPreference p = (CheckBoxPreference) preferenceCategory.findPreference(s);
                        p.setEnabled(!((CheckBoxPreference) preference).isChecked());
                    }
                }

                List<String> list = new ArrayList<>();
                SharedPreferences.Editor editor = sharedPreferencesPackageNames.edit();

                if (((CheckBoxPreference) preference).isChecked()) {
                    list.add(getString(R.string.string_all_key));
                } else {
                    if (l != null) {
                        for (String s : l) {
                            CheckBoxPreference p = (CheckBoxPreference) preferenceCategory.findPreference(s);

                            if (p.isChecked()) {
                                Log.d(TAG, preference.getKey() + ": is checked. Put in selected list");
                                list.add(s);
                            }
                        }
                    }
                }

                editor.putStringSet(getString(R.string.shared_pref_key_package_name_selected), new HashSet<>(list));
                editor.apply();
            } else {
                Set<String> l = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_all), null);
                if (l != null) {
                    List<String> list = new ArrayList<>();

                    for (String s : l) {
                        CheckBoxPreference p = (CheckBoxPreference) preferenceCategory.findPreference(s);
                        if (p.isChecked()) {
                            Log.d(TAG, preference.getKey() + ": is checked. Put in selected list");
                            list.add(s);
                        }
                    }

                    for (String s : list) {
                        Log.d(TAG, "list: " + s);
                    }

                    SharedPreferences.Editor editor = sharedPreferencesPackageNames.edit();
                    editor.putStringSet(getString(R.string.shared_pref_key_package_name_selected), new HashSet<>(list));
                    editor.apply();
                }
            }

            return false;
        }
    }
}