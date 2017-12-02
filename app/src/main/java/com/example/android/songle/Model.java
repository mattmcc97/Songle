package com.example.android.songle;

/**
 * Created by Matthew on 26/11/2017.
 *
 * This class Model is used to populate the RecyclerView with various types of data. The Model can
 * be used to create an incomplete song to be added to the RecyclerView on the MainMenu, it can
 * be used to create a complete song or it can be used to create a separator to split up different
 * item types.
 *
 */

public class Model {

    public static final int INCOMPLETE_TYPE = 0;
    public static final int COMPLETE_TYPE = 1;
    public static final int SEPARATOR = 2;

    public int type;
    public int progress;
    public String text;
    public String link;
    public Song theSong;
    public IncompleteSong theIncompleteSong;

    public Model(int type, String text, int progress, String link, Song theSong,
                 IncompleteSong theIncompleteSong) {
        this.type = type;
        this.progress = progress;
        this.text = text;
        this.link = link;
        this.theSong = theSong;
        this.theIncompleteSong = theIncompleteSong;
    }
}
