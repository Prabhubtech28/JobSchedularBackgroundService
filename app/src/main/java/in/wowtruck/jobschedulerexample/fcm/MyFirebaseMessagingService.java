package in.wowtruck.jobschedulerexample.fcm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import in.wowtruck.jobschedulerexample.HttpConnection;
import in.wowtruck.jobschedulerexample.MainActivity;
import in.wowtruck.jobschedulerexample.MyJobService;
import in.wowtruck.jobschedulerexample.TrackingController;
import in.wowtruck.jobschedulerexample.Utils;


/**
 * Created by Prabhu on 15-May-17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;
    public static JSONObject data;
    public String notification_body, notification_title;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage == null)
            return;

        Log.v("FCM","****** remoteMessage **********" +remoteMessage.toString());
        Log.v("FCM","****** From **********" +remoteMessage.getFrom());
        Log.v("FCM","****** Message Type **********" +remoteMessage.getMessageType());
        Log.v("FCM","****** Notification **********" +remoteMessage.getNotification());
        Log.v("FCM","****** Message Id **********" +remoteMessage.getMessageId());


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            notification_body =  remoteMessage.getNotification().getBody();
            notification_title = remoteMessage.getNotification().getTitle();

            Log.v("FCM","****** Notification Body **********" +notification_body);
            Log.v("FCM","****** Notification Title **********" +notification_title);
            Log.v("FCM","****** Notification Click Action **********" +remoteMessage.getNotification().getClickAction());
            Log.v("FCM","****** Notification Body Localization key **********" +remoteMessage.getNotification().getBodyLocalizationKey());

        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.v("FCM","****** Data **********" +remoteMessage.getData());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "FCM Exception: " + e.getMessage());
            }
        }

    }

    private void handleDataMessage(JSONObject json) {
        Log.v("FCM","****** Data JSON **********" +json.toString());

        try {
            String type_fcm      = json.getString("type");

            Log.v("FCM","****** action_flag_fcm **********" +type_fcm);


            // When app run in background
            if (NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                if(type_fcm.equalsIgnoreCase("25")) {

                    Log.v("FCM","****** action_flag_fcm 25 called **********");

                    SendFCMCallBackend(json);

                    ContextCompat.startForegroundService(MyFirebaseMessagingService.this, new Intent(MyFirebaseMessagingService.this, MyJobService.class));

                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception en) {
            Log.e(TAG, "Exception: " + en.getMessage());
        }
    }




    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
//        Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(getApplicationContext().getPackageName());
//        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(launchIntent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }


    private void handleNotification(String message) {

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }else{
            // If the app is in background, firebase itself handles the notification
        }
    }

    void SendFCMCallBackend(JSONObject jsonObject) {

        try {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("driver_id", preferences.getString(MainActivity.KEY_DEVICE, "undefined"));
            jsonObject1.put("notification_data", jsonObject);


            HttpConnection.getInstance(getApplicationContext()).objectRequest(null, "http://admin-staging.wowtruck.in/driverwebservice/notificationreceived", jsonObject1, Utils.retryPolicy, new HttpConnection.VolleyCallBack() {
                @Override
                public void onSuccess(final JSONObject response) {

                    try {

                        String status_str = response.getString("status");

                        Log.v(" FCM RES STATUS ", " ******** " + status_str);
                        if (status_str.equalsIgnoreCase("1")) {

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

}
