package com.example.android.songle;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    private Location mLastLocation;
    private static final String TAG = "MapsActivity";
    private static final String kml_url =
            "http://www.inf.ed.ac.uk/teaching/courses/selp/data/songs/01/map5.kml";

    private static Placemark placemark;
    private static ArrayList<Placemark> placemarks = new ArrayList<Placemark>();

    public String songTitle = "Bohemian Rhapsody";

    private ProgressDialog pgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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
        float zoomLevel = 19.0f; //This goes up to 21
        LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));

    }

    public void guessSong(View view) {

        Intent intent = new Intent(this, GuessSong.class);
        intent.putExtra("songTitle", songTitle);
        startActivity(intent);

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
                    colour = BitmapDescriptorFactory.HUE_BLUE;
                    break;
            }
            mMap.addMarker(new MarkerOptions()
                    .position(marker.getCoordinates())
                    .snippet(marker.getDescription())
                    .title(marker.getName())
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
        if (marker.getPosition().equals(userCloseToMarker())) {
            int points = 10;
            //remove the marker from the map and show a toast
            marker.remove();
            Toast.makeText(MapsActivity.this, "Well done! You collected the word: "
                            + marker.getTitle() + ". +" + points + " points!",
                    Toast.LENGTH_LONG).show();

        } else {
            //display a message letting the user know they must be closer to the marker to
            //collect it
            Toast.makeText(MapsActivity.this, "You are not close enough to this word to collect it.",
                    Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private LatLng userCloseToMarker() {
        Log.i(TAG, "Running method: userCloseToMarker");
        //if there is a nearby marker then return that marker
        LatLng collectableMarker = null;
        for (Placemark marker : placemarks) {
            //Get the coordinates from the LatLng and convert them to a Location type
            //so that we can use the distanceTo method in the Location class.
            Location markerLocation = new Location(marker.getName());
            markerLocation.setLatitude(marker.getCoordinates().latitude);
            markerLocation.setLongitude(marker.getCoordinates().longitude);

            float distance = mLastLocation.distanceTo(markerLocation);
            //7.5f seems a reasonable distance to be away

            if (distance < 7.5f) {
                //inRange = true;
                collectableMarker = marker.getCoordinates();
            }
        }
        //return nearby marker
        return collectableMarker;
    }


    private class ASyncKMLDownloader extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            pgDialog = new ProgressDialog(MapsActivity.this);
            pgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pgDialog.setMessage("Loading Map...");
            pgDialog.setIndeterminate(true);
            pgDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {

            //Call the appropriate methods to download and parse the xml data
            Log.i(TAG, "doInBackground: " + "Started");
            XmlPullParser receivedData = tryDownloadKmlData();
            int songsFound = tryParseKmlData(receivedData);
            return songsFound;
        }

        private XmlPullParser tryDownloadKmlData() {
            try {
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

        private int tryParseKmlData(XmlPullParser receivedData) {
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
            return 0;
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
                            placemark = new Placemark(name, description, styleUrl, coordinates);
                            Log.i("Adding to placemarks:", "Coords: " + placemark.getCoordinates());
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
