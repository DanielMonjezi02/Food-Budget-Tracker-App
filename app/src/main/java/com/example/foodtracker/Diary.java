package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Diary extends AppCompatActivity implements View.OnClickListener{

    private FloatingActionButton scanButton;
    private BottomNavigationView bottomNavigationView;
    private TextView calenderWeek;
    private RelativeLayout topBar;
    private ImageButton backButton, forwardButton;
    private Calendar calendar = Calendar.getInstance();
    private ListView categoriesList;
    private String currentUserID;
    private Date startDate, endDate;
    double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        scanButton = (findViewById(R.id.scanButton));
        forwardButton = findViewById(R.id.forwardButton);
        backButton = findViewById(R.id.backButton);
        categoriesList = findViewById(R.id.categoriesList);
        calenderWeek = findViewById(R.id.calenderWeek);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(new NavigationListener(this));
        scanButton.setOnClickListener(this);
        forwardButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        categoriesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // If user clicks add category
                if (position == categoriesList.getCount()-1) {
                    Log.i(TAG, "HAPPENED");
                    showInputDialog();
                } else if(position == categoriesList.getCount() - 2) {
                    // User clicked on total spent row, do nothing
                }else{
                    // User clicked on category, display products for that category
                    String selectedCategory = (String) parent.getItemAtPosition(position);
                    Intent intent = new Intent(Diary.this, CategoryProductList.class);
                    intent.putExtra("selectedCategory", selectedCategory);
                    intent.putExtra("startDate", startDate.toString());
                    intent.putExtra("endDate", endDate.toString());
                    startActivity(intent);
                }
            }
        });

        categoriesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == categoriesList.getCount()-1 || position == categoriesList.getCount() - 2) {
                    // Do nothing, user trying to remove "Add category" or "Total Spent"
                }else {
                    removeCategory(parent.getItemAtPosition(position).toString());
                }
                return true;

            }
        });

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getInitialWeek();
        getCategories();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scanButton:
                startActivity(new Intent(this, BarcodeScanner.class));
                break;
            case R.id.forwardButton:
                changeWeek("Increase");
                break;
            case R.id.backButton:
                changeWeek("Decrease");
                break;
        }
    }


    protected void getCategories() {
        totalAmount = 0;
        DecimalFormat priceFormat = new DecimalFormat("0.00");
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID);

        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> userCategoriesList = new ArrayList<>();
                ArrayList<String> userAmountList = new ArrayList<>();

                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String category = categorySnapshot.getKey();
                    Log.i(TAG, "Category: " + category);
                    userCategoriesList.add(category);

                    double categoryTotalAmount = 0.0;

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
                            Log.i(TAG, "productDate: " + productDate);
                            double productPrice = Double.parseDouble(productSnapshot.child("Price").getValue(String.class));
                            categoryTotalAmount += productPrice;
                            totalAmount += productPrice;
                        }
                    }
                    String formattedAmount = priceFormat.format(categoryTotalAmount);
                    userAmountList.add("£" + formattedAmount);
                }
                userCategoriesList.add("Total Spent:");
                userAmountList.add("£" + priceFormat.format(totalAmount));



                Log.i(TAG, "userAmountList: " + userAmountList);
                CategoryAdapter adapter = new CategoryAdapter(Diary.this, R.layout.list_category_layout, userCategoriesList, userAmountList);

                categoriesList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error retrieving categories: " + error.getMessage());
            }
        });
    }


    private void changeWeek(String option) {

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (option == "Increase") {
            // Add 1 week to the calendar
            LocalDate monday = startLocalDate.plusDays(7);
            startDate = Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant());
            LocalDate sunday = endLocalDate.plusDays(7);
            endDate = Date.from(sunday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        if(option == "Decrease"){
            // Remove 1 week from calender
            LocalDate monday = startLocalDate.minusDays(7);
            startDate = Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant());
            LocalDate sunday = endLocalDate.minusDays(7);
            endDate = Date.from(sunday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // Format the dates to display in the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        String startString = dateFormat.format(startDate);
        String endString = dateFormat.format(endDate);

        // Display the new week start and end dates in the text view
        calenderWeek.setText(startString + " - " + endString);
        getCategories();


    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Diary.this, R.style.AlertDialog);
        builder.setTitle("Add Category");
        // Inflate the layout for the dialog
        View dialogView = LayoutInflater.from(Diary.this).inflate(R.layout.input_dialog, null);
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.input);
        input.setHint("Category Name");
        final TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Enter category name:");

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the input text and add it to the categories list
                String categoryName;
                String inputString = input.getText().toString();
                if(inputString.length() < 4 || inputString.isEmpty()){
                    Toast.makeText(Diary.this, "Category name must be at least 4 characters long", Toast.LENGTH_SHORT).show();
                    return;
                } else{
                    categoryName = inputString.substring(0, 1).toUpperCase() + inputString.substring(1);
                }
                Boolean categoryExists = false;
                for (int i = 0; i < categoriesList.getCount() - 1; i++) {
                    String listItemName = categoriesList.getItemAtPosition(i).toString();

                    // Check if the listItemName matches the categoryName
                    if (listItemName.equals(categoryName)) {
                        categoryExists = true;
                        break;
                    }
                }

                if(categoryExists == false){
                    addCategory(categoryName);
                } else{
                    Toast.makeText(Diary.this, "Category name already exists", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }

    private void addCategory(String categoryName){
        DatabaseReference addCategoryRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID);


        addCategoryRef.child(categoryName).setValue(true);
        getCategories();
    }

    private void removeCategory(String categoryName){
        DatabaseReference removeCategoryRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID);


        removeCategoryRef.child(categoryName).removeValue();
        getCategories();

    }

    private void getInitialWeek(){
        LocalDate currentDate = LocalDate.now();

        LocalDate monday = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        startDate = Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LocalDate sunday = monday.plusDays(6);
        endDate = Date.from(sunday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        String startDateString = formatter.format(monday);
        String endDateString = formatter.format(sunday);

        calenderWeek.setText(startDateString + " - " + endDateString);
    }


}