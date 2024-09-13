package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class ProductDetailsBase extends AppCompatActivity {
    protected String productName, categories, currentUserID, shoppingListId;
    protected EditText priceEdit;
    protected TextInputEditText productNameTextView;
    protected ImageButton addProduct, backButton;
    protected Spinner categoryDropdown, storeNameDropdown, shoppingListDropdown;
    protected TextView dateEdit, banner;
    protected ProgressBar loadingBar;
    protected LinearLayout priceRow, categoryRow, storeRow, dateRow, shoppingListRow;
    protected RelativeLayout topBar;
    protected View bar;
    protected int currentYear, currentMonth, currentDay;
    protected boolean sharedShoppingList = false;

    protected void initViews() {
        productNameTextView = findViewById(R.id.productNameTextView);
        bar = findViewById(R.id.bar);
        priceEdit = findViewById(R.id.priceEdit);
        dateEdit = findViewById(R.id.dateEdit);
        banner = findViewById(R.id.banner);
        loadingBar = findViewById(R.id.loadingBar);
        topBar = findViewById(R.id.topBar);
        addProduct = findViewById(R.id.addProduct);
        backButton = findViewById(R.id.backButton);
        priceRow = findViewById(R.id.priceRow);
        categoryRow = findViewById(R.id.categoryRow);
        storeRow = findViewById(R.id.storeRow);
        dateRow = findViewById(R.id.dateRow);
        shoppingListRow = findViewById(R.id.shoppingListRow);
        shoppingListDropdown = findViewById(R.id.shoppingListDropdown);
        categoryDropdown = findViewById(R.id.categoryDropdown);
        storeNameDropdown = findViewById(R.id.storeNameDropdown);

        String[] storeNames = {"Morrisons", "Sainsbury's", "Tesco", "Asda", "Aldi", "Iceland"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, storeNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        storeNameDropdown.setAdapter(adapter);
    }

    protected void getUserCategories(UserCategoriesCallback callback){
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID);

        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> userCategories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String category = categorySnapshot.getKey();
                    Log.i(TAG, "Category: " + category);
                    userCategories.add(category);
                }
                callback.onUserCategoriesReceived(userCategories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error retrieving categories: " + error.getMessage());
            }
        });
    }

    protected void getCurrentDate()
    {
        final Calendar c = Calendar.getInstance();
        currentYear = c.get(Calendar.YEAR);
        currentMonth = c.get(Calendar.MONTH);
        currentDay = c.get(Calendar.DAY_OF_MONTH);

        dateEdit.setText(currentDay + "/" + (currentMonth + 1) + "/" + currentYear);

    }

    protected void enableVisibility(){
        loadingBar.setVisibility(View.INVISIBLE);
        topBar.setVisibility(View.VISIBLE);
        productNameTextView.setVisibility(View.VISIBLE);
        bar.setVisibility(View.VISIBLE);
        priceRow.setVisibility(View.VISIBLE);
        categoryRow.setVisibility(View.VISIBLE);
        dateRow.setVisibility(View.VISIBLE);
        storeRow.setVisibility(View.VISIBLE);
        shoppingListRow.setVisibility(View.VISIBLE);
    }

    protected void getUserShoppingList(){
        ArrayList<String> shoppingListItems = new ArrayList<String>();
        shoppingListItems.add("None");

        DatabaseReference shoppingListRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List");

        shoppingListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot shoppingListSnapshot : snapshot.getChildren()) {
                    // Check if the current user logged in is within any shared shopping lists
                    Log.i(TAG, "Shopping List Key: " + shoppingListSnapshot.getKey());
                    Log.i(TAG, "Value: " + shoppingListSnapshot.child("Users").child(currentUserID).getValue());
                    if (shoppingListSnapshot.child("Users").hasChild(currentUserID) && "Read".equals(shoppingListSnapshot.child("Users").child(currentUserID).getValue())) {
                        if (shoppingListSnapshot.child("Accepted Permissions").child(currentUserID).getValue() != null && shoppingListSnapshot.child("Accepted Permissions").child(currentUserID).getValue().equals(true)) {
                            // User is within a shared shopping list and has accepted permissions
                            shoppingListId = shoppingListSnapshot.getKey();
                            sharedShoppingList = true;
                            DataSnapshot itemsSnapshot = shoppingListSnapshot.child("Items");
                            for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                                String item = itemSnapshot.getKey();
                                Log.i(TAG, "Items: " + item);
                                shoppingListItems.add(item);
                            }

                            createShoppingListDropdown(shoppingListItems);
                        }
                    }

                }
                if(sharedShoppingList == false){
                    shoppingListRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot shoppingListSnapshot : snapshot.getChildren()) {
                                Log.i(TAG, "shoppingListSnapshot" + shoppingListSnapshot);
                                if(shoppingListSnapshot.child("Users").hasChild(currentUserID)){
                                    shoppingListId = shoppingListSnapshot.getKey();
                                    DataSnapshot itemsSnapshot = shoppingListSnapshot.child("Items");
                                    for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                                        String item = itemSnapshot.getKey();
                                        Log.i(TAG, "Items1: " + item);
                                        shoppingListItems.add(item);
                                    }

                                    createShoppingListDropdown(shoppingListItems);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    protected void createShoppingListDropdown( ArrayList<String> shoppingListItems ){
        Log.i(TAG, "HAPPENED?: ");
        // Call a method to set the adapter and populate the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_layout, shoppingListItems);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        shoppingListDropdown.setAdapter(adapter);

    }

}
