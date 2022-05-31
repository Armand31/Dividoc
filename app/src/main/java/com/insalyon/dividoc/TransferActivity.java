package com.insalyon.dividoc;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

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

    private static final int MINUTES = 24 * 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        //Initialization
        setButtonListeners();

        File[] cases = new File(FilesPath.getCasesFolder()).listFiles();
        if (cases != null && cases.length > 0) {

            // Watermarking and resizing every image of every case
            for (File _case : cases) {
                for (File image : Objects.requireNonNull(new File(FilesPath.getCaseImageFolder(_case.getName())).listFiles())) {
                    resizeAndWatermarkImage(image, _case.getName());
                }
            }

            // Zipping cases files into one archive
            zipFiles();
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
     * Set button listeners
     */
    private void setButtonListeners() {

        Button returnButton = findViewById(R.id.return_transfer);
        returnButton.setOnClickListener(view -> this.finish());
    }

    /**
     * Zip the files
     */
    public void zipFiles() {

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

        // Generate a password to protect the zip file, if super user mode is deactivated
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean("super_user_mode", true)) {

            String password = getPassword(12);
            ((TextView) findViewById(R.id.password)).setText(password);
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(password);
        }

        // Creating the zip file
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(FilesPath.getExportDirectory() + File.separator + "zipFile_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".zip");
            zipFile.addFolder(FilesPath.getCasesFolder(), zipParameters);

            // Deletes the cases only if the zipping was successful
            try {
                FilesPath.deleteDirectory(new File(FilesPath.getCasesFolder()));
            } catch (IOException e) {
                Toast.makeText(this, "The cases could not be deleted but the zipping was successful", Toast.LENGTH_SHORT).show();
            }
        } catch (ZipException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to zip the files", Toast.LENGTH_SHORT).show();
        }
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

        System.out.println("Password : " + password);
        return password.toString();
    }

    public static int getHours() {
        return MINUTES / 60;
    }
}
