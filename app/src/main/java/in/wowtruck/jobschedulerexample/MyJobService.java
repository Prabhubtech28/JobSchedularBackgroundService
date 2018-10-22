package in.wowtruck.jobschedulerexample;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by PRABHU on 20-10-2018.
 */

public class MyJobService extends JobService {

    private static final String TAG = MyJobService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.i(TAG, " onStartJob ");
        startForeground(NOTIFICATION_ID, createNotification(this));

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            TrackingController trackingController = new TrackingController(this);
            trackingController.start();
        }

//        jobFinished(params, true);

        return true;
    }

    private static Notification createNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.PRIMARY_CHANNEL)

                /*Prabhu code*/
                .setContentTitle("ETPL Driver")
                .setContentText("Forground Service")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(true)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(Notification.CATEGORY_SERVICE);
        return builder.build();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
