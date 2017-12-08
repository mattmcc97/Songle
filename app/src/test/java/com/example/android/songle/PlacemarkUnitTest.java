package com.example.android.songle;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Matthew on 06/12/2017.
 */

public class PlacemarkUnitTest {

    public static String word = "Scaramouche,";
    public static String location = "35:1";
    public static String description = "veryinteresting";
    public static String styleUrl = "#veryinteresting";
    public static String coordinates = "-3.1844496246809113,55.943760470402964,0";
    public static Placemark placemark;

    @BeforeClass
    public static void setUp() {
        placemark = new Placemark(word, location, description, styleUrl, coordinates);
    }

    @Test
    public void testWordGetter() {
        assertTrue("Scaramouche,".equals(placemark.getWord()));
    }

    @Test
    public void testLocationGetter() {
        assertTrue("35:1".equals(placemark.getLocation()));
    }

    @Test
    public void testDescriptionGetter() {
        assertTrue("veryinteresting".equals(placemark.getDescription()));
    }

    @Test
    public void testStyleUrl() {
        assertTrue("#veryinteresting".equals(placemark.getStyleUrl()));
    }

    @Test
    public void testCoordinatesGetter() {
        LatLng coords = new LatLng(55.943760470402964, -3.1844496246809113);
        assertTrue(coords.equals(placemark.getCoordinates()));
    }
}
