package db.drury.com.dbchallenge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {


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
                            byte[] otp = OTPHandler.generateOtp(key);
                            resultOTP = OTPHandler.getTokenOTP6Digits(otp);
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


    // save data on prefs
    public void saveKey(String tokenKey) {
        if (prefs == null)
            prefs = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefsKey, OTPHandler.encrypt(tokenKey));
        editor.commit();
        dataStored = true;
        changeButtonsVisibility();
    }

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
    public String getValueFromPrefs(String tokenKey) {;
        if (prefs == null)
            prefs = this.getPreferences(MODE_PRIVATE);
        String result = prefs.getString(tokenKey, "");
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
        dataStored = false;
        changeButtonsVisibility();
    }



    public void initialize() {

        prefs = this.getPreferences(MODE_PRIVATE);
        resultOTP = getValueFromPrefs(prefsKey);

        // object references
        buttonGetKey = (BootstrapButton) findViewById(R.id.buttonGetKey);
        buttonDeleteData = (BootstrapButton) findViewById(R.id.buttonDeleteData);
        textViewTOTPLabel = (BootstrapLabel) findViewById(R.id.textViewTOTPLabel);
        textViewTokenInfo = (TextView) findViewById(R.id.textViewTokenLabel);
        textViewLabelOTPResult = (TextView) findViewById(R.id.textViewLabelOTPResult);

        if (resultOTP != null && resultOTP != "") {
            dataStored = true;
            textViewTOTPLabel.setText(resultOTP);
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
    }

}
