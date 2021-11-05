package com.zahran;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zahran.Utilities.NotificationUtilities;
import com.zahran.Utilities.Tasks;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            Tasks.executeTask(context , action);
            NotificationUtilities.clearAllNotifications(context);

            //Close Notification Bar
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }

    }
}
