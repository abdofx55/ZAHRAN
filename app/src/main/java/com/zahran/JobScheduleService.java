package com.zahran;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.zahran.Utilities.NotificationUtilities;

public class JobScheduleService extends JobService {
    private AsyncTask mBackgroundTask;

    @Override
    public boolean onStartJob(@NonNull final JobParameters job) {

        mBackgroundTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                Context context = JobScheduleService.this;
                NotificationUtilities.createNotification(context);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(job, false);
            }
        };

        mBackgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(@NonNull JobParameters job) {
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}