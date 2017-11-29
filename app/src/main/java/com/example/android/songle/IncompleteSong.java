package com.example.android.songle;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Matthew on 27/11/2017.
 */

public class IncompleteSong implements Serializable{
    public final String songTitle;
    public HashSet<String> collectedMarkers;
    public final Integer totalNumberOfPlacemarks;
    public final Song theSong;
    public final Integer levelOfDifficulty;


    public IncompleteSong(String songTitle, HashSet<String> collectedMarkers,
                          Integer totalNumberOfPlacemarks, Song theSong, Integer levelOfDifficulty) {
        this.songTitle = songTitle;
        this.collectedMarkers = collectedMarkers;
        this.totalNumberOfPlacemarks = totalNumberOfPlacemarks;
        this.theSong = theSong;
        this.levelOfDifficulty = levelOfDifficulty;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public HashSet<String> getCollectedMarkers() {
        return collectedMarkers;
    }

    public Integer getTotalNumberOfPlacemarks() {
        return totalNumberOfPlacemarks;
    }

    public Song getTheSong() {
        return theSong;
    }
}
