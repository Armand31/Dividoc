package com.insalyon.dividoc;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.insalyon.dividoc.services.ZipEncryptionJobService;
import com.insalyon.dividoc.util.AppContext;
import com.insalyon.dividoc.util.FilesPath;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TransferActivity extends AppCompatActivity {

    private static final int PASSWORD_LENGTH = 12;
    private static final int ENC_TIME = 24 * 60 * 60 * 1000; // Hours * Minutes * Seconds * Milliseconds


    /* ---------------------------------------------------------------------------------

        THE FILE WAS MODIFIED AND (LINES 158 AND 187 WITH GETEXPORTDIRECTORY) AND NEED A REVIEW

     --------------------------------------------------------------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        // Block the screenshots and video recording
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        //Initialization
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        String zipPathWithoutExtension = null;

        if (getIntent().getBooleanExtra("displayInfoOnly", false)) {
            zipPathWithoutExtension = getIntent().getStringExtra("zipPathWithoutExtension");
        } else {

            File[] cases = new File(FilesPath.getCasesFolder()).listFiles();
            if (cases != null && cases.length > 0) {

                // Watermarking and resizing every image of every case
                for (File _case : cases) {
                    for (File image : Objects.requireNonNull(new File(FilesPath.getCaseImageFolder(_case.getName())).listFiles())) {
                        resizeAndWatermarkImage(image, _case.getName());
                    }
                }

                // Zipping cases files into one archive
                zipPathWithoutExtension = zipFiles();

                // Encrypt generated zip after X time
                encryptZipFile(zipPathWithoutExtension);
            }
        }

        // Set the information on the view
        if (zipPathWithoutExtension != null) {
            setInformation(zipPathWithoutExtension);
        }
    }

    /**
     * Resizes and watermarks the given image
     */
    private void resizeAndWatermarkImage(File image, String watermarkText) {

        // Load the image in immutable form
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true; // Set the options of the bitmap to immutable
        Bitmap source = BitmapFactory.decodeFile(image.getAbsolutePath(), options);

        // Resizing options
        // TODO : Verify that the image is correctly resized (use real phone)
        int pictureMaxSize = 1500; //getResources().getInteger(R.integer.PictureMaxSize);
        int maxDimension = Math.max(source.getHeight(), source.getWidth());
        if (pictureMaxSize < maxDimension) {
            float ratio = (float) pictureMaxSize / maxDimension;
            source = Bitmap.createScaledBitmap(source, (int) (source.getWidth()*ratio), (int) (source.getHeight()*ratio), true);
        }

        // Watermarking options
        int h = source.getHeight();
        int x = 50;
        int y = h - x;
        int margin = 20; // margin of the box around the text

        Canvas canvas = new Canvas(source);
        canvas.drawBitmap(source, 0, 0, null);
        Paint paint = new Paint();
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.setColor(Color.WHITE);
        paint.getFontMetrics(fm);
        paint.setFakeBoldText(true);
        paint.setTextSize((float) (h * 0.03));
        paint.setAlpha(90);
        canvas.drawRect(x - margin, (float) (y - fm.top - margin - h * 0.03),
                x + paint.measureText(watermarkText) + margin, y + fm.bottom
                        + margin, paint);
        paint.setColor(Color.RED);
        canvas.drawText(watermarkText, x, y, paint);

        // Deleting the previous image and saving the new one, resized and watermarked
        try {
            if (image.exists()) {
                if (!image.delete()) { Toast.makeText(this, "Could not watermark the image", Toast.LENGTH_SHORT).show(); }
            }

            FileOutputStream out = new FileOutputStream(image);
            source.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Zip the files
     */
    public String zipFiles() {

        // Creates the export folder if it not exist
        // TODO : Factorization with com.insalyon.dividoc.TagActivity.createImageNewCaseFolder
        File exportDirectory = new File(FilesPath.getExportDirectory());
        if (!exportDirectory.exists()) {
            if (!exportDirectory.mkdirs()) {
                (Toast.makeText(this, "Export directory cannot be created", Toast.LENGTH_SHORT)).show();
            }
        }

        // Setting compression for the zipped file
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

        // Generate a password to protect the zip file, if super user mode is deactivated and saving it
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String password;
        if (!preferences.getBoolean("super_user_mode", true)) {

            password = getPassword(PASSWORD_LENGTH);
            ((TextView) findViewById(R.id.password)).setText(password);
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(password);
        } else {
            password = getString(R.string.no_password);
        }

        // Creating the zip file (empty)
        String dateAndTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String zipPathWithoutExtension = FilesPath.getExportDirectory() + File.separator + "zip_" + dateAndTime;

        // Renaming cases folder with date and time to avoid duplicate during zip extraction
        // TODO : Could collide anyway if 2 users zip the same second -> Correcting by adding country code and serial number
        String casesFolderNewName = FilesPath.getAppRootFolder() + File.separator + "cases_" + dateAndTime;
        //noinspection StatementWithEmptyBody
        if (!(new File(FilesPath.getCasesFolder())).renameTo(new File(casesFolderNewName))) {}

        try {
            ZipFile zipFile = new ZipFile(zipPathWithoutExtension + ".zip");
            // Zipping the cases folder
            zipFile.addFolder(casesFolderNewName, zipParameters);
        } catch (ZipException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to zip the files. Aborting...", Toast.LENGTH_SHORT).show();
            // Recovering the name of the cases folder to not loose cases files in main menu
            //noinspection StatementWithEmptyBody
            if(!(new File(casesFolderNewName)).renameTo(new File(FilesPath.getCasesFolder()))) {}
            this.finish();
        }

        // Saving the password and the expiration date in a custom shared preferences file, used to display information on the zip file
        SharedPreferences zipInfoSharedPrefs = AppContext.getAppContext().getSharedPreferences("zipInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = zipInfoSharedPrefs.edit();
        editor.putString(zipPathWithoutExtension + "_password", password);
        editor.putString(zipPathWithoutExtension + "_expiration_date", new SimpleDateFormat("MM dd yyyy - HH:mm", Locale.getDefault()).format(new Date(new Date().getTime() + ENC_TIME)));
        editor.apply();

        // At this point, deletes the cases only if the zipping was successful
        try {
            FilesPath.deleteDirectory(new File(casesFolderNewName));
        } catch (IOException e) {
            Toast.makeText(this, "The cases could not be deleted", Toast.LENGTH_SHORT).show();
        }

        return zipPathWithoutExtension;
    }

    /**
     * Returns a password of length size
     * @param length of the password
     * @return the generated password
     */
    public String getPassword(int length) {

        String chars = "0123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ!@$%&*";
        StringBuilder password = new StringBuilder(length);
        SecureRandom rand = new SecureRandom();

        for (int i = 0; i < length; i++){
            int index = rand.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }

    public static int getHours() {
        return ENC_TIME / 1000 / 60 / 60;
    }

    /**
     * Encrypt generated zip after X time
     */
    public void encryptZipFile(String zipPathWithoutExtension) {

        if (zipPathWithoutExtension == null) {
            return;
        }

        // Giving the path of the zip file
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("zipPathWithoutExtension", zipPathWithoutExtension);

        // Retrieving the next usable job ID to ensure that multiples jobs are not overwriting each other using the same job ID
        int jobID = 0; // job ID must be > 0
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.contains("jobID")) {
            // Getting the job ID if it exists in the shared preferences
            jobID = preferences.getInt("jobID", jobID);
        }
        // Saving the incremented job ID
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putInt("jobID", ++jobID);
        preferencesEditor.apply();

        // Setting job information
        ComponentName componentName = new ComponentName(this, ZipEncryptionJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(jobID, componentName)
                .setPersisted(true)
                .setMinimumLatency(ENC_TIME)
                .setExtras(bundle)
                .build();

        // Create a job the be executed
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    /**
     * Set the information on the view
     */
    private void setInformation(String zipPathWithoutExtension) {

        SharedPreferences zipInfoSharedPrefs = AppContext.getAppContext().getSharedPreferences("zipInfo", Context.MODE_PRIVATE);

        // Encrypted date
        ((TextView) findViewById(R.id.timeout)).setText(zipInfoSharedPrefs.getString(zipPathWithoutExtension + "_expiration_date", "Error"));

        // Password
        ((TextView) findViewById(R.id.password)).setText(zipInfoSharedPrefs.getString(zipPathWithoutExtension + "_password", "Error"));

        // Path
        String path = "Downloads > " + AppContext.getAppContext().getString(AppContext.getAppContext().getApplicationInfo().labelRes) + " > " + zipPathWithoutExtension.substring(zipPathWithoutExtension.lastIndexOf(File.separator) + 1) + ".zip";
        ((TextView) findViewById(R.id.pathView)).setText(path);
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
