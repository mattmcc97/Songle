package com.example.android.songle;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Matthew on 26/10/2017.
 */

public class Placemark implements Serializable {
    public final String word;
    public final String description;
    public final String styleUrl;
    public final String coordinates;


    public Placemark(String word, String description, String styleUrl, String coordinates) {
        this.word = word;
        this.description = description;
        this.styleUrl = styleUrl;
        this.coordinates = coordinates;
    }

    public String getWord() {
        return word;
    }

    public String getDescription() {
        return description;
    }

    public String getStyleUrl() {
        return styleUrl;
    }

    public LatLng getCoordinates() {
        //remove 0 off the coordinates string and split it on the comma to extract lat and long Strings
        String[] coords = coordinates.substring(0, coordinates.length() - 2).split(",");
        LatLng latlong = new LatLng(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
        return latlong;
    }
}
