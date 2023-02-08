/*
 * https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example/40584425#40584425
 */

package com.insalyon.dividoc.fragments.encrypted;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import com.insalyon.dividoc.R;

public class EncryptedFragmentAdapter extends RecyclerView.Adapter<EncryptedFragmentAdapter.ViewHolder> {

    private final List<File> archivesList;
    private ItemClickListener mClickListener;

    public EncryptedFragmentAdapter(List<File> archivesList) {

        this.archivesList = archivesList;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_archives, parent, false);
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

        holder.mItem = archivesList.get(position);
        holder.mTextView.setText(archivesList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return this.archivesList.size();
    }

    public File getItem(int id) {
        return archivesList.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     * Interface for an item click listener
     */
    public interface ItemClickListener {
        void onItemClick(int position);
    }

    /**
     * Class ViewHolder which extends from RecyclerView.ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mTextView;
        public final Button seeInfo;
        public File mItem;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.list_fragment_text_view);
            seeInfo = itemView.findViewById(R.id.view_file_info);
            seeInfo.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(getAdapterPosition());
            }
        }
    }
}
