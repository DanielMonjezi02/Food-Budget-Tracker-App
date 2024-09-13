package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Homepage extends AppCompatActivity implements View.OnClickListener{

    private FloatingActionButton scanButton;
    private BottomNavigationView bottomNavigationView;
    private String currentUserID;
    private TextView banner, weeklySpending;
    private Date startDate, endDate;
    private double totalAmount;
    private int productCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        scanButton = findViewById(R.id.scanButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        weeklySpending = findViewById(R.id.weeklySpending);
        banner = findViewById(R.id.banner);

        scanButton.setOnClickListener(this);
        bottomNavigationView.setOnItemSelectedListener(new NavigationListener(this));

        // Get a reference to the SharedPreferences object
        SharedPreferences preferences = getSharedPreferences("my_app", MODE_PRIVATE);

        boolean isLoggedIn = preferences.getBoolean("is_logged_in", false);

        if (!isLoggedIn) {
            Intent intent = new Intent(this, GuestPage.class);
            startActivity(intent);
            finish();
        } else{
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            getUserFullName();
        }



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scanButton:
                startActivity(new Intent(this, BarcodeScanner.class));
                break;
        }
    }

    private void getUserFullName(){
        DatabaseReference userFullNameRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users").child(currentUserID).child("fullName");

        userFullNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userFullName = snapshot.getValue().toString();
                StringBuilder stringBuilder = new StringBuilder();
                String[] words = userFullName.split("\\s+");
                for (String word : words) {
                    String capitalizedWord = Character.toUpperCase(word.charAt(0)) + word.substring(1);
                    stringBuilder.append(capitalizedWord).append(" ");
                }
                userFullName = stringBuilder.toString().trim();
                Log.i(TAG, "userFullName " + userFullName);
                banner.setText("Welcome Back " + userFullName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        displayWeeklySpending();
    }

    private void displayWeeklySpending(){
        LocalDate currentDate = LocalDate.now();

        LocalDate monday = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        startDate = Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LocalDate sunday = monday.plusDays(6);
        endDate = Date.from(sunday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        DecimalFormat priceFormat = new DecimalFormat("0.00");

        DatabaseReference categoriesRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID);

        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {


                    // Get list of all the product prices within the selected date
                    for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                        String dateString = productSnapshot.child("Date").getValue(String.class);
                        Date productDate = null;
                        try {
                            productDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateString);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        if (productDate != null && (productDate.equals(startDate) || productDate.after(startDate)) && (productDate.equals(endDate) || productDate.before(endDate))) {
                            // The product falls within the current week
                            productCounter = productCounter + 1;
                            double price = Double.parseDouble(productSnapshot.child("Price").getValue(String.class));
                            totalAmount += price;
                        }
                    }
                }

                String formattedAmount = priceFormat.format(totalAmount);
                weeklySpending.setText("Spending's this week:" + "\n" + "£" +formattedAmount + "\n" + "\n" + "\n" + "You've added " + productCounter + " product's this week. Keep tracking!");



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error retrieving categories: " + error.getMessage());
            }
        });


    }

}