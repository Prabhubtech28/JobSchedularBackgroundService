package in.wowtruck.jobschedulerexample;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import in.wowtruck.jobschedulerexample.fcm.Config;
import in.wowtruck.jobschedulerexample.fcm.NotificationUtils;

public class MainActivity extends AppCompatActivity {

    private MainActivity MAIN_CONTEXT;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x3;
    Button start_job, stop_job;
    public static final String KEY_DEVICE = "id";
    private SharedPreferences sharedPreferences;
    EditText driver_id_ET;


    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static String fcmregId ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MAIN_CONTEXT = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MAIN_CONTEXT);

        /*fcm device id code generation*/
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    displayFirebaseRegId();
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received inside the application
                    // String message = intent.getStringExtra("message");
                }
            }
        };

        displayFirebaseRegId();


        start_job = (Button) findViewById(R.id.start_job);
        stop_job = (Button) findViewById(R.id.stop_job);
        driver_id_ET = (EditText) findViewById(R.id.driver_id_ET);

        start_job.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(driver_id_ET.getText().toString().equalsIgnoreCase("")) {
                    Toast.makeText(MAIN_CONTEXT, "Enter driver id", Toast.LENGTH_SHORT).show();
                } else {
                    String id = String.valueOf(driver_id_ET.getText().toString());
                    sharedPreferences.edit().putString(KEY_DEVICE, id).apply();
                    checkPermissions();
                }


            }
        });

        stop_job.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    // FCM Fetches reg id from shared preferences and displays on the screen
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        fcmregId = pref.getString("regId", null);
        if (fcmregId == null) {
            fcmregId = FirebaseInstanceId.getInstance().getToken();
        }
        Log.v("FCM SPLASH", "### displayFirebaseRegId ***** " + fcmregId);
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
//                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) /*This compulsory req internet connection for this service*/
                    .setOverrideDeadline(500)
//                    .setPeriodic(60 * 1000) /* Run ever 1 min time interval, but */
                    .setPersisted(true) /*Boot complete to restart*/
                    .setRequiresCharging(false) /* set true to run your job in only in charge mode*/
                    .setRequiresDeviceIdle(false) /* set true to run your job in only idle state only*/
                    .build());

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("driver_id", sharedPreferences.getString(MainActivity.KEY_DEVICE, "undefined"));
                jsonObject.put("version", "1");
                jsonObject.put("device_token", fcmregId);


                HttpConnection.getInstance(getApplicationContext()).objectRequest(null, "http://admin-staging.wowtruck.in/driverwebservice/splashscreen", jsonObject, Utils.retryPolicy, new HttpConnection.VolleyCallBack() {
                    @Override
                    public void onSuccess(final JSONObject response) {

                        try {

                            String status_str = response.getString("status");

                            Log.v(" SPLASH STATUS ", " ******** " + status_str);
                            if (status_str.equalsIgnoreCase("1")) {

                                MAIN_CONTEXT.finish();
                            }

                        } catch (JSONException JsonException) {
                            JsonException.printStackTrace();

                        }
                    }

                    @Override
                    public void onFailure(VolleyError error) {

                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.REGISTRATION_COMPLETE));
        // register new push message receiver by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
        fcmregId = FirebaseInstanceId.getInstance().getToken();
        Log.v("FCM SPLASH", "### ONRESUME ***** " + fcmregId);

    }
}
