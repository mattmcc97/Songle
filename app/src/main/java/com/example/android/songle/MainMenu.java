package com.example.android.songle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainMenu extends AppCompatActivity{

    //URL
    private static final String xml_url = "http://www.inf.ed.ac.uk/teaching/courses/selp/data/songs/songs.xml";
    private static final String TAG = "MainMenu";

    //An ArrayList containing all of the songs
    public static ArrayList<Song> songs;

    private static Song song;

    private boolean connectedToNetwork = true;
    private boolean locationServicesAvailable = true;

    Dialog dialogYouTube;
    public static final String API_KEY = "AIzaSyBjaJZj0WwqxFVOD8pUsAuGVnYCqXUvYa8";
    public static final String VIDEO_ID = "fJ9rUzIMcZQ";
    Button youtubeButton;

    //HashMap containing the SongTitle (as the key) and the Youtube link
    HashMap<String, String> completedSongs;
    String completedSongTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        songs = new ArrayList<Song>();

        completedSongs = new HashMap<>();
        /*try
        {
            FileOutputStream fileOutputStream = openFileOutput("CompletedSongs.ser", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(completedSongs);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //Execute the methods in the AsyncTask class
        AsyncXMLDownloader downloader = new AsyncXMLDownloader();
        downloader.execute();

        ArrayList list= new ArrayList();
        list.add(new Model(Model.INCOMPLETE_TYPE,"Song 1",53, ""));
        list.add(new Model(Model.INCOMPLETE_TYPE,"Song 2",88, ""));
        list.add(new Model(Model.INCOMPLETE_TYPE,"Song 3",22, ""));
        list.add(new Model(Model.SEPARATOR, "Completed Songs",0, ""));
        list.add(new Model(Model.COMPLETE_TYPE,"Bohemian Rhapsody",100, "fJ9rUzIMcZQ"));
        list.add(new Model(Model.COMPLETE_TYPE,"I Fought The Law",100, "AL8chWFuM-s"));
        list.add(new Model(Model.COMPLETE_TYPE,"Life On Mars?",100, "v--IqqusnNQ"));
        list.add(new Model(Model.COMPLETE_TYPE,"Nothing Compares 2 U",100, "SJwb_KXWsbk"));


        MultiViewTypeSongsAdapter adapter = new MultiViewTypeSongsAdapter(list,this,MainMenu.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, OrientationHelper.VERTICAL, false);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.songs_list);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);


    }

    public void newSong(View view){
        //When the new song button is clicked, open the MapsActivity
        if (connectedToNetwork) {
            if (locationServicesAvailable) {
                if(songs.size() > 0){
                    Intent intent = new Intent(this, MapsActivity.class);
                    intent.putParcelableArrayListExtra("listOfSongs", songs);
                    startActivityForResult(intent, 1);
                }else{
                    Snackbar.make(view, "Sorry, no songs are available at the minute. " +
                            "Please try again later.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            } else {
                Snackbar.make(view, "Songle can't get your location. Please ensure you have a " +
                        "mobile signal and location services enabled.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else {
            Snackbar.make(view, "No internet connection. Please reconnect and try again.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void populateCompletedSongs(){
        try
        {
            FileInputStream fileInputStream = new FileInputStream(getFilesDir()+"/CompletedSongs.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            completedSongs = (HashMap)objectInputStream.readObject();
            Log.i(TAG, "populateCompletedSongs: initial read from file: " + completedSongs);
            objectInputStream.close();
        }
        catch(ClassNotFoundException | IOException | ClassCastException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "populateCompletedSongs: completedSongs before adding new one: " + completedSongs);

        completedSongTitle = getIntent().getStringExtra("songTitle");
        if(completedSongTitle != null){
            Log.i(TAG, "populateCompletedSongs: songLink: not null sizeOfSongs: " + songs.size());
            for (Song song: songs){
                if(song.getTitle().equals(completedSongTitle)){
                    Log.i(TAG, "populateCompletedSongs: songLink: " + song.getLink());
                    completedSongs.put(completedSongTitle, song.getLink());
                }
            }
        }
        Log.i(TAG, "populateCompletedSongs: completedSongs: after read from file about to write: " + completedSongs);
        try
        {
            FileOutputStream fileOutputStream = openFileOutput("CompletedSongs.ser", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(completedSongs);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == RESULT_OK){

            }
        }
    }

    public void viewStatistics(View view) {
        //When the statistics button is clicked, open the StatisticsActivity
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }

    public void giveUpOnSong(View view) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        TextView title = new TextView(this);
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
                        Toast.makeText(MainMenu.this, "The song was Bohemian Rhapsody.", Toast.LENGTH_LONG).show();
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

    public void showToast(View view) {
        Toast.makeText(MainMenu.this, "Sorry you gave up! The song was: Song 2 by Blur.",
                Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private boolean isWifiConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && (ConnectivityManager.TYPE_WIFI == networkInfo.getType())
                && networkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        TopBarFragment fragment = (TopBarFragment)
                getFragmentManager().findFragmentById(R.id.top_bar_fragment);
        fragment.updateLevel();
        super.onResume();
    }

    private class AsyncXMLDownloader extends AsyncTask<Object, String, Integer>{

        @Override
        protected Integer doInBackground(Object... params) {
            //Call the appropriate methods to download and parse the xml data
            XmlPullParser receivedData = tryDownloadXmlData();
            int songsFound = tryParseXmlData(receivedData);
            populateCompletedSongs();
            return songsFound;
        }

        private XmlPullParser tryDownloadXmlData() {
            try{
                URL xmlURL = new URL(xml_url);
                //Create a new instance of the XmlPullParser class and set the input stream
                XmlPullParser receivedData = XmlPullParserFactory.newInstance().newPullParser();
                receivedData.setInput(xmlURL.openStream(), null);
                return receivedData;
            }catch(XmlPullParserException e){
                Log.e("Songle", "XmlPullParserException - tryDownloadXmlData", e);
            }catch (IOException e){
                Log.e("Songle", "IOException - tryDownloadXmlData" , e);
            }
            return null;
        }

        private int tryParseXmlData(XmlPullParser receivedData) {
            //If there is data from the input stream call the method to parse the data
            if(receivedData != null){
                try{
                    processReceivedData(receivedData);
                }catch(XmlPullParserException e){
                    Log.e("Songle", "XmlPullParserException - tryParseXmlData", e);
                }catch (IOException e){
                    Log.e("Songle", "IOException - tryParseXmlData" , e);
                }
            }
            return 0;
        }

        private void processReceivedData(XmlPullParser xmlData) throws IOException, XmlPullParserException {

            String number = "";
            String artist = "";
            String title = "";
            String link = "";

            /*
            This block goes through the xml data and when it reaches a new Song tag
            it extracts the raw data into a variable. The temporary variables are then
            used to create a new Song object which is added to the ArrayList of songs.
             */
            int eventType = xmlData.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                String name = xmlData.getName();
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (name.equalsIgnoreCase("Song")){
                            break;
                        }else if (name.equalsIgnoreCase("Number")){
                            number = xmlData.nextText();
                        }else if (name.equalsIgnoreCase("Artist")){
                            artist = xmlData.nextText();
                        }else if (name.equalsIgnoreCase("Title")){
                            title = xmlData.nextText();
                        }else if (name.equalsIgnoreCase("Link")){
                            link = xmlData.nextText();
                            song = new Song(title, number, link, artist);
                            Log.i("Adding to songs:", "Title: " + song.getTitle());
                            try {
                                songs.add(song);
                            }catch (NullPointerException e){
                                Log.e("MainMenu ", "processReceivedData: ", e);
                            }
                        }
                        break;
                }
                eventType = xmlData.next();
            }
        }
    }

}
