package com.example.covid_19alertapp.extras;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.covid_19alertapp.R;
import com.example.covid_19alertapp.activities.TrackerSettingsActivity;


public abstract class Notifications {

    public static void createNotificationChannel(Context activity) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = activity.getString(R.string.notification_channel_name);
            String description = activity.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Log.d(LogTags.Notification_TAG, "createNotificationChannel: notification channel created");
        }
    }

    public static Notification showNotification(int notification_id, Context context, boolean notify){

        String title, content;
        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID);

        //start TrackerSettingsActivity on notification tap
        intent = new Intent(context, TrackerSettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        switch (notification_id){

            case Constants.PromptTrackerNotification_ID:
                /*
                let user know app is not tracking
                pushing notification button will start tracking
                    */

                title = context.getString(R.string.promptToTrackNotificationTitle);
                content = context.getString(R.string.promptToTrackNotificationContent);

                //build notification
                builder.setContentTitle(title)
                        .setContentText(content)

                        //TODO: set custom icons

                        .setSmallIcon(R.drawable.ic_launcher_background)

                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                        // Intent(TrackerSettingsActivity) that will start when the user taps the button
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                Log.d(Constants.NOTIFICATION_CHANNEL_ID, "PromptToTrackNotification: notification builder created");

                break;

            case Constants.TrackingLocationNotification_ID:
                /*
                shows notification when app is actively tracking user
                    */

                title = context.getString(R.string.trackingNotificationTitle);
                content = context.getString(R.string.trackingNotificationContent);

                //build notification
                builder.setContentTitle(title)
                        .setContentText(content)
                        //TODO: set custom icons
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        // Intent(Activity) that will start when the user taps the button
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setAutoCancel(false);

                Log.d(LogTags.Notification_TAG, "TrackingNotification: notification builder created");

                break;

        }


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = builder.build();

        if(notification_id == Constants.TrackingLocationNotification_ID) {
            //makes notification persistent
            notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        }

        //show notification here for older version
        if(notify)
            notificationManager.notify(notification_id, notification);

        Log.d(LogTags.Notification_TAG, "PromptToTrackNotification: notification showed");

        //return for newer version to start on Foreground
        return notification;

    }

    public static void removeNotification(int notification_id, Context context){
        /*
        removes the notification
         */

        //TODO: test this
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notification_id);
    }

}