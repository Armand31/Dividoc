package com.insalyon.dividoc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.security.SecureRandom;

public class Zip {

    private final String outputAbsolutePath;
    private String password;
    private final String filename;
    private final String outputAbsolutePathOCDCCropped;

    /**
     * Public constructor, zip the given file and output the path of the result
     * @param folderToZipAbsolutePath the path of the file / folder to zip
     */
    public Zip(String folderToZipAbsolutePath, String inputFilename, boolean toBeZipped) {

        this.filename = inputFilename;
        this.outputAbsolutePath = setOutputAbsolutePath();
        this.outputAbsolutePathOCDCCropped = outputAbsolutePath.substring(0, outputAbsolutePath.indexOf("_", outputAbsolutePath.indexOf("_") + 1));

        if (toBeZipped) {
            zipFolder(folderToZipAbsolutePath);
        }
    }

    /**
     * Sets the output path for the zipped file
     * @return the output path of the zipped file
     * TODO : Factorize with com.insalyon.dividoc.util.FilesPath#getZipPathFromName(java.lang.String)
     */
    private String setOutputAbsolutePath() {
        return FilesPath.getExportDirectory() + File.separator + this.filename + ".zip";
    }

    /**
     * Archiving the folder given in input
     * @param folderToZipAbsolutePath path of the folder to zip
     */
    private void zipFolder(String folderToZipAbsolutePath) {

        // Creating the zip file (empty)
        FilesPath.createDirectory(FilesPath.getExportDirectory(), "The export directory cannot be created. Aborting...");

        // Setting compression for the zipped file
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

        // Generate a password to protect the zip file, if super user mode is deactivated and saving it in shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppContext.getAppContext());
        String password;
        if (!preferences.getBoolean("super_user_mode", true)) {

            password = generatePassword();
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(password);

            // Saving the password and the expiration date in a custom shared preferences file, used to display information on the zip file
            SharedPreferences zipInfoSharedPrefs = AppContext.getAppContext().getSharedPreferences("zip_passwords", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = zipInfoSharedPrefs.edit();
            editor.putString(this.getOutputAbsolutePathOCDCCropped(), password);
            editor.apply();
        }

        // Zipping the folder
        try {
            ZipFile zipFile = new ZipFile(this.getOutputAbsolutePath());
            zipFile.addFolder(folderToZipAbsolutePath, zipParameters);
        } catch (ZipException e) {
            e.printStackTrace();
            Toast.makeText(AppContext.getAppContext(), "Unable to zip the files. Aborting...", Toast.LENGTH_SHORT).show();
        }
    }

    public String getOutputAbsolutePath() {
        return outputAbsolutePath;
    }

    public String getOutputAbsolutePathOCDCCropped() { return outputAbsolutePathOCDCCropped; }

    public String getPassword() { return this.password; }

    /**
     * Returns a password of length size
     * @return the generated password
     */
    private String generatePassword() {

        String chars = "0123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ!@$%&*";
        int PASSWORD_LENGTH = 12;
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        SecureRandom rand = new SecureRandom();

        for (int i = 0; i < PASSWORD_LENGTH; i++){
            int index = rand.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        this.password = password.toString();
        return password.toString();
    }
}
