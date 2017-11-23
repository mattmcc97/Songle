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
        super.onResume();
    }
}
