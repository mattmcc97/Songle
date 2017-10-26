package com.example.android.songle;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainMenu extends AppCompatActivity{

    //URL
    private static final String xml_url = "http://www.inf.ed.ac.uk/teaching/courses/selp/data/songs/songs.xml";

    //An ArrayList containing all of the songs
    public static ArrayList<Song> songs = new ArrayList<Song>();

    private static Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Execute the methods in the AsyncTask class
        AsyncXMLDownloader downloader = new AsyncXMLDownloader();
        downloader.execute();

    }

    public void newSong(View view){
        //When the new song button is clicked, open the MapsActivity
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
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

    private class AsyncXMLDownloader extends AsyncTask<Object, String, Integer>{

        @Override
        protected Integer doInBackground(Object... params) {
            //Call the appropriate methods to download and parse the xml data
            XmlPullParser receivedData = tryDownloadXmlData();
            int songsFound = tryParseXmlData(receivedData);
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
                            songs.add(song);
                        }
                        break;
                }
                eventType = xmlData.next();
            }
        }
    }

}
