/*
 * https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example/40584425#40584425
 */

package com.insalyon.dividoc.fragments;

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

public class FilesFragmentAdapter extends RecyclerView.Adapter<FilesFragmentAdapter.ViewHolder> {

    private final List<File> casesList;
    private ItemClickListener mClickListener;

    public FilesFragmentAdapter(List<File> casesList) {

        this.casesList = casesList;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_cases, parent, false);
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

        holder.mItem = casesList.get(position);
        holder.mTextView.setText(casesList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return this.casesList.size();
    }

    File getItem(int id) {
        return casesList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     * Interface for an item click listener
     */
    public interface ItemClickListener {
        void editCase(int position);
        void deleteCase(int position);
        void shareCase(int adapterPosition);
    }

    /**
     * Class ViewHolder which extends from RecyclerView.ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mTextView;
        public final Button editCaseButton, deleteCaseButton, shareButton;
        public File mItem;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.list_fragment_text_view);
            editCaseButton = itemView.findViewById(R.id.edit_case_button);
            editCaseButton.setOnClickListener(this::onClickEditCase);
            deleteCaseButton = itemView.findViewById(R.id.delete_case_button);
            deleteCaseButton.setOnClickListener(this::onClickDeleteCase);
            shareButton = itemView.findViewById(R.id.share_case_button);
            shareButton.setOnClickListener(this::onClickShareCase);
        }

        public void onClickEditCase(View view) {
            if (mClickListener != null) {
                mClickListener.editCase(getAdapterPosition());
            }
        }

        public void onClickDeleteCase(View view) {
            if (mClickListener != null) {
                mClickListener.deleteCase(getAdapterPosition());
            }
        }

        public void onClickShareCase(View view) {
            if (mClickListener != null) {
                mClickListener.shareCase(getAdapterPosition());
            }
        }

        @Override
        public void onClick(View view) {

        }
    }
}
