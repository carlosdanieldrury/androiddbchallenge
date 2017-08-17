package db.drury.com.dbchallenge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class MainActivity extends AppCompatActivity {

    //Objects

    private BootstrapButton buttonGetKey;
    private BootstrapButton buttonDeleteData;
    private TextView textViewTokenInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void initialize() {

        // object references
        buttonGetKey = (BootstrapButton) findViewById(R.id.buttonGetKey);
        buttonDeleteData = (BootstrapButton) findViewById(R.id.buttonDeleteData);

        // onClickListeners
        View.OnClickListener buttonGetKeyListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO

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
