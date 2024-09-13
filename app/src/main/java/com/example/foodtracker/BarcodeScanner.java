package com.example.foodtracker;

import static android.content.ContentValues.TAG;
import static java.sql.DriverManager.println;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.Manifest;


import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BarcodeScanner extends AppCompatActivity {
    private View viewfinderView;
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Intent originalIntent = result.getOriginalIntent();
                    if (originalIntent == null) {
                        Log.d("MainActivity", "Cancelled scan");
                        // Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                    } else if(originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                        Log.d("MainActivity", "Cancelled scan due to missing camera permission");
                        // Toast.makeText(MainActivity.this, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("MainActivity", "Scanned");
                    // Toast.makeText(MainActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String barcodeInput = intent.getStringExtra("barcodeInput");
        if(barcodeInput != null && !barcodeInput.isEmpty()){
            checkForProduct(barcodeInput);
        }else{
            viewfinderView = findViewById(R.id.zxing_viewfinder_view);

            scanCustomScanner(viewfinderView);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK) {
            // Get the barcode text from the intent
            String barcodeText = data.getStringExtra(Intents.Scan.RESULT);
            Log.i(TAG, "Barcode text: " + barcodeText);
            checkForProduct(barcodeText);

            // You can then use the barcode text as needed
        }
    }

    public void scanCustomScanner(View view) {
        ScanOptions options = new ScanOptions().setOrientationLocked(false).setCaptureActivity(CustomScannerActivity.class);
        barcodeLauncher.launch(options);
    }

    protected void checkForProduct(String barcode) {
        OkHttpClient client = new OkHttpClient();
        String apiUrl = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
        Request request = new Request.Builder().url(apiUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                startActivity(new Intent(BarcodeScanner.this, Homepage.class));
                Toast.makeText(BarcodeScanner.this, "Failed to look for product in our database, please try again!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        String productName = json.getJSONObject("product").getString("product_name");
                        String categories = json.getJSONObject("product").getString("categories");
                        Intent productFoundActivity = new Intent(BarcodeScanner.this, AddProduct.class);
                        productFoundActivity.putExtra("productName", productName);
                        productFoundActivity.putExtra("categories", categories);
                        startActivity(productFoundActivity);
                    } catch (JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScanner.this);
                                builder.setMessage("Barcode can not be found in our database. " + "\n" + "Would you like to create a product?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                startActivity(new Intent(BarcodeScanner.this, CreateProduct.class));
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                startActivity(new Intent(BarcodeScanner.this, Homepage.class));
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });
                    }
                } else {
                    startActivity(new Intent(BarcodeScanner.this, Homepage.class));
                    Toast.makeText(BarcodeScanner.this, "Failed to look for product in our database, please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
