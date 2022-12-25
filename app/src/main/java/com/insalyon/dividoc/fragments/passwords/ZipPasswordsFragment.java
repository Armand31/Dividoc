package com.insalyon.dividoc.fragments.passwords;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.R;
import com.insalyon.dividoc.fragments.files.FilesFragmentAdapter;
import com.insalyon.dividoc.util.AppContext;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Fragment class : https://developer.android.com/guide/fragments
 * Class used to manage the active files display (files which are not exported yet but still in the app memory)
 * It uses the RecyclerView class to do a dynamic list (Recommended by developer.android.com for our usage)
 * The RecyclerView class needs an adapter {@link FilesFragmentAdapter}
 */

public class ZipPasswordsFragment extends Fragment implements ZipPasswordsFragmentAdapter.ItemClickListener{

    ZipPasswordsFragmentAdapter adapter;
    RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        recyclerView = (RecyclerView) inflater.inflate(R.layout.zip_passwords_fragment, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        adapter = new ZipPasswordsFragmentAdapter(getZipPasswordsList());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    /**
     * Get the list of passwords, sorted by VSN
     * @return the list of the zip file / password pairs
     */
    public LinkedHashMap<String, String> getZipPasswordsList() {

        SharedPreferences zipInfoSharedPrefs = AppContext.getAppContext().getSharedPreferences("zip_passwords", Context.MODE_PRIVATE);
        LinkedHashMap<String, String> passwords = new LinkedHashMap<>(); // Final map
        Map<Integer, String> indexedFromVSN = new HashMap<>(); // Used to keep track of the index of each file according to the VSN

        // Get all entries from shared preferences file
        Map<String, ?> allEntries = zipInfoSharedPrefs.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = (entry.getKey().substring(entry.getKey().lastIndexOf(File.separator) + 1));

            // Getting VSN number from file name and put it in the indexedFromVSN map
            String indexString = key.replace(".zip", "").substring(key.indexOf("_") + 1);
            int index = Integer.parseInt(indexString.substring(0, indexString.indexOf("_")));

            indexedFromVSN.put(index, entry.getKey());
        }

        // By going in order of indexes from indexedFromVSN, we result in putting the good order in the final map
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            indexedFromVSN.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> passwords.put(entry.getValue().substring(entry.getValue().lastIndexOf(File.separator) + 1), Objects.requireNonNull(allEntries.get(entry.getValue())).toString()));
        }

        return passwords;
    }

    /**
     * Copies the password to the clipboard when it is clicked
     */
    public void copyPasswordToClipboard(int position) {

        SharedPreferences zipInfoSharedPrefs = AppContext.getAppContext().getSharedPreferences("zip_passwords", Context.MODE_PRIVATE);
        String password = zipInfoSharedPrefs.getString(FilesPath.getZipPathFromName(adapter.getItem(position).replace(".zip", "")), "Copy error");

        // Copying password to clipboard
        ClipboardManager clipboard = (ClipboardManager) AppContext.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("password", password);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(AppContext.getAppContext(), R.string.password_was_copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }
}
