package com.insalyon.dividoc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setButtonListeners();

        // If this is the first time the user is using the app, he has to input his serial number via the InitActivity
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        if (!preferences.contains("FirstStart")) {
            startActivity(new Intent(MainActivity.this, InitActivity.class));
        }

        context = getApplicationContext();
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
     * Give the context of the app to FilesPath class
     * @return the context of the app
     */
    public static Context getAppContext() {
        Log.d("idiot", "Context : " + context);
        return context;
    }
}