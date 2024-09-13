package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.Manifest;
import android.location.Location;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddProduct extends ProductDetailsBase implements View.OnClickListener{
    private ImageView priceAlert;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    private String priceAlertMessage;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        initViews();

        Intent barcodeScannerIntent = getIntent();
        productName = barcodeScannerIntent.getStringExtra("productName");
        categories = barcodeScannerIntent.getStringExtra("categories"); // List of all the category words of the product

        addProduct = findViewById(R.id.addProduct);
        priceAlert = findViewById(R.id.priceAlert);

        addProduct.setOnClickListener(this);
        dateEdit.setOnClickListener(this);
        priceAlert.setOnClickListener(this);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddProduct.this, Homepage.class));
            }
        });


        productNameTextView.setText(productName);


        priceEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkPriceChange();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getCurrentDate();
        createLocationRequest();
        requestLocationUpdates();

        // Checks if location has been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddProduct.this, Homepage.class));
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.addProduct:
                addProductDetails();
                break;
            case R.id.dateEdit:
                final Calendar c = Calendar.getInstance();
                // Show DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddProduct.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Set selected date to EditText
                                dateEdit.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, currentYear, currentMonth, currentDay);
                datePickerDialog.show();
                break;
            case R.id.priceAlert:
                TooltipCompat.setTooltipText(priceAlert, priceAlertMessage);
                break;
        }
    }

    private void getLastLocation() {
        Log.i(TAG, "getLastLocation!");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLastLocation = location;
                        getStoreName();
                    } else {
                        // Request location updates instead
                        requestLocationUpdates();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void requestLocationUpdates() {
        Log.i(TAG, "requestLocationUpdates");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        for (Location location : locationResult.getLocations()) {
                            if (location != null) {
                                mLastLocation = location;
                                break;
                            }
                        }
                    }
                }
            }, null);
        } else {
            Log.i(TAG, "LOCATION PERMISSION NOT GRANTED");
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); // Update every 10 seconds
        mLocationRequest.setFastestInterval(5000); // Fastest update interval
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // High accuracy location
    }


    private void getStoreName() {
        Log.d(TAG, "GETTING STORE NAME" + mLastLocation);
        if (mLastLocation != null) {
            Log.d(TAG, "Location Latitude: " + mLastLocation.getLatitude());
            Log.d(TAG, "Location Longitude: " + mLastLocation.getLongitude());


            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(40, TimeUnit.SECONDS)
                    .build();

            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() +
                    "&radius=" + 50 +
                    "&keyword=" + "Sainsbury's|Tesco|Asda|Morrisons|Lidl|Aldi" +
                    "&key=" + "AIzaSyB5K8LQxlhcVciz7do3U9FYftNkqlElKAI";
            Request request = new Request.Builder().url(url).build();


            Log.d(TAG, "request" + request);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error during API call", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray results = jsonResponse.getJSONArray("results");
                        Log.d(TAG, " results: " + results);
                        if (results.length() > 0) {
                            JSONObject result = results.getJSONObject(0);
                            String storeName = result.getString("name");
                            Log.d(TAG, "Store name: " + storeName);

                            getProductPrice(productName, storeName);
                        }else{
                            enableVisibility();
                        }

                    } catch (JSONException e) {
                        // Handle JSON parsing error
                    }
                }
            });
        }
    }

    private void getProductPrice(String productName, String storeName) {
        final String url = "https://www.trolley.co.uk/search/?from=search&q=" + productName;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to trolley.co.uk to look for price of item
                    Document doc = Jsoup.connect(url).get();

                    Element product = doc.selectFirst(".product-item"); // select all elements with class "product-item"

                    String productUrl = product.select("a").attr("href");
                    Document productDoc = Jsoup.connect("https://www.trolley.co.uk/" + productUrl).get();

                    Element comparisonTable = productDoc.select("div.comparison-table").first();

                    Elements collapse = comparisonTable.select("div.collapse ");

                    Element storeSvg = collapse.select("svg[title=" + storeName + "]").first();

                    Element storeParent = storeSvg.parent();

                    Element priceElement = storeParent.nextElementSibling().selectFirst("._price");

                    String productPrice = priceElement.text().split("\\s")[0];


                    if (productPrice == null || productPrice.isEmpty()) {
                        // Set text to empty if price is not found
                        priceEdit.setText("");
                    } else {
                        // Price is found, display the price
                        productPrice = productPrice.replace("£", "");
                        priceEdit.setText(productPrice);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        getUserCategories(new UserCategoriesCallback() {
            @Override
            public void onUserCategoriesReceived(ArrayList<String> userCategories) {
                setSpinnerAdapter(userCategories);
            }
        });
    }

    private void checkPriceChange() {
        String productNameText = productNameTextView.getText().toString();
        if (!priceEdit.getText().toString().isEmpty()) {
            double productPrice = Double.parseDouble(priceEdit.getText().toString());

            DatabaseReference priceRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID);

            priceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<DataSnapshot> productList = new ArrayList<>();
                    for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                            if(productSnapshot.exists())
                            {
                                String productName = productSnapshot.child("Product Name").getValue(String.class);
                                if (productName.equals(productNameText)) {
                                    productList.add(productSnapshot);
                                }
                            }
                        }
                    }
                    // Sort the list by date in descending order
                    Collections.sort(productList, new Comparator<DataSnapshot>() {
                        @Override
                        public int compare(DataSnapshot o1, DataSnapshot o2) {
                            String date1 = o1.child("Date").getValue(String.class);
                            String date2 = o2.child("Date").getValue(String.class);
                            return date2.compareTo(date1);
                        }
                    });

                    if(!productList.isEmpty()){
                        Log.d(TAG, "NOT EMPTY");
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        String productDatabasePrice = productList.get(0).child("Price").getValue(String.class);
                        double recentPrice = Double.parseDouble(productDatabasePrice);
                        if (productPrice < recentPrice) {
                            priceAlert.setVisibility(View.VISIBLE);
                            priceAlertMessage = "Price has decreased since the last time you added this item by " + decimalFormat.format(recentPrice - productPrice);
                            Log.i(TAG, "Decreased" + decimalFormat.format(productPrice - recentPrice));
                            TooltipCompat.setTooltipText(priceAlert, priceAlertMessage);
                        } else if (productPrice > recentPrice){
                            priceAlert.setVisibility(View.VISIBLE);
                            priceAlertMessage = "Price has increased since the last time you added this item by " + decimalFormat.format(productPrice - recentPrice);
                            Log.i(TAG, "Increased" + decimalFormat.format(productPrice - recentPrice));
                            TooltipCompat.setTooltipText(priceAlert, priceAlertMessage);
                        } else {
                            priceAlert.setVisibility(View.INVISIBLE);
                            Log.i(TAG, "Stayed the same");
                        }
                    }
                    }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
       enableVisibility();
    }

    private void setSpinnerAdapter(ArrayList<String> userCategories) {
        // Create an dropdown for category
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddProduct.this, R.layout.spinner_layout, userCategories);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        categoryDropdown.setAdapter(adapter);

        String[] category = categories.split(",");

        // Checks if there's a one to one match in the words in the users categories and from the products category
        for (String categoryWord : category){
            for(String userCategoryWord : userCategories)
            {
                if(categoryWord.equals(userCategoryWord))
                {
                    int position = adapter.getPosition(categoryWord);
                    categoryDropdown.setSelection(position);
                }
            }
        }

        // Checks if there's a similar match instead
        for (String categoryWord : category){
            for(String userCategoryWord : userCategories)
            {
                if(userCategoryWord.contains(categoryWord))
                {
                    int position = adapter.getPosition(categoryWord);
                    categoryDropdown.setSelection(position);
                }
            }
        }

        getUserShoppingList();
        checkPriceChange();
    }

    private void addProductDetails() {
        if (currentUserID != null) {
            if (priceEdit.getText().toString().isEmpty()) {
                Toast.makeText(AddProduct.this, "Product price can't be empty!", Toast.LENGTH_SHORT).show();
            } else if(productNameTextView.getText().toString().isEmpty()) {
                Toast.makeText(AddProduct.this, "Product name can't be empty!", Toast.LENGTH_SHORT).show();
            }else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app");
                    DatabaseReference categoryRef = database.getReference("User Categories").child(currentUserID).child(categoryDropdown.getSelectedItem().toString());

                    // Check if the category node  exists
                    categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Add product to database
                                String productId = categoryRef.push().getKey(); // Generate a unique key for the new product
                                String price = String.format("%.2f", Double.parseDouble(priceEdit.getText().toString()));
                                categoryRef.child(productId).child("Date").setValue(dateEdit.getText().toString());
                                categoryRef.child(productId).child("Price").setValue(price);
                                categoryRef.child(productId).child("Product Name").setValue(productName);
                                categoryRef.child(productId).child("Store Name").setValue(storeNameDropdown.getSelectedItem().toString());
                                if (shoppingListDropdown.getSelectedItem().toString() != "None") {
                                    DatabaseReference shoppingListItemRef = database.getReference("Shopping List").child(shoppingListId).child("Items").child(shoppingListDropdown.getSelectedItem().toString());
                                    shoppingListItemRef.removeValue();
                                }
                                startActivity(new Intent(AddProduct.this, Homepage.class));
                            } else{
                                // Category node doesn't exist anymore, must have been deleted.
                                Toast.makeText(AddProduct.this, "The category you selected no longer exits!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error checking if category exists", error.toException());
                        }
                    });
                }
            }
        }


    // Handling upon location request (initial setup)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "HAPPENED!");
                getLastLocation();
            } else {
                // Location permission has been denied
            }
        }
    }
}