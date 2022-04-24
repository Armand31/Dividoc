package com.insalyon.dividoc;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class InitActivity extends AppCompatActivity {

    private EditText countryCode;
    private EditText serialNumber;
    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        countryCode = (EditText) findViewById(R.id.country_code);
        serialNumber = (EditText) findViewById(R.id.serial_number);
        setTextChangeListeners();

        start = (Button) findViewById(R.id.start);
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
                if (countryCode.getText().toString().length() == 2) {
                    countryCode.clearFocus();
                    serialNumber.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                start.setEnabled(countryCode.getText().toString().length() == 2 && serialNumber.getText().toString().length() == 3);
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
                start.setEnabled(countryCode.getText().toString().length() == 2 && serialNumber.getText().toString().length() == 3);
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
        builder.setMessage(String.format(getResources().getString(R.string.input_verification), countryCode.getText().toString() + " " + serialNumber.getText().toString()));

        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {

            SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
            SharedPreferences.Editor preferencesEditor = preferences.edit();
            preferencesEditor.putBoolean("FirstStart", false);
            preferencesEditor.putString("countryCode", countryCode.getText().toString());
            preferencesEditor.putString("serialNumber", serialNumber.getText().toString());
            preferencesEditor.apply();

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
     * @TODO : Better handling of permission requests as in com.insalyon.dividoc.TagActivity#verifyCameraPermission()
     */
    public void askForPermissions() {

        String[] permissions = {
                Manifest.permission.CAMERA
        };
        int requestCode = 1;

        ActivityCompat.requestPermissions(this, permissions, requestCode);

        InitActivity.this.finish();
    }

    /**
     * Forces the user to input his country code and serial number
     */
    @Override
    public void onBackPressed() {
    }
}
