package com.insalyon.dividoc;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class InitActivity extends AppCompatActivity {

    private TextInputEditText countryCode;
    private TextInputEditText serialNumber;
    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        // Block the screenshots and video recording
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );

        countryCode = findViewById(R.id.country_code);
        serialNumber = findViewById(R.id.serial_number);
        setTextChangeListeners();

        start = findViewById(R.id.start);
        start.setEnabled(false);
        start.setOnClickListener(view -> confirmSerialNumber());
    }

    /**
     * Set text change listeners to enable the button and give the focus if the country code and
     * the serial number are correct
     */
    private void setTextChangeListeners() {

        // If the country code is correct, the focus is made on the serial number input
        countryCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Objects.requireNonNull(countryCode.getText()).length() == 2) {
                    // TODO : Make the focus to work again
                    countryCode.clearFocus();
                    serialNumber.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                start.setEnabled(Objects.requireNonNull(countryCode.getText()).length() == 2 && Objects.requireNonNull(serialNumber.getText()).toString().length() == 3);
            }
        });

        // If the serial code and the country code are correct, the start button is enabled
        serialNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (Objects.requireNonNull(countryCode.getText()).toString().length() == 2 && Objects.requireNonNull(serialNumber.getText()).toString().length() == 3) {
                    start.setEnabled(true);
                    serialNumber.clearFocus();
                }
            }
        });
    }

    /**
     * Ask to the user for confirmation of his input (country code and serial number). If he
     * confirms, the user is now asked for permissions
     */
    private void confirmSerialNumber() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getResources().getString(R.string.warning));
        builder.setMessage(String.format(getResources().getString(R.string.input_verification), Objects.requireNonNull(countryCode.getText()) + " " + Objects.requireNonNull(serialNumber.getText())));

        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {

            SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor defaultPreferencesEditor = defaultPreferences.edit();
            defaultPreferencesEditor.putBoolean("FirstStart", false);
            defaultPreferencesEditor.putString("countryCode", countryCode.getText().toString().toUpperCase());
            defaultPreferencesEditor.putString("serialNumber", serialNumber.getText().toString());
            defaultPreferencesEditor.apply();

            askForPermissions();
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        });

        // Cancel button (do nothing, only quit the dialog)
        AlertDialog dialog = builder.create();
        // Show dialog
        dialog.show();
    }

    /**
     * Ask the user for permissions and quit
     * TODO : Better handling of permission requests as in com.insalyon.dividoc.TagActivity#verifyCameraPermission()
     */
    public void askForPermissions() {

        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO
        };

        ActivityCompat.requestPermissions(this, permissions, 1);

        InitActivity.this.finish();
    }

    /**
     * Forces the user to input his country code and serial number
     */
    @Override
    public void onBackPressed() {
    }
}
