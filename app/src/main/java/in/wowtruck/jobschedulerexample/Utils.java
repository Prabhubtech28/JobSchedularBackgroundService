package in.wowtruck.jobschedulerexample;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;

/**
 * Created by PRABHU on 13-10-2018.
 */

public class Utils {

    private static Utils instance;
    private static Context context;

    static Utils sharUtils(Context ctx){
        if(instance == null) {
            instance = new Utils();
        }
        context = ctx;
        return instance;
    }

    public static DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(15000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    boolean isMyServiceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning? ",   " ### " +true);
                return true;
            }
        }
        Log.i("isMyServiceRunning? ",  " ### " +false);
        return false;
    }
}
