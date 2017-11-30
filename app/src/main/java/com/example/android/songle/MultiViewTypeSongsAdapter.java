package com.example.android.songle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.ArrayList;

/**
 * Created by Matthew on 25/11/2017.
 */

public class MultiViewTypeSongsAdapter extends RecyclerView.Adapter {

    private Context context;
    private Activity activity;
    private ArrayList<Model> dataSet;
    int totalTypes;
    public static final String API_KEY = "AIzaSyBjaJZj0WwqxFVOD8pUsAuGVnYCqXUvYa8";


    /*public MultiViewTypeSongsAdapter(Context context, ArrayList<IncompleteSong> incompleteSongs) {

        this.context = context;
        this.incompleteSongs = incompleteSongs;
        inflater = LayoutInflater.from(context);
    }*/

    public static class IncompleteSongViewHolder extends RecyclerView.ViewHolder {

        Button songButton;
        Button giveUpButton;
        ProgressBar songProgressBar;


        public IncompleteSongViewHolder(View itemView) {
            super(itemView);

            this.songButton = (Button) itemView.findViewById(R.id.list_item_song);
            this.giveUpButton = (Button) itemView.findViewById(R.id.list_item_give_up);
            this.songProgressBar = (ProgressBar) itemView.findViewById(R.id.list_item_progress_bar);

        }
    }

    public static class CompleteSongViewHolder extends RecyclerView.ViewHolder {

        Button songButtonComplete;
        Button youTubeButton;
        ProgressBar songProgressBarComplete;


        public CompleteSongViewHolder(View itemView) {
            super(itemView);

            this.songButtonComplete = (Button) itemView.findViewById(R.id.complete_list_item_song);
            this.youTubeButton = (Button) itemView.findViewById(R.id.complete_list_item_youtube);
            this.songProgressBarComplete = (ProgressBar) itemView.findViewById(R.id.complete_list_item_progress_bar);

        }
    }

    public static class SeparatorSongViewHolder extends RecyclerView.ViewHolder {

        View songSeparator;
        TextView completedSongsTv;


        public SeparatorSongViewHolder(View itemView) {
            super(itemView);

            this.songSeparator = (View) itemView.findViewById(R.id.incomplete_complete_separator);
            this.completedSongsTv = (TextView) itemView.findViewById(R.id.complete_songs_tv);

        }
    }

    public MultiViewTypeSongsAdapter(ArrayList data, Context context, Activity activity) {
        this.dataSet = data;
        this.context = context;
        this.activity = activity;
        totalTypes = dataSet.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case Model.INCOMPLETE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.incomplete_list_layout, parent, false);
                return new IncompleteSongViewHolder(view);
            case Model.COMPLETE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.completed_list_layout, parent, false);
                return new CompleteSongViewHolder(view);
            case Model.SEPARATOR:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_separator_layout, parent, false);
                return new SeparatorSongViewHolder(view);
        }
        return null;
    }

    /*@Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int position) {

        View view = inflater.inflate(R.layout.incomplete_list_layout, parent, false);

        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }*/

    @Override
    public int getItemViewType(int position) {

        switch (dataSet.get(position).type) {
            case 0:
                return Model.INCOMPLETE_TYPE;
            case 1:
                return Model.COMPLETE_TYPE;
            case 2:
                return Model.SEPARATOR;
            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int listPosition) {

        final Model object = dataSet.get(listPosition);
        if (object != null) {
            switch (object.type) {
                case Model.INCOMPLETE_TYPE:
                    ((IncompleteSongViewHolder) holder).songButton.setText(object.text);
                    ((IncompleteSongViewHolder) holder).songButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent= new Intent(context, MapsActivity.class);
                            //An ArrayList containing only one song which will be used by the
                            //maps activity to get a random Song, because there is only one Song
                            //in the ArrayList, this is the only song that can be returned by the
                            //Maps Activity and this song will be chosen.
                            ArrayList<Song> theSongs = new ArrayList<Song>();
                            theSongs.add(object.theSong);
                            Log.i("", "onClick: theSong: " + object.theSong.getTitle());
                            intent.putParcelableArrayListExtra("listOfSongs",theSongs);
                            intent.putExtra("collectedMarkersMainMenu", object.theIncompleteSong.collectedMarkers);
                            intent.putExtra("incompleteLevel", object.theIncompleteSong.levelOfDifficulty);
                            ((Activity) context).startActivityForResult(intent,1);

                        }

                    });
                    ((IncompleteSongViewHolder) holder).songProgressBar.setProgress(object.progress);
                    ((IncompleteSongViewHolder) holder).giveUpButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                            TextView title = new TextView(activity);
                            title.setText("Give up?");
                            title.setPadding(10, 50, 10, 0);
                            title.setTextColor(Color.DKGRAY);
                            title.setTypeface(null, Typeface.BOLD);
                            title.setGravity(Gravity.CENTER);
                            title.setTextSize(20);

                            alertDialogBuilder.setCustomTitle(title);

                            alertDialogBuilder.setTitle("Give up?");
                            alertDialogBuilder.setMessage("Are you sure you want to give up on this song? You won't be" +
                                    " able to try it again.");
                            alertDialogBuilder.setPositiveButton("Give Up",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            Toast.makeText(activity, "The song was: " + object.link + ".", Toast.LENGTH_LONG).show();
                                            MainMenu.incompleteSongs.remove(object.theIncompleteSong);
                                            removeItem(object);
                                        }
                                    });

                            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                            TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
                            messageText.setGravity(Gravity.CENTER);
                            alertDialog.show();

                        }

                    });
                    break;
                case Model.COMPLETE_TYPE:
                    ((CompleteSongViewHolder) holder).songButtonComplete.setText(object.text);
                    ((CompleteSongViewHolder) holder).songButtonComplete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            /*
                            * Calling youtube stand alone player
                            *
                            * You should read this parameter to change them
                            * Parameters
                            *activity*  The calling activity from which the standalone player will be started.
                            *developerKey*  A valid API key which is enabled to use the YouTube Data API v3 service. To generate a new key, visit the Google APIs Console.
                            *videoId*  The id of the video to be played.
                            *timeMillis*  The time, in milliseconds, where playback should start in the video.
                            *autoplay*  true to have the video start playback as soon as the standalone player loads, false to cue the video.
                            *lightboxMode*  true to have the video play in a dialog view above your current Activity, false to have the video play fullscreen.
                            */

                            try{
                                view.getContext().startActivity(YouTubeStandalonePlayer.createVideoIntent(
                                        activity, API_KEY, object.link, 0, true, true));
                            }catch(ActivityNotFoundException e){
                                Toast.makeText(activity, "Please install YouTube on your device.", Toast.LENGTH_LONG).show();
                            }

                        }

                    });
                    ((CompleteSongViewHolder) holder).songProgressBarComplete.setProgress(100);
                    ((CompleteSongViewHolder) holder).youTubeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            try{
                                view.getContext().startActivity(YouTubeStandalonePlayer.createVideoIntent(
                                        activity, API_KEY, object.link, 0, true, true));

                            }catch(ActivityNotFoundException e){
                                Toast.makeText(activity, "Please install YouTube on your device.", Toast.LENGTH_LONG).show();
                            }

                        }

                            });
                    break;
                case Model.SEPARATOR:
                    ((SeparatorSongViewHolder) holder).completedSongsTv.setText(object.text);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    // This removes the data from our Dataset and Updates the Recycler View.
    private void removeItem(Model model) {

        int currPosition = dataSet.indexOf(model);
        dataSet.remove(currPosition);
        notifyItemRemoved(currPosition);
    }


}
