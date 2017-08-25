package db.drury.com.dbchallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {


    //Objects
    private static final int BARCODE_READER_REQUEST_CODE = 100;

    public static String QR_CODE_KEY = "qrcodekey";
    public static String prefsPeriodUpdateOTP = "pref_time";

    private int periodUpdateOTP;
    private SharedPreferences sharedPreferences;

    //internal Service
    public static final String mBroadcastStringAction = "db.drury.com.dbchallenge.broadcast.String";
    private IntentFilter mIntentFilter;

    // Keep save data
    private SharedPreferences prefs;
    private boolean dataStored = false;

    private String prefsKey = "qrcodekey";
    private String prefsQRCode = "stringQRCode";

    private String resultOTP;
    private String resultQRCode;

    // view components
    private BootstrapButton buttonGetKey;
    private BootstrapButton buttonDeleteData;
    private TextView textViewTokenInfo;
    private BootstrapLabel textViewTOTPLabel;
    private TextView textViewLabelOTPResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TypefaceProvider.registerDefaultIconSets();
        initialize();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        // After choosing the period of updating OTP on PreferencesActivity, onResume is called


        periodUpdateOTP = Integer.parseInt(sharedPreferences.getString(prefsPeriodUpdateOTP, "30"));
        resultQRCode = getValueFromPrefs(prefsQRCode);
        registerReceiver(mReceiver, mIntentFilter);
        if (resultQRCode != "") {
            setServicePropertiesAndStart();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        if (resultQRCode == "") {
            deletePrefs(prefsQRCode);
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BARCODE_READER_REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                final Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                textViewTokenInfo.post(new Runnable() {
                    @Override
                    public void run() {
                        resultQRCode = barcode.rawValue;
                        setServicePropertiesAndStart();
                    }
                });
            }
        }
    }

    public String getKeyFromJson(String objString) {
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(objString);
            String key = jObj.getString("key");
            return key;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setKeyOnView(String key) {

        if (resultQRCode != "") {
            resultOTP = key;
            textViewTOTPLabel.setText(resultOTP);
            saveDataOnPref(prefsKey, resultOTP);
            saveDataOnPref(prefsQRCode, resultQRCode);
        }

    }

    // save data on prefs
    public void saveDataOnPref(String prefCode ,  String token) {
        if (prefs == null)
            prefs = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefCode, OTPHandler.encrypt(token));
        editor.commit();
        dataStored = true;
        changeButtonsVisibility();
    }

    // change visibility of items on view
    public void changeButtonsVisibility() {
        buttonGetKey.setVisibility(dataStored ? View.INVISIBLE : View.VISIBLE);
        buttonDeleteData.setVisibility(dataStored ? View.VISIBLE : View.INVISIBLE);
        textViewLabelOTPResult.setVisibility(dataStored ? View.VISIBLE : View.INVISIBLE);

        if (!dataStored) {
            textViewTOTPLabel.setText("");
            textViewTokenInfo.setText("");
        }
    }


    // get the value from Preferences
    public String getValueFromPrefs(String key) {
        if (prefs == null)
            prefs = this.getPreferences(MODE_PRIVATE);
        String result = prefs.getString(key, "");
        if (result != "")
            result = OTPHandler.decrypt(result);
        return result;
    }

    public void deletePrefs(String tokenKey) {
        if (prefs == null)
            prefs = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(tokenKey);
        editor.commit();
        resultOTP = "";
        resultQRCode = "";
        dataStored = false;
        Intent stopIntent = new Intent(MainActivity.this,
                BroadcastService.class);
        stopIntent.putExtra(QR_CODE_KEY, getKeyFromJson(resultQRCode));
        stopService(stopIntent);
        changeButtonsVisibility();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_otp:
                //Toast.makeText(this, "ADD!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initialize() {

        prefs = this.getPreferences(MODE_PRIVATE);
        resultOTP = getValueFromPrefs(prefsKey);
        resultQRCode = getValueFromPrefs(prefsQRCode);
        String valueFromPrefsForPeriodUpdateOTP = getValueFromPrefs(prefsPeriodUpdateOTP);
        periodUpdateOTP = Integer.parseInt(valueFromPrefsForPeriodUpdateOTP != "" ? valueFromPrefsForPeriodUpdateOTP : "30" );
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


        // object references
        buttonGetKey = (BootstrapButton) findViewById(R.id.buttonGetKey);
        buttonDeleteData = (BootstrapButton) findViewById(R.id.buttonDeleteData);
        textViewTOTPLabel = (BootstrapLabel) findViewById(R.id.textViewTOTPLabel);
        textViewTokenInfo = (TextView) findViewById(R.id.textViewTokenLabel);
        textViewLabelOTPResult = (TextView) findViewById(R.id.textViewLabelOTPResult);


        if (resultQRCode != null && resultQRCode != "") {
            textViewTokenInfo.setText(resultQRCode);
            dataStored = true;
        }

        // Change visibility of buttons
        changeButtonsVisibility();


        // onClickListeners
        View.OnClickListener buttonGetKeyListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        };

        View.OnClickListener buttonDeleteDataListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePrefs(prefsKey);
            }
        };

        buttonGetKey.setOnClickListener(buttonGetKeyListener);
        buttonDeleteData.setOnClickListener(buttonDeleteDataListener);


        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastStringAction);

    }

    public void setServicePropertiesAndStart() {
        if (resultQRCode != "") {
            Intent serviceIntent = new Intent(this, BroadcastService.class);
            serviceIntent.putExtra(QR_CODE_KEY, getKeyFromJson(resultQRCode));
            serviceIntent.putExtra(prefsPeriodUpdateOTP, periodUpdateOTP);
            if (textViewTokenInfo.getText() == "") {
                textViewTokenInfo.setText(resultQRCode);
            }
            startService(serviceIntent);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(mBroadcastStringAction)) {
                    String value = intent.getStringExtra("Data");
                    //Log.i(BroadcastService.LOG_TAG, "Value from receiver: " + value);
                    if (resultQRCode != "") {
                        setKeyOnView(value);
                    }
                }
                Intent stopIntent = new Intent(MainActivity.this,
                        BroadcastService.class);
                stopService(stopIntent);

            }
    };


}
