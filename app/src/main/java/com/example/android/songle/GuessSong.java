package com.example.android.songle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class GuessSong extends AppCompatActivity {

    private String TAG = "GuessSong";

    //Dialog that appears if the user guesses the song correctly.
    Dialog dialogCorrect;
    //Dialog that appears if the user guesses the song incorrectly.
    Dialog dialogWrong;
    //Dialog that appears if the user levels up by guessing the song correctly.
    Dialog levelUpDialog;

    public static String songTitle = "";
    private HashMap<Integer, HashMap<Integer, String>> wholeSong = null;
    private HashSet<String> collectedMarkers = null;
    private HashSet<Placemark> placemarks = null;

    private float distanceWalkedWhilePlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_song);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
                The song title is passed in by the maps activity to this activity after it has been
                generated. The song title is used in this activity to check if the user's guess is
                correct.

         */
        songTitle = getIntent().getStringExtra("songTitle");

        /*
                The wholeSong contains a HashMap where the keys is the line number and the value is
                another HashMap corresponding to the line of the song. This contains the word number
                within the line and the word itself. This data is passed in to this activity from the
                MapsActivity.
         */
        wholeSong = (HashMap<Integer, HashMap<Integer, String>>)
                getIntent().getSerializableExtra("wholeSong");

        /*
                A set of markers that have been collected i.e. (13:5) which is the marker that is
                linked to the word on the 13th line 5 across. This data is passed in to this activity
                from the MapsActivity.
         */
        collectedMarkers = (HashSet<String>) getIntent().getSerializableExtra("collectedMarkers");

        /*
                A set of placemarks, this contains all the markers that would be visible on the map.
                This data is passed in to this activity from the MapsActivity.
         */
        placemarks = (HashSet<Placemark>) getIntent().getSerializableExtra("placemarks");

        /*
                This is used to track the total distance walked by the user in this game. If the user
                guesses correctly, then this data is used to update the total distance ever walked by
                the user, in the SharedPreferences, for the statistics.
         */
        distanceWalkedWhilePlaying = getIntent().getFloatExtra("distanceWalkedWhilePlaying", 0.0f);

        //Refresh the text containing number of songle coins.
        updateNumberOfSongleCoinsText();

        //Builds the song on screen, replacing words that haven't been colected with underlines.
        buildSong(wholeSong);
    }


    /*
                This method is used to update the text box containing the number of songle coins
                by accessing the SharedPreferences.
    */
    private void updateNumberOfSongleCoinsText() {
        SharedPreferences songleCoins = getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
        int currentNumberOfCoins = songleCoins.getInt("currentNumberOfCoins", 0);
        TextView numberOfSongleCoinsTv = (TextView) findViewById(R.id.number_of_coins_tv);
        numberOfSongleCoinsTv.setText("You have " + currentNumberOfCoins + " Songle coins available.");

    }


    /*
            A method that takes the HashMap containing the line numbers and word locations. It goes
            through this HashMap checking it against the collected markers, to decide whether to
            show the word (collected) or replace the word with underlines (not collected). This creates
            a large String that is then used to set the text in the TextView.
     */
    private void buildSong(HashMap<Integer, HashMap<Integer, String>> songLyrics) {
        String song = "";
        for (int i = 1; i <= songLyrics.size(); i++) {
            HashMap<Integer, String> songLine = null;
            songLine = songLyrics.get(i);
            //Insert line numbers i.e. "1. ", "2. " etc.
            song += i + ". ";
            for (int j = 1; j <= songLine.size(); j++) {
                String wordLocation = i + ":" + j;
                if (collectedMarkers.contains(wordLocation)) {
                    song = song + songLine.get(j) + " ";
                } else {
                    song = song + songLine.get(j).replaceAll("[A-Za-z0-9]", "_") + " ";
                }
            }
            song += "\n";
        }

        TextView lyricsTextView = (TextView) this.findViewById(R.id.lyrics_tv);
        lyricsTextView.setText(song);
    }

    public void submitGuess(View view) {
        EditText songGuessEt = (EditText) (findViewById(R.id.song_guess_et));

        //Adding to shared preferences the total number of guesses for statistics page
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        int numberOfGuesses = statistics.getInt("NumberOfGuesses", 0);
        SharedPreferences.Editor editorStats = statistics.edit();
        editorStats.putInt("NumberOfGuesses", numberOfGuesses + 1);
        editorStats.apply();

        //If the guess is correct
        if (isGuessCorrect(songGuessEt.getText().toString(), songTitle)) {

            //Adding to shared preferences the total number of correctGuesses for statistics page
            int numberOfCorrectGuesses = statistics.getInt("NumberOfCorrectGuesses", 0);
            editorStats.putInt("NumberOfCorrectGuesses", numberOfCorrectGuesses + 1);
            editorStats.apply();

            //Add an additional 500 points for a correct guess
            SharedPreferences sharedPrefs = getSharedPreferences("score", Context.MODE_PRIVATE);
            int currentLevel = sharedPrefs.getInt("level", 1);
            int currentScore = sharedPrefs.getInt("score", 0);
            int newScore = currentScore + 500;
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt("score", newScore);
            editor.apply();

            //calculate the users level after the 500 points has been added
            newScore = sharedPrefs.getInt("score", 1);
            double level = newScore / 1000.0;
            int integerLevel = (int) level;
            int newLevel = integerLevel + 1;

            //If the guessing of a song scores the user enough points to level up then a
            //dialog should popup letting them know, this is done by comparing the level before
            //and after the marker is collected.
            if (newLevel == (currentLevel + 1)) {
                levelUpDialog = new Dialog(this);
                levelUpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                levelUpDialog.setContentView(R.layout.level_up);
                levelUpDialog.show();
                levelUpDialog.setCancelable(false);
                TextView tv = (TextView) levelUpDialog.findViewById(R.id.new_level_number_tv);
                tv.setText("" + newLevel);

                Button okBtnLevelUp = (Button) levelUpDialog.findViewById(R.id.level_up_ok_button);

                /*
                        Once the OK button has been pressed the song should be removed from the
                        incomplete songs, the dialog should close and the correct answer dialog
                        should appear.
                 */
                okBtnLevelUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeFromIncomplete();
                        levelUpDialog.dismiss();
                        showCorrectAnswerDialog();
                    }
                });

            } else {
                removeFromIncomplete();
                showCorrectAnswerDialog();
            }


        } else {
            /*
                    If the guess was wrong then the wrong answer dialog should be shown.
             */
            dialogWrong = new Dialog(this);
            dialogWrong.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogWrong.setContentView(R.layout.dialog_wrong_answer);
            dialogWrong.show();

            Button okBtn = (Button) dialogWrong.findViewById(R.id.wrong_ok_button);

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogWrong.dismiss();
                }
            });

        }
    }

    /*
            Strip the guess and the answer down to remove punctuation and spaces and then compare
            the strings, not considering the case.
     */
    private boolean isGuessCorrect(String guess, String answer) {
        String guessStripped = (guess.replaceAll("[^a-zA-Z0-9]", "")).replaceAll("\\s+", "");
        String answerStripped = (answer.replaceAll("[^a-zA-Z0-9]", "")).replaceAll("\\s+", "");
        return answerStripped.equalsIgnoreCase(guessStripped);
    }

    /*
            This method removes the song from the list of incomplete songs, and then overwrites the
            file of incomplete songs, with the new list.
     */
    private void removeFromIncomplete() {
        IncompleteSong toBeRemoved = null;
        for (IncompleteSong incomplete : MainMenu.incompleteSongs) {
            if (incomplete.getSongTitle().equals(songTitle)) {
                toBeRemoved = incomplete;
            }
        }
        try {
            MainMenu.incompleteSongs.remove(toBeRemoved);
            FileOutputStream fileOutputStream = openFileOutput(
                    "IncompleteSongs.ser", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(MainMenu.incompleteSongs);
            objectOutputStream.close();

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
            This dialog should appear when the user guesses the song correctly.
     */
    private void showCorrectAnswerDialog() {
        //Instantiation and inflation of the dialog
        dialogCorrect = new Dialog(this);
        dialogCorrect.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogCorrect.setContentView(R.layout.dialog_correct_answer);
        dialogCorrect.show();
        dialogCorrect.setCancelable(false);

        //Set the text of a TextView to display the correct answer that the user guessed.
        TextView songTitleTv = (TextView) dialogCorrect.findViewById(R.id.song_title_tv);
        songTitleTv.setText(songTitle);

        //Update the total distance walked when the user leaves the map screen
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        float totalDistanceWalked = statistics.getFloat("totalDistanceWalked", 0.0f);
        SharedPreferences.Editor editorStats = statistics.edit();
        editorStats.putFloat("totalDistanceWalked", totalDistanceWalked + distanceWalkedWhilePlaying);
        editorStats.apply();

        Button okBtn = (Button) dialogCorrect.findViewById(R.id.correct_ok_button);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCorrect.dismiss();
                backToMainMenu();
            }
        });
    }


    /*
            This method is used when the user clicks on the Songle coin button, if they have enough
            Songle coins they will be asked if they are sure they want to spend a coin. If they do
            not have enough Songle coins, they will be notified.
     */
    public void spendSongleCoin(View view) {

        SharedPreferences songleCoins = getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
        int currentNumberOfCoins = songleCoins.getInt("currentNumberOfCoins", 0);

        if (currentNumberOfCoins > 0) {

            //Custom alert dialog

            TextView title = new TextView(this);
            title.setText("Spend Songle coin?");
            title.setPadding(10, 50, 10, 0);
            title.setTextColor(Color.DKGRAY);
            title.setTypeface(null, Typeface.BOLD);
            title.setGravity(Gravity.CENTER);
            title.setTextSize(20);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCustomTitle(title);


            alertDialogBuilder.setTitle("Spend Songle coin?");
            alertDialogBuilder.setMessage("Are you sure you want to spend a Songle coin? This will" +
                    " reveal 10 words from the song you are playing.");
            alertDialogBuilder.setPositiveButton("Spend",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //Dismiss dialog
                            arg0.dismiss();
                            //Reveal 10 words
                            spentSongleCoin();
                            //Remove a Songle coin
                            decrementNumberOfSongleCoins();
                            //Update TextView containing number of Songle coins.
                            updateNumberOfSongleCoinsText();
                        }
                    });

            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            alertDialog.show();

        } else {

            Snackbar.make(view, "Sorry, you do not have any Songle coins " +
                    "available at the moment.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }

    }


    /*
            This method removes one Songle coin from SharedPreferences.
     */
    private void decrementNumberOfSongleCoins() {
        SharedPreferences songleCoins = getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
        int currentNumberOfCoins = songleCoins.getInt("currentNumberOfCoins", 0);
        SharedPreferences.Editor editorSongleCoins = songleCoins.edit();
        editorSongleCoins.putInt("currentNumberOfCoins", (currentNumberOfCoins - 1));
        editorSongleCoins.apply();
    }


    /*
            This method keeps trying to collect markers from the map until it collects 10 random ones
            and whilst there are more than 10 markers still on the map.
     */
    public void spentSongleCoin() {
        int numberOfRandomMarkersAdded = 0;
        if (placemarks.size() - collectedMarkers.size() > 10) {
            while ((numberOfRandomMarkersAdded <= 10) &&
                    (placemarks.size() - collectedMarkers.size() > 10)) {
                numberOfRandomMarkersAdded = collectRandomMarker(numberOfRandomMarkersAdded);
            }
            /*
                 Once 10 markers have been collected, the lyrics should be rebuilt showing the
                 newest words.
             */
            buildSong(wholeSong);
        } else {
            Toast.makeText(GuessSong.this, "There is less than 10 markers to be collected, your" +
                    " Songle coin can't be redeemed. Please use it on another song.",
                    Toast.LENGTH_LONG).show();
        }
    }


    /*
            This method retrieves a random marker from the HashSet of placemarks and adds it to
            the collected markers HashSet. This method is called when a songle coin has been spent.
            If a marker has already been added then it shouldn't be added again. This algorithm may
            be relatively slow but I believe it is better than using the inherent randomness of the
            HashSet.
     */
    private int collectRandomMarker(int numberOfRandomMarkersAdded) {

        Random rand = new Random();
        int randomMarker = rand.nextInt(placemarks.size());
        int i = 0;
        for (Placemark placemark : placemarks) {
            if ((i == randomMarker) && (!collectedMarkers.contains(placemark.getLocation()))) {
                collectedMarkers.add(placemark.getLocation());
                numberOfRandomMarkersAdded++;
                i++;
            }
        }
        return numberOfRandomMarkersAdded;
    }

    /*
            When the back button is pressed we must pass the collected markers back to the
            MapsActivity because some markers may have been collected by the Songle coins.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("collectedMarkers", collectedMarkers);
        setResult(RESULT_OK, intent);
        finish();
    }


    /*
            When the user has correctly guessed the song they will be returned to the MainMenu
            the songTitle will be given back to the MainMenu so that it can add it to the
            list of completed songs.
     */
    private void backToMainMenu() {

        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("songTitle", songTitle);
        startActivity(intent);
        finish();

    }

}
