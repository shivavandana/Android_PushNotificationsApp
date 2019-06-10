package com.shiva.firebasepushnotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by shivavandana on 6/21/17.*/

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    MainActivity context;
    public MyFirebaseInstanceIDService(MainActivity at)
    {
        context = at;
    }
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("FirebaseInstanceClass", "Refreshed token: " + refreshedToken);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("email", "");
        if(!name.equalsIgnoreCase(""))
        {

           new MainActivity().tempfunc();
        }
    }



}
