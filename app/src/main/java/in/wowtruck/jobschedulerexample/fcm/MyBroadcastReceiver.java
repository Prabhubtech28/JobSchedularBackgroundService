package in.wowtruck.jobschedulerexample.fcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import in.wowtruck.jobschedulerexample.MainActivity;

/**
 * Created by Prabhu on 15-May-17.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("{{{{{{{{{{{{{{{{{{{{{{{MyBroadcastReceiver}}}}}}}}}}}}}}}}}}}}}}}}}");
        context.startActivity(new Intent(context, MainActivity.class));
    }
}