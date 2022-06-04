package com.insalyon.dividoc.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.R;
import com.insalyon.dividoc.TagActivity;
import com.insalyon.dividoc.util.DiviContext;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fragment class : https://developer.android.com/guide/fragments
 * Class used to manage the active files display (files which are not exported yet but still in the app memory)
 * It uses the RecyclerView class to do a dynamic list (Recommended by developer.android.com for our usage)
 * The RecyclerView class needs an adapter {@link FilesFragmentAdapter}
 */

public class FilesFragment extends Fragment implements FilesFragmentAdapter.ItemClickListener {

    FilesFragmentAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<File> caseList = getCaseList();

        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.files_fragment, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        adapter = new FilesFragmentAdapter(caseList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        return recyclerView;
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
            Collections.sort(casesList, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        }

        return casesList;
    }

    /**
     * Launches tag activity when an item of the list is clicked
     * @param position of the item on the list
     */
    public void onItemClick(int position) {

        Intent editIntent = new Intent(DiviContext.getAppContext(), TagActivity.class);
        editIntent.putExtra("newCase", false);
        editIntent.putExtra("workingDirectory", FilesPath.getCaseAbsolutePath(adapter.getItem(position).getName()));
        startActivity(editIntent);
    }
}
