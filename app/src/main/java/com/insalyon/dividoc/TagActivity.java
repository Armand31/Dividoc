package com.insalyon.dividoc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class TagActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        // Spinners widgets initialization
        defineSpinners();

        // If this is a new case
        if (getIntent().getBooleanExtra("newCase", false))
        {
            verifyCameraPermission();
        }
    }

    /**
     * Define age and gender spinner for form completion
     */
    public void defineSpinners() {

        // Gender spinner
        Spinner gender_spinner = (Spinner) findViewById(R.id.gender_spinner);
        ArrayAdapter<CharSequence> gender_adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        gender_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender_spinner.setAdapter(gender_adapter);

        // Age Spinner
        Spinner age_spinner = (Spinner) findViewById(R.id.age_spinner);
        ArrayAdapter<CharSequence> age_adapter = ArrayAdapter.createFromResource(this, R.array.age, android.R.layout.simple_spinner_item);
        age_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_spinner.setAdapter(age_adapter);
    }

    /**
     * Verifies if the camera permission is granted. If it is not, the function is asking for permission
     */
    public void verifyCameraPermission() {

        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher, as an instance variable.
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        this.finish();
                    }
                });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            Toast.makeText(this, "You need to provide camera permission from your phone settings", Toast.LENGTH_SHORT).show();
        }
    }
}