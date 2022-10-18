package com.insalyon.dividoc.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.MainActivity;
import com.insalyon.dividoc.R;
import com.insalyon.dividoc.TagActivity;
import com.insalyon.dividoc.util.AppContext;
import com.insalyon.dividoc.util.FilesPath;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment class : https://developer.android.com/guide/fragments
 * Class used to manage the active files display (files which are not exported yet but still in the app memory)
 * It uses the RecyclerView class to do a dynamic list (Recommended by developer.android.com for our usage)
 * The RecyclerView class needs an adapter {@link FilesFragmentAdapter}
 */

public class FilesFragment extends Fragment implements FilesFragmentAdapter.ItemClickListener {

    FilesFragmentAdapter adapter;
    RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<File> caseList = getCaseList();

        recyclerView = (RecyclerView) inflater.inflate(R.layout.files_fragment, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        adapter = new FilesFragmentAdapter(caseList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    /**
     * Reloads the view
     */
    public void onResume() {
        List<File> caseList = getCaseList();
        adapter = new FilesFragmentAdapter(caseList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        super.onResume();
    }

    /**
     * Get the list of the cases
     * @return the list of the cases (List<File> Object)
     */
    public List<File> getCaseList() {

        File casesFile = new File(FilesPath.getCasesFolder());
        File[] casesArray = casesFile.listFiles();
        List<File> casesList = new ArrayList<>();

        if (casesArray != null) {
            casesList.addAll(Arrays.asList(casesArray));

            // Comparator is used to sort cases, in alphabetical order here (getName() method)
            // noinspection ComparatorCombinators
            //Collections.sort(casesList, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        }

        return casesList;
    }

    /**
     * Launches tag activity when an item of the list is clicked
     * @param position of the item on the list
     */
    public void editCase(int position) {

        Intent editIntent = new Intent(AppContext.getAppContext(), TagActivity.class);
        editIntent.putExtra("newCase", false);
        editIntent.putExtra("workingDirectory", FilesPath.getCaseAbsolutePath(adapter.getItem(position).getName()));
        startActivity(editIntent);
    }

    /**
     * Deletes the selected case
     * @param position of the item on the list
     */
    public void deleteCase(int position) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
        builder.setMessage(getResources().getString(R.string.delete_case_label))
                .setTitle(getResources().getString(R.string.warning))
                .setPositiveButton(getResources().getString(R.string.delete_label), (dialog, id) -> {
                    FilesPath.deleteDirectory(FilesPath.getCaseAbsolutePath(adapter.getItem(position).getName()));
                    this.onResume();
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialogInterface, i) -> {})
                .show();
    }

    public void shareCase(int position) {

        //Log.d("SHARE", FileSelectActivity.getMimeType(FilesPath.getCaseAbsolutePath(adapter.getItem(position).getName())));

        String uriString = zipFolder(FilesPath.getCaseAbsolutePath(adapter.getItem(position).getName()));
        Log.d("SHARE", "uriString" + uriString);
        /*
        if (uriString != null) {
            Intent resultIntent = new Intent();
            resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            resultIntent.setDataAndType(Uri.parse(uriString), "application/zip");
            //MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
            startActivity(resultIntent);
        }
        */

//        Intent resultIntent = new Intent("com.insalyon.dividoc.ACTION_RETURN_FILE");
//        // Get the files/ subdirectory of internal storage
//        File privateRootDir = new File(FilesPath.getAppRootFolder());
//        // Get the files/images subdirectory;
//        File imagesDir = new File(privateRootDir, "images");
//        // Get the files in the images subdirectory
//        File[] imageFiles = imagesDir.listFiles();
//        // Set the Activity's result to null to begin with
//        //setResult(Activity.RESULT_CANCELED, null);

        Uri uri = FileProvider.getUriForFile(AppContext.getAppContext(), "com.insalyon.dividoc.fileprovider", new File(uriString));

        Intent shareFile = new Intent(Intent.ACTION_SEND);
        //File fileWithinMyDir = new File(FilesPath.getCaseAbsolutePath(adapter.getItem(position).getName()));
        shareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        shareFile.setType("application/zip");
        //String relativeUri = uriString.replace(FilesPath.getAppRootFolder() + "/", "");
        shareFile.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareFile, "Share File"));

//        if(fileWithinMyDir.exists()) {
//
//        }


    }

    private static final int PASSWORD_LENGTH = 12;

    /**
     * TODO: Factorize with the zipFiles in TransferActivity and get the function into FilesPath
     */
    public String zipFolder(String casePath) {

        File exportDirectory = new File(FilesPath.getExportDirectory());
        if (!exportDirectory.exists()) {
            if (!exportDirectory.mkdirs()) {
                (Toast.makeText(AppContext.getAppContext(), "Export directory cannot be created", Toast.LENGTH_SHORT)).show();
            }
        }

        // Setting compression for the zipped file
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

        // Generate a password to protect the zip file, if super user mode is deactivated and saving it
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppContext.getAppContext());
        String password;
        if (!preferences.getBoolean("super_user_mode", true)) {

            password = getPassword(PASSWORD_LENGTH);
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(password);
        } else {
            password = getString(R.string.no_password);
        }

        // Creating the zip file (empty)
        String dateAndTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        //String zipPathWithoutExtension = FilesPath.getExportDirectory() + File.separator + "zip_" + dateAndTime;
        String zipPathWithoutExtension = FilesPath.getCasesFolder() + File.separator + "zip_" + dateAndTime;

        // Renaming cases folder with date and time to avoid duplicate during zip extraction
        String casesFolderNewName = casePath + dateAndTime;
        //noinspection StatementWithEmptyBody
        if (!(new File(casePath)).renameTo(new File(casesFolderNewName))) {}

        try {
            ZipFile zipFile = new ZipFile(zipPathWithoutExtension + ".zip");
            // Zipping the cases folder
            zipFile.addFolder(casesFolderNewName, zipParameters);
        } catch (ZipException e) {
            e.printStackTrace();
            Toast.makeText(AppContext.getAppContext(), "Unable to zip the files. Aborting...", Toast.LENGTH_SHORT).show();
            // Recovering the name of the cases folder to not loose cases files in main menu
            //noinspection StatementWithEmptyBody
            if(!(new File(casesFolderNewName)).renameTo(new File(FilesPath.getCasesFolder()))) {}
        }

        // Saving the password and the expiration date in a custom shared preferences file, used to display information on the zip file
        SharedPreferences zipInfoSharedPrefs = AppContext.getAppContext().getSharedPreferences("zipInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = zipInfoSharedPrefs.edit();
        editor.putString(zipPathWithoutExtension + "_password", password);
        editor.apply();

        // At this point, deletes the cases only if the zipping was successful
        try {
            FilesPath.deleteDirectory(new File(casesFolderNewName));
        } catch (IOException e) {
            Toast.makeText(AppContext.getAppContext(), "The cases could not be deleted", Toast.LENGTH_SHORT).show();
        }

        return zipPathWithoutExtension + ".zip";
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
}
