package in.wowtruck.jobschedulerexample;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MainActivity MAIN_CONTEXT;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x3;
    Button start_job, stop_job;
    public static final String KEY_DEVICE = "id";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MAIN_CONTEXT = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MAIN_CONTEXT);
        String id = String.valueOf(300);
        sharedPreferences.edit().putString(KEY_DEVICE, id).apply();


        start_job = (Button) findViewById(R.id.start_job);
        stop_job = (Button) findViewById(R.id.stop_job);

        start_job.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

        stop_job.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

        void checkPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(MAIN_CONTEXT, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(MAIN_CONTEXT, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.v("LOCATION M :::::: ", " ****** PERMISSION NOT YET");

                    ActivityCompat.requestPermissions(MAIN_CONTEXT,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_ID_MULTIPLE_PERMISSIONS);
                } else {
                    Log.v("LOCATION M ::::::", " ******PERMISSION GRANTED");
                    callLocationActivity();
                }
            } else {
                callLocationActivity();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            Log.v("LOCATION", "**** onRequestPermissionsResult > M " + requestCode);
            switch (requestCode) {
                case REQUEST_ID_MULTIPLE_PERMISSIONS:
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                        callLocationActivity();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(MAIN_CONTEXT, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(MAIN_CONTEXT, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

                                Toast.makeText(MAIN_CONTEXT, "Enable Permission !!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    break;
            }
        }

        void callLocationActivity() {

            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

            assert jobScheduler != null;
            jobScheduler.schedule(new JobInfo.Builder(303,
                    new ComponentName(this, MyJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setOverrideDeadline(60 * 1000) /*Run ever 1 min*/
//                    .setPeriodic(60 * 1000)
                    .setPersisted(true) /*Boot complete to restart*/
                    .setRequiresCharging(false) /* set true to run your job in only in charge mode*/
                    .setRequiresDeviceIdle(false) /* set true to run your job in only idle state only*/
                    .build());

        }


}
