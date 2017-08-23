/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This is was changed by Carlos Daniel Drury
 *
 */
package db.drury.com.dbchallenge;

import android.*;
import android.Manifest;
import android.app.Activity;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class BarcodeCaptureActivity extends AppCompatActivity {

    // string to save data
    public static final String BarcodeObject = "barcode";
    private static final String TAG = "BarcodeCaptureActivity";
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;


    // declarations
    private SurfaceView cameraView;
    private BarcodeDetector barcode;
    private CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_capture);

        initiliaze();

        setCameraSettings();
        setBarcodeProcessor();
    }

    private void setBarcodeProcessor() {
        barcode.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes =  detections.getDetectedItems();
                if(barcodes.size() > 0){
                    Intent intent = new Intent();
                    // stores the barcode result on the activity
                    intent.putExtra(BarcodeObject, barcodes.valueAt(0));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        if(!barcode.isOperational()){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.detector_error), Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    private void releaseCamera(){
        if (cameraSource != null){
            cameraSource.release();        // release the camera for other applications
            cameraSource = null;
        }
    }


    private void setCameraSettings() {
        cameraSource = new CameraSource.Builder(this, barcode)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1920,1024)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // Checks Camera permission, if there is no permission, the system handles the permission
                // if the user chooses 'DENY', it is possible to open the window of permissions again
                // clicking on the black screen.
                int rc = ActivityCompat.checkSelfPermission(BarcodeCaptureActivity.this, Manifest.permission.CAMERA);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    try {
                        // app opens the camera
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    requestCameraPermission();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }
        });
    }

    // handles the request camera permission
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(BarcodeCaptureActivity.this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = BarcodeCaptureActivity.this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        // Calls the listener if the screen keeps dark
        findViewById(R.id.activity_scan).setOnClickListener(listener);

    }


    // When the OS answers back after accepting or not the permissions for the app
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case RC_HANDLE_CAMERA_PERM:
                if (ActivityCompat.checkSelfPermission(BarcodeCaptureActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        setCameraSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }


    public void initiliaze() {
        // set all objects
        cameraView = (SurfaceView) findViewById(R.id.cameraView);
        cameraView.setZOrderMediaOverlay(true);
        barcode = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

    }



}