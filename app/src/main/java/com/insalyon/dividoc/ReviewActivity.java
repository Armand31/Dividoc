package com.insalyon.dividoc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.insalyon.dividoc.util.FilesPath;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class ReviewActivity extends AppCompatActivity {

    private File workingCaseDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Initialization
        setButtonListeners();
        setData();
    }

    /**
     * Set listeners for the buttons
     */
    private void setButtonListeners() {

        // Delete case button listener
        FloatingActionButton deleteButton = findViewById(R.id.delete_button_review);
        deleteButton.setOnClickListener(view -> deleteCase());

        // Edit case button listener
        FloatingActionButton editButton = findViewById(R.id.edit_button_review);
        editButton.setOnClickListener(view -> {
            setResult(Activity.RESULT_CANCELED);
            this.finish();
        });

        // Save case button
        FloatingActionButton saveButton = findViewById(R.id.save_button_review);
        saveButton.setOnClickListener(view -> {
            try {
                saveCase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Sets the data on the view
     */
    private void setData() {
        ((TextView) findViewById(R.id.name)).setText(getIntent().getStringExtra("name"));
        ((TextView) findViewById(R.id.gender)).setText(getIntent().getStringExtra("gender"));
        ((TextView) findViewById(R.id.age)).setText(getIntent().getStringExtra("age"));
        ((TextView) findViewById(R.id.location)).setText(getIntent().getStringExtra("manual_location"));
        ((TextView) findViewById(R.id.additional_info)).setText(getIntent().getStringExtra("additional_information"));
        ((TextView) findViewById(R.id.tag)).setText(getIntent().getStringExtra("tag"));
    }

    /**
     * Deletes the generated case
     */
    private void deleteCase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this);
        builder.setMessage(getResources().getString(R.string.delete_case_label))
                .setTitle(getResources().getString(R.string.warning))
                .setPositiveButton(getResources().getString(R.string.delete_label), (dialog, id) -> {
                    try {
                        FilesPath.deleteDirectory(new File(getIntent().getStringExtra("workingImageDirectory")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finish();
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialogInterface, i) -> {
                })
                .show();
    }

    /**
     * Save the information into the phone's memory, in a dedicated folder
     */
    private void saveCase() throws IOException {

        this.workingCaseDirectory = new File(FilesPath.getCasesFolder() + File.separator + getIntent().getStringExtra("tag"));

        // If we are saving a new case
        if (getIntent().getBooleanExtra("newCase", true)) {

            // Renaming the new_case folder with the tag if this is a new case
            File newCaseFolder = new File(FilesPath.getNewCaseFolder());
            if (!newCaseFolder.renameTo(this.workingCaseDirectory)) {
                Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
            }
        }

        // Format data to json and writing it to file
        createJSON();

        // Format data to html and writing it to a file
        createHTML();

        setResult(Activity.RESULT_OK);
        this.finish();
    }

    /**
     * Creates a JSON file to save the data formatted in JSON
     */
    private void createJSON() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Save data to JSON object
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("Application", getString(R.string.app_name));
            jsonData.put("Tag", getIntent().getStringExtra("tag"));
            jsonData.put("CountryCode", preferences.getString("countryCode", "unknown"));
            jsonData.put("SerialNumber", Integer.parseInt(preferences.getString("serialNumber", "unknown")));
            jsonData.put("VSN", preferences.getInt("VSN", -1));
            jsonData.put("OCDC", getIntent().getStringExtra("OCDC"));
            jsonData.put("Name", getIntent().getStringExtra("name"));
            jsonData.put("Gender", getIntent().getStringExtra("gender"));
            jsonData.put("Age", getIntent().getStringExtra("age"));
            jsonData.put("HandwrittenLocation", getIntent().getStringExtra("manual_location"));
            jsonData.put("AdditionalInformation", getIntent().getStringExtra("additional_information"));
            jsonData.put("Latitude", getIntent().getDoubleExtra("latitude", 0.0));
            jsonData.put("Longitude", getIntent().getDoubleExtra("longitude", 0.0));
        } catch (JSONException e) {
            Toast.makeText(this, "Unable to format the data to JSON", Toast.LENGTH_SHORT).show();
        }

        // Create the json file and write data into it
        File jsonFile = new File(FilesPath.getJsonDataFile(this.workingCaseDirectory.getAbsolutePath()));
        try {
            FileOutputStream out = new FileOutputStream(jsonFile);
            out.write(jsonData.toString().getBytes(StandardCharsets.UTF_8));
            out.close();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to write data to json file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates an HTML file to save the formatted data
     */
    // TODO Improve the HTML file information
    private void createHTML() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // LinkedHashMap is an HashMap that preserve the insertion order
        LinkedHashMap<String, String> data = new LinkedHashMap<>();
        data.put("Application", getString(R.string.app_name));
        data.put("Tag", getIntent().getStringExtra("tag"));
        data.put("CountryCode", preferences.getString("countryCode", "unknown"));
        data.put("SerialNumber", preferences.getString("serialNumber", "unknown"));
        data.put("VSN", String.valueOf(preferences.getInt("VSN", -1)));
        data.put("OCDC", getIntent().getStringExtra("OCDC"));
        data.put("Name", getIntent().getStringExtra("name"));
        data.put("Gender", getIntent().getStringExtra("gender"));
        data.put("Age", getIntent().getStringExtra("age"));
        data.put("HandwrittenLocation", getIntent().getStringExtra("manual_location"));
        data.put("AdditionalInformation", getIntent().getStringExtra("additional_information"));
        data.put("Latitude", Double.toString(getIntent().getDoubleExtra("latitude", 0.0)));
        data.put("Longitude", Double.toString(getIntent().getDoubleExtra("longitude", 0.0)));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!DOCTYPE html>\n" +
                "<html lang=\"en-US\">\n" +
                "\t<head>\n" +
                "\t\t<title>Information file</title>\n" +
                "\t\t<meta charset=\"UTF-8\">\n" +
                "\t</head>\n" +
                "\t<body>\n");

        for (int i = 0; i < data.size(); i++) {
            String key = (String) data.keySet().toArray()[i];
            stringBuilder.append(String.format(
                   "\t\t<div>%s : %s</div>\n", key, data.get(key)
            ));
        }

        stringBuilder.append(
                "\t</body>\n" +
                "</html>"
        );

        // Create the html file and write data into it
        File htmlFile = new File(FilesPath.getHtmlDataFile(this.workingCaseDirectory.getAbsolutePath()));
        try {
            FileOutputStream out = new FileOutputStream(htmlFile);
            out.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
            out.close();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to write data to html file", Toast.LENGTH_SHORT).show();
        }

    }
}