package com.insalyon.dividoc.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.R;

import java.util.Map;

public class ZipPasswordsFragmentAdapter extends RecyclerView.Adapter<ZipPasswordsFragmentAdapter.ViewHolder> {

    Map<String, String> zipPasswordsList;

    public ZipPasswordsFragmentAdapter(Map<String, String> zipPasswordsList) {

        this.zipPasswordsList = zipPasswordsList;
    }

    /**
     * Creates a ViewHolder which manage the item views, invoked by the layout manager
     * Inflates the row layout from xml when needed
     * @param parent Parent view where the item are grouped
     * @param viewType The type of view
     * @return the ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_zip_passwords, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Manage all item view by using the ViewHolders, invoked by the layout manager
     * Binds the data to the TextView in each row
     * https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onBindViewHolder(VH,%20int)
     * @param holder   View holder for each item
     * @param position Position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Object key = zipPasswordsList.keySet().toArray()[position];
        String display = key.toString() + " :\n" + zipPasswordsList.get(key);
        holder.mTextView.setText(display);
    }

    @Override
    public int getItemCount() {
        return zipPasswordsList.size();
    }

    /**
     * Class ViewHolder which extends from RecyclerView.ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.list_passwords_text_view);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
