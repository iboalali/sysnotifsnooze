package com.iboalali.sysnotifsnooze;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by iboalali on 02-Nov-17.
 */

public class AppSelectorActivity extends AppCompatActivity{

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
            if (all != null){
                for(String s: all){
                    CheckBoxPreference p = new CheckBoxPreference(getContext());
                    p.setKey(s);
                    p.setSummary(s);

                    if (selected != null){
                        if (selected.contains(s)){
                            p.setChecked(true);
                        }
                    }

                    p.setTitle(Utils.getAppName(getContext(), s));
                    p.setOnPreferenceClickListener(this);
                    preferenceCategory.addPreference(p);
                }
            }

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

            Set<String> l = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_all), null);
            if (l != null) {
                List<String> list = new ArrayList<>();

                for (String s : l) {
                    CheckBoxPreference p = (CheckBoxPreference) preferenceCategory.findPreference(s);
                    if (p.isChecked()){
                        Log.d(TAG, preference.getKey() + ": is checked. Put in selected list");
                        list.add(s);
                    }
                }

                for(String s: list){
                    Log.d(TAG, "list: " + s);
                }

                SharedPreferences.Editor editor = sharedPreferencesPackageNames.edit();
                editor.putStringSet(getString(R.string.shared_pref_key_package_name_selected), new HashSet<String>(list));
                editor.apply();
            }

            return false;
        }
    }
}