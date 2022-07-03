package com.insalyon.dividoc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DisplayImageActivity extends AppCompatActivity {

    String picturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        // Block the screenshots and video recording
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );

        // Initialization
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setButtonListeners();
        setImage();
    }

    /**
     * Set listeners for the buttons
     */
    private void setButtonListeners() {

        // Take picture if the camera button is clicked
        Button deletePicture = findViewById(R.id.delete_picture);
        deletePicture.setOnClickListener(click -> deletePicture());
    }

    /**
     * Displays the image
     */
    private void setImage() {

        this.picturePath = getIntent().getStringExtra("picturePath");
        Bitmap bmp = BitmapFactory.decodeFile(this.picturePath);
        ((ImageView) findViewById(R.id.image_display)).setImageBitmap(bmp);
    }

    /**
     * Shows a builder to ask if the user really wants to delete the case and deletes the picture
     * if the answer from the user is positive
     */
    private void deletePicture() {

        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayImageActivity.this);
        builder.setMessage(getResources().getString(R.string.delete_picture_warning))
                .setTitle(getResources().getString(R.string.warning))
                .setPositiveButton(getResources().getString(R.string.delete_label), (dialog, id) -> {
                    try {
                        FilesPath.deleteDirectory(new File(this.picturePath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.finish();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {}).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
