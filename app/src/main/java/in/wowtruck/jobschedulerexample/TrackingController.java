package in.wowtruck.jobschedulerexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONObject;


/**
 * Created by PRABHU on 20-10-2018.
 */

public class TrackingController implements PositionProvider.PositionListener, NetworkManager.NetworkHandler {

    private static final String TAG = TrackingController.class.getSimpleName();
    private static final int RETRY_DELAY = 30 * 1000;
    private static final int WAKE_LOCK_TIMEOUT = 60 * 1000; /*Prabhu code*/

    private boolean isOnline;
    private boolean isWaiting;

    private Context context;
    private Handler handler;
    private SharedPreferences preferences;

    private String url;

    private PositionProvider positionProvider;
    private DatabaseHelper databaseHelper;
    private NetworkManager networkManager;

//    private PowerManager.WakeLock wakeLock;
//
//    private void lock() {
//        wakeLock.acquire(WAKE_LOCK_TIMEOUT);
//    }
//
//    private void unlock() {
//        if (wakeLock.isHeld()) {
//            wakeLock.release();
//        }
//    }

    public TrackingController(Context context) {
        this.context = context;
        handler = new Handler();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        positionProvider = new PositionProvider(context, this);
        databaseHelper = new DatabaseHelper(context);
        networkManager = new NetworkManager(context, this);
        isOnline = networkManager.isOnline();

        url = "http://admin-staging.wowtruck.in/driverwebservice/sendpollingdata_v2";

//        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

//        /*Prabhu code*/
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
//            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK |
//                    PowerManager.ACQUIRE_CAUSES_WAKEUP , getClass().getName());
//        } else {
//            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK , getClass().getName());
//        }

    }

    public void start() {
        if (isOnline) {
            read();
        }
        try {
            positionProvider.startUpdates();
        } catch (SecurityException e) {
            Log.w(TAG, e);
        }
        networkManager.start();
    }
    @Override
    public void onPositionUpdate(Position position) {
        if (position != null) {
            write(position);
        }
    }

    @Override
    public void onNetworkUpdate(boolean isOnline) {
        Log.i(TAG, "### " +isOnline );
        if (!this.isOnline && isOnline) {
            read();
        }
        this.isOnline = isOnline;
    }

    //
    // State transition examples:
    //
    // write -> read -> send -> delete -> read
    //
    // read -> send -> retry -> read -> send
    //

    private void log(String action, Position position) {
        if (position != null) {
            action += " (" +
                    "id:" + position.getId() +
                    " time:" + position.getTime().getTime() / 1000 +
                    " lat:" + position.getLatitude() +
                    " lon:" + position.getLongitude() + ")";
        }
        Log.d(TAG, action);
    }

    private void write(Position position) {
        log("write", position);
//        lock();
        databaseHelper.insertPositionAsync(position, new DatabaseHelper.DatabaseHandler<Void>() {
            @Override
            public void onComplete(boolean success, Void result) {
                if (success) {
                    if (isOnline && isWaiting) {
                        read();
                        isWaiting = false;
                    }
                }
//                unlock();
            }
        });
    }

    private void read() {
        log("read", null);
//        lock();
        databaseHelper.selectPositionAsync(new DatabaseHelper.DatabaseHandler<Position>() {
            @Override
            public void onComplete(boolean success, Position result) {

                if (success) {
                    if (result != null) {

                        Log.i("### READ DEVICEID "," **************" +result.getDeviceId());
                        Log.i("### READ ID "," **************" +preferences.getString(MainActivity.KEY_DEVICE, null));


                        if (result.getDeviceId().equals(preferences.getString(MainActivity.KEY_DEVICE, null))) {
                            send(result);
                        } else {
                            delete(result);
                        }
                    } else {
                        isWaiting = true;
                    }
                } else {
                    retry();
                }
//                unlock();
            }
        });
    }

    private void delete(Position position) {
        log("delete", position);
//        lock();
        databaseHelper.deletePositionAsync(position.getId(), new DatabaseHelper.DatabaseHandler<Void>() {
            @Override
            public void onComplete(boolean success, Void result) {
                if (success) {
                    read();
                } else {
                    retry();
                }
//                unlock();
            }
        });
    }

    private void send(final Position position) {
        log("send", position);
//        lock();
        String request = ProtocolFormatter.formatRequest(url, position);

        HttpConnection.getInstance(context).objectRequestGet( request, Utils.retryPolicy, new HttpConnection.VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject response) {

                try {
                    String status_str = response.getString("status");

                    if(status_str.equalsIgnoreCase("1")) {
                        Log.i(TAG, "**** ### *******" + status_str);
                        delete(position);
                    } else {
                        Log.i(TAG, "**** ### ******* VOLLEY FAILED");
                        retry();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

//                unlock();
            }

            @Override
            public void onFailure(VolleyError error) {
            }
        });

//        RequestManager.sendRequestAsync(request, new RequestManager.RequestHandler() {
//            @Override
//            public void onComplete(boolean success) {
//                if (success) {
//                    delete(position);
//                } else {
//                    StatusActivity.addMessage(context.getString(R.string.status_send_fail));
//                    retry();
//                }
//                unlock();
//            }
//        });
    }

    private void retry() {
        log("retry", null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOnline) {
                    read();
                }
            }
        }, RETRY_DELAY);
    }
}
