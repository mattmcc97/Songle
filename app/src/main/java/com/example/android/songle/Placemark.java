package com.example.android.songle;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Matthew on 26/10/2017.
 *
 * This is the class for a Placemark. This is what is retrieved from the KML file.
 *
 * The Placemark class stores:
 * word - This is the word linked to the placemark.
 * location - This is the location of the word within the lyrics e.g. 13:5 = 13th line 5th word.
 * description - This is either boring, not boring, interesting, very interesting or unclassified.
 * styleUrl - the same as description but contains a '#' before it.
 * coordinates - a String containing the coordinates of the marker within Edinburgh University.
 *
 */

public class Placemark implements Parcelable, Serializable {
    public final String word;
    public final String location;
    public final String description;
    public final String styleUrl;
    public final String coordinates;


    public Placemark(String word, String location, String description, String styleUrl, String coordinates) {
        this.word = word;
        this.location = location;
        this.description = description;
        this.styleUrl = styleUrl;
        this.coordinates = coordinates;
    }

    public String getWord() {
        return word;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getStyleUrl() {
        return styleUrl;
    }

    //Converts the coordinates from a String to a LatLng type.
    public LatLng getCoordinates() {
        //remove z axis (0) off the coordinates string and split it on the comma
        //to extract lat and long Strings
        String[] coords = coordinates.substring(0, coordinates.length() - 2).split(",");
        LatLng latlong = new LatLng(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
        return latlong;
    }

    /*
        The following methods are used to allow a Placemark to be parcelable, this is a more
        efficient way to pass data than Serializing it.
    */
    protected Placemark(Parcel in) {
        word = in.readString();
        location = in.readString();
        description = in.readString();
        styleUrl = in.readString();
        coordinates = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(word);
        dest.writeString(location);
        dest.writeString(description);
        dest.writeString(styleUrl);
        dest.writeString(coordinates);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Placemark> CREATOR = new Parcelable.Creator<Placemark>() {
        @Override
        public Placemark createFromParcel(Parcel in) {
            return new Placemark(in);
        }

        @Override
        public Placemark[] newArray(int size) {
            return new Placemark[size];
        }
    };
}
