package com.example.dogzear;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.prefs.PreferencesFactory;

public class SettingPreference extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = "SettingPreference";
    SharedPreferences prefs;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.app_setting);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Log.d(TAG, "getActivity() :         "+ getActivity().toString());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("lock")) {
           if(prefs.getBoolean("lock",false) == true) {
               Log.d(TAG, "설정했습니다.");
           }else {
               Log.d(TAG, "설정을 해제했습니다.");
           }
        }
    }
}














