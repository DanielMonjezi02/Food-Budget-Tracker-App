package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

public class CreateProduct extends ProductDetailsBase {

    private TextInputEditText productNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_product);
        initViews();

        productNameTextView = findViewById(R.id.productNameTextView);

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createProduct();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateProduct.this, Homepage.class));
            }
        });

        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                // Show DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateProduct.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Set selected date to EditText
                                dateEdit.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, currentYear, currentMonth, currentDay);
                datePickerDialog.show();
            }
        });

        getCurrentDate();
        getUserShoppingList();
        getUserCategories(new UserCategoriesCallback() {
            @Override
            public void onUserCategoriesReceived(ArrayList<String> userCategories) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateProduct.this, R.layout.spinner_layout, userCategories);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
                Log.i(TAG, "userCategories: " + userCategories);
                Log.i(TAG, "adapter: " + adapter);
                categoryDropdown.setAdapter(adapter);
            }
        });

    }

    private void createProduct(){
        if (currentUserID != null) {
            if (productNameTextView.getText().toString().isEmpty()) {
                Toast.makeText(CreateProduct.this, "Product name can't be empty!", Toast.LENGTH_SHORT).show();
            } else if (priceEdit.getText().toString().isEmpty()){
                Toast.makeText(CreateProduct.this, "Product price can't be empty!", Toast.LENGTH_SHORT).show();
            } else {
                String price = String.format("%.2f", Double.parseDouble(priceEdit.getText().toString()));
                FirebaseDatabase database = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app");
                DatabaseReference categoryRef = database.getReference("User Categories").child(currentUserID).child(categoryDropdown.getSelectedItem().toString());

                // Check if the category node  exists
                categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, "Snapshot: " + snapshot);
                        if (snapshot.exists()) {
                            // Add product to database
                            String productId = categoryRef.push().getKey(); // Generate a unique key for the new product
                            categoryRef.child(productId).child("Date").setValue(dateEdit.getText().toString());
                            categoryRef.child(productId).child("Price").setValue(price);
                            categoryRef.child(productId).child("Product Name").setValue(productNameTextView.getText().toString().substring(0, 1).toUpperCase() + productNameTextView.getText().toString().substring(1));
                            categoryRef.child(productId).child("Store Name").setValue(storeNameDropdown.getSelectedItem().toString());
                            if (shoppingListDropdown.getSelectedItem() != null && shoppingListDropdown.getSelectedItem().toString() != "None" ) {
                                DatabaseReference shoppingListItemRef = database.getReference("Shopping List").child(shoppingListId).child("Items").child(shoppingListDropdown.getSelectedItem().toString());
                                shoppingListItemRef.removeValue();
                            }
                            startActivity(new Intent(CreateProduct.this, Homepage.class));
                        } else{ // Category node doesn't exist anymore, must have been deleted.
                            Toast.makeText(CreateProduct.this, "The category you selected no longer exits!", Toast.LENGTH_SHORT).show();
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



}