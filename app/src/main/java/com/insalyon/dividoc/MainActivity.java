package com.insalyon.dividoc;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.insalyon.dividoc.fragments.FilesFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializaiton
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
        Button addNewCaseButton = (Button) findViewById(R.id.add_new_case);
        addNewCaseButton.setOnClickListener(view -> {
            Intent tagActivityNewCase = new Intent(MainActivity.this, TagActivity.class);
            tagActivityNewCase.putExtra("newCase", true);
            startActivity(tagActivityNewCase);
        });

        // Start ExportActivity if the "Export files" button is clicked
        Button exportFilesButton = (Button) findViewById(R.id.export_file_button);
        exportFilesButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ExportActivity.class)));
    }

    /**
     * Allows switching between files and archives menu in the main activity
     * Triggered by callback (see onClick field in id/select_cases_files_button in activity_main.xml)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @Override
    public void onResume() {

        super.onResume();
        switchBetweenFilesAndArchives(findViewById(R.id.select_cases_files_button));
    }
}