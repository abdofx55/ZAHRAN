package com.zahran;

import android.content.Context;

import androidx.annotation.NonNull;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class JobManager {
    private static final int TIME_INTERVAL_DAYS = 1;
    private static final int TIME_INTERVAL_SECONDS = (int) (TimeUnit.HOURS.toSeconds(TIME_INTERVAL_DAYS));
    private static final int SYNC_FLEXTIME_SECONDS = (int) (TimeUnit.HOURS.toSeconds(TIME_INTERVAL_DAYS));;

    private static final String JOB_TAG = "notification_tag";

    private static boolean sInitialized;

    synchronized public static void scheduleNotification(@NonNull final Context context) {

        if (sInitialized) return;
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job notifyJob = dispatcher.newJobBuilder()
                .setService(JobScheduleService.class)
                .setTag(JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        TIME_INTERVAL_SECONDS,
                        TIME_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(notifyJob);
        sInitialized = true;
    }
}
