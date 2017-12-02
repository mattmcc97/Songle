package com.example.android.songle;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Matthew on 18/10/2017.
 *
 * This is the class for a Song. This is what is retrieved from the songs.xml file.
 *
 * The Song class stores:
 * title - This is the title of the song.
 * link - This is the part after the "https://youtu.be/" that is passed to the YouTube player.
 * number - This is the song number.
 * artist - This is the artist of the song.
 */

public class Song implements Parcelable, Serializable {
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

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getNumber() {
        return number;
    }

    //Extracts the part of the link after the "https://youtu.be".
    public String getLink() {
        String idOfLink = (link.split("/"))[3];
        return idOfLink;
    }


    /*
        The following methods are used to allow a Placemark to be parcelable, this is a more
        efficient way to pass data than Serializing it.
    */
    protected Song(Parcel in) {
        title = in.readString();
        link = in.readString();
        number = in.readString();
        artist = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(link);
        dest.writeString(number);
        dest.writeString(artist);
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

}
