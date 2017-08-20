package db.drury.com.dbchallenge;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.vision.barcode.Barcode;


public class MainActivity extends AppCompatActivity {

    // Link to native Lib wrote in C
    static {
        System.loadLibrary("otpjni");
    }

    //Objects
    private static final int BARCODE_READER_REQUEST_CODE = 100;

    // key to be tested
    private String key = "QYDVQQLEwpUZWNoIERlcHQuMSgwJgYDV";

    private BootstrapButton buttonGetKey;
    private BootstrapButton buttonDeleteData;
    private TextView textViewTokenInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        // testing key to the native lib
        textViewTokenInfo.setText(generateOtp(key).toString());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BARCODE_READER_REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                final Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                textViewTokenInfo.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewTokenInfo.setText(barcode.displayValue);
                    }
                });
            }
        }
    }

    // a interface to the native code
    public static native byte[] generateOtp(String key);

    public void initialize() {

        // object references
        buttonGetKey = (BootstrapButton) findViewById(R.id.buttonGetKey);
        buttonDeleteData = (BootstrapButton) findViewById(R.id.buttonDeleteData);
        textViewTokenInfo = (TextView) findViewById(R.id.textViewTokenLabel);

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
                //TODO

            }
        };

        buttonGetKey.setOnClickListener(buttonGetKeyListener);
        buttonDeleteData.setOnClickListener(buttonDeleteDataListener);
    }

}
