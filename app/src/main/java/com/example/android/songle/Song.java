package com.example.android.songle;

/**
 * Created by Matthew on 18/10/2017.
 */

public class Song {
    public final String title;
    public final String link;
    public final String number;
    public final String artist;


    public Song(String title, String number, String link, String artist) {
        this.title = title;
        this.link = link;
        this.number = number;
        this.artist = artist;
    }

    public String getTitle(){
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getNumber() {
        return number;
    }

    public String getLink() {
        return link;
    }
}
