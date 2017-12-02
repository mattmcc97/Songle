package com.example.android.songle;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

    }


    /*
            This method is called when the start button is pressed. It takes the user to the
            Main Menu.
     */
    public void startGame(View view) {
        //When the new song button is clicked, open the MapsActivity
        if (isWifiConnected() || isNetworkConnected()) {
            Intent intent = new Intent(this, MainMenu.class);
            startActivity(intent);
        } else {
            Snackbar.make(view, "No internet connection. Please reconnect and try again.",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }


    @Override
    protected void onResume() {
        //Update the user's level and their progress in that level, at the top right of the screen
        TopBarFragment fragment = (TopBarFragment)
                getFragmentManager().findFragmentById(R.id.top_bar_fragment);
        fragment.updateLevel();
        super.onResume();
    }


    /*
            Check to see if the user has a data internet connection.
    */
    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    /*
            Check to see if the user has a WiFi internet connection.
    */
    private boolean isWifiConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && (ConnectivityManager.TYPE_WIFI == networkInfo.getType())
                && networkInfo.isConnected();
    }
}
