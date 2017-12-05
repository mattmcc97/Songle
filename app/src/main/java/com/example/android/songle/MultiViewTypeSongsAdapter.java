package com.example.android.songle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by Matthew on 25/11/2017.
 */

public class MultiViewTypeSongsAdapter extends RecyclerView.Adapter {

    //Pass in the context and activity from where this adapter is used
    private Context context;
    private Activity activity;

    //The array list will form the data that will be used to populate the RecyclerView
    private ArrayList<Model> dataSet;

    //The number of types e.g. complete, incomplete, separator
    int totalTypes;

    public static final String YOUTUBE_API_KEY = "AIzaSyBjaJZj0WwqxFVOD8pUsAuGVnYCqXUvYa8";


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

            this.songButtonComplete = (Button)
                    itemView.findViewById(R.id.complete_list_item_song);
            this.youTubeButton = (Button)
                    itemView.findViewById(R.id.complete_list_item_youtube);
            this.songProgressBarComplete = (ProgressBar)
                    itemView.findViewById(R.id.complete_list_item_progress_bar);

        }
    }

    public static class SeparatorSongViewHolder extends RecyclerView.ViewHolder {

        View songSeparator;
        TextView completedSongsTv;


        public SeparatorSongViewHolder(View itemView) {
            super(itemView);

            this.songSeparator = itemView.findViewById(R.id.incomplete_complete_separator);
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

        /*
                Depending on the type of model/object, a layout will be chosen for each
                element in the RecyclerView.
         */
        View view;
        switch (viewType) {
            case Model.INCOMPLETE_TYPE:

                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.incomplete_list_layout, parent, false);
                return new IncompleteSongViewHolder(view);

            case Model.COMPLETE_TYPE:

                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.completed_list_layout, parent, false);
                return new CompleteSongViewHolder(view);

            case Model.SEPARATOR:

                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.list_separator_layout, parent, false);
                return new SeparatorSongViewHolder(view);

        }
        return null;
    }


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

                    //When an incomplete song is clicked, it should be loaded to the MapsActivity
                    ((IncompleteSongViewHolder) holder).songButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (isNetworkConnected() || isWifiConnected()) {

                                if (isLocationEnabled(context)) {

                                    Intent intent = new Intent(context, MapsActivity.class);

                                    //An ArrayList containing only one song which will be used by
                                    //the maps activity to get a random Song, because there
                                    //is only one Song in the ArrayList, this is the only song that
                                    //can be returned by the Maps Activity and this
                                    //song will be chosen.
                                    ArrayList<Song> theSongs = new ArrayList<Song>();
                                    theSongs.add(object.theSong);
                                    intent.putParcelableArrayListExtra("listOfSongs", theSongs);
                                    intent.putExtra("collectedMarkersMainMenu",
                                            object.theIncompleteSong.collectedMarkers);
                                    intent.putExtra("incompleteLevel",
                                            object.theIncompleteSong.levelOfDifficulty);

                                    //The song (in a list), the collected markers for that song and
                                    //the level of difficulty of the song are passed to the
                                    //Maps Activity
                                    ((Activity) context).startActivityForResult(intent, 1);
                                } else {
                                    Snackbar.make(view, "Songle can't get your location. " +
                                            "Please ensure you have a " +
                                            "mobile signal and location services enabled.",
                                            Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            } else {
                                Snackbar.make(view, "No internet connection. Please reconnect " +
                                        "and try again.", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }

                        }

                    });
                    ((IncompleteSongViewHolder) holder).songProgressBar
                            .setProgress(object.progress);

                    /*
                            When the give up button is clicked, an alert dialog will appear asking
                            the user if they want to proceed with that option.
                     */
                    ((IncompleteSongViewHolder) holder).giveUpButton
                            .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final AlertDialog.Builder alertDialogBuilder =
                                    new AlertDialog.Builder(activity);

                            TextView title = new TextView(activity);
                            title.setText("Give up?");
                            title.setPadding(10, 50, 10, 0);
                            title.setTextColor(Color.DKGRAY);
                            title.setTypeface(null, Typeface.BOLD);
                            title.setGravity(Gravity.CENTER);
                            title.setTextSize(20);

                            alertDialogBuilder.setCustomTitle(title);

                            alertDialogBuilder.setTitle("Give up?");
                            alertDialogBuilder.setMessage("Are you sure you want to give up " +
                                    "on this song? You won't be" +
                                    " able to try it again.");
                            alertDialogBuilder.setPositiveButton("Give Up",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            /*
                                            When the user gives up the following will happen:
                                            - A toast will appear giving the songTitle and artist
                                              (the songTitle and artist are passed in to the models
                                               'link' attribute).
                                            - The song will be removed from the list of incomplete
                                              songs
                                            - The song will be removed from the recycler view
                                            - The incomplete song file saved in internal storage
                                              will be overwritten with the new list.
                                             */
                                            Toast.makeText(activity, "The song was:\n" +
                                                    object.link + ".", Toast.LENGTH_LONG).show();
                                            MainMenu.incompleteSongs.remove(
                                                    object.theIncompleteSong);
                                            removeItem(object);
                                            try {
                                                FileOutputStream fileOutputStream =
                                                        context.openFileOutput(
                                                                "IncompleteSongs.ser",
                                                                Context.MODE_PRIVATE);
                                                ObjectOutputStream objectOutputStream =
                                                        new ObjectOutputStream(fileOutputStream);
                                                objectOutputStream.writeObject(
                                                        MainMenu.incompleteSongs);
                                                objectOutputStream.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                            alertDialogBuilder.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                            TextView messageText = (TextView)
                                    alertDialog.findViewById(android.R.id.message);
                            messageText.setGravity(Gravity.CENTER);
                            alertDialog.show();

                        }

                    });
                    break;
                case Model.COMPLETE_TYPE:
                    ((CompleteSongViewHolder) holder).songButtonComplete.setText(object.text);
                    ((CompleteSongViewHolder) holder).songButtonComplete
                            .setOnClickListener(new View.OnClickListener() {

                                /*
                                    A YouTubeStandalonePlayer will be instantiated and the song
                                    will be loaded using the youtube link. It will start
                                    straight away, automatically and in a dialog. This will appear
                                    in the activity that is using the adapter.

                                    Ref:
                                    https://github.com/mohit008/Android-Youtube-Stand-Alone-Player
                                 */
                        @Override
                        public void onClick(View view) {

                            try {
                                view.getContext().startActivity(
                                        YouTubeStandalonePlayer.createVideoIntent(
                                        activity, YOUTUBE_API_KEY, object.link, 0, true, true));
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(
                                        activity, "Please install YouTube on your device.",
                                        Toast.LENGTH_LONG).show();
                                //watchYoutubeVideoFromBrowser(object.link);
                            }

                        }

                    });
                    ((CompleteSongViewHolder) holder).songProgressBarComplete.setProgress(100);
                    ((CompleteSongViewHolder) holder).youTubeButton
                            .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            try {
                                view.getContext().startActivity(
                                        YouTubeStandalonePlayer.createVideoIntent(
                                        activity, YOUTUBE_API_KEY, object.link, 0, true, true));

                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(
                                        activity, "Please install YouTube on your device.",
                                        Toast.LENGTH_LONG).show();
                                //watchYoutubeVideoFromBrowser(object.link);
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

    //Opens YouTube in the app, if it isn't available, it opens in the browser.
    public void watchYoutubeVideoFromBrowser(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    /*
            This removes the data from our Dataset and Updates the Recycler View.
    */
    private void removeItem(Model model) {

        int currPosition = dataSet.indexOf(model);
        dataSet.remove(currPosition);
        notifyItemRemoved(currPosition);
    }

    /*
            Check to see if the user has a data internet connection.
    */
    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    /*
            Check to see if the user has a WiFi internet connection.
    */
    private boolean isWifiConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && (ConnectivityManager.TYPE_WIFI == networkInfo.getType())
                && networkInfo.isConnected();
    }


    /*
            Check to see if the user has location services enabled.

            Ref: https://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
    */
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(
                        context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }


}
