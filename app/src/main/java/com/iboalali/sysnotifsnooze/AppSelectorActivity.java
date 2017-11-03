package com.iboalali.sysnotifsnooze;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

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

        private SharedPreferences sharedPreferencesPackageNames;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_selector_screen);

            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("apps");
            sharedPreferencesPackageNames = getActivity().getSharedPreferences("myPackageNames", MODE_PRIVATE);

            Set<String> l = sharedPreferencesPackageNames.getStringSet(getString(R.string.shared_pref_key_package_name_all), null);
            if (l != null){
                for(String s: l){
                    CheckBoxPreference p = new CheckBoxPreference(getContext());
                    p.setKey(s);
                    p.setSummary(s);
                    p.setTitle(Utils.getAppName(getContext(), s));
                    preferenceCategory.addPreference(p);
                }
            }

        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            return false;
        }
    }
}
