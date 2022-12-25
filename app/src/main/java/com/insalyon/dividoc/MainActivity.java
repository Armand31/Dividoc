package com.insalyon.dividoc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewManager;
import android.view.WindowManager;
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

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.insalyon.dividoc.fragments.files.FilesFragment;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        applyPreferences();
        setContentView(R.layout.activity_main);

        // Block the screenshots and video recording
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        // TODO : Layout Animation : https://developer.android.com/training/animation/overview
        // TODO : Security best practices : https://developer.android.com/topic/security/data

        // Initialization
        setButtonListeners();
        switchBetweenFilesAndArchives();
        firstFragmentsLoad();
        deactivateExportAll();

        // If this is the first time the user is using the app, he has to input his serial number via the InitActivity
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.contains("FirstStart")) {
            startActivity(new Intent(MainActivity.this, InitActivity.class));
        }

        // Registration of the callback that will trigger the reload of the view when the locale has changed
        // and the settings activity is closed
        this.activityResultLauncher = registration();

        // Delete the new_case folder if it already exists (due to app closing in camera, tag or review) to ensure correct initialization
        if (new File(FilesPath.getNewCaseFolder()).exists()) {
            try {
                FilesPath.deleteDirectory(new File(FilesPath.getNewCaseFolder()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
     */
    public void switchBetweenFilesAndArchives() {

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.choose_fragment_layout);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            /*
            You have to check the checkedId value but also the isChecked value. The same listener is called
            when you check a button but also when you uncheck a button.
            It means that if you click the button1 the listener is called with isChecked=true and checkedId=1.
            Then if you click the button2 the listener is called twice. Once with isChecked=false and checkedId=1,
            once with isChecked=true and checkedId=2.
            */
            if (isChecked) {
                if (checkedId == R.id.select_cases_files_button) {

                    FilesFragment filesFragment = new FilesFragment();
                    // FragmentManager is the class used to manage the fragments of a layout. More information here : https://developer.android.com/guide/fragments/fragmentmanager
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragments_frame_layout, filesFragment);
                    fragmentTransaction.commit();

                }
            }
        });
    }

    /**
     * Loads the fragment view at startup of the application
     */
    private void firstFragmentsLoad() {

        FilesFragment filesFragment = new FilesFragment();
        // FragmentManager is the class used to manage the fragments of a layout. More information here : https://developer.android.com/guide/fragments/fragmentmanager
        FragmentManager frag_man = getSupportFragmentManager();
        FragmentTransaction frag_trans = frag_man.beginTransaction();
        frag_trans.replace(R.id.fragments_frame_layout, filesFragment);
        frag_trans.commit();
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
     * Apply the preferences at startup if they were changed from the defaults
     */
    private void applyPreferences() {
        SettingsActivity.setTheme();
        SettingsActivity.setLang(MainActivity.this);
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
     * Deactivates the view for exporting all cases in one click. This is an old functionality
     */
    private void deactivateExportAll() {

        // Deletes the export all button
        ((ViewManager)findViewById(R.id.export_file_button).getParent()).removeView(findViewById(R.id.export_file_button));
    }
}