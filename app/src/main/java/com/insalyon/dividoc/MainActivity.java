package com.insalyon.dividoc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.insalyon.dividoc.fragments.FilesFragment;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization
        setButtonListeners();
        switchBetweenFilesAndArchives(findViewById(R.id.select_cases_files_button));


        // If this is the first time the user is using the app, he has to input his serial number via the InitActivity
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        if (!preferences.contains("FirstStart")) {
            startActivity(new Intent(MainActivity.this, InitActivity.class));
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
     *
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
     * Reloads the cases list in the menu
     */
    @Override
    public void onResume() {

        super.onResume();
        switchBetweenFilesAndArchives(findViewById(R.id.select_cases_files_button));
    }
}