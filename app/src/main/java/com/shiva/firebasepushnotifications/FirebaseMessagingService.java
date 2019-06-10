package com.shiva.firebasepushnotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Random;

import static android.R.id.message;
import static com.shiva.firebasepushnotifications.R.attr.icon;

/**
 * Created by User on 2/20/2017.
 */

public class  FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService  {
    private static final String TAG = "FirebaseMessagingServic";

    public FirebaseMessagingService() {
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();
        String action = remoteMessage.getNotification().getClickAction();
        Log.d(TAG, "onMessageReceived: Message Received: \n" +
                "Title: " + title + "\n" +
                "Message: " + message);
        String[] parts = message.split(":");
        String jobid =data.get("jobId");
        String jobtype =data.get("jobtype");
        String subdivision =data.get("subdivision");
        String lot =data.get("lot");
        sendNotification(title,message,action,jobid,jobtype,subdivision,lot);
    }

    @Override
    public void onDeletedMessages() {

    }

    private void sendNotification(String title,String messageBody,String click_action, String jobid,String jobtype,String subdivision,String lot) {
        Intent intent = null;
            intent = new Intent(this, YesNoActivity.class);
            intent.putExtra("jobid",jobid);
            intent.putExtra("jobtype",jobtype);
            intent.putExtra("subdivision",subdivision);
            intent.putExtra("lot",lot);
            intent.putExtra("from","activity1");

        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,Integer.parseInt(jobid), intent, PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.bell_ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.bell_ic_launcher))
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);


        NotificationManager manager = (NotificationManager)     getSystemService(NOTIFICATION_SERVICE);
        android.app.Notification notification = new android.app.Notification(icon, messageBody, System.currentTimeMillis());
        manager.notify(Integer.parseInt(jobid), notificationBuilder.build());
    }


}
