package com.example.android.songle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Matthew on 13/10/2017.
 */

class NetworkReceiver extends BroadcastReceiver{

    public static String networkPref = "WIFI";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkPref.equals("WIFI") && networkInfo != null
                && networkInfo.getType() ==
                ConnectivityManager.TYPE_WIFI) {
        // WiFi is connected, so use WiFi
        }else if (networkPref.equals("ANY") && networkInfo != null) {
        // Have a network connection and permission, so use data
        } else {
        // No WiFi and no permission, or no network connection
        }

    }
}
