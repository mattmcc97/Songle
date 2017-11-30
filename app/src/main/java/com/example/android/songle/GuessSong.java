package com.example.android.songle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.util.LoggingInputStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class GuessSong extends AppCompatActivity{

    private String TAG = "GuessSong";

    Dialog dialogCorrect;
    Dialog dialogWrong;
    Dialog levelUpDialog;

    private String songTitle = "";
    private HashMap<Integer, HashMap<Integer, String>> wholeSong = null;
    private HashSet<String> collectedMarkers = null;
    private HashSet<Placemark> placemarks = null;

    private TextView lyricsTextView;

    private float distanceWalkedWhilePlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_song);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if no songle coins
                Snackbar.make(view, "No Songle Coins available.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //else - are you sure you want to reveal 5 lyrics using 1 songle coin?

            }
        });*/
        Log.i(TAG, "onCreate: songTitle: " + getIntent().getStringExtra("songTitle"));
        songTitle = getIntent().getStringExtra("songTitle");
        wholeSong = (HashMap<Integer, HashMap<Integer, String>>) getIntent().getSerializableExtra("wholeSong");

        collectedMarkers = (HashSet<String>) getIntent().getSerializableExtra("collectedMarkers");

        placemarks = (HashSet<Placemark>) getIntent().getSerializableExtra("placemarks");

        distanceWalkedWhilePlaying = getIntent().getFloatExtra("distanceWalkedWhilePlaying", 0.0f);

        updateNumberOfSongleCoinsText();

        buildSong(wholeSong);
    }

    private void updateNumberOfSongleCoinsText() {

        SharedPreferences songleCoins = getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
        int currentNumberOfCoins = songleCoins.getInt("currentNumberOfCoins", 0);
        TextView numberOfSongleCoinsTv = (TextView) findViewById(R.id.number_of_coins_tv);
        numberOfSongleCoinsTv.setText("You have " + currentNumberOfCoins  + " Songle coins available.");

    }

    private void buildSong(HashMap<Integer, HashMap<Integer, String>> songLyrics) {
        Log.i(TAG, "buildSong: sizeOfSong: " + songLyrics.size());
        String song = "";
        for(int i=1; i <= songLyrics.size(); i++){
            HashMap<Integer, String> songLine = null;
            songLine = songLyrics.get(i);
            song += i + ". ";
            for(int j = 1; j <= songLine.size(); j++){
                String wordLocation = i + ":" + j;
                if(collectedMarkers.contains(wordLocation)){
                    song = song + songLine.get(j) + " ";
                    Log.i(TAG, "buildSong: This marker has been collected: " + wordLocation);
                }else{
                    song = song + songLine.get(j).replaceAll("[A-Za-z0-9]", "_") + " ";
                    Log.i(TAG, "buildSong: This marker has not been collected.");
                }
            }
            song += "\n";
        }
        /*song = song.replaceAll("[A-Za-z0-9]", "_");
        Log.i(TAG, "buildSong: " + song);*/

        lyricsTextView = (TextView) this.findViewById(R.id.lyrics_tv);
        lyricsTextView.setText(song);
    }

    public void submitGuess(View view) {
        EditText songGuessEt = (EditText) (findViewById(R.id.song_guess_et));

        //Adding to shared preferences the total number of guesses for statistics page
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        int numberOfGuesses =
                statistics.getInt("NumberOfGuesses", 0);
        SharedPreferences.Editor editorStats = statistics.edit();
        editorStats.putInt("NumberOfGuesses",
                numberOfGuesses + 1);
        editorStats.apply();
        Log.i(TAG, "submitGuess: NumberOfGuesses: " +
                statistics.getInt("NumberOfGuesses", 0));

        //If the guess is correct
        if (songGuessEt.getText().toString().equalsIgnoreCase(getIntent().getStringExtra("songTitle"))) {

            //Adding to shared preferences the total number of correctGuesses for statistics page
            int numberOfCorrectGuesses =
                    statistics.getInt("NumberOfCorrectGuesses", 0);
            editorStats.putInt("NumberOfCorrectGuesses",
                    numberOfCorrectGuesses + 1);
            editorStats.apply();
            Log.i(TAG, "submitGuess: NumberOfCorrectGuesses: " +
                    statistics.getInt("NumberOfCorrectGuesses", 0));

            //Add 500 points for a correct guess
            SharedPreferences sharedPrefs = getSharedPreferences("score", Context.MODE_PRIVATE);
            int currentLevel = sharedPrefs.getInt("level", 1);
            int currentScore = sharedPrefs.getInt("score", 0);
            Log.i(TAG, "onMarkerClick: score: currentScore: " + currentScore);
            int newScore = currentScore + 500;
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt("score", newScore);
            Log.i(TAG, "onMarkerClick: score: newScore: " + newScore);

            editor.apply();

            //calculate the users level after the 500 points has been added
            newScore = sharedPrefs.getInt("score", 1);
            double level = newScore/1000.0;
            int integerLevel = (int) level;
            int newLevel = integerLevel + 1;

            //If the guessing of a song scores the user enough points to level up then a
            //dialog should popup letting them know, this is done by comparing the level before
            //and after the marker is collected.
            Log.i(TAG, "submitGuess: current level: " + currentLevel);
            Log.i(TAG, "submitGuess: new level: " + newLevel);
            if(newLevel == (currentLevel+1)){
                levelUpDialog = new Dialog(this);
                levelUpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                levelUpDialog.setContentView(R.layout.level_up);
                levelUpDialog.show();
                levelUpDialog.setCancelable(false);
                TextView tv = (TextView) levelUpDialog.findViewById(R.id.new_level_number_tv);
                tv.setText("" + newLevel);

                Button okBtnLevelUp = (Button) levelUpDialog.findViewById(R.id.level_up_ok_button);

                okBtnLevelUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeFromIncomplete();
                        levelUpDialog.dismiss();
                        showCorrectAnswerDialog();
                    }
                });

            }else{
                removeFromIncomplete();
                showCorrectAnswerDialog();
            }


        } else {
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

    private void removeFromIncomplete(){
        IncompleteSong toBeRemoved = null;
        Log.i(TAG, "onClick: IncompleteSongFound list size: " + MainMenu.incompleteSongs.size());
        for(IncompleteSong incomplete : MainMenu.incompleteSongs){
            Log.i(TAG, "onClick: The IncompleteSongFound list: " + incomplete.getSongTitle() + incomplete);
            if (incomplete.getSongTitle().equals(getIntent().getStringExtra("songTitle"))){
                Log.i(TAG, "onClick: IncompleteSongFound: "+ incomplete.getSongTitle());
                toBeRemoved = incomplete;
            }
        }
        try{
            MainMenu.incompleteSongs.remove(toBeRemoved);
            Log.i(TAG, "onClick: IncompleteSongFound list after remove: " + MainMenu.incompleteSongs);
            FileOutputStream fileOutputStream = openFileOutput("IncompleteSongs.ser", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(MainMenu.incompleteSongs);
            objectOutputStream.close();

        }catch (NullPointerException e){
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showCorrectAnswerDialog() {
        dialogCorrect = new Dialog(this);
        dialogCorrect.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogCorrect.setContentView(R.layout.dialog_correct_answer);
        dialogCorrect.show();
        dialogCorrect.setCancelable(false);

        TextView songTitleTv = (TextView) dialogCorrect.findViewById(R.id.song_title_tv);
        songTitleTv.setText(getIntent().getStringExtra("songTitle"));

        Button okBtn = (Button) dialogCorrect.findViewById(R.id.correct_ok_button);

        //Update the total distance walked when the user leaves the map screen
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        float totalDistanceWalked = statistics.getFloat("totalDistanceWalked", 0.0f);
        SharedPreferences.Editor editorStats = statistics.edit();
        editorStats.putFloat("totalDistanceWalked", totalDistanceWalked + distanceWalkedWhilePlaying );
        editorStats.apply();
        Log.i(TAG, "onClick: Distance: totalDistanceWalked: " + statistics.getFloat("totalDistanceWalked", 0.0f));

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCorrect.dismiss();
                backToMainMenu();
            }
        });
    }

    public void spendSongleCoin(View view) {

        SharedPreferences songleCoins = getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
        int currentNumberOfCoins = songleCoins.getInt("currentNumberOfCoins", 0);
        if(currentNumberOfCoins > 0){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            TextView title = new TextView(this);
            title.setText("Spend Songle coin?");
            title.setPadding(10, 50, 10, 0);
            title.setTextColor(Color.DKGRAY);
            title.setTypeface(null, Typeface.BOLD);
            title.setGravity(Gravity.CENTER);
            title.setTextSize(20);

            alertDialogBuilder.setCustomTitle(title);


            alertDialogBuilder.setTitle("Spend Songle coin?");
            alertDialogBuilder.setMessage("Are you sure you want to spend a Songle coin? This will" +
                    " reveal 10 words from the song you are playing.");
            alertDialogBuilder.setPositiveButton("Spend",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                            spentSongleCoin();
                            decrementNumberOfSongleCoins();
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

        }else{

            Snackbar.make(view, "Sorry, you do not have any Songle coins " +
                    "available at the moment.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }

    }

    private void decrementNumberOfSongleCoins() {
        SharedPreferences songleCoins = getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
        int currentNumberOfCoins = songleCoins.getInt("currentNumberOfCoins", 0);
        SharedPreferences.Editor editorSongleCoins = songleCoins.edit();
        editorSongleCoins.putInt("currentNumberOfCoins", (currentNumberOfCoins - 1));
        editorSongleCoins.apply();
    }

    public void spentSongleCoin(){
        int numberOfRandomMarkersAdded = 0;
        while((numberOfRandomMarkersAdded <=10)&&(placemarks.size() - collectedMarkers.size() > 10)){
            numberOfRandomMarkersAdded = collectRandomMarker(numberOfRandomMarkersAdded);
            Log.i(TAG, "spentSongleCoin: numberOfRandomMarkersAdded: " + numberOfRandomMarkersAdded);
        }
        if(placemarks.size() - collectedMarkers.size() > 10){
            buildSong(wholeSong);
        }else{
            Toast.makeText(GuessSong.this, "There is less than 10 markers to be collected, your" +
                    " Songle coin can't be redeemed. Please use it on another song.", Toast.LENGTH_LONG).show();
        }
    }

    private int collectRandomMarker(int numberOfRandomMarkersAdded){

        /*
                This method retrieves a random marker from the HashSet of markers and adds it to
                the collected markers. This method is called when a songle coin has been spent. If
                a marker has already been added then it shouldn't be added again.
                 */
        Random rand = new Random();
        int randomMarker = rand.nextInt(placemarks.size());
        int i = 0;
        for(Placemark placemark: placemarks){
            if((i == randomMarker) && (!collectedMarkers.contains(placemark.getLocation()))){
                collectedMarkers.add(placemark.getLocation());
                numberOfRandomMarkersAdded++;
                Log.i(TAG, "collectRandomMarker: SUCCESS: " + "Marker added to collected markers: " +
                        "collectedMarkers size: " + collectedMarkers.size());
                i++;
            }
        }
        return numberOfRandomMarkersAdded;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("collectedMarkers", collectedMarkers);
        setResult(RESULT_OK, intent);
        finish();
    }


    private void backToMainMenu() {

        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("songTitle", songTitle);
        startActivity(intent);
        finish();

    }

}
