package com.insalyon.dividoc.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.insalyon.dividoc.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
