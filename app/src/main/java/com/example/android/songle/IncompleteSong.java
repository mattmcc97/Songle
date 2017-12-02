package com.example.android.songle;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by Matthew McCarrison on 27/11/2017.
 * <p>
 * This class is used to model an incomplete song. An incomplete song has to store:
 * - songTitle: so the MapsActivity knows which markers to show when the incomplete song has
 * been clicked.
 * - collectedMarkers: the collected markers that correspond to that song, so the MapsActivity
 * knows which markers to remove when the song is reloaded.
 * - totalNumberOfPlacemarks: so the progress can be calculated against the collectedMarkers.
 * - theSong: the Song that it was originally created as.
 * - levelOfDifficulty:  the level of difficulty that was used when it was created - this is needed
 * so that if a user started it on level 1 and didn't finish it until they
 * were level 99, the difficulty would be the same each time they continued
 * with the song.
 */

public class IncompleteSong implements Serializable {
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
