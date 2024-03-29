package com.insalyon.dividoc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.color.MaterialColors;
import com.insalyon.dividoc.fragments.audios.AudioFragment;
import com.insalyon.dividoc.util.AppContext;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AudioGalleryActivity extends AppCompatActivity {

    private String workingAudioDirectory;
    private AudioFragment audioFragment;
    private MediaRecorder recorder;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_gallery);

        // Block the screenshots and video recording
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );

        // Initialization
        this.workingAudioDirectory = getIntent().getStringExtra("workingAudioDirectory");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setButtonListeners();
        loadFragment();

        // Verify audio recording permission
        verifyPermission(Manifest.permission.RECORD_AUDIO, getResources().getString(R.string.provide_audio), () -> {}, this::finish, getString(R.string.revoked_audio));
    }

    /**
     * Set listeners for the buttons
     */
    private void setButtonListeners() {

        // Record button listener
        Button record = findViewById(R.id.record_audio);
        record.setOnClickListener(view -> {
            if (!isRecording) {
                this.isRecording = true;
                FilesPath.createDirectory(workingAudioDirectory, getString(R.string.cannot_create_pictures_dir));
                modifyUI();
                startRecording();
            } else {
                this.isRecording = false;
                revertUI();
                stopRecording();
                loadFragment(); // Reloads the fragment view
            }
        });

    }

    /**
     * Loads the audio fragment
     */
    private void loadFragment() {

        // Passing workingAudioDirectory as an argument to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("workingAudioDirectory", this.workingAudioDirectory);
        this.audioFragment = new AudioFragment();
        audioFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.audio_frame_layout, audioFragment);
        fragmentTransaction.commit();
    }

    /**
     * Verifies the state of the given permission
     * @param permission the permission to verify
     * @param messageBody the message to display to let the user understand why he needs to give the permission
     * @param toPerformOnSuccess the action to perform if the permission was granted
     * @param toPerformOnFailure the action to perform if the permission was not granted
     * TODO : Factorize code, maybe using https://developer.android.com/training/basics/intents/result#separate
     */
    public void verifyPermission(String permission, String messageBody, Runnable toPerformOnSuccess, Runnable toPerformOnFailure, String permissionRevoked) {

        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                toPerformOnSuccess.run();
            } else {
                toPerformOnFailure.run();
            }
        });

        if (ActivityCompat.checkSelfPermission(AppContext.getAppContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            toPerformOnSuccess.run();

        } else if (shouldShowRequestPermissionRationale(permission)) {
            // Explain to the user why the user needs to allow the permission and ask him if he wants to grant the permission
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.to_help_us))
                    .setMessage(messageBody)
                    .setPositiveButton(getResources().getString(android.R.string.ok), ((dialogInterface, i) -> requestPermissionLauncher.launch(permission)))
                    .setOnCancelListener(dialogInterface -> requestPermissionLauncher.launch(permission)) // If the return button is clicked
                    .setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> toPerformOnFailure.run()));
            builder.create().show();

        } else {
            // If the permission was not granted, create a dialog to say that the permission was revoked
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.permission_revoked))
                    .setMessage(permissionRevoked)
                    .setPositiveButton(getResources().getString(android.R.string.ok), ((dialogInterface, i) -> {
                        // Opens the settings of the app (through System Settings) to grant the revoked permission
                        Intent appSettings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        appSettings.addCategory(Intent.CATEGORY_DEFAULT);
                        appSettings.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(appSettings); // There is no information about when the exits, that's why we then run toPerformOnFailure : https://developer.android.com/reference/android/app/Activity#startActivity(android.content.Intent,%20android.os.Bundle)
                        toPerformOnFailure.run();
                    }))
                    .setNegativeButton(getResources().getString(android.R.string.cancel), ((dialogInterface, i) -> toPerformOnFailure.run()))
                    .setOnCancelListener(dialogInterface -> toPerformOnFailure.run()); // If the return button is clicked
            builder.create().show();
        }
    }

    /**
     * Starts the recording of the audio and save the stream in the specified file
     */
    private void startRecording() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            this.recorder = new MediaRecorder();
            this.recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            this.recorder.setOutputFile(workingAudioDirectory + File.separator + "Record_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".mp4");
            this.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

            try {
                this.recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.recorder.start();
        }
    }

    /**
     * Stops the recording of the audio
     */
    private void stopRecording() {

        this.recorder.stop();
        this.recorder.release();
    }

    /**
     * Modifies the UI to inform the user that he is recording
     */
    private void modifyUI() {

        // Displaying a chronometer
        Chronometer chrono = findViewById(R.id.chronometer);
        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();

        // Changing button's text and icon
        Button record = findViewById(R.id.record_audio);
        record.setText(getString(R.string.recording));
        record.setCompoundDrawablesWithIntrinsicBounds(R.drawable.recording, 0, 0, 0);

        // Changing button's background color with the secondary color from theme
        record.setBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.CYAN));
    }

    /**
     * Reverts the UI to inform the user that he ended the recording
     */
    private void revertUI() {

        // Stopping the chronometer
        Chronometer chrono = findViewById(R.id.chronometer);
        chrono.stop();

        // Reverting button's text and icon
        Button record = findViewById(R.id.record_audio);
        record.setText(R.string.record_audio);
        record.setCompoundDrawablesWithIntrinsicBounds(R.drawable.record_voice, 0, 0, 0);

        // Reverting button's background color
        record.setBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, Color.MAGENTA));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.audioFragment.stopAudio();
        super.onBackPressed();
    }
}
