package com.insalyon.dividoc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputEditText;
import com.insalyon.dividoc.util.AppContext;
import com.insalyon.dividoc.util.FilesPath;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TagActivity extends AppCompatActivity {

    private String workingDirectory;
    private ActivityResultLauncher<Uri> dispatchTakePictureIntentLauncher;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private double latitude = 0.0, longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        // Block the screenshots and video recording
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );

        // TODO : Manage layout screen rotation with View Model : https://developer.android.com/topic/libraries/architecture/viewmodel

        // Initialization
        callbacksRegistration();
        defineLists();
        loadTagFields();
        setOCDCFilter();
        setButtonListeners();

        // If this is a new case
        if (getIntent().getBooleanExtra("newCase", false))
        {
            this.workingDirectory = FilesPath.getNewCaseFolder();

            // Verify location permission and get the case location if the permission is granted
            verifyPermission(Manifest.permission.ACCESS_FINE_LOCATION, getResources().getString(R.string.provide_location), this::getCaseLocation, () -> {});

            // Verify storage permission to ensure correct execution of the code
            verifyPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getResources().getString(R.string.provide_file_access), () -> {}, this::finish);
            //verifyPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getResources().getString(R.string.provide_file_access), () -> {}, this::finish);

            // Verify camera permission and open the camera activity if the permission is granted
            verifyPermission(Manifest.permission.CAMERA, getResources().getString(R.string.provide_camera), this::dispatchTakePictureIntent, () -> {
                FilesPath.deleteDirectory(workingDirectory);
                this.finish();
            });

            // TODO : Implement verifyReadAndWriteExternalStorage() when persistent VSN is done (if done using storage)
            setVSN();
        } else {
            this.workingDirectory = getIntent().getStringExtra("workingDirectory");
            fetchData();
        }
    }

    /**
     * Register callbacks that will be triggered to get a result from an activity
     * These callbacks must be declared before the activity is started otherwise
     * an error is triggered and the activity is crashing
     * See https://stackoverflow.com/questions/64476827/how-to-resolve-the-error-lifecycleowners-must-call-register-before-they-are-sta
     */
    private void callbacksRegistration() {

        // Callback triggered for the launching of the camera activity and the implementation of the code we want on success / failure
        this.dispatchTakePictureIntentLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (!result) {
                        try {
                            // Deleting the previously generated case if the camera activity is finished and no photo was taken
                            FilesPath.deleteDirectory(new File(FilesPath.getNewCaseFolder()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            // And finishing the tag activity as we force the user to take a picture to register a case
                            this.finish();
                        }
                    }
                }
        );

        // If the case is saved or deleted from the review activity (child of tag activity) , the result set will be RESULT_OK, so this callback will be triggered
        // and this activity will also finish due to this.finish() function
        this.activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_CANCELED) {
                        this.finish();
                    }
                }
        );
    }

    /**
     * Define age and gender lists for form completion
     */
    private void defineLists() {

        // Gender spinner
        AutoCompleteTextView gender = findViewById(R.id.gender_list);
        ArrayAdapter<String> gender_adapter = new ArrayAdapter<>(this, R.layout.list_item_gender, getGenderList());
        gender.setAdapter(gender_adapter);

        // Age Spinner
        AutoCompleteTextView age = findViewById(R.id.age_list);
        ArrayAdapter<String> age_adapter = new ArrayAdapter<>(this, R.layout.list_item_age, getAgeList());
        age.setAdapter(age_adapter);
    }

    /**
     * Get the gender list
     * @return the gender list in ArrayList<String> object
     */
    private ArrayList<String> getGenderList() {
        ArrayList<String> genderList = new ArrayList<>();
        genderList.add(getString(R.string.man));
        genderList.add(getString(R.string.woman));
        genderList.add(getString(R.string.unknown));
        return genderList;
    }

    /**
     * Get the gender list
     * @return the gender list in ArrayList<String> object
     */
    private ArrayList<String> getAgeList() {
        ArrayList<String> ageList = new ArrayList<>();
        ageList.add(getString(R.string.adult));
        ageList.add(getString(R.string.teenager));
        ageList.add(getString(R.string.kid));
        ageList.add(getString(R.string.baby));
        ageList.add(getString(R.string.unknown));
        return ageList;
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
    private void setButtonListeners() {

        // Start the gallery activity
        Button galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(view -> {
            Intent photoGalleryIntent = new Intent(TagActivity.this, PhotoGalleryActivity.class);
            photoGalleryIntent.putExtra("workingImageDirectory", FilesPath.getCaseImageFolder(this.workingDirectory));
            startActivity(photoGalleryIntent);
        });

        // Start the record activity
        Button recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(view -> {
            Intent audioGalleryIntent = new Intent(TagActivity.this, AudioGalleryActivity.class);
            audioGalleryIntent.putExtra("workingAudioDirectory", FilesPath.getCaseAudioFolder(workingDirectory));
            startActivity(audioGalleryIntent);
        });

        // Delete the case
        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(view -> deleteCase());

        // Start the review activity
        Button saveButton = findViewById(R.id.save_case_button);
        saveButton.setOnClickListener(view -> startReviewActivity());
    }

    /**
     * Verifies the state of the given permission
     * @param permission the permission to verify
     * @param messageBody the message to display to let the user understand why he needs to give the permission
     * @param toPerformOnSuccess the action to perform if the permission was granted
     * @param toPerformOnFailure the action to perform if the permission was not granted
     * TODO : Factorize code using https://developer.android.com/training/basics/intents/result#separate
     */
    public void verifyPermission(String permission, String messageBody, Runnable toPerformOnSuccess, Runnable toPerformOnFailure) {

        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                toPerformOnSuccess.run();
            } else {
                toPerformOnFailure.run();
            }
        });

        if (ActivityCompat.checkSelfPermission(AppContext.getAppContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            // Get case location if the permission was granted
            toPerformOnSuccess.run();

        } else if (this.shouldShowRequestPermissionRationale(permission)) {
            // Explain to the user why the user needs to allow the permission and ask him if he wants to grant the permission
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.to_help_us))
                    .setMessage(messageBody)
                    .setPositiveButton(getResources().getString(android.R.string.ok), ((dialogInterface, i) -> requestPermissionLauncher.launch(permission)));
            //.setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> toPerformOnFailure.run()));
            builder.create().show();

        } else {
            // If the permission was not granted
            toPerformOnFailure.run();
        }
    }

    /**
     * Get the location
     */
    @SuppressLint("MissingPermission")
    // TODO : Ensure that location is active in the app
    private void getCaseLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
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
     * Opens camera and saves the photo at the specified URI upon success
     * TODO : Better camera handling ? Check https://developer.android.com/training/camerax/choose-camera-library
     */
    private void dispatchTakePictureIntent() {

        FilesPath.createDirectory(FilesPath.getCaseImageFolder(workingDirectory), getString(R.string.cannot_create_pictures_dir));

        File imageFile = new File(FilesPath.getCaseImageFolder(workingDirectory), "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg");
        Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", imageFile);

        /// Callback used to launch camera and trigger action on success and/or failure
        // Documentation here : https://developer.android.com/training/basics/intents/result
        // StackOverflow here : https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
        // About TakePicture() : https://stackoverflow.com/questions/61941959/activityresultcontracts-takepicture
        /*
        ActivityResultLauncher<Uri> dispatchTakePictureIntentLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (!result) {
                        try {
                            FilesPath.deleteDirectory(new File(FilesPath.getNewCaseFolder()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            this.finish();
                        }
                    }
                }
        );*/

        this.dispatchTakePictureIntentLauncher.launch(photoUri);
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
     * Displays an alert dialog to the user to confirm the deletion of the case. The case folder is deleted upon accept
     * TODO : Factorize code with the other delete functions (in ReviewActivity, MainActivity, AudioGalleryActivity...)
     */
    private void deleteCase() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TagActivity.this);
        builder.setMessage(getResources().getString(R.string.delete_case_label))
                .setTitle(getResources().getString(R.string.warning))
                .setPositiveButton(getResources().getString(R.string.delete_label), (dialog, id) -> {
                    try {
                        FilesPath.deleteDirectory(new File(workingDirectory));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finish();
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialogInterface, i) -> {})
                .show();
    }

    /**
     * Starts the review activity and pass the info to it
     */
    private void startReviewActivity() {

        Intent reviewIntent = new Intent(TagActivity.this, ReviewActivity.class);

        // Control information
        reviewIntent.putExtra("newCase", getIntent().getBooleanExtra("newCase", false));
        reviewIntent.putExtra("workingDirectory", workingDirectory);

        // Inputted information
        reviewIntent.putExtra("name", Objects.requireNonNull(((TextInputEditText) findViewById(R.id.name_input)).getText()).toString());
        reviewIntent.putExtra("gender", ((AutoCompleteTextView) findViewById(R.id.gender_list)).getText().toString());
        reviewIntent.putExtra("manual_location", ((TextView) findViewById(R.id.location_input)).getText().toString());
        reviewIntent.putExtra("age", ((AutoCompleteTextView) findViewById(R.id.age_list)).getText().toString());
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

        this.activityResultLauncher.launch(reviewIntent);
    }

    /**
     * Fills the input fields with the previously saved information (in data.json file)
     */
    private void fetchData() {

        try {
            JSONObject json = openJSON();

            // Inputs feeding
            ((TextInputEditText) findViewById(R.id.name_input)).setText(json.getString("Name"));
            ((EditText) findViewById(R.id.location_input)).setText(json.getString("HandwrittenLocation"));
            ((EditText) findViewById(R.id.additional_info_input)).setText(json.getString("AdditionalInformation"));
            ((EditText) findViewById(R.id.ocdc_tag)).setText(json.getString("OCDC"));
            ((TextView) findViewById(R.id.vsn_tag)).setText(json.getString("VSN"));

            // Spinners inputs feeding
            AutoCompleteTextView ageTextView = findViewById(R.id.age_list);
            ageTextView.setText(json.getString("Age"), false);

            AutoCompleteTextView genderTextView = findViewById(R.id.gender_list);
            genderTextView.setText(json.getString("Gender"), false);

        } catch (JSONException e) {
            Toast.makeText(this, getString(R.string.cannot_parse_json_file), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Opens the JSON file that is linked with the current case
     * @return the JSONObject
     */
    private JSONObject openJSON() throws JSONException {

        File jsonFile = new File(FilesPath.getJsonDataFile(this.workingDirectory));

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