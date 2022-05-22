package com.insalyon.dividoc;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.insalyon.dividoc.util.FilesPath;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.lingala.zip4j.core.ZipFile;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

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
            zipFiles();
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

        // Generate a password to protect the zip file
        String password = getPassword(12);
        ((TextView) findViewById(R.id.password)).setText(password);

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
        zipParameters.setPassword(password);

        // Creating the zip file
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(FilesPath.getExportDirectory() + File.separator + "zipFile.zip");
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
