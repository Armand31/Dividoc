package com.insalyon.dividoc.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.R;
import com.insalyon.dividoc.util.AppContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fragment class : https://developer.android.com/guide/fragments
 * Class used to manage the active files display (files which are not exported yet but still in the app memory)
 * It uses the RecyclerView class to do a dynamic list (Recommended by developer.android.com for our usage)
 * The RecyclerView class needs an adapter {@link FilesFragmentAdapter}
 */

public class ZipPasswordsFragment extends Fragment {

    ZipPasswordsFragmentAdapter adapter;
    RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        recyclerView = (RecyclerView) inflater.inflate(R.layout.zip_passwords_fragment, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        adapter = new ZipPasswordsFragmentAdapter(getZipPasswordsList());
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    /**
     * Get the list of passwords
     * @return the list of passwords (List<File> Object)
     */
    public Map<String, String> getZipPasswordsList() {

        SharedPreferences zipInfoSharedPrefs = AppContext.getAppContext().getSharedPreferences("zipInfo", Context.MODE_PRIVATE);
        Map<String, String> passwords = new HashMap<>();

        // Get all entries from shared preferences file
        Map<String, ?> allEntries = zipInfoSharedPrefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("_password")) {
                String key = (entry.getKey().substring(entry.getKey().lastIndexOf(File.separator) + 1)).replace("_password", "");
                Log.d("zip_passwords", key + " : " + entry.getValue().toString());
                passwords.put(key, entry.getValue().toString());
            }
        }

        return passwords;
    }
}
