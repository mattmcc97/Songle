package com.example.android.songle;

/**
 * Created by Matthew on 26/11/2017.
 */

public class Model {

    public static final int INCOMPLETE_TYPE = 0;
    public static final int COMPLETE_TYPE = 1;
    public static final int SEPARATOR = 2;

    public int type;
    public int progress;
    public String text;
    public String link;

    public Model(int type, String text, int progress, String link)
    {
        this.type = type;
        this.progress = progress;
        this.text = text;
        this.link = link;
    }
}
