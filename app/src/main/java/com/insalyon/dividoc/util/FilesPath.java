package com.insalyon.dividoc.util;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.insalyon.dividoc.MainActivity;

import java.io.File;
import java.io.IOException;

public class FilesPath extends AppCompatActivity {

    private static final String appRootFolder = MainActivity.getAppContext().getFilesDir().getAbsolutePath();

    private static final String newCaseFolder = getAppRootFolder() + File.separator + "new_case";

    private static final String newCaseImageFolder = getNewCaseFolder() + File.separator + "images";

    public static String getAppRootFolder() {
        return appRootFolder;
    }

    public static String getNewCaseFolder() {
        return newCaseFolder;
    }

    public static String getNewCaseImageFolder() {
        return newCaseImageFolder;
    }

    /**
     * Deletes a directory recursively
     * The delete() method from File Class can delete a directory only if it's empty, that's why we need the deleteDirectory method
     * @param file the directory we want to delete
     */
    public static void deleteDirectory(File file) throws IOException {

        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete " + file);
        }
    }
}
