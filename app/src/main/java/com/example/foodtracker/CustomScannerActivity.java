package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.util.Random;

public class CustomScannerActivity extends Activity implements
        DecoratedBarcodeView.TorchListener {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private Button switchFlashlightButton;
    private ViewfinderView viewfinderView;
    protected ImageButton searchBarcode;
    private EditText barcodeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.setTorchListener(this);
        searchBarcode = findViewById(R.id.searchBarcode);
        barcodeInput = findViewById(R.id.barcodeInput);

        viewfinderView = findViewById(R.id.zxing_viewfinder_view);

        searchBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(barcodeInput.getText().toString().isEmpty()){
                    Toast.makeText(CustomScannerActivity.this, "Barcode input can't be empty!", Toast.LENGTH_LONG).show();
                }
                else{
                    Intent intent = new Intent(CustomScannerActivity.this, BarcodeScanner.class);
                    intent.putExtra("barcodeInput", barcodeInput.getText().toString());
                    startActivity(intent);
                }
            }
        });

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.setShowMissingCameraPermissionDialog(false);
        capture.decode();

        changeMaskColor(null);
        changeLaserVisibility(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }


    public void changeMaskColor(View view) {
        Random rnd = new Random();
        int color = Color.argb(100, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        viewfinderView.setMaskColor(color);
    }

    public void changeLaserVisibility(boolean visible) {
        viewfinderView.setLaserVisibility(visible);
    }

    @Override
    public void onTorchOn() {
    }

    @Override
    public void onTorchOff() {
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CustomScannerActivity.this, Homepage.class));
    }

}