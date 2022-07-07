package com.example.turnen.ui.SettingsFragment;

import android.content.SharedPreferences;

public interface MyListener {
    public void callback(SharedPreferences sharedPreferences, String key);
}
