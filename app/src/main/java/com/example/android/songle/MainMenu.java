package com.example.android.songle;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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

public class MainMenu extends AppCompatActivity {

    //URL for the songs
    private static final String xml_url =
            "http://www.inf.ed.ac.uk/teaching/courses/selp/data/songs/songs.xml";

    private static final String TAG = "MainMenu";

    //An ArrayList containing all of the songs the user could get, if they click new song.
    public static ArrayList<Song> songs;

    //Loading dialog that appears while Songle is accessing the list of Songs.
    ProgressDialog loadingDialog;

    //HashMap containing the SongTitle (as the key) and the YouTube link.
    HashMap<String, String> completedSongs;
    String completedSongTitle;

    //HashMap<songName, HashMap<ListOfCollectedPlacemarks, numberOfPlacemarksForThatSong>.
    public static ArrayList<IncompleteSong> incompleteSongs;

    //The incomplete song that will be returned by the MapsActivity.
    IncompleteSong incompleteSong;
    //The corresponding song linked to the incomplete song that will be returned by the MapsActivity.
    Song theSong;

    //The list of incomplete and complete songs for use in the RecyclerView.
    ArrayList mainMenuList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        getLocationPermissions();

        //Initialisation of Lists and Maps.
        songs = new ArrayList<Song>();
        mainMenuList = new ArrayList();
        completedSongs = new HashMap<>();
        incompleteSongs = new ArrayList<IncompleteSong>();

        //Initialisation of the Custom Adapter and the RecyclerView it will be used on
        MultiViewTypeSongsAdapter adapter = new MultiViewTypeSongsAdapter(
                mainMenuList, this, MainMenu.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this, OrientationHelper.VERTICAL, false);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.songs_list);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);

        //Execute the methods in the AsyncTask class
        AsyncXMLDownloader downloader = new AsyncXMLDownloader();
        downloader.execute();

    }


    /*
                When the new song button is clicked, open the MapsActivity if:
                - The user has internet connection
                - The user has location services available
                - The user has less than 3 incomplete songs in progress

                The list of songs should be passed to the MapsActivity so that a random song
                can be chosen
    */
    public void newSong(View view) {

        if (isNetworkConnected() || isWifiConnected()) {
            if (isLocationEnabled(this)) {
                if (songs.size() > 0) {
                    if (incompleteSongs.size() < 3) {
                        getLocationPermissions();
                        Intent intent = new Intent(this, MapsActivity.class);
                        intent.putParcelableArrayListExtra("listOfSongs", songs);
                        writeIncompleteSongsToFile();
                        startActivityForResult(intent, 1);
                    } else {
                        Toast.makeText(MainMenu.this, "The maximum number of songs you can " +
                                "play, at once, is 3. Please give up on one of the " +
                                "incomplete songs, if you want to guess a " +
                                "new song.", Toast.LENGTH_LONG).show();
                    }
                } else {
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
            Snackbar.make(view, "No internet connection. Please reconnect and try again.",
                    Snackbar.LENGTH_LONG)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tryNetworkAgain();
                        }
                    }).show();
        }
    }

    /*
            Ask the user for permission to access their location.
     */
    private void getLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                MainMenu.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        MainMenu.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainMenu.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
    }


    /*
            When the user returns from the MapsActivity having attempted a song, the incomplete song
            and the song should be retrieved. So that it can either be added to the list of incomplete
            songs or update it's progress, if it was already in the list.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                incompleteSong = (IncompleteSong) data.getSerializableExtra("incompleteSong");
                theSong = (Song) data.getSerializableExtra("theSong");
                buildIncompleteSongs();
                populateSongs();
            }
        }
    }


    /*
            When the user has completed a song, the song title is passed back to the MainMenu so it
            can be added to the list of completed songs.
     */
    private void buildCompletedSongs() {

        completedSongTitle = getIntent().getStringExtra("songTitle");
        if ((completedSongTitle != null) && (!completedSongs.containsKey(completedSongTitle))) {
            for (Song song : songs) {
                if (song.getTitle().equals(completedSongTitle)) {
                    completedSongs.put(completedSongTitle, song.getLink());
                }
            }
        }

    }


    /*
            This method overwrites the list of completed songs in the file, with the current list
            of completed songs. This method is called once the completedSongs HashMap is updated.
     */
    private void writeCompleteSongsToFile() {
        try {
            FileOutputStream fileOutputStream = openFileOutput(
                    "CompletedSongs.ser", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(completedSongs);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
            This method reads the list of completed songs from the file.
    */
    private void readCompleteSongsFromFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream(
                    getFilesDir() + "/CompletedSongs.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            completedSongs = (HashMap) objectInputStream.readObject();
            objectInputStream.close();
        } catch (ClassNotFoundException | IOException | ClassCastException e) {
            e.printStackTrace();
        }
    }


    /*
            When the user has returned to the MainMenu after attempting a song either:
            - The song was already incomplete before the user went to the MapsActivity, in this case,
            the collected markers are appended to the previously collected markers. This is so the
            progress can be updated.
            - The song is brand new and so it will be added to the ArrayList of incomplete songs.
     */
    private void buildIncompleteSongs() {

        boolean songIsAlreadyIncomplete = false;
        if (incompleteSong != null) {
            for (IncompleteSong incomplete : incompleteSongs) {
                if (incomplete.getSongTitle().equals(incompleteSong.getSongTitle())) {
                    songIsAlreadyIncomplete = true;
                    incomplete.collectedMarkers.addAll(incompleteSong.collectedMarkers);
                }
            }
            if (!songIsAlreadyIncomplete) {
                incompleteSongs.add(incompleteSong);
            }
        }
    }


    /*
            This method reads the list of incomplete songs from the file.
    */
    private void readIncompleteSongsFromFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream(
                    getFilesDir() + "/IncompleteSongs.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            incompleteSongs = (ArrayList<IncompleteSong>) objectInputStream.readObject();
            objectInputStream.close();
        } catch (ClassNotFoundException | IOException | ClassCastException e) {
            e.printStackTrace();
        }
    }


    /*
            This method overwrites the list of incomplete songs in the file, with the current list
            of incomplete songs. This method is called once the incompleteSongs ArrayList is updated.
     */
    private void writeIncompleteSongsToFile() {
        try {
            FileOutputStream fileOutputStream = openFileOutput(
                    "IncompleteSongs.ser", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(incompleteSongs);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
            This method is responsible for taking the current state of incomplete and complete songs
            and updating the state of the RecyclerView.
     */
    private void populateSongs() {

        readCompleteSongsFromFile();
        buildCompletedSongs();
        writeCompleteSongsToFile();

        readIncompleteSongsFromFile();
        buildIncompleteSongs();
        writeIncompleteSongsToFile();

        //Reset completed songs
        /*File dir1 = getFilesDir();
        File file1 = new File(dir1, "CompletedSongs.ser");
        boolean deleted1 = file1.delete();*/


        mainMenuList = new ArrayList();
        addIncompleteSongsToMainMenuList();

        MultiViewTypeSongsAdapter adapter = new MultiViewTypeSongsAdapter(
                mainMenuList, this, MainMenu.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this, OrientationHelper.VERTICAL, false);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.songs_list);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);

        /*
                If the very first item in the RecyclerView is incomplete, then we know there is at
                least one incomplete song in the list. This means we should not show the message:
                "There are no incomplete songs".
         */
        if (mRecyclerView.getAdapter().getItemViewType(0) == 0) {
            TextView noIncompleteSongs = (TextView) findViewById(R.id.no_incomplete_songs_tv);
            noIncompleteSongs.setVisibility(View.GONE);
        }


        /*
                If the very last item in the RecyclerView is complete, then we know there is at
                least one complete song in the list. This means we should not show the message:
                "There are no complete songs".
         */
        int numberOfObjects = mRecyclerView.getAdapter().getItemCount();
        if (mRecyclerView.getAdapter().getItemViewType(numberOfObjects - 1) == 1) {
            TextView completeSongs = (TextView) findViewById(R.id.no_complete_songs_tv);
            completeSongs.setVisibility(View.GONE);
        }

        //Close the loading dialog if it is still showing, at this point.
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }


    /*
            Adding the RecyclerView entry to the main menu list. After all the incomplete songs
            have been added, the separator should be added and then call the method to add the
            completed songs to the main menu list.
     */
    private void addIncompleteSongsToMainMenuList() {
        //This is used to give unknown songs names e.g. Song 1, Song 2, Song 3
        int i = 1;
        for (IncompleteSong incomplete : incompleteSongs) {
            /*
                    Calculate the song progress (as a %) by comparing the number of collected
                    markers with the total number of markers on the map.
             */
            int progress =
                    (int) (((incomplete.getCollectedMarkers().size()) /
                            ((double) incomplete.getTotalNumberOfPlacemarks())) * 100);

            mainMenuList.add(
                    new Model(Model.INCOMPLETE_TYPE,
                            "Song " + i,
                            progress,
                            incomplete.getSongTitle() + " - " + incomplete.getTheSong().getArtist(),
                            incomplete.getTheSong(),
                            incomplete));

            i++;
        }
        mainMenuList.add(new Model(Model.SEPARATOR, "Completed Songs", 0, "", null, null));
        addCompletedSongsToMainMenuList(completedSongs);
    }


    /*
            This method adds all of the completed songs to the main menu list.
     */
    private void addCompletedSongsToMainMenuList(HashMap<String, String> completedSongs) {
        for (Map.Entry<String, String> entry : completedSongs.entrySet()) {
            String songTitle = entry.getKey();
            String songLink = entry.getValue();
            String artist = "";
            Song completedSong = null;
            for(Song aSong : songs){
                if(aSong.getTitle().equals(songTitle)){
                    artist = aSong.getArtist();
                    completedSong = aSong;
                }
            }
            mainMenuList.add(new Model(Model.COMPLETE_TYPE, songTitle + " - " + artist,
                    100, songLink, completedSong, null));
        }
    }


    /*
            When the statistics button is clicked, open the StatisticsActivity.
     */
    public void viewStatistics(View view) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }


    /*
            Check to see if the user has a data internet connection.
    */
    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    /*
            Check to see if the user has a WiFi internet connection.
    */
    private boolean isWifiConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
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


    /*
            When the user returns to the main menu, update their level in the top bar.
     */
    @Override
    protected void onResume() {
        TopBarFragment fragment = (TopBarFragment)
                getFragmentManager().findFragmentById(R.id.top_bar_fragment);
        fragment.updateLevel();
        super.onResume();
    }


    /*
            This empty method stops the user from going back to the song after guessing it correctly.
            It also prevents the user from returning to the Start screen.
     */
    @Override
    public void onBackPressed() {
    }


    /*
             Execute the methods in the AsyncTask class, to get a fresh set of songs. It may be
             that the user had originally no signal when they started the app and now do, this
             allows the user to get the songs.
    */
    private void tryNetworkAgain() {
        AsyncXMLDownloader downloader = new AsyncXMLDownloader();
        downloader.execute();
    }


    private class AsyncXMLDownloader extends AsyncTask<Object, String, Integer> {

        /*
                When loading the map display a circular loading dialog that says "Loading Map...".
        */
        @Override
        protected void onPreExecute() {
            loadingDialog = new ProgressDialog(MainMenu.this);
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadingDialog.setMessage("Loading Menu...");
            loadingDialog.setIndeterminate(true);
            loadingDialog.show();
        }

        @Override
        protected Integer doInBackground(Object... params) {
            //Call the appropriate methods to download and parse the xml data
            XmlPullParser receivedData = tryDownloadXmlData();
            int songsFound = tryParseXmlData(receivedData);
            return 0;
        }

        private XmlPullParser tryDownloadXmlData() {
            try {
                URL xmlURL = new URL(xml_url);
                /*
                        Create a new instance of the XmlPullParser class and set the input stream
                        to the URL of the songs.xml file.
                 */
                XmlPullParser receivedData = XmlPullParserFactory.newInstance().newPullParser();
                receivedData.setInput(xmlURL.openStream(), null);
                return receivedData;
            } catch (XmlPullParserException e) {
                Log.e("Songle", "XmlPullParserException - tryDownloadXmlData", e);
            } catch (IOException e) {
                Log.e("Songle", "IOException - tryDownloadXmlData", e);
            }
            return null;
        }

        private int tryParseXmlData(XmlPullParser receivedData) {
            //If there is data from the input stream call the method to parse the data
            if (receivedData != null) {
                try {
                    processReceivedData(receivedData);
                } catch (XmlPullParserException e) {
                    Log.e("Songle", "XmlPullParserException - tryParseXmlData", e);
                } catch (IOException e) {
                    Log.e("Songle", "IOException - tryParseXmlData", e);
                }
            }
            return 0;
        }

        private void processReceivedData(XmlPullParser xmlData) throws IOException,
                XmlPullParserException {

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
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = xmlData.getName();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (name.equalsIgnoreCase("Song")) {
                            break;
                        } else if (name.equalsIgnoreCase("Number")) {
                            number = xmlData.nextText();
                        } else if (name.equalsIgnoreCase("Artist")) {
                            artist = xmlData.nextText();
                        } else if (name.equalsIgnoreCase("Title")) {
                            title = xmlData.nextText();
                        } else if (name.equalsIgnoreCase("Link")) {
                            link = xmlData.nextText();
                            Song song = new Song(title, number, link, artist);
                            try {
                                songs.add(song);
                            } catch (NullPointerException e) {
                                Log.e("MainMenu ", "processReceivedData: ", e);
                            }
                        }
                        break;
                }
                eventType = xmlData.next();
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            populateSongs();
            //Close the loading dialog if it is still showing, at this point.
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }

}
