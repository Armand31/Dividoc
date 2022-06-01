package com.insalyon.dividoc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.insalyon.dividoc.fragments.SettingsFragment;
import com.insalyon.dividoc.util.DiviContext;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);// Setting the settings fragment as a view

        // Block the screenshots and video recording
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

        // Listener that launch different functions according to the preference that changed
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences1, key) -> {
            if (key.equals("dark_mode")) {
                setTheme();
            } else if (key.equals("lang")) {
                setLang();
                this.recreate();
                //loadView(); // Reloads the view after language change
            }
        };

        // Applying the listener to the shared preferences
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Sets or unset the dark theme
     */
    public static void setTheme() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DiviContext.getAppContext());
        boolean darkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Changes the language
     * Supported locales : https://stackoverflow.com/questions/7973023/what-is-the-list-of-supported-languages-locales-on-android
     */
    public void setLang() {

        // Getting the selected language
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = sharedPreferences.getString("lang", "en");

        // Changes the application's configuration
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = this.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {

        // Returns as result_ok in order to reload the main activity's view
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}





