package com.example.android.songle;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.math.BigDecimal;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class StatisticsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        updateDistanceWalked();
        updateTotalPlayTime();
        updateNumberOfMarkersCollected();
        updateNumberOfGuesses();
        updateNumberCorrectGuesses();
        updateTotalNumberOfSongleCoins();

    }

    private void updateTotalNumberOfSongleCoins() {

        SharedPreferences songleCoins = getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
        int totalNumberOfCoins = songleCoins.getInt("totalNumberOfCoins", 0);

        TextView numberOfSongleCoins = (TextView) findViewById(R.id.number_of_songle_coins_tv);
        numberOfSongleCoins.setText("" + totalNumberOfCoins);

    }

    private void updateNumberCorrectGuesses() {
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        int numberOfCorrectGuesses = statistics.getInt("NumberOfCorrectGuesses", 0);

        TextView numberOfCorrectGuessesTv = (TextView) findViewById(R.id.number_of_correct_guesses_tv);
        numberOfCorrectGuessesTv.setText("" + numberOfCorrectGuesses);
    }

    private void updateNumberOfGuesses() {
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        int numberOfGuesses = statistics.getInt("NumberOfGuesses", 0);

        TextView numberOfGuessesTv = (TextView) findViewById(R.id.number_of_guesses_tv);
        numberOfGuessesTv.setText("" + numberOfGuesses);
    }

    private void updateNumberOfMarkersCollected() {
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        int numberOfMarkersCollected = statistics.getInt("LifetimeNumberOfMarkersCollected", 0);

        TextView numberOfMarkersTv = (TextView) findViewById(R.id.number_of_markers_tv);
        numberOfMarkersTv.setText("" + numberOfMarkersCollected);

    }

    private void updateTotalPlayTime() {
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        long totalPlayTime = statistics.getLong("totalPlayTime", 0);
        TextView totalPlayTimeTv = (TextView) findViewById(R.id.total_play_time_tv);
        long hours = totalPlayTime/3600;
        long minutes = totalPlayTime/60;

        totalPlayTimeTv.setText(hours + "hr " + minutes + "m" );
    }

    private void updateDistanceWalked() {
        SharedPreferences statistics = getSharedPreferences("statistics", Context.MODE_PRIVATE);
        float totalDistanceWalked = statistics.getFloat("totalDistanceWalked", 0.0f)/(1000.0f);

        TextView distanceWalkedTv = (TextView) findViewById(R.id.distance_walked_tv);

        distanceWalkedTv.setText(String.format("%.1f", totalDistanceWalked) + "km");

    }


    @Override
    protected void onResume() {
        TopBarFragment fragment = (TopBarFragment)
                getFragmentManager().findFragmentById(R.id.top_bar_fragment);
        fragment.updateLevel();
        updateDistanceWalked();
        updateTotalPlayTime();
        updateNumberOfMarkersCollected();
        updateNumberOfGuesses();
        updateNumberCorrectGuesses();
        updateTotalNumberOfSongleCoins();
        super.onResume();
    }
}
