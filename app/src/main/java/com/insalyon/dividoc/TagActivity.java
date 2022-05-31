package com.insalyon.dividoc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.insalyon.dividoc.util.FilesPath;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TagActivity extends AppCompatActivity {

    private String workingImageDirectory;
    private double latitude = 0.0, longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        // Initialization
        defineSpinners();
        loadTagFields();
        setOCDCFilter();
        setButtonListeners(registration());

        // If this is a new case
        if (getIntent().getBooleanExtra("newCase", false))
        {
            this.workingImageDirectory = FilesPath.getNewCaseImageFolder();
            getCaseLocation();
            createImageNewCaseFolder();
            verifyCameraPermission();
            dispatchTakePictureIntent();
            // TODO : Implement verifyReadAndWriteExternalStorage() when persistent VSN is done (if done using storage)
            setVSN();
        } else {
            this.workingImageDirectory = FilesPath.getCaseImageFolder(getIntent().getStringExtra("workingDirectory"));
            fetchData();
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

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
    private void setButtonListeners(ActivityResultLauncher<Intent> activityResultLauncher) {

        // Start the gallery activity
        FloatingActionButton galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(TagActivity.this, GalleryActivity.class);
            galleryIntent.putExtra("workingImageDirectory", workingImageDirectory);
            startActivity(galleryIntent);
        });

        // Start the record activity
        FloatingActionButton recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(TagActivity.this, RecordActivity.class);
            startActivity(galleryIntent);
        });

        // Delete the case
        FloatingActionButton deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(view -> deleteCase());

        // Start the review activity
        FloatingActionButton saveButton = findViewById(R.id.save_case_button);
        saveButton.setOnClickListener(view -> startReviewActivity(activityResultLauncher));
    }

    /**
     * Gets the location of the body
     */
    private void getCaseLocation() {

        // Verify location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Get the location if the permission was granted
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, location -> {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        });

        /*
        Requires Google Play Store and a google account

        Needs to add the dependency in the build.gradle file : implementation 'com.google.android.gms:play-services-location:19.0.1'

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    System.out.println("location : " + location);
                }
            }
        });
        */
    }

    /**
     * Creates the image directory in new_case directory
     */
    private void createImageNewCaseFolder() {

        File workingImageDirectoryFileObject = new File(workingImageDirectory);
        if (!workingImageDirectoryFileObject.exists()) {
            if (!workingImageDirectoryFileObject.mkdirs()) {
                (Toast.makeText(this, getString(R.string.cannot_create_pictures_dir), Toast.LENGTH_SHORT)).show();
            }
        }
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
     * Opens camera and saves the photo at the specified URI upon success
     */
    private void dispatchTakePictureIntent() {

        File imageFile = new File(workingImageDirectory, "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg");
        Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", imageFile);

        /// Callback used to launch camera and trigger action on success and/or failure
        // Documentation here : https://developer.android.com/training/basics/intents/result
        // StackOverflow here : https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
        // About TakePicture() : https://stackoverflow.com/questions/61941959/activityresultcontracts-takepicture
        ActivityResultLauncher<Uri> dispatchTakePictureIntentLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (!result) {
                        try {
                            FilesPath.deleteDirectory(new File(FilesPath.getNewCaseFolder()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        this.finish();
                    }
                }
        );

        dispatchTakePictureIntentLauncher.launch(photoUri);
    }

    /**
     *  Get the VSN from shared preferences
     * TODO : Persist the VSN after uninstallation of the app
     * TODO : Manage multiple external storage devices if used ?
     */
    private void setVSN() {

        int VSN = 0;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences.contains("VSN")) {
            // Getting the VSN if it exists in the shared preferences
            VSN = preferences.getInt("VSN", -1);
        } else {
            // If the VSN does not exists, it is set at 0 in the shared preferences
            SharedPreferences.Editor preferencesEditor = preferences.edit();
            preferencesEditor.putInt("VSN", ++VSN);
            preferencesEditor.apply();
        }

        ((TextView) findViewById(R.id.vsn_tag)).setText(String.valueOf(VSN));
    }

    /**
     * Deletes the generated case
     */
    private void deleteCase() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TagActivity.this);
        builder.setMessage(getResources().getString(R.string.delete_case_label))
                .setTitle(getResources().getString(R.string.warning))
                .setPositiveButton(getResources().getString(R.string.delete_label), (dialog, id) -> {
                    try {
                        FilesPath.deleteDirectory(new File(workingImageDirectory));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finish();
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialogInterface, i) -> {})
                .show();
    }

    /**
     * Callback that will finish this tag activity when the review activity (which is a child of tag activity) will end
     * The return variable must be declared before the activity is started otherwise
     * an error is triggered and the activity is crashing
     * See https://stackoverflow.com/questions/64476827/how-to-resolve-the-error-lifecycleowners-must-call-register-before-they-are-sta
     * @return the activity result launcher
     */
    private ActivityResultLauncher<Intent> registration() {

        return registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_CANCELED) {
                    this.finish();
                }
            }
        );
    }

    /**
     * Starts the review activity and pass the info to it
     */
    private void startReviewActivity(ActivityResultLauncher<Intent> activityResultLauncher) {

        Intent reviewIntent = new Intent(TagActivity.this, ReviewActivity.class);

        // Control information
        reviewIntent.putExtra("newCase", getIntent().getBooleanExtra("newCase", false));
        reviewIntent.putExtra("workingImageDirectory", this.workingImageDirectory);

        // Inputted information
        reviewIntent.putExtra("name", ((EditText) findViewById(R.id.name_input)).getText().toString());
        reviewIntent.putExtra("gender", ((Spinner) findViewById(R.id.gender_spinner)).getSelectedItem().toString());
        reviewIntent.putExtra("manual_location", ((TextView) findViewById(R.id.location_input)).getText().toString());
        reviewIntent.putExtra("age", ((Spinner) findViewById(R.id.age_spinner)).getSelectedItem().toString());
        reviewIntent.putExtra("additional_information", ((TextView) findViewById(R.id.additional_info_input)).getText().toString());

        // OCDC
        String ocdc;
        if (((TextView) findViewById(R.id.ocdc_tag)).getText().toString().equals("")) {
            ocdc = getString(R.string.ocdc_default_value);
        } else {
            ocdc = ((TextView) findViewById(R.id.ocdc_tag)).getText().toString();
        }
        reviewIntent.putExtra("OCDC", ocdc);

        // VSN and coordinates
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int VSN = 0;
        if (getIntent().getBooleanExtra("newCase", true)) {

            // VSN
            VSN = preferences.getInt("VSN", 0);

            // Coordinates
            reviewIntent.putExtra("latitude", this.latitude);
            reviewIntent.putExtra("longitude", this.longitude);

        } else {
            try {

                JSONObject json = openJSON();

                // VSN
                VSN = json.getInt("VSN");

                // Coordinates
                reviewIntent.putExtra("latitude", json.getString("Latitude"));
                reviewIntent.putExtra("longitude", json.getString("Longitude"));

                // Old tag (for case editing, in case of folder renaming due to ocdc change)
                reviewIntent.putExtra("oldTag", json.getString("Tag"));

            } catch (JSONException e) {
                Toast.makeText(this, getString(R.string.cannot_parse_json_file), Toast.LENGTH_SHORT).show();
            }
        }
        reviewIntent.putExtra("VSN", VSN);

        // Tag (or new tag in case of case editing)
        String tag = preferences.getString("countryCode", "")
                + preferences.getString("serialNumber", "")
                + "_" + VSN
                + "_" + ocdc;
        reviewIntent.putExtra("tag", tag);

        activityResultLauncher.launch(reviewIntent);
    }

    /**
     * Fills the input fields with the previously saved information (in data.json file)
     */
    private void fetchData() {

        try {
            JSONObject json = openJSON();

            // Inputs feeding
            ((EditText) findViewById(R.id.name_input)).setText(json.getString("Name"));
            ((EditText) findViewById(R.id.location_input)).setText(json.getString("HandwrittenLocation"));
            ((EditText) findViewById(R.id.additional_info_input)).setText(json.getString("AdditionalInformation"));
            ((EditText) findViewById(R.id.ocdc_tag)).setText(json.getString("OCDC"));
            ((TextView) findViewById(R.id.vsn_tag)).setText(json.getString("VSN"));

            // Spinners inputs feeding
            String compareValue = json.getString("Age");
            Spinner mSpinner = findViewById(R.id.age_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.age, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            int spinnerPosition = adapter.getPosition(compareValue);
            mSpinner.setSelection(spinnerPosition);

            compareValue = json.getString("Gender");
            mSpinner = findViewById(R.id.gender_spinner);
            adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            spinnerPosition = adapter.getPosition(compareValue);
            mSpinner.setSelection(spinnerPosition);

        } catch (JSONException e) {
            Toast.makeText(this, getString(R.string.cannot_parse_json_file), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Opens the JSON file that is linked with the current case
     * @return the JSONObject
     */
    private JSONObject openJSON() throws JSONException {

        String caseFolder = FilesPath.getCasesFolder() + File.separator + getIntent().getStringExtra("workingDirectory");
        File jsonFile = new File(FilesPath.getJsonDataFile(caseFolder));

        // Reading the JSON file
        StringBuilder jsonText = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(jsonFile));
            String line;

            while ((line = br.readLine()) != null) {
                jsonText.append(line);
                jsonText.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            Toast.makeText(this, getString(R.string.cannot_open_json_file), Toast.LENGTH_SHORT).show();
        }

        return new JSONObject(String.valueOf(jsonText));
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