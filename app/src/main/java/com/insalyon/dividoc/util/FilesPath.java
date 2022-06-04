package com.insalyon.dividoc.util;

import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.FieldPosition;
import java.util.Objects;

public class FilesPath extends AppCompatActivity {

    private static final String appRootFolder = DiviContext.getAppContext().getFilesDir().getAbsolutePath();

    private static final String casesFolder = getAppRootFolder() + File.separator + "cases";

    private static final String newCaseFolder = getCasesFolder() + File.separator + "new_case";

    private static final String jsonDataFile = "data.json";

    private static final String htmlDataFile = "index.html";

    // Download directory / App name folder
    private static final String exportDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + DiviContext.getAppContext().getString(DiviContext.getAppContext().getApplicationInfo().labelRes);

    public static String getAppRootFolder() { return appRootFolder; }

    public static String getCasesFolder() { return casesFolder; }

    public static String getNewCaseFolder() { return newCaseFolder; }

    public static String getCaseAbsolutePath(String _case) { return getCasesFolder() + File.separator + _case; }

    public static String getJsonDataFile(String baseFolder) { return baseFolder + File.separator + jsonDataFile; }

    public static String getHtmlDataFile(String baseFolder) { return baseFolder + File.separator + htmlDataFile; }

    public static String getCaseImageFolder(String caseString) {

        if (new File(caseString).getParent() == null) {
            return getCasesFolder() + File.separator + caseString + File.separator + "images";
        } else if (new File(Objects.requireNonNull(new File(caseString).getParent())).getAbsolutePath().equals(getCasesFolder())) {
            return caseString + File.separator + "images";
        }
        return getCasesFolder() + File.separator + caseString + File.separator + "images";
    }

    public static String getCaseAudioFolder(String caseString) {

        if (new File(caseString).getParent() == null) {
            return getCasesFolder() + File.separator + caseString + File.separator + "audios";
        } else if (new File(Objects.requireNonNull(new File(caseString).getParent())).getAbsolutePath().equals(getCasesFolder())) {
            return caseString + File.separator + "audios";
        }
        return getCasesFolder() + File.separator + caseString + File.separator + "audios";
    }

    public static String getExportDirectory() { return exportDirectory; }

    /**
     * Deletes file or a directory recursively
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

    /*
      Copy folder source's files into destination folder
      @param source the source directory
      @param destination the destination directory
    public static void copyDirectory(File source, File destination) throws IOException {

        InputStream in;
        OutputStream out;

        if (source.isDirectory()) {
            File[] entries = source.listFiles();
            if (entries != null) {
                for (File entry : entries) {

                    in = new FileInputStream(entry);
                    out = new FileOutputStream(destination + File.separator + entry.toString().substring(entry.toString().lastIndexOf("/")+1));

                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }

                    in.close();
                    out.close();
                }
            }
        }
    }
    */
}
