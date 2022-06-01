package com.insalyon.dividoc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.io.IOException;

public class DisplayImageActivity extends AppCompatActivity {

    String picturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        // Block the screenshots and video recording
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );

        // Initialization
        setButtonListeners();
        setImage();
    }

    /**
     * Set listeners for the buttons
     */
    private void setButtonListeners() {

        // Finish the activity when return button is clicked
        FloatingActionButton backButton = findViewById(R.id.return_gallery);
        backButton.setOnClickListener(click -> this.finish());

        // Take picture if the camera button is clicked
        FloatingActionButton deletePicture = findViewById(R.id.delete_picture);
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
}
