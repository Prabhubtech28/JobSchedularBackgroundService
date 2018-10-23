package in.wowtruck.jobschedulerexample;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by PRABHU on 20-10-2018.
 */

public class MyJobService extends JobService {

    private static final String TAG = MyJobService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;
    private static SharedPreferences preferences;
    TrackingController trackingController;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.i(TAG, " ### onStartJob Running ? " + (Utils.sharUtils(this).isMyServiceRunning(MyJobService.class) ? "YES" : "ON"));
        startForeground(NOTIFICATION_ID, createNotification(this));

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            trackingController = new TrackingController(this);
            trackingController.start();
        }

//        jobFinished(params, true);

        return true;
    }

    private static Notification createNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.PRIMARY_CHANNEL)

                /*Prabhu code*/
                .setContentTitle("ETPL Driver ID: " + preferences.getString(MainActivity.KEY_DEVICE, "undefined"))
                .setContentText("Forground Service")
                .setSmallIcon(R.drawable.ic_app_logo_64)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(true)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(Notification.CATEGORY_SERVICE);
        return builder.build();
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        if (trackingController != null) {
            trackingController.stop();
        }

        return true;
    }
}
