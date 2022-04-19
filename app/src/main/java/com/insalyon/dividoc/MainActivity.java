package com.insalyon.dividoc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button addNewCaseButton;
    private Button exportFilesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If this is the first time the user is using the app, he has to input his serial number
        // via the InitActivity
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        if (!preferences.contains("FirstStart")) {
            startActivity(new Intent(MainActivity.this, InitActivity.class));
        }

        setButtonListeners();
    }

    /**
     * Set button listeners for "Add a new case" and "Export files" buttons, which will trigger
     * new activities
     */
    private void setButtonListeners() {
        // Start TagActivity if the "Add a new case" button is clicked
        Button addNewCaseButton = (Button) findViewById(R.id.add_new_case);
        addNewCaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TagActivity.class));
            }
        });

        // Start ExportActivity if the "Export files" button is clicked
        Button exportFilesButton = (Button) findViewById(R.id.export_file_button);
        exportFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ExportActivity.class));
            }
        });
    }
}