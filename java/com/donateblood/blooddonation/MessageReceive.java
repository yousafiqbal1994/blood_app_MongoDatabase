package com.donateblood.blooddonation;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.donateblood.blooddonation.R;
import com.google.android.gms.gcm.GcmListenerService;

import java.security.MessageDigest;

public class MessageReceive extends GcmListenerService {

    public static String number,RequesterID,RequesterName,RequesterImage;

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String daata = data.getString("data");
        if(daata.length()>50){
        String[] parts = daata.split(",");
        String Message = parts[0];
        number = parts[1];
        RequesterID = parts[2];
        RequesterName = parts[3];
        CreateNotification(Message);
        } else {
            if(daata.contains("Medically")){
                CreateUnfitNotification(daata);
            }
            else {
                CreateNotAvaiableNotification(daata);
            }
        }
    }
    public void CreateNotification(String Message){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(MessageReceive.this, ContactBack.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(MessageReceive.this, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Blood Required")
                .setContentText(Message)
                .setAutoCancel(false)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentIntent(pi)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(Message));
        notificationManager.notify(1, mBuilder.build());
    }

    public void CreateUnfitNotification(String Message){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.star_on)
                        .setContentTitle("Blood Notification")
                        .setContentText(Message);
        int mNotificationId = 01;
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
    public void CreateNotAvaiableNotification(String Message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.star_on)
                        .setContentTitle("Blood Notification")
                        .setContentText(Message);
        int mNotificationId = 001;
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

}