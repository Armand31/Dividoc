package com.insalyon.dividoc.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.insalyon.dividoc.DecryptionActivity;
import com.insalyon.dividoc.MainActivity;
import com.insalyon.dividoc.R;
import com.insalyon.dividoc.TagActivity;
import com.insalyon.dividoc.TransferActivity;
import com.insalyon.dividoc.util.DiviContext;
import com.insalyon.dividoc.util.FilesPath;

/**
 * Fragment class : https://developer.android.com/guide/fragments
 * Class used to manage the exported files display
 * It uses the RecyclerView class to do a dynamic list (Recommended by developer.android.com for our usage)
 * The RecyclerView class needs an adapter {@link FilesFragmentAdapter}
 * When an item (file in the list) is clicked, the program launches the callback onActiveFilesInteraction(File item) in {@link MainActivity}
 */

public class ArchivesFragment extends Fragment implements ArchivesFragmentAdapter.ItemClickListener {

    ArchivesFragmentAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<File> fileList = getExportList();

        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.archives_fragment, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        adapter = new ArchivesFragmentAdapter(fileList);
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
            Collections.sort(archivesList, (f1, f2) -> f2.getName().compareTo(f1.getName()));
        }

        return archivesList;
    }

    /**
     * Launches decryption activity when an item of the list is clicked
     * @param position of the item on the list
     */
    public void onItemClick(int position) {

        /*
        File exportDirectory = new File(FilesPath.getExportDirectory());
        String path = adapter.getItem(position).getAbsolutePath();
        String extension = path.substring(path.lastIndexOf("."));
        path = path.replaceFirst("[.][^.]+$", "");
        String FileName = path.replace(exportDirectory.getAbsolutePath() + File.separator, "");

        switch (extension) {
            case (".zip"):
                Intent activity = new Intent(DiviContext.getAppContext(), TransferActivity.class);
                activity.putExtra("toZip", false);
                activity.putExtra("zipName",FileName );
                startActivity(activity);
                break;
             case (".enc"):
                Intent intent = new Intent(this.context, DecryptionActivity.class);
                Log.i(MainActivity.DEBUG_PREFIX,"Name : "+adapter.getItem(position).getName());
                intent.putExtra("FileName", adapter.getItem(position).getName());
                startActivity(intent);
                break;
            default :
                Toast.makeText(DiviContext.getAppContext(), "bruh", Toast.LENGTH_SHORT).show();
                break;
        }
        */

        Intent decryptionActivity = new Intent(DiviContext.getAppContext(), DecryptionActivity.class);
        startActivity(decryptionActivity);
    }
}
