package com.insalyon.dividoc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TagActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        // Initialization
        defineSpinners();
        loadTagFields();
        setOCDCFilter();
        setButtonListeners();

        // If this is a new case
        if (getIntent().getBooleanExtra("newCase", false))
        {
            verifyCameraPermission();
            dispatchTakePictureIntent();
            // TODO : Implement verifyReadAndWriteExternalStorage() when persistent VSN is done (if done using storage)
            setVSN();
        }
    }

    /**
     * Define age and gender spinner for form completion
     */
    private void defineSpinners() {

        // Gender spinner
        Spinner gender_spinner = findViewById(R.id.gender_spinner);
        ArrayAdapter<CharSequence> gender_adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        gender_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender_spinner.setAdapter(gender_adapter);

        // Age Spinner
        Spinner age_spinner = findViewById(R.id.age_spinner);
        ArrayAdapter<CharSequence> age_adapter = ArrayAdapter.createFromResource(this, R.array.age, android.R.layout.simple_spinner_item);
        age_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_spinner.setAdapter(age_adapter);
    }

    /**
     * Loads tag fields in the view (country code and serial number)
     */
    private void loadTagFields() {

        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);

        ((TextView) findViewById(R.id.cc_tag)).setText(preferences.getString("countryCode", "error"));
        ((TextView) findViewById(R.id.sn_tag)).setText(preferences.getString("serialNumber", "error"));
    }

    /**
     * Configure OCDC filter. Filter to have OCDC being only letters, digits, - or _ characters and maximum 20 characters long
     */
    private void setOCDCFilter() {

        EditText ocdcEditText = findViewById(R.id.ocdc_tag);

        InputFilter charFilter = (charSequence, start, end, spanned, i2, i3) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(charSequence.charAt(i)) && !((Character)charSequence.charAt(i)).equals(('-')) && !((Character)charSequence.charAt(i)).equals('_')) {
                ocdcEditText.setError(getString(R.string.invalid_ocdc));
                return "";
                }
            }
            return null;
        };

        InputFilter lengthFilter = new InputFilter.LengthFilter(20);

        ocdcEditText.setFilters(new InputFilter[] {charFilter, lengthFilter});
    }

    /**
     * Set listeners for the buttons, launching different activities
     */
    private void setButtonListeners() {

        FloatingActionButton galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(TagActivity.this, GalleryActivity.class);
            startActivity(galleryIntent);
        });

        FloatingActionButton recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(TagActivity.this, RecordActivity.class);
            startActivity(galleryIntent);
        });

        FloatingActionButton deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(view -> {

        });

        FloatingActionButton saveButton = findViewById(R.id.save_case_button);
        saveButton.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(TagActivity.this, ReviewActivity.class);
            startActivity(galleryIntent);
        });
    }

    /**
     * Verifies if the camera permission is granted. If it is not, the function is asking for permission
     */
    private void verifyCameraPermission() {

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

    /**
     * Opens camera and request for a photo before continuing
     */
    private void dispatchTakePictureIntent() {

        // Callback to wait for result. Triggers actions.
        // Documentation here : https://developer.android.com/training/basics/intents/result
        // StackOverflow here : https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
        ActivityResultLauncher<Intent> dispatchTakePictureIntentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // If a picture was shot
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        // Create directory to store pictures
                        File newCaseImageFolder = new File(FilesPath.getNewCaseImageFolder());
                        if (!newCaseImageFolder.exists()) {
                            if (newCaseImageFolder.mkdirs()) {
                                Log.d("idiot", "newCaseImageFolder was created successfully");
                            } else {
                                Toast.makeText(this, "Error : New folder could not be created - Please report it", Toast.LENGTH_SHORT).show();
                                this.finish();
                            }
                        } else {
                            // TODO Delete this else statement when the deletion of new_case folder is handled
                            Toast.makeText(this, "newCaseImageFolder already exists", Toast.LENGTH_SHORT).show();
                        }

                        // Save the picture
                        // TODO Handle AM and PM to avoid overwritting
                        String pictureFileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + "_.jpg";
                        File pictureFile = new File(newCaseImageFolder.getPath() + File.separator + pictureFileName);
                        try {
                            if (!pictureFile.createNewFile()) {
                                Toast.makeText(this, "Error : File already exists", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            Toast.makeText(this, "Error : Picture file could not be created", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else {
                        this.finish();
                    }
                }
        );

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        dispatchTakePictureIntentLauncher.launch(takePictureIntent);
    }

    /**
     *  Get the VSN from shared preferences, increments it and saves it for next use
     * TODO : Persist the VSN after uninstallation of the app
     * TODO : Manage multiple external storage devices if used ?
     */
    private void setVSN() {

        int VSN = 1;
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);

        if (preferences.contains("VSN")) {
            VSN = preferences.getInt("VSN", -1);
        }

        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putInt("VSN", ++VSN);
        preferencesEditor.apply();

        ((TextView) findViewById(R.id.vsn_tag)).setText(String.valueOf(VSN));

    }

    /**
     * Delete generated new case folder if the user created a new case and discard changes if the case
     * was already saved into a folder
     */
    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.cancel_changes);
        builder.setPositiveButton(R.string.discard_changes,
                (dialogInterface, i) -> {

                    // If this is a new case, the new case folder is deleted
                    if (getIntent().getBooleanExtra("newCase", false)) {
                        try {
                            FilesPath.deleteDirectory(new File(FilesPath.getNewCaseFolder()));
                        } catch (IOException e) {
                            Log.e("Storage", FilesPath.getNewCaseFolder() + " could not be deleted.");
                            e.printStackTrace();
                        }
                    }
                    this.finish();
                });
        builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> { });

        builder.create().show();
    }
}