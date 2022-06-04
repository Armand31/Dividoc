package com.insalyon.dividoc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.color.MaterialColors;
import com.insalyon.dividoc.fragments.AudioFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioGalleryActivity extends AppCompatActivity {

    private String workingAudioDirectory;
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
        setButtonListeners();
        loadFragment();
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
                createAudioDirectory();
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
        AudioFragment audioFragment = new AudioFragment();
        audioFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.audio_frame_layout, audioFragment);
        fragmentTransaction.commit();
    }

    /**
     * Creates the audio directory in new_case directory
     * TODO : Code factorization with com.insalyon.dividoc.TagActivity#createImageNewCaseFolder()
     */
    private void createAudioDirectory() {

        File workingImageDirectoryFileObject = new File(workingAudioDirectory);
        if (!workingImageDirectoryFileObject.exists()) {
            if (!workingImageDirectoryFileObject.mkdirs()) {
                (Toast.makeText(this, getString(R.string.cannot_create_pictures_dir), Toast.LENGTH_SHORT)).show();
            }
        }
    }

    /**
     * Starts the recording of the audio and save the stream in the specified file
     * TODO : Handle permission
     */
    private void startRecording() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            this.recorder = new MediaRecorder();
            this.recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            this.recorder.setOutputFile(workingAudioDirectory + File.separator + "Record_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".mp4");
            this.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

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
}
