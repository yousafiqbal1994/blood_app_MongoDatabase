package com.donateblood.blooddonation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

public class CheckConnectivity extends BroadcastReceiver{
    public static boolean NetAvailable;
    @Override
    public void onReceive(Context context, Intent arg1) {

        boolean isnotConnected = arg1.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if(isnotConnected){
            //Toast.makeText(context, "Internet Connection Lost", Toast.LENGTH_LONG).show();
            NetAvailable = false;
        }
        else{
            NetAvailable=true;
        }
    }
}