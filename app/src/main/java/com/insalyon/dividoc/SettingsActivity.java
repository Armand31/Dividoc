package com.insalyon.dividoc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.insalyon.dividoc.fragments.SettingsFragment;
import com.insalyon.dividoc.util.AppContext;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);// Setting the settings fragment as a view

        // Block the screenshots and video recording
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment(), "Your_Fragment_TAG")
                .commit();

        // Listener that launch different functions according to the preference that changed
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences1, key) -> {
            switch (key) {
                case "dark_mode":
                    setTheme();
                    break;
                case "lang":
                    setLang(this);
                    this.recreate(); // Reloads the fragment view
                    break;
                case "show_zip_passwords":
                    showZipPasswords();
                    break;
            }
        };

        // Applying the listener to the shared preferences
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Sets or unset the dark theme
     */
    public static void setTheme() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppContext.getAppContext());

        if (!sharedPreferences.contains("FirstStart") || !sharedPreferences.contains("dark_mode")) {

            SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();

            // Retrieving the system theme in order to set the dark_mode preferences boolean, correcting a bug
            switch (AppContext.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    preferencesEditor.putBoolean("dark_mode", true);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    preferencesEditor.putBoolean("dark_mode", false);
                    break;
            }

            preferencesEditor.apply();

        } else if (sharedPreferences.getBoolean("dark_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Changes the language
     * Supported locales : https://stackoverflow.com/questions/7973023/what-is-the-list-of-supported-languages-locales-on-android
     */
    @SuppressLint("ObsoleteSdkInt")
    public static void setLang(Context context) {

        // Getting the selected language
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = sharedPreferences.getString("lang", "en");

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLayoutDirection(locale);
        } else {
            config.locale = locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLayoutDirection(locale);
            }
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    /**
     * Opens an the showPasswordsActivity
     */
    private void showZipPasswords() {

        Intent showZipPasswords = new Intent(this, ShowZipPasswordsActivity.class);
        startActivity(showZipPasswords);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Returns as result_ok in order to reload the main activity's view
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}
