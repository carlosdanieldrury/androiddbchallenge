package db.drury.com.dbchallenge;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


/**
 * Created by carlos.drury on 25/08/2017.
 */

public class BroadcastService extends Service {
    public static String LOG_TAG = "BroadcastService";
    private int periodUpdateOTP;
    private String key;
    Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        LOG_TAG = this.getClass().getSimpleName();
        Log.i(LOG_TAG, "In onCreate");
        periodUpdateOTP = 30;
    }

    @Override
    public void onRebind(Intent intent) {
        key = intent.getStringExtra(MainActivity.QR_CODE_KEY);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "In onStartCommand");
        handler = new Handler();
        final Runnable runnable = new Runnable() {
            public void run() {

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MainActivity.mBroadcastStringAction);
                // Updates period of updating OTP
                periodUpdateOTP = intent.getIntExtra(MainActivity.prefsPeriodUpdateOTP, 30);
                //Log.i(LOG_TAG, "time OTP" + periodUpdateOTP);
                // Gets the key from QR code which comes from BarcodeCaptureActivity
                key = intent.getStringExtra(MainActivity.QR_CODE_KEY);
                if (key != "") {
                    // Calls native code to generate OTP
                    byte[] resultBytes = OTPHandler.generateOtp(key);
                    // Handles the bytes from native code
                    String result = OTPHandler.getTokenOTP6Digits(resultBytes);
                    if (result != "") {
                        // Broadcasts the result for MainActivity
                        broadcastIntent.putExtra("Data", result);
                        sendBroadcast(broadcastIntent);
                        // Loop
                        handler.postDelayed(this, periodUpdateOTP * 1000);
                    }
                } else {
                    handler.removeCallbacksAndMessages(this);
                    sendBroadcast(broadcastIntent);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        return START_REDELIVER_INTENT;
    }



    @Override
    public IBinder onBind(Intent intent) {
        // Wont be called as service is not bound
        Log.i(LOG_TAG, "In onBind");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(LOG_TAG, "In onTaskRemoved");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
    }
}