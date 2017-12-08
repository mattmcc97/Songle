package com.example.android.songle;

import android.util.Log;

import org.junit.BeforeClass;
import org.junit.Test;

import static android.content.ContentValues.TAG;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Matthew on 06/12/2017.
 */

public class SongUnitTest {

    public static String title = "Bohemian Rhapsody";
    public static String link = "https://youtu.be/fJ9rUzIMcZQ";
    public static String number = "01";
    public static String artist = "Queen";
    public static Song song;

    @BeforeClass
    public static void setUp(){
        song = new Song(title, number, link, artist);
    }

    @Test
    public void testTitleGetter() {
        assertTrue("Bohemian Rhapsody".equals(song.getTitle()));
    }

    @Test
    public void testLinkGetter() {
        assertTrue("fJ9rUzIMcZQ".equals(song.getLink()));
    }

    @Test
    public void testNumberGetter() {
        assertTrue("01".equals(song.getNumber()));
    }

    @Test
    public void testArtistGetter() {
        assertTrue("Queen".equals(song.getArtist()));
    }
}
