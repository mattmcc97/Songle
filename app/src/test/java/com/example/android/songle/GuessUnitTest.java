package com.example.android.songle;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Matthew on 06/12/2017.
 */

public class GuessUnitTest {

    public static String guessWrong1 = "Song 2";
    public static String guessWrong2 = "aBohemian Rhapsody";
    public static String guessWrong3 = "";
    public static String guessWrong4 = "Bohemian Rhapsodya";
    public static ArrayList<String> wrongGuesses;

    public static String guessCorrect1 = "Bohemian Rhapsody";
    public static String guessCorrect2 = "BohemianRhapsody";
    public static String guessCorrect3 = "!$%^&£^£&Bohemian Rhapsody";
    public static String guessCorrect4 = "bohemian!#@^rhapsody";
    public static String guessCorrect5 = "bOh EmIA Nrha pSODY£%^£%&£%^&£%";
    public static ArrayList<String> correctGuesses;

    public static String answer = "Bohemian Rhapsody";

    public static GuessSong guessTheSong;

    @BeforeClass
    public static void setUp(){
        guessTheSong = new GuessSong();

        wrongGuesses = new ArrayList<>();
        wrongGuesses.add(guessWrong1);
        wrongGuesses.add(guessWrong2);
        wrongGuesses.add(guessWrong3);
        wrongGuesses.add(guessWrong4);

        correctGuesses = new ArrayList<>();
        correctGuesses.add(guessCorrect1);
        correctGuesses.add(guessCorrect2);
        correctGuesses.add(guessCorrect3);
        correctGuesses.add(guessCorrect4);
        correctGuesses.add(guessCorrect5);
    }

    @Test
    public void testWrongGuesses() {
        for (String wrongGuess : wrongGuesses){
            assertFalse(guessTheSong.isGuessCorrect(wrongGuess, answer));
        }
    }

    @Test
    public void testCorrectGuesses() {
        for (String correctGuess : correctGuesses){
            assertTrue(guessTheSong.isGuessCorrect(correctGuess, answer));
        }
    }

}
