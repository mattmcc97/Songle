package com.example.android.songle;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    /*
            The following variables are needed for the functionality of the map itself.
     */
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location mLastLocation;

    private static final String TAG = "MapsActivity";

    //Initialise the URL's for the KML document and the text file containing the lyrics
    private String kml_url = "";
    private String text_url = "";

    //A set of all the placemarks for the song.
    private static HashSet<Placemark> placemarks;

    //The markers on the map and their song line:number as key
    private HashMap<String, Marker> hashMapMarkers;

    //The markers that have been collected when a user previously attempted the song.
    private HashMap<String, Marker> incompleteSongCollectedMarkers;

    /*
            The wholeSong contains a HashMap where the keys is the line number and the value is
            another HashMmap corresponding to the line of the song. This contains the word number
            within the line and the word itself.
    */
    private HashMap<Integer, HashMap<Integer, String>> wholeSong;

    //The list of songs that are built in the MainMenu
    private ArrayList<Song> songs;

    /*
            The songTitle and songNumber corresponding to the song that is being used to play
            the current game.
     */
    public String songTitle;
    private String songNumber;

    //A set of the collected markers for the song the user is playing.
    private HashSet<String> collectedMarkers;

    /*
            3 separate dialogs:
            - pgDialog: Used while the map is loading.
            - levelUpDialog: this is shown when the user collects enough markers to level up.
            - songleCoinCollectedDialog: this is shown when the user walks another 1km.
     */
    private ProgressDialog pgDialog;
    Dialog levelUpDialog;
    Dialog songleCoinCollectedDialog;

    //User level taken from SharedPreferences to help choose a map difficulty.
    private int userLevel;

    //Statistics
    private float distanceWalkedWhilePlaying = 0.0f;
    //Once the user leaves the MapsActivity the difference between the start and end time will be
    //used to calculate the time spent playing.
    long startTime;
    long endTime;

    //The map number corresponding to the difficulty of the current map.
    int mapNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        placemarks = new HashSet<>();

        wholeSong = new HashMap<>();

        //Passed in from the MainMenu activity
        songs = getIntent().getParcelableArrayListExtra("listOfSongs");

        collectedMarkers = new HashSet<>();

        hashMapMarkers = new HashMap<>();

        startTime = Calendar.getInstance().getTimeInMillis();

        //Obtain the user's level from SharedPreferences
        SharedPreferences scoreAndLevel = getSharedPreferences("score", Context.MODE_PRIVATE);
        userLevel = scoreAndLevel.getInt("level", 1);

        //Execute the methods in the AsyncTask class
        ASyncKMLDownloader downloader = new ASyncKMLDownloader();
        downloader.execute();

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //Long running activities are performed asynchronously in order to
        //keep the user interface responsive
        mapFragment.getMapAsync(this);
        //Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    /*
            When the location button is clicked, this method causes the UI to zoom to the users
            current location.
     */
    public void goToCurrentLocation(View view) {
        try {
            float zoomLevel = 19.0f; //This goes up to 21
            LatLng currentLocation = new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));
        } catch (NullPointerException e) {
            Snackbar.make(view, "No location found.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }


    /*
          Using the songNumber generated in the AsyncTask, the songTitle is retrieved.
    */
    private String getSongTitle(String songNum) {
        String songName = "";
        for (Song song : songs) {
            if (songNum.equals(song.getNumber())) {
                songName = song.getTitle();
            }
        }
        return songName;
    }


    /*
          Using the songNumber generated in the AsyncTask, the corresponding song is retrieved.
    */
    private Song getTheSong(String songNum) {
        Song theSong = null;
        for (Song song : songs) {
            if (songNum.equals(song.getNumber())) {
                theSong = song;
            }
        }
        return theSong;
    }


    /*
            This method is called when the guess song button is pressed, it launches the
            GuessSong activity.
     */
    public void guessSong(View view) {

        songTitle = getSongTitle(songNumber);
        Intent intent = new Intent(this, GuessSong.class);
        intent.putExtra("songTitle", songTitle);
        intent.putExtra("wholeSong", wholeSong);
        intent.putExtra("collectedMarkers", collectedMarkers);
        intent.putExtra("placemarks", placemarks);
        intent.putExtra("distanceWalkedWhilePlaying", distanceWalkedWhilePlaying);
        startActivityForResult(intent, 1);

    }


    /*
            When the user returns from the GuessSong activity having attempted a song, the markers
            that may have been collected, by spending a songle coin, should be removed from the map.
    */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                collectedMarkers = (HashSet<String>) data.getSerializableExtra("collectedMarkers");
                removeCollectedMarkersFromMap();
            }
        }
    }

    /*
            Loop through the collected markers and if there is a marker that is still present on
            the map, remove it.
    */
    private void removeCollectedMarkersFromMap() {
        if (collectedMarkers != null) {
            for (String markerLocation : collectedMarkers) {
                if (hashMapMarkers.containsKey(markerLocation)) {
                    Marker marker = hashMapMarkers.get(markerLocation);

                    marker.remove();
                    hashMapMarkers.remove(markerLocation);
                }

            }
        } else {
            collectedMarkers = new HashSet<>();
        }
        pgDialog.dismiss();
    }



    /*
            This method will randomly select a song number to be chosen for the KML URL. If the
            number chosen is less than 10 then a "0" must be added to the front of the number.
            Also, if the song is incomplete and it is being continued then the songNumber will be
            chosen from a list of size 1.
     */
    private String chooseSongNumber() {
        //This will be entered when an incomplete Song is chosen rather than a new song
        if (songs.size() == 1) {
            return songs.get(0).number;
        } else {
            Random rand = new Random();
            int randomSongNumberInt = rand.nextInt(songs.size()) + 1;
            String randomSongNumber = "";
            if (randomSongNumberInt <= 9) {
                randomSongNumber = "0" + randomSongNumberInt;
            } else {
                randomSongNumber = "" + randomSongNumberInt;
            }
            return randomSongNumber;
        }
    }

    /*
            This method chooses a map difficulty that will be used for the KML URL. The map
            difficulty is based on the user's level. A variation parameter is also used so that
            the level is not the complete and final decider on the map difficulty.
     */
    private int getMapDifficulty(int userLevel) {
        //This will be entered when an incomplete Song is chosen rather than a new song
        if (songs.size() == 1) {
            Integer incompleteMapNumber = getIntent().getIntExtra("incompleteLevel", 1);
            return incompleteMapNumber;
        } else {

            //Calculates the fraction  of the max level (99), if the user was level 99 they would
            //get a difficulty of 4 and a msp number of 1 (5 - 4 = 1). If the user was level 1 they
            //would get a difficulty of 0 and a map number of 5 (5 - 0 = 5).
            //Variation is used so that the user can get a range of map difficulties. The variation
            //basically edits the level from a range of -25 to + 25.
            Random rand = new Random();
            int variation = rand.nextInt((25 - (-25)) + 1) + (-25);
            int difficultyLevel = (int) Math.round(((userLevel + variation) / 99.0) * 4);

            //This makes sure the variation doesn't cause the difficulty to go out of the bounds of
            //0 to 4.
            if (difficultyLevel > 4) {
                difficultyLevel = 4;
            } else if (difficultyLevel < 0) {
                difficultyLevel = 0;
            }
            int mapNumber = 5 - difficultyLevel;
            return mapNumber;
        }
    }

    /*
            This method is responsible for adding the markers to the map. The markers have different
            descriptions, and depending on their descriptions, they are given a colour on the map.
            The markers are also added to the HashMap of markers so they can be removed easily.
     */
    private void addMarkers() {

        for (Placemark marker : placemarks) {
            float colour = BitmapDescriptorFactory.HUE_CYAN;
            String desc = marker.getDescription();
            switch (desc) {
                case ("boring"):
                    colour = BitmapDescriptorFactory.HUE_CYAN;
                    break;
                case ("notboring"):
                    colour = BitmapDescriptorFactory.HUE_YELLOW;
                    break;
                case ("interesting"):
                    colour = BitmapDescriptorFactory.HUE_ORANGE;
                    break;
                case ("veryinteresting"):
                    colour = BitmapDescriptorFactory.HUE_RED;
                    break;
                case ("unclassified"):
                    colour = BitmapDescriptorFactory.HUE_CYAN;
                    break;
            }
            Marker mapMarker = mMap.addMarker(new MarkerOptions()
                    .position(marker.getCoordinates())
                    .snippet(marker.getDescription())
                    .title(marker.getWord())
                    .alpha(0.82f)
                    .icon(BitmapDescriptorFactory.defaultMarker(colour)));
            hashMapMarkers.put(marker.getLocation(), mapMarker);
        }
    }


    /*
            The distance walked is reset and the GoogleMapAPI client is connected.
     */
    @Override
    protected void onStart() {
        super.onStart();
        //reset distance walked
        distanceWalkedWhilePlaying = 0.0f;
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        /*
                The distance travelled in the current game is added to the total distance travelled
                in all of the users time playing the game. The total time played by the user is
                also updated in SharedPreferences by calculating the time played in the current
                game.
         */
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        float totalDistanceWalked = statistics.getFloat("totalDistanceWalked", 0.0f);
        SharedPreferences.Editor editor = statistics.edit();
        editor.putFloat("totalDistanceWalked", totalDistanceWalked + distanceWalkedWhilePlaying);
        editor.apply();

        long totalTimePlayed = statistics.getLong("totalPlayTime", Context.MODE_PRIVATE);
        endTime = Calendar.getInstance().getTimeInMillis();
        long elapsedTime = (endTime - startTime) / 1000;
        editor.putLong("totalPlayTime", totalTimePlayed + elapsedTime);
        editor.apply();

        //Disconnect the GoogleAPI client.
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Restart the timer for calculating the user's time played.
        startTime = Calendar.getInstance().getTimeInMillis();
    }


    /*
            This method creates a location request and specifies that it must be to a high accuracy.
            This is because we are working with distances that are small.
     */
    protected void createLocationRequest() {
        // Set the parameters for the location request.
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // preferably every 5 seconds.
        mLocationRequest.setFastestInterval(1000); // at most every second.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Can we access the user’s current location?
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            createLocationRequest();
        } catch (java.lang.IllegalStateException ise) {
            System.out.println("IllegalStateException thrown [onConnected]");
        }
        // Can we access the user’s current location?
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mLastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onConnectionSuspended(int flag) {
        System.out.println(" >>>> onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently.
        System.out.println(" >>>> onConnectionFailed");
    }


    /*
            When the user hits the back button to go back to the MainMenu from the maps activity,
            their progress for that game has to be saved so that it can be displayed in the
            incomplete song list. This method checks the user really wants to go back to the main
            menu, if they do, their progress is passed back to the MainMenu where it is saved to
            file.
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(
                        "Are you sure you want to exit? Your progress for this song will be saved.")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        //Create a new incomplete song.
                        IncompleteSong incompleteSong = new
                                IncompleteSong(getSongTitle(songNumber), collectedMarkers,
                                placemarks.size(), getTheSong(songNumber), mapNumber);
                        intent.putExtra("incompleteSong", incompleteSong);
                        intent.putExtra("theSong", (Serializable) getTheSong(songNumber));
                        setResult(RESULT_OK, intent);
                        MapsActivity.this.finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set the camera location at Edinburgh University.
        LatLng edinburgh = new LatLng(55.944425, -3.188396);
        float zoomLevel = 16.0f; //This goes up to 21.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edinburgh, zoomLevel));

        try {
            // Visualise current position with a small blue circle.
            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerClickListener(this);
        } catch (SecurityException se) {
            System.out.println("Security exception thrown [onMapReady]");
        }
        // Add ‘‘My location’’ button to the user interface.
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

    }

    @Override
    public void onLocationChanged(Location current) {

        if (mLastLocation != null) {
            //Get the total distance walked and add the distance walked during this game.
            SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
            float totalDistanceWalked = statistics.getFloat("totalDistanceWalked", 0.0f);
            int totalKilometres = (int) (totalDistanceWalked / 1000);
            distanceWalkedWhilePlaying += mLastLocation.distanceTo(current);

            /*
                    If the total distance walked (including the distance walked this game), is
                    greater than the next milestone i.e. 4000m, 5000m then that means that
                    another 1km has been walked and a songle coin must be given to the user.
             */
            if (totalDistanceWalked + distanceWalkedWhilePlaying > ((totalKilometres+1)*1000.0f)) {

                /*
                        The total distance is now updated in SharedPreferences and the distance
                        walked in this game, is reset.
                 */
                SharedPreferences.Editor editor = statistics.edit();
                editor.putFloat("totalDistanceWalked",
                        (totalDistanceWalked + distanceWalkedWhilePlaying));
                editor.apply();
                distanceWalkedWhilePlaying = 0.0f;

                //Songle coin dialog appears.
                songleCoinCollectedDialog = new Dialog(this);
                songleCoinCollectedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                songleCoinCollectedDialog.setContentView(R.layout.congratulations_songle_coin);
                songleCoinCollectedDialog.show();

                /*
                        When the OK button is clicked the dialog will disappear and the user can
                        continue with the current game.
                 */
                Button okBtn = (Button)
                        songleCoinCollectedDialog.findViewById(R.id.congratulations_ok_button);

                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        songleCoinCollectedDialog.dismiss();
                    }
                });

                /*
                        The number of songle coins available is incremented and also the total
                        number of lifetime songle coins is incremented for the statistics page.
                 */
                SharedPreferences songleCoins =
                        getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
                int totalNumberOfCoins = songleCoins.getInt("totalNumberOfCoins", 0);
                int currentNumberOfCoins = songleCoins.getInt("currentNumberOfCoins", 0);
                SharedPreferences.Editor editorSongleCoins = songleCoins.edit();
                editorSongleCoins.putInt("totalNumberOfCoins", (totalNumberOfCoins + 1));
                editorSongleCoins.putInt("currentNumberOfCoins", (currentNumberOfCoins + 1));
                editorSongleCoins.apply();
            }


            /*
                    When the user's location changes the centre of the camera should move to their
                    location.
             */
            LatLng currentLocation = new LatLng(
                    mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

        }

        //update location
        mLastLocation = current;
    }

    /*
            When a marker is clicked, a number of things will happen:
            - The marker should be collected and a toast will appear.
            - The number of collected markers will be updated in statistics.
            - The user should receive some points.
            - The users score will be updated in the top right of the screen.
            - If the users level increases due to scoring points, a dialog will appear.
            - If the user is not close enough to the word then they will be notified.
     */
    @Override
    public boolean onMarkerClick(Marker marker) {

        //So the user can't see the marker title/snippet, that contain information about the word.
        marker.hideInfoWindow();

        //If the marker that is clicked on is nearby then do the following:
        if (userCloseToMarker().contains(marker.getPosition())) {

            //Adding to shared preferences the total number of markers collected for statistics page
            incrementNumberOfLifetimeMarkersCollected();

            int points = 0;
            String desc = marker.getSnippet();
            switch (desc) {
                case ("boring"):
                    points = 5;
                    break;
                case ("notboring"):
                    points = 10;
                    break;
                case ("interesting"):
                    points = 15;
                    break;
                case ("veryinteresting"):
                    points = 20;
                    break;
                case ("unclassified"):
                    points = 15;
                    break;
            }



            //Remove the word from the Placemarks
            removeWordFromPlacemarks(marker);

            //Remove the marker from the map and show a toast.
            marker.remove();
            Toast.makeText(MapsActivity.this, "Well done! You collected the word: "
                            +
                            marker.getTitle()
                            + ". +" + points + " points!",
                    Toast.LENGTH_SHORT).show();

            //Obtain the score from SharedPreferences and update the score after a marker has been
            //collected.
            SharedPreferences scoreAndLevel = getSharedPreferences("score", Context.MODE_PRIVATE);
            int currentScore = scoreAndLevel.getInt("score", 0);

            int newScore = currentScore + points;
            SharedPreferences.Editor editor = scoreAndLevel.edit();
            editor.putInt("score", newScore);
            editor.apply();

            //Get the user's current level before it is updated with the score.
            int currentLevel = scoreAndLevel.getInt("level", 1);

            //Update the score on the progress bar in the fragment above the map and calculate
            //the new level.
            TopBarFragment fragment = (TopBarFragment)
                    getFragmentManager().findFragmentById(R.id.top_bar_fragment);
            fragment.updateLevel();

            //Get the users level after it has been updated
            int newLevel = scoreAndLevel.getInt("level", 1);

            //Check to see if the added points has caused the user to level up.
            didTheUserLevelUp(currentLevel, newLevel);

        } else {
            //display a message letting the user know they must be closer to the marker to
            //collect it
            Toast.makeText(MapsActivity.this, "You are not close enough to this word to collect it.",
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    /*
            If the collection of a marker scores the user enough points to level up then a
            dialog should popup letting them know, this is done by comparing the level before
            and after the marker is collected.
     */
    private void didTheUserLevelUp(int currentLevel, int newLevel) {
        if (newLevel == (currentLevel + 1)) {
            levelUpDialog = new Dialog(this);
            levelUpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            levelUpDialog.setContentView(R.layout.level_up);
            levelUpDialog.show();
            TextView tv = (TextView) levelUpDialog.findViewById(R.id.new_level_number_tv);
            tv.setText("" + newLevel);

            Button okBtn = (Button) levelUpDialog.findViewById(R.id.level_up_ok_button);

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    levelUpDialog.dismiss();
                }
            });
        }
    }


    /*
            This method removes the marker that was clicked on from the Placemarks.
     */
    private void removeWordFromPlacemarks(Marker marker) {
        String lyricLocation = "";
        Placemark forRemoval = null;
        for (Placemark word : placemarks) {
            if (word.getCoordinates().equals(marker.getPosition())) {
                lyricLocation = word.getLocation();
                forRemoval = word;
            }
        }
        placemarks.remove(forRemoval);

        //Add the lyric location (e.g. 13:5 - 13th line 5th word) to the set of collected markers.
        collectedMarkers.add(lyricLocation);
    }

    /*
            This method increments the lifetime number of markers collected for the statistics page.
     */
    private void incrementNumberOfLifetimeMarkersCollected() {
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        int LifetimeNumberOfMarkersCollected =
                statistics.getInt("LifetimeNumberOfMarkersCollected", 0);
        SharedPreferences.Editor editorStats = statistics.edit();
        editorStats.putInt("LifetimeNumberOfMarkersCollected",
                LifetimeNumberOfMarkersCollected + 1);
        editorStats.apply();

    }

    /*
            This returns an ArrayList of marker locations that are within a short distance of the
            user. If the user then clicks on any of these nearby markers they will be collected, and
            if they click on any other marker not contained within this list, they will get an
            error message.
     */
    private ArrayList<LatLng> userCloseToMarker() {

        ArrayList<LatLng> collectableMarkers = new ArrayList<>();

        for (Placemark marker : placemarks) {
            //Get the coordinates from the LatLng and convert them to a Location type
            //so that we can use the distanceTo method in the Location class.
            Location markerLocation = new Location(marker.getWord());
            markerLocation.setLatitude(marker.getCoordinates().latitude);
            markerLocation.setLongitude(marker.getCoordinates().longitude);

            //Calculate the distance to every marker
            float distance = Float.MAX_VALUE;
            try{
                distance = mLastLocation.distanceTo(markerLocation);
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            //If the distance to the marker ius less than 10 metres then it can be collected.
            if (distance < 2000.0f) {
                collectableMarkers.add(marker.getCoordinates());
            }
        }
        return collectableMarkers;
    }


    private class ASyncKMLDownloader extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            /*
            When loading the map display a circular loading map that says "Loading Map...".
             */
            pgDialog = new ProgressDialog(MapsActivity.this);
            pgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pgDialog.setMessage("Loading Map...");
            pgDialog.setIndeterminate(true);
            pgDialog.show();

            songNumber = chooseSongNumber();
        }

        @Override
        protected Integer doInBackground(String... params) {

            //Call the appropriate methods to read the text file from the url
            //and after that parse the kml map data.
            readAndParseTextFile(songNumber);
            return 0;
        }

        private void readAndParseTextFile(String songNumber) {

            //A HashMap that contains all the lines of the song and the line numbers.
            wholeSong = new HashMap<>();

            text_url = "http://www.inf.ed.ac.uk/teaching/courses/selp/data/songs/" + songNumber
                    + "/words.txt";
            URL url = null;
            try {
                url = new URL(text_url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            //Read all the text returned by the server.
            try {
                //Read in the text file using the url.
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String songLine;
                Integer i = 1;
                //While not at the end of the text file.
                while ((songLine = in.readLine()) != null) {
                    //Create a HashMap of all the words of the song and their position in the line.
                    HashMap<Integer, String> lineOfSong = new HashMap();
                    Integer j = 1;

                    //Remove the first 7 characters to remove the line numbers and whitespace.
                    //Split the string into the words.
                    for (String word : songLine.substring(7).split(" ")) {
                        //Add the word to the HashMap for that line.
                        lineOfSong.put(j, word);
                        j++;
                    }
                    //Add that line to the HashMap for the song.
                    wholeSong.put(i, lineOfSong);
                    i++;
                }
                //Close buffer stream.
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            //Now once we have the words linked to the line numbers we can parse the kml data.
            XmlPullParser receivedData = tryDownloadKmlData(songNumber);
            tryParseKmlData(receivedData);
        }

        private XmlPullParser tryDownloadKmlData(String songNumber) {
            try {
                //A map number is calculated to be used in the KML URL.
                mapNumber = getMapDifficulty(userLevel);

                kml_url =
                        "http://www.inf.ed.ac.uk/teaching/courses/selp/data/songs/" + songNumber +
                                "/map" + mapNumber + ".kml";

                URL kmlURL = new URL(kml_url);
                //Create a new instance of the XmlPullParser class and set the input stream.
                XmlPullParser receivedData = XmlPullParserFactory.newInstance().newPullParser();
                receivedData.setInput(kmlURL.openStream(), null);
                return receivedData;
            } catch (XmlPullParserException e) {
                Log.e("Songle", "XmlPullParserException - tryDownloadKmlData", e);
            } catch (IOException e) {
                Log.e("Songle", "IOException - tryDownloadKmlData", e);
            }
            return null;
        }

        private void tryParseKmlData(XmlPullParser receivedData) {
            //If there is data from the input stream call the method to parse the data.
            if (receivedData != null) {
                try {
                    processReceivedData(receivedData);
                } catch (XmlPullParserException e) {
                    Log.e("Songle", "XmlPullParserException - tryParseKmlData", e);
                } catch (IOException e) {
                    Log.e("Songle", "IOException - tryParseKmlData", e);
                }
            }
        }

        private void processReceivedData(XmlPullParser kmlData) throws IOException, XmlPullParserException {

            String name = "";
            String description = "";
            String styleUrl = "";
            String coordinates = "";

            /*
            This block goes through the kml data and when it reaches a new Placemark tag
            it extracts the raw data into a variable. The temporary variables are then
            used to create a new Placemark object which is added to the ArrayList of placemarks.
             */
            int eventType = kmlData.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String data = kmlData.getName();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (data.equalsIgnoreCase("Placemark")) {
                            break;
                        } else if (data.equalsIgnoreCase("name")) {
                            name = kmlData.nextText();
                        } else if (data.equalsIgnoreCase("description")) {
                            description = kmlData.nextText();
                        } else if (data.equalsIgnoreCase("styleUrl")) {
                            styleUrl = kmlData.nextText();
                        } else if (data.equalsIgnoreCase("coordinates")) {
                            coordinates = kmlData.nextText();

                            String location = name;
                            //Break up the line and the word location.
                            String[] lineWord = name.split(":");
                            //Find the actual word by searching the hashmap with the line number
                            //and word location in that line.
                            String word = wholeSong.get(Integer.parseInt(lineWord[0]))
                                    .get(Integer.parseInt(lineWord[1]));

                            //Instantiate/create a new placemark.
                            Placemark newPlacemark = new Placemark(
                                    word, location, description, styleUrl, coordinates);

                            //Add the new Placemark to the set of placemarks.
                            placemarks.add(newPlacemark);
                        }
                        break;
                }
                eventType = kmlData.next();
            }
        }


        //After the background thread has been executed add the markers to the map.
        @Override
        protected void onPostExecute(Integer integer) {
            addMarkers();
            collectedMarkers = (HashSet<String>) getIntent().getSerializableExtra(
                    "collectedMarkersMainMenu");
            removeCollectedMarkersFromMap();
        }
    }


}
