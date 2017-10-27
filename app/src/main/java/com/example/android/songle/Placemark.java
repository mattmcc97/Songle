package com.example.android.songle;

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

    public String getCoordinates() {
        return coordinates;
    }
}
