package com.insalyon.dividoc;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GalleryActivity extends AppCompatActivity {

    private String workingImageDirectory;
    private final int MAX_PHOTOS_ALLOWED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Initialization
        this.workingImageDirectory = getIntent().getStringExtra("workingImageDirectory");
        setButtonListeners(registration());
        ((TextView) findViewById(R.id.max_photo_info)).setText(getResources().getString(R.string.max_photos_gallery, MAX_PHOTOS_ALLOWED));
    }

    /**
     * Set listeners for the buttons
     */
    private void setButtonListeners(ActivityResultLauncher<Uri> registerForActivityResult) {

        // Finish the activity when return button is clicked
        FloatingActionButton backButton = findViewById(R.id.return_gallery);
        backButton.setOnClickListener(click -> this.finish());

        // Take picture if the camera button is clicked
        FloatingActionButton takePictureButton = findViewById(R.id.take_photo_button);
        takePictureButton.setOnClickListener(click -> {
            // The user can take a photo. It is useless to ask for permission as the user already
            // have accepted the permission to land here
            if (getNumberOfPictures() < MAX_PHOTOS_ALLOWED) {
                dispatchTakePictureIntent(registerForActivityResult);
            } else {
                // Warn the user that he is allowed to take maximum MAX_PHOTOS_ALLOWED images
                makeInfoMessageBlinks();
                takePictureButton.setEnabled(false);
            }
        });
    }

    /**
     * Opens camera and saves the photo at the specified URI upon success
     */
    private void dispatchTakePictureIntent(ActivityResultLauncher<Uri> registerForActivityResult) {

        File pictureFile = new File(workingImageDirectory + File.separator + "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg");
        Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", pictureFile);

        registerForActivityResult.launch(photoUri);
    }

    /**
     * The registerForActivityResult must be declared before the activity is started otherwise
     * an error is triggered and the activity is crashing
     * See https://stackoverflow.com/questions/64476827/how-to-resolve-the-error-lifecycleowners-must-call-register-before-they-are-sta
     */
    private ActivityResultLauncher<Uri> registration() {

        // Callback used to launch camera and trigger action on success and/or failure
        // Documentation here : https://developer.android.com/training/basics/intents/result
        // StackOverflow here : https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
        // About TakePicture() : https://stackoverflow.com/questions/61941959/activityresultcontracts-takepicture
        return registerForActivityResult(
                new ActivityResultContracts.TakePicture(), isSuccess -> {
                    if (!isSuccess) {
                        this.finish();
                    }
                }
        );
    }

    /**
     * Get the number of pictures taken for a specified case (number of files in the images/ subdirectory)
     * @return the number of pictures
     */
    private int getNumberOfPictures() {

        String[] pictures = new File(this.workingImageDirectory).list();

        if (pictures != null) {
            return pictures.length;
        } else {
            Toast.makeText(this, getString(R.string.cannot_access_pictures_dir), Toast.LENGTH_SHORT).show();
            return MAX_PHOTOS_ALLOWED;
        }
    }

    /**
     * Makes the informational message blink
     */
    private void makeInfoMessageBlinks() {

        TextView maxPhotoInfo = findViewById(R.id.max_photo_info);

        // Blinking animation
        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(100); // duration - a second
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

        // Color animation
        Integer colorFrom = ContextCompat.getColor(this, R.color.text);
        Integer colorTo = ContextCompat.getColor(this, R.color.warning);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(valueAnimator -> maxPhotoInfo.setTextColor((Integer) valueAnimator.getAnimatedValue()));
        animation.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));

        // Stop animation after 2000 milliseconds
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            maxPhotoInfo.clearAnimation();
            colorAnimation.reverse();
            colorAnimation.end();
        }, 1000);

        // Start blinking and color animations
        maxPhotoInfo.startAnimation(animation);
        colorAnimation.start();
    }

    /**
     * Reloads the pictures' grid
     */
    @Override
    public void onResume() {

        super.onResume();

        ((GridView) findViewById(R.id.grid_view)).setAdapter(new ImageAdapter(this));
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    /**
     * Public class used as an adapter for the grid view
     */
    private class ImageAdapter extends BaseAdapter {

        private final Context context;
        private final File[] fileList;

        public ImageAdapter(Context context) {
            this.context = context;
            this.fileList = new File(workingImageDirectory).listFiles();
        }

        @Override
        public int getCount() {
            return fileList.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            ImageView imageView;
            if (view == null) {
                imageView = new ImageView(this.context);
                // TODO Set dynamic width and height
                imageView.setLayoutParams(new GridView.LayoutParams(400, 400));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) view;
            }

            // Launch new activity when clicking an image in the gallery
            Bitmap bmp = BitmapFactory.decodeFile(fileList[i].getAbsolutePath());
            imageView.setImageBitmap(bmp);
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(GalleryActivity.this, DisplayImageActivity.class);
                intent.putExtra("picturePath", fileList[i].getAbsolutePath());
                startActivity(intent);
            });

            return imageView;
        }
    }
}