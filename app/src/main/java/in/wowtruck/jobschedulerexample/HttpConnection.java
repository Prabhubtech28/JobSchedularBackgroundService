package in.wowtruck.jobschedulerexample;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by PRABHU on 14-10-2018.
 */

public class HttpConnection {

    Context context;
    private RequestQueue mRequestQueue;
    public static final String TAG = HttpConnection.class.getSimpleName();
    private static HttpConnection mInstance;
    private static Context mCtx;

    private HttpConnection(Context context) {
        this.context = context;
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized HttpConnection getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HttpConnection(context);
        };
        return mInstance;
    }

    /*Post Method*/
    public void objectRequest(final String authToken, String url, JSONObject object, DefaultRetryPolicy retryPolicy, final VolleyCallBack callBack) {

        Log.e(TAG, "URL ***********" + url);
        Log.e(TAG, "REQUEST ***********" + object);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "RESPONSE ***********" + response);
                callBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "ERROR ***********" + volleyError);
                callBack.onFailure(volleyError);

            }
        });

        request.setRetryPolicy(retryPolicy);
        addToRequestQueue(request);
    }

    /**/
    public void objectRequestGet(String url, DefaultRetryPolicy retryPolicy, final VolleyCallBack callBack) {

        Log.e(TAG, "### URL ***********" + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "### RESPONSE ***********" + response);
                callBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "### ERROR ***********" + volleyError);
                callBack.onFailure(volleyError);

            }
        });

        request.setRetryPolicy(retryPolicy);
        addToRequestQueue(request);
    }

    public interface VolleyCallBack {
        void onSuccess(JSONObject object);

        void onFailure(VolleyError error);
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }

        return mRequestQueue;
    }

/*
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
*/

    private <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
