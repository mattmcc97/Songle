package com.example.android.songle;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Matthew on 26/10/2017.
 */

public class Placemark {
    public final String name;
    public final String description;
    public final String styleUrl;
    public final String coordinates;


    public Placemark(String name, String description, String styleUrl, String coordinates) {
        this.name = name;
        this.description = description;
        this.styleUrl = styleUrl;
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
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
