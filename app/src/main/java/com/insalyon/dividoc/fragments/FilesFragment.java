package com.insalyon.dividoc.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.R;
import com.insalyon.dividoc.TagActivity;
import com.insalyon.dividoc.util.AppContext;
import com.insalyon.dividoc.util.FilesPath;
import com.insalyon.dividoc.util.Zip;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * Deletes the selected case AND the corresponding zip file
     * @param position of the item on the list
     */
    public void deleteCase(int position) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
        builder.setMessage(getResources().getString(R.string.delete_case_label))
                .setTitle(getResources().getString(R.string.warning))
                .setPositiveButton(getResources().getString(R.string.delete_label), (dialog, id) -> {
                    FilesPath.deleteDirectory(FilesPath.getCaseAbsolutePath(adapter.getItem(position).getName()));
                    FilesPath.deleteDirectory(Zip.exportDir + File.separator + "zip_" + adapter.getItem(position).getName() + ".zip");
                    this.onResume();
                })
                .setIcon(R.drawable.delete_trash)
                .setNegativeButton(getString(android.R.string.cancel), (dialogInterface, i) -> {})
                .show();
    }

    /**
     * Zip the case, then show and copy password of the archive to clipboard if super user mode is deactivated
     */
    public void zipAndShowPassword(int position) {

        // Generating the zip file
        Zip zip = new Zip(FilesPath.getCaseAbsolutePath(adapter.getItem(position).getName()), adapter.getItem(position).getName());

        // Show a dialog with the password if super user mode is deactivated
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppContext.getAppContext());
        if (!preferences.getBoolean("super_user_mode", true)) {

            // Copying password to clipboard
            ClipboardManager clipboard = (ClipboardManager) AppContext.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", zip.getPassword());
            clipboard.setPrimaryClip(clip);

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
            builder.setMessage(getResources().getString(R.string.password_is, zip.getPassword()))
                    .setTitle(getResources().getString(R.string.warning))
                    .setPositiveButton(getResources().getString(R.string.copy_password_and_proceed), (dialog, id) -> startActionSendActivity(zip))
                    .setIcon(R.drawable.super_user_mode)
                    .setNegativeButton(getString(android.R.string.cancel), (dialogInterface, i) -> {
                    })
                    .show();
        } else {
            startActionSendActivity(zip);
        }
    }

    /**
     * Send the generated zip file to ACTION_SEND intent through URI, after permissions were granted
     */
    private void startActionSendActivity(Zip zip) {

        // Referencing the generated zip through the URI in order to share it with the ACTION_SEND intent
        String uriString = zip.getOutputAbsolutePath(); //(FilesPath.getCaseAbsolutePath(adapter.getItem(position).getName()));
        Uri uri = FileProvider.getUriForFile(AppContext.getAppContext(), "com.insalyon.dividoc.fileprovider", new File(uriString));

        // Defining and starting the ACTION_SEND intent, granting the URI of the zip file
        Intent shareFile = new Intent(Intent.ACTION_SEND);
        shareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareFile.setType("application/zip");
        shareFile.putExtra(Intent.EXTRA_STREAM, uri);
        Intent chooser = Intent.createChooser(shareFile, getString(R.string.share_file));

        // https://stackoverflow.com/questions/45893294/permission-denial-with-file-provider-through-intent
        List<ResolveInfo> resInfoList = AppContext.getAppContext().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            AppContext.getAppContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivity(chooser);

    }
}
