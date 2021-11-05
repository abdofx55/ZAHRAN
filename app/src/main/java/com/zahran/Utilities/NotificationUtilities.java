package com.zahran.Utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.zahran.Activities.SplashScreen;
import com.zahran.R;
import com.zahran.Receiver;

public class NotificationUtilities {
    private static final int PENDING_INTENT_ID = 3417;
    private static final String NOTIFICATION_CHANNEL_ID = "notification_channel";
    private static final int NOTIFICATION_ID = 135;
    private static final int ACTION_SHARE_PENDING_INTENT_ID = 17;


    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    public static void createNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);

            mChannel.enableVibration(true);
            mChannel.enableLights(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        // Create Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.notification_text)))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(contentIntent(context))
                .addAction(shareAction(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }



    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, SplashScreen.class);
        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent. FLAG_ACTIVITY_SINGLE_TOP) ;
        return PendingIntent.getActivity(
                context,
                PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static NotificationCompat.Action shareAction(Context context) {
        Intent sharingIntent = new Intent(context , Receiver.class);
        sharingIntent.setAction(Tasks.ACTION_SHARE);

        PendingIntent sharePendingIntent = PendingIntent.getBroadcast(
                context,
                ACTION_SHARE_PENDING_INTENT_ID,
                sharingIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action(R.drawable.ic_share4dp,
                context.getString(R.string.مشاركة),
                sharePendingIntent);
    }


    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
    }
}

