package com.example.android.songle;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    private Location mLastLocation;
    private static final String TAG = "MapsActivity";

    private int userLevel = 2;
    private String kml_url = "";
    private  String text_url = "";

    private static Placemark placemark;
    private static HashSet<Placemark> placemarks;

    private HashMap<Integer, HashMap<Integer, String>> wholeSong;

    private ArrayList<Song> songs;
    public String songTitle;
    private String songNumber;

    private ArrayList<String> collectedMarkers;

    private ProgressDialog pgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        placemarks = new HashSet<>();
        wholeSong = new HashMap<>();
        songs =  getIntent().getParcelableArrayListExtra("listOfSongs");
        collectedMarkers = new ArrayList<>();

        Log.i(TAG, "onCreate: " + songs);

        Log.i("MainMenu", "newSong: listOfSongs: " + songs.get(0));
        Log.i("MainMenu", "newSong: listOfSongs: " + songs.get(1));
        Log.i("MainMenu", "newSong: listOfSongs: " + songs.get(2));

        //Execute the methods in the AsyncTask class
        ASyncKMLDownloader downloader = new ASyncKMLDownloader();
        downloader.execute();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // Long running activities are performed asynchronously in order to
        // keep the user interface responsive
        mapFragment.getMapAsync(this);
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void goToCurrentLocation(View view) {
        //On the click of location button this method causes the UI to zoom to the users current location

        float zoomLevel = 19.0f; //This goes up to 21
        LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));
        /*Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.congratulations_songle_coin);
        dialog.show();*/

    }

    private String getSongTitle(String songNum){
        String songName = "";
        for(Song song: songs){
            if(songNum.equals(song.getNumber())){
                songName = song.getTitle();
            }
        }
        return songName;
    }

    public void guessSong(View view) {

        /*When the guess song button is pressed get the song title using the song number and then
        pass the song title and the HashMap containing all the lyrics to the song, to the
        GuessActivity
         */
        songTitle = getSongTitle(songNumber);
        Log.i(TAG, "guessSong: " + songTitle);
        Intent intent = new Intent(this, GuessSong.class);
        intent.putExtra("songTitle", songTitle);
        intent.putExtra("wholeSong", wholeSong);
        intent.putExtra("collectedMarkers", collectedMarkers);
        startActivity(intent);

    }

    private String chooseSongNumber(){
        //When the new song button is clicked, a new random song is selected from the available list
        Random rand = new Random();
        Log.i(TAG, "chooseSongNumber: sizeOfSongs: " + songs.size());
        int randomSongNumberInt = rand.nextInt(songs.size()) + 1;
        String randomSongNumber = "";
        if(randomSongNumberInt <= 9){
            randomSongNumber = "0" + randomSongNumberInt;
        }else{
            randomSongNumber = "" + randomSongNumberInt;
        }
        return randomSongNumber;
    }

    private int getMapDifficulty(int userLevel){
        //Produces a map difficulty number based on the users level

        //Calculates the fraction  of the max level (99), if the user was level 99 they would get a
        //difficulty of 4 and a level of 1 (5 - 4 = 1). If the user was level 1 they would get a
        //difficulty of 0 and a level of 5 (5 - 0 = 5).
        //variation is used so that the user can get a small range of map difficulties
        Random rand = new Random();
        int variation = rand.nextInt((25 - (-25)) + 1) + (-25);
        Log.i(TAG, "getMapDifficulty: SongNumber variation: " + variation);
        int difficulty = (int) Math.round(((userLevel+variation)/99.0)*4);
        //make sure the variation doesn't cause the difficulty to be an impossible value
        if(difficulty > 4 ){
            difficulty = 4;
        }else if(difficulty < 0){
            difficulty = 0;
        }
        int level = 5 - difficulty;
        return level;
    }

    private void addMarkers() {

        int count = 0;
        Log.i(TAG, "addMarkers: Adding markers...");
        for (Placemark marker : placemarks) {
            float colour = BitmapDescriptorFactory.HUE_BLUE;
            String desc = marker.getDescription();
            switch (desc) {
                case ("boring"):
                    colour = 170.0f;
                    break;
                case ("notboring"):
                    colour = 190.0f;
                    break;
                case ("interesting"):
                    colour = 210.0f;
                    break;
                case ("veryinteresting"):
                    colour = 230.0f;
                    break;
                case ("unclassified"):
                    colour = BitmapDescriptorFactory.HUE_AZURE;
                    break;
            }
            mMap.addMarker(new MarkerOptions()
                    .position(marker.getCoordinates())
                    .snippet(marker.getDescription())
                    .title(marker.getWord())
                    .alpha(0.82f)
                    .icon(BitmapDescriptorFactory.defaultMarker(colour)));
            count++;
        }
        Log.i(TAG, "addMarkers: Number of markers: " + count);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    protected void createLocationRequest() {
        // Set the parameters for the location request
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // preferably every 5 seconds
        mLocationRequest.setFastestInterval(1000); // at most every second
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
        // the failure silently
        System.out.println(" >>>> onConnectionFailed");
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit? Your progress for this song will be saved.")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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

        // Add a marker in Sydney and move the camera
        LatLng edinburgh = new LatLng(55.944425, -3.188396);
        //mMap.addMarker(new MarkerOptions().position(edinburgh).title("Marker in Edinburgh"));
        float zoomLevel = 16.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edinburgh, zoomLevel));

        try {
            // Visualise current position with a small blue circle
            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        } catch (SecurityException se) {
            System.out.println("Security exception thrown [onMapReady]");
        }
        // Add ‘‘My location’’ button to the user interface
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //mMap.getUiSettings().setZoomControlsEnabled(true);

    }

    @Override
    public void onLocationChanged(Location current) {
        Log.i(TAG, "Running method: onLocationChanged");
        //update location
        mLastLocation = current;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //if the marker clicked is nearby
        if (userCloseToMarker().contains(marker.getPosition())) {
            int points = 15;

            Toast.makeText(MapsActivity.this, "Well done! You collected the word: "
                            +
                            marker.getTitle()
                            + ". +" + points + " points!",
                    Toast.LENGTH_LONG).show();

            //remove the word from the set of lyrics
            //lyric location e.g. 18:13 18th line 13th word
            String lyricLocation = "";
            Placemark forRemoval = null;
            for(Placemark word : placemarks){
                Log.i(TAG, "onMarkerClick: word: (" + word.getWord() + " - " + marker.getTitle()+ ")");
                if(word.getCoordinates().equals(marker.getPosition())){
                    Log.i(TAG, "onMarkerClick: boom: " + marker.getTitle());
                    lyricLocation = word.getLocation();
                    forRemoval = word;
                }
            }

            Log.i(TAG, "onMarkerClick: " + lyricLocation);
            collectedMarkers.add(lyricLocation);

            /*String[] lineWord = lyricLocation.split(":");
            Log.i(TAG, "onMarkerClick: wordRemoved: " +
                    wholeSong.get(Integer.parseInt(lineWord[0])).get(Integer.parseInt(lineWord[1])));
            Log.i(TAG, "onMarkerClick: wordRemoved: " + lyricLocation + wholeSong);

            wholeSong.get(Integer.parseInt(lineWord[0])).get(Integer.parseInt(lineWord[1]));*/

            placemarks.remove(forRemoval);

            //remove the marker from the map and show a toast
            marker.remove();

            Log.i(TAG, "onMarkerClick: Number of remaining markers: " + placemarks.size());
            /*Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.level_up);
            dialog.show();*/

        } else {
            //display a message letting the user know they must be closer to the marker to
            //collect it
            Toast.makeText(MapsActivity.this, "You are not close enough to this word to collect it.",
                    Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private ArrayList<LatLng> userCloseToMarker() {
        Log.i(TAG, "Running method: userCloseToMarker");
        //if there is a nearby marker then return that marker
        ArrayList<LatLng> collectableMarkers = new ArrayList<>();
        for (Placemark marker : placemarks) {
            //Get the coordinates from the LatLng and convert them to a Location type
            //so that we can use the distanceTo method in the Location class.
            Location markerLocation = new Location(marker.getWord());
            markerLocation.setLatitude(marker.getCoordinates().latitude);
            markerLocation.setLongitude(marker.getCoordinates().longitude);

            float distance = mLastLocation.distanceTo(markerLocation);
            //7.5f seems a reasonable distance to be away

            if (distance < 2000.0f) {
                Log.i(TAG, "inside 20: " + marker.getCoordinates() + " - " + marker.getWord());
                collectableMarkers.add(marker.getCoordinates());
            }
        }
        //return nearby marker
        return collectableMarkers;
    }


    private class ASyncKMLDownloader extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
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
            // and after that parse the kml map data
            Log.i(TAG, "doInBackground: " + "Started");
            readTextFile(songNumber);
            Log.i(TAG, "doInBackground: guessSong: " + songNumber);
            return 0;
        }

        private void readTextFile(String songNumber) {

            //A HashMap that contains all the lines of the song and the line numbers
            wholeSong = new HashMap<>();

            text_url = "http://www.inf.ed.ac.uk/teaching/courses/selp/data/songs/" + songNumber
                    + "/words.txt";
            URL url = null;
            try {
                url = new URL(text_url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            // Read all the text returned by the server
            try {
                //read in the text file using the url
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String songLine;
                Integer i = 1;
                //while not at the end of the text file
                while ((songLine = in.readLine()) != null) {
                    //create a HashMap of all the words of the song and their position in the line
                    HashMap<Integer, String> lineOfSong = new HashMap();
                    Integer j = 1;

                    //remove the first 7 characters to remove the line numbers and whitespace
                    //split the string into the words
                    for(String word : songLine.substring(7).split(" ")) {
                        //add the word to the HashMap for that line
                        lineOfSong.put(j, word);
                        j++;
                    }
                    //addthat line to the HashMap for the song
                    wholeSong.put(i, lineOfSong);
                    i++;
                }
                //close buffer stream
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "readTextFile: " + Arrays.asList(wholeSong));

            //Now once we have the words linked to the line numbers we can parse the kml data
            XmlPullParser receivedData = tryDownloadKmlData(songNumber);
            tryParseKmlData(receivedData);
        }

        private XmlPullParser tryDownloadKmlData(String songNumber) {
            try {
                Integer difficulty = getMapDifficulty(userLevel);
                kml_url =
                        "http://www.inf.ed.ac.uk/teaching/courses/selp/data/songs/" + songNumber +
                                "/map" + difficulty + ".kml";
                Log.i(TAG, "tryDownloadKmlData: SongNumber: " + songNumber +
                        " - MapDifficulty: " + difficulty);
                URL kmlURL = new URL(kml_url);
                //Create a new instance of the XmlPullParser class and set the input stream
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
            //If there is data from the input stream call the method to parse the data
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


                            Log.i(TAG, "processReceivedData: " + name);
                            String location = name;
                            //break up the line and the word location
                            String[] lineWord = name.split(":");
                            //Find the actual word by searching the hashmap with the line number
                            //and word location in that line
                            String word = wholeSong.get(Integer.parseInt(lineWord[0]))
                                    .get(Integer.parseInt(lineWord[1]));

                            //Instantiate/create a new placemark
                            placemark = new Placemark(word, location, description, styleUrl, coordinates);
                            Log.i("Adding to placemarks:", "Coords: " + placemark.getCoordinates()
                            + " Word: " + word);

                            //add the new Placemark to the set of placemarks
                            placemarks.add(placemark);
                        }
                        break;
                }
                eventType = kmlData.next();
            }
        }


        //After the background thread has been executed add the markers to the map
        @Override
        protected void onPostExecute(Integer integer) {
            addMarkers();
            pgDialog.hide();
        }
    }


}
