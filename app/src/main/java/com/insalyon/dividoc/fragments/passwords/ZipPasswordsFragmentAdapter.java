package com.insalyon.dividoc.fragments.passwords;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.R;

import java.util.LinkedHashMap;

public class ZipPasswordsFragmentAdapter extends RecyclerView.Adapter<ZipPasswordsFragmentAdapter.ViewHolder> {

    private LinkedHashMap<String, String> zipPasswordsList;
    private ZipPasswordsFragmentAdapter.ItemClickListener mClickListener;

    public ZipPasswordsFragmentAdapter(LinkedHashMap<String, String> zipPasswordsList) {

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
        String display = "<b>" + key.toString() + "</b> : " + zipPasswordsList.get(key);
        holder.mTextView.setText(Html.fromHtml(display));
    }

    @Override
    public int getItemCount() {
        return zipPasswordsList.size();
    }

    public String getItem(int id) {
        return this.zipPasswordsList.keySet().toArray()[id].toString();
    }

    void setClickListener(ZipPasswordsFragmentAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     * Interface for an item click listener
     */
    public interface ItemClickListener {
        void copyPasswordToClipboard(int position);
    }

    /**
     * Class ViewHolder which extends from RecyclerView.ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.list_passwords_text_view);
            Button copyPassword = itemView.findViewById(R.id.copy_password);
            copyPassword.setOnClickListener(this::copyPasswordToClipboard);
        }

        public void copyPasswordToClipboard(View view) {
            if (mClickListener != null) {
                mClickListener.copyPasswordToClipboard(getAdapterPosition());
            }
        }

        @Override
        public void onClick(View view) {

        }
    }
}
