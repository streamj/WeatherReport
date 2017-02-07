package com.example.stream.weatherreport;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by StReaM on 2/7/2017.
 */

public class SettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
