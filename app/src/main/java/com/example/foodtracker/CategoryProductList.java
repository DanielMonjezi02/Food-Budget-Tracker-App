package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoryProductList extends AppCompatActivity implements View.OnClickListener{

    private String categoryName;
    private String currentUserID;
    private String startDateString, endDateString;
    private Date startDate, endDate;
    private ListView productList;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_product_list);



        productList = findViewById(R.id.productList);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(this);

        categoryName = getIntent().getStringExtra("selectedCategory");
        startDateString = getIntent().getStringExtra("startDate");
        endDateString = getIntent().getStringExtra("endDate");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        try {
            startDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault()).parse(startDateString);
            endDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault()).parse(endDateString);
            getProductList();
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing product date: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        // Disables back button on their phone
    }

    @Override
    public void onClick(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        switch(view.getId()){
            case R.id.backButton:
                startActivity(new Intent(this, Diary.class));
                break;
        }
    }

    private void getProductList(){
        DatabaseReference productListRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID).child(categoryName);

        productListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> productIdList = new ArrayList<>();
                ArrayList<String> productNameList = new ArrayList<>();
                ArrayList<String> productDateList = new ArrayList<>();
                ArrayList<String> productPriceList = new ArrayList<>();
                ArrayList<String> productCategory = new ArrayList<>();
                ArrayList<String> productStoreName = new ArrayList<>();
                for (DataSnapshot productListSnapshot : snapshot.getChildren()) {
                    String dateString = productListSnapshot.child("Date").getValue(String.class);
                    Date date = null;
                    try {
                        date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateString);
                    } catch (ParseException e) {
                        Log.e(TAG, "Error parsing product date: " + e.getMessage());
                    }
                    if (date != null && (date.equals(startDate) || date.after(startDate)) && (date.equals(endDate) || date.before(endDate))) {
                        Log.i(TAG, "HELLO " + productListSnapshot.child("Product Name"));
                        productIdList.add(productListSnapshot.getKey());
                        productNameList.add(productListSnapshot.child("Product Name").getValue(String.class));
                        productPriceList.add(productListSnapshot.child("Price").getValue(String.class));
                        productCategory.add(categoryName);
                        productDateList.add(dateString);
                        productStoreName.add(productListSnapshot.child("Store Name").getValue(String.class));
                    }
                }

                ProductListAdapter adapter = new ProductListAdapter(CategoryProductList.this, R.layout.product_list_design, CategoryProductList.this, productIdList, productNameList, productDateList, productPriceList, productStoreName, productCategory);

                productList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    protected void removeProduct(String productId){
        DatabaseReference removeProductRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID).child(categoryName);

        removeProductRef.child(productId).removeValue();

        removeProductRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Category exists in database
                } else {
                    removeProductRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}