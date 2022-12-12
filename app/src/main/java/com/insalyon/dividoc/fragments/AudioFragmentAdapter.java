package com.insalyon.dividoc.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.R;
import com.insalyon.dividoc.util.AppContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioFragmentAdapter extends RecyclerView.Adapter<AudioFragmentAdapter.ViewHolder> {

    private final List<File> audioList;
    private final List<Button> playButton;
    private AudioFragmentAdapter.ItemClickListener mClickListener;

    public AudioFragmentAdapter(List<File> audioList) {

        this.audioList = audioList;
        playButton = new ArrayList<>();
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_audio, parent, false);
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

        holder.mItem = audioList.get(position);
        String display = AppContext.getAppContext().getString(R.string.audio) + " " + position;
        holder.mTextView.setText(display);
        playButton.add(holder.playButton);
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    File getItem(int id) {
        return audioList.get(id);
    }

    public Button getPlayButton(int id) {

        return playButton.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     * Interface for an item click listener
     */
    public interface ItemClickListener {
        void startAudio(int position);
        void deleteAudio(int position);
    }

    /**
     * Class ViewHolder which extends from RecyclerView.ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mTextView;
        public final Button playButton, deleteButton;
        public File mItem;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.list_audio_text_view);
            playButton = itemView.findViewById(R.id.play_audio);
            playButton.setOnClickListener(this::onClickPlayAudio);
            deleteButton = itemView.findViewById(R.id.delete_audio_button);
            deleteButton.setOnClickListener(this::onClickDeleteAudio);
        }

        public void onClickPlayAudio(View view) {
            if (mClickListener != null) {
                mClickListener.startAudio(getAdapterPosition());
            }
        }

        public void onClickDeleteAudio(View view) {
            if (mClickListener != null) {
                mClickListener.deleteAudio(getAdapterPosition());
            }
        }

        @Override
        public void onClick(View view) {

        }
    }
}
