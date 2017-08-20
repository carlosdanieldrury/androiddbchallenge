package db.drury.com.dbchallenge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    // Link to native Lib wrote in C
    static {
        System.loadLibrary("otpjni");
    }

    //Objects
    private static final int BARCODE_READER_REQUEST_CODE = 100;

    // Keep save data
    private SharedPreferences prefs;
    private boolean dataStored = false;
    
    private String prefsKey = "qrcode";
    private String resultOTP;

    // view components
    private BootstrapButton buttonGetKey;
    private BootstrapButton buttonDeleteData;
    private TextView textViewTokenInfo;
    private TextView textViewTOTPLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BARCODE_READER_REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                final Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                textViewTokenInfo.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jObj = new JSONObject(barcode.rawValue);
                            String key = jObj.getString("key");
                            // calls native code
                            byte[] otp = generateOtp(key);
                            resultOTP = getTokenOTP6Digits(otp);
                            textViewTOTPLabel.setText(resultOTP);
                            saveKey(resultOTP);
                            textViewTokenInfo.setText(barcode.displayValue);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }


    // get the byte[] from generateOtp method and returns a String with the result of 6 digits
    public String getTokenOTP6Digits(byte[] dbc) {
        String res = "";
        if (dbc != null) {
            byte[] dbc1 = Arrays.copyOfRange(dbc, 9, 13);
            String aux = toHexadecimal(dbc1);
            long decimalValue = Long.parseLong(aux, 16);
            String decimalValueString = Long.toString(decimalValue);
            int decimalValueStringSize = decimalValueString.length();
            res = decimalValueString.substring(decimalValueStringSize-6, decimalValueStringSize);
        }

        return res;
    }


    // converts the byte[] to hex string
    private String toHexadecimal(byte[] digest){
        String hash = "";
        for(byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += Integer.toHexString(b);
        }
        return hash;
    }

    // save data on prefs
    public void saveKey(String tokenKey) {
        if (prefs == null)
            prefs = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefsKey, encrypt(tokenKey));
        editor.commit();
        dataStored = true;
        changeButtonsVisibility();
    }

    public void changeButtonsVisibility() {
        buttonGetKey.setVisibility(dataStored ? View.INVISIBLE : View.VISIBLE);
        buttonDeleteData.setVisibility(dataStored ? View.VISIBLE : View.INVISIBLE);

        if (!dataStored) {
            textViewTOTPLabel.setText("");
            textViewTokenInfo.setText("");
        }
    }


    // get the value from Preferences
    public String getValueFromPrefs(String tokenKey) {;
        if (prefs == null)
            prefs = this.getPreferences(MODE_PRIVATE);
        String result = prefs.getString(tokenKey, "");
        if (result != "")
            result = decrypt(result);
        return result;
    }

    public void deletePrefs(String tokenKey) {
        if (prefs == null)
            prefs = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(tokenKey);
        editor.commit();
        resultOTP = "";
        dataStored = false;
        changeButtonsVisibility();
    }

    public static String encrypt(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public static String decrypt(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }

    // a interface to the native code
    public static native byte[] generateOtp(String key);


    public void initialize() {

        prefs = this.getPreferences(MODE_PRIVATE);
        resultOTP = getValueFromPrefs(prefsKey);

        if (resultOTP != null && resultOTP != "") {
            dataStored = true;
        }


        // object references
        buttonGetKey = (BootstrapButton) findViewById(R.id.buttonGetKey);
        buttonDeleteData = (BootstrapButton) findViewById(R.id.buttonDeleteData);
        textViewTOTPLabel = (TextView) findViewById(R.id.textViewTOTPLabel);
        textViewTokenInfo = (TextView) findViewById(R.id.textViewTokenLabel);

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
    }

}
