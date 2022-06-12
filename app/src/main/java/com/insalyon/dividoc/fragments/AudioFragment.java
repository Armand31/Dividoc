package com.insalyon.dividoc.fragments;

import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.insalyon.dividoc.MainActivity;
import com.insalyon.dividoc.R;
import com.insalyon.dividoc.util.AppContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment class : https://developer.android.com/guide/fragments
 * Class used to manage the exported files display
 * It uses the RecyclerView class to do a dynamic list (Recommended by developer.android.com for our usage)
 * The RecyclerView class needs an adapter {@link FilesFragmentAdapter}
 * When an item (file in the list) is clicked, the program launches the callback onActiveFilesInteraction(File item) in {@link MainActivity}
 */

public class AudioFragment extends Fragment implements AudioFragmentAdapter.ItemClickListener {

    private AudioFragmentAdapter adapter;
    private String workingAudioDirectory;
    private MediaPlayer mediaPlayer;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        assert this.getArguments() != null;
        workingAudioDirectory = this.getArguments().getString("workingAudioDirectory");
        List<File> audioList = getAudioList(this.workingAudioDirectory);

        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.audio_fragment, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        adapter = new AudioFragmentAdapter(audioList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        return recyclerView;
    }

    /**
     * Get the list of the audio files
     * @return the list of the audio files
     */
    public List<File> getAudioList(String workingAudioDirectory) {

        File audioFiles = new File(workingAudioDirectory);
        File[] audioArray = audioFiles.listFiles();
        List<File> audioList = new ArrayList<>();

        if (audioArray != null) {
            audioList.addAll(Arrays.asList(audioArray));

            // Comparator is used to sort cases, in alphabetical order here (getName() method)
            // noinspection ComparatorCombinators
            //Collections.sort(audioList, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        }

        return audioList;
    }

    public void onItemClick(int position) {

        Button record;

        // Stop other audio file if one is already playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

            int numberOfAudio = adapter.getItemCount();
            for (int i = 0 ; i < numberOfAudio ; i++) {
                // Change button play to pause
                record = adapter.getPlayButton(i);
                record.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play, 0, 0, 0);
            }
        }

        /*
         Play the audio file that was clicked
         */

        // Play the audio file
        mediaPlayer = new MediaPlayer();
        String filePath = this.workingAudioDirectory + File.separator + this.adapter.getItem(position).getName();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(AppContext.getAppContext(), "Audio file could not be played", Toast.LENGTH_SHORT).show();
        }

        // Get audio file duration
        Uri uri = Uri.parse(filePath);
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(AppContext.getAppContext(), uri);
        int duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        // Change the text view
        /*
        TextView textView = this.adapter.getItem(position).get;
        int color1 = (MaterialColors.getColor(DiviContext.getAppContext(), com.google.android.material.R.attr.colorSecondary, Color.CYAN));
        int color2 = (MaterialColors.getColor(DiviContext.getAppContext(), com.google.android.material.R.attr.color, Color.CYAN));

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), color1, color2);
        colorAnimation.setDuration(1000); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                textView.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();

         */
        /*
        AnimationDrawable animationDrawable = new AnimationDrawable();
        animationDrawable.setEnterFadeDuration(1000);
        animationDrawable.setTint(color1);

        textView.setBackground(animationDrawable);
        animationDrawable.start();*/
        /*
        Canvas canvas = new Canvas();
        canvas.save();

        int color1 = (MaterialColors.getColor(DiviContext.getAppContext(), com.google.android.material.R.attr.colorSecondary, Color.CYAN));
        int color2 = (MaterialColors.getColor(DiviContext.getAppContext(), com.google.android.material.R.attr.color, Color.CYAN));

        textView.setTextColor(color1);
        textView.setBackgroundColor(color2);
        canvas.clipRect(new Rect(0, 0, (int) (textView.getWidth() * 0.30), textView.getHeight()));
        textView.draw(canvas);
        canvas.restore();
        */
        /*
        {

            @Override
            public void draw(Canvas canvas) {

                int color1 = Color.RED;
                int color2 = Color.WHITE;

                canvas.save();
                setTextColor(color1);
                setBackgroundColor(color2);
                canvas.clipRect(new Rect(0, 0, (int) (getWidth() * 0.30), getHeight()));
                super.draw(canvas);
                canvas.restore();

                canvas.save();
                setTextColor(color2);
                setBackgroundColor(color1);
                canvas.clipRect(new Rect((int) (getWidth() * 0.70), 0, getWidth(), getHeight()));
                super.draw(canvas);
                canvas.restore();
            }
        };*/

        // Change button play to pause
        // TODO : Ensure oclorOnPrimary on button tint
        record = adapter.getPlayButton(position);
        record.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pause, 0, 0, 0);

        // Execute code after <duration> milliseconds
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {

            // Change button pause to play
            Button record1 = adapter.getPlayButton(position);
            record1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play, 0, 0, 0);
        }, duration);
    }
}
