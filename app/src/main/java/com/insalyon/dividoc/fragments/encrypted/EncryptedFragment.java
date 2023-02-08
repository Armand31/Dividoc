package com.insalyon.dividoc.fragments.encrypted;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.MainActivity;
import com.insalyon.dividoc.R;
import com.insalyon.dividoc.TransferActivity;
import com.insalyon.dividoc.util.AppContext;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment class : https://developer.android.com/guide/fragments
 * Class used to manage the exported files display
 * It uses the RecyclerView class to do a dynamic list (Recommended by developer.android.com for our usage)
 * The RecyclerView class needs an adapter {@link EncryptedFragmentAdapter}
 * When an item (file in the list) is clicked, the program launches the callback onActiveFilesInteraction(File item) in {@link MainActivity}
 */

public class EncryptedFragment extends Fragment implements EncryptedFragmentAdapter.ItemClickListener {

    EncryptedFragmentAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<File> fileList = getExportList();

        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.archives_fragment, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        adapter = new EncryptedFragmentAdapter(fileList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    /**
     * Get the list of the exported files
     * @return the list of archives (List<File> Object)
     */
    public List<File> getExportList() {

        File archivesFile = new File(FilesPath.getExportDirectory());
        File[] archivesArray = archivesFile.listFiles();
        List<File> archivesList = new ArrayList<>();

        if (archivesArray != null) {
            archivesList.addAll(Arrays.asList(archivesArray));

            // Comparator is used to sort cases, in alphabetical order here (getName() method)
            // noinspection Comparator
            //Collections.sort(archivesList, (f1, f2) -> f2.getName().compareTo(f1.getName()));
        }

        return archivesList;
    }

    /**
     * Launches decryption activity when an item of the list is clicked
     * @param position of the item on the list
     */
    public void onItemClick(int position) {

        Intent transferActivity = new Intent(AppContext.getAppContext(), TransferActivity.class);
        transferActivity.putExtra("displayInfoOnly", true);
        transferActivity.putExtra("zipPathWithoutExtension", FilesPath.getZipPathFromName(adapter.getItem(position).getName()).replaceFirst("[.][^.]+$", ""));
        startActivity(transferActivity);
    }
}
