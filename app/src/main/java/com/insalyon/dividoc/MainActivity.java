package com.insalyon.dividoc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.insalyon.dividoc.fragments.FilesFragment;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.util.Locale;

// TODO : Set the theme as the system default at initialization
// TODO : Delete new_case at startup if it exist
public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        applyPreferences();
        setContentView(R.layout.activity_main);

        // Initialization
        setButtonListeners();
        switchBetweenFilesAndArchives(findViewById(R.id.select_cases_files_button));

        // If this is the first time the user is using the app, he has to input his serial number via the InitActivity
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        if (!preferences.contains("FirstStart")) {
            startActivity(new Intent(MainActivity.this, InitActivity.class));
        }

        // Registration of the callback that will trigger the reload of the view when the locale as changed
        // and the settings activity is closed
        this.activityResultLauncher = registration();
    }

    /**
     * Set button listeners for "Add a new case" and "Export files" buttons, which will trigger
     * new activities
     */
    private void setButtonListeners() {

        // Start TagActivity if the "Add a new case" button is clicked
        Button addNewCaseButton = findViewById(R.id.add_new_case);
        addNewCaseButton.setOnClickListener(view -> {
            Intent tagActivityNewCase = new Intent(MainActivity.this, TagActivity.class);
            tagActivityNewCase.putExtra("newCase", true);
            startActivity(tagActivityNewCase);
        });

        // Start ExportActivity if the "Export files" button is clicked
        Button exportFilesButton = findViewById(R.id.export_file_button);
        exportFilesButton.setOnClickListener(view -> startTransfer());
    }

    /**
     * Allows switching between files and archives menu in the main activity
     * Triggered by callback (see onClick field in id/select_cases_files_button in activity_main.xml)
     */
    public void switchBetweenFilesAndArchives(View view) {

        Button currentCases = findViewById(R.id.select_cases_files_button);
        Button archiveCases = findViewById(R.id.select_cases_archives_button);

        if (view == findViewById(R.id.select_cases_files_button)) {

            FilesFragment filesFragment = new FilesFragment();
            currentCases.setBackgroundColor(getColor(R.color.purple_500));
            currentCases.setTextColor(getColor(R.color.white));
            archiveCases.setBackgroundColor(getColor(R.color.white));
            // FragmentManager is the class used to manage the fragments of a layout. More information here : https://developer.android.com/guide/fragments/fragmentmanager
            FragmentManager frag_man = getSupportFragmentManager();
            FragmentTransaction frag_trans = frag_man.beginTransaction();
            frag_trans.replace(R.id.fragments_frame_layout, filesFragment);
            frag_trans.commit();
        }

        /*
        if (view == findViewById(R.id.select_cases_archives_button)) {

            ArchivesFragment archivesFragment;
            archivesFragment = new ArchivesFragment();
            currentCases.setBackgroundColor(getColor(R.color.Grey));
            archiveCases.setBackgroundColor(getColor(R.color.colorPrimaryDark));

            FragmentManager frag_man = getSupportFragmentManager();
            FragmentTransaction frag_trans = frag_man.beginTransaction();
            frag_trans.replace(R.id.fragments_frame_layout, archivesFragment);
            frag_trans.commit();
        }
        */
    }

    /**
     * Starts the transfer activity that will zip the files
     */
    public void startTransfer() {

        if (new File(FilesPath.getCasesFolder()).listFiles() != null) {

            //String date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            //((TextView) findViewById(R.id.max_photo_info)).setText(getResources().getString(R.string.max_photos_gallery, MAX_PHOTOS_ALLOWED));

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.warning))
                    .setMessage(getResources().getString(R.string.read_before_export, TransferActivity.getHours()))
                    .setPositiveButton(getResources().getString(R.string.confirm_export),
                            ((dialogInterface, i) -> {
                                Intent exportIntent = new Intent(this, TransferActivity.class);
                                startActivity(exportIntent);
                            }))
                    .setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> {}));
            builder.create().show();
        } else {
            Toast.makeText(this, "There is no file to export", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates an Options Menu
     */
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    /**
     * Handles settings icon click event
     * @param item the menu item that is clicked
     * @return true on success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings_menu) {
            Intent settings = new Intent(this, SettingsActivity.class);
            this.activityResultLauncher.launch(settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Apply the preferences if they were changed from the defaults
     */
    private void applyPreferences() {
        SettingsActivity.setTheme();
        setLang();
    }

    /**
     * Sets the lang
     * TODO : Factorize code with com.insalyon.dividoc.SettingsActivity#setLang()
     */
    private void setLang() {

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

    /**
     * Callback that will reloads the view if the locale was changed in the settings
     * The return variable must be declared before the activity is started otherwise
     * an error is triggered and the activity is crashing
     * See https://stackoverflow.com/questions/64476827/how-to-resolve-the-error-lifecycleowners-must-call-register-before-they-are-sta
     * @return the activity result launcher
     */
    private ActivityResultLauncher<Intent> registration() {

        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        this.recreate();
                    }
                }
        );
    }

    /**
     * Reloads the cases list in the menu
     */
    @Override
    public void onResume() {

        super.onResume();
        switchBetweenFilesAndArchives(findViewById(R.id.select_cases_files_button));
    }
}