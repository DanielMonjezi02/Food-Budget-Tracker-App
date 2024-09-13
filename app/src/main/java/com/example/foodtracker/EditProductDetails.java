package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class EditProductDetails extends ProductDetailsBase  {

    private String productId, productName, productPrice, productStore, productDate, productCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        initViews();

        productId = getIntent().getStringExtra("productId");
        productName = getIntent().getStringExtra("productName");
        productPrice = getIntent().getStringExtra("productPrice");
        productStore = getIntent().getStringExtra("productStore");
        productDate = getIntent().getStringExtra("productDate");
        productCategory = getIntent().getStringExtra("productCategory");
        shoppingListRow.setVisibility(View.INVISIBLE);

        banner.setText("Edit Product");
        productNameTextView.setText(productName);
        priceEdit.setText(productPrice);

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProductDetails();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditProductDetails.this, Diary.class));
            }
        });

        getUserCategories(new UserCategoriesCallback() {
            @Override
            public void onUserCategoriesReceived(ArrayList<String> userCategories) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_layout, userCategories);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
                categoryDropdown.setAdapter(adapter);
                int position = adapter.getPosition(productCategory);
                categoryDropdown.setSelection(position);
                position = adapter.getPosition(productStore);
                storeNameDropdown.setSelection(position);
                dateEdit.setText(productDate);
                enableVisibility();
            }
        });

    }


    private void saveProductDetails(){
        if (dateEdit.getText().toString().isEmpty() || priceEdit.getText().toString().isEmpty() || productNameTextView.getText().toString().isEmpty()) {
            Log.i(TAG, "dateEdit: " + dateEdit.getText().toString() + "\n" + "priceEdit: " + priceEdit.getText().toString() + "\n" + "productNameView: " + productNameTextView.getText().toString()) ;

            Toast.makeText(EditProductDetails.this, "Please add all fields to update the product", Toast.LENGTH_SHORT).show();
        } else{
            DatabaseReference productRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID);
            String productKey = productRef.child(productCategory).push().getKey();
            productRef.child(categoryDropdown.getSelectedItem().toString()).child(productKey).child("Date").setValue(dateEdit.getText().toString());
            String priceString = priceEdit.getText().toString();
            double priceDouble = Double.parseDouble(priceString);
            String formattedPrice = String.format("%.2f", priceDouble);
            productRef.child(categoryDropdown.getSelectedItem().toString()).child(productKey).child("Price").setValue(formattedPrice);
            productRef.child(categoryDropdown.getSelectedItem().toString()).child(productKey).child("Product Name").setValue(productNameTextView.getText().toString());
            productRef.child(categoryDropdown.getSelectedItem().toString()).child(productKey).child("Store Name").setValue(storeNameDropdown.getSelectedItem().toString());

            // Remove the old product
            productRef.child(productCategory).child(productId).removeValue();

            Intent intent = new Intent(EditProductDetails.this, Diary.class);
            startActivity(intent);
            Toast.makeText(EditProductDetails.this, "Product details have been updated", Toast.LENGTH_SHORT).show();
        }

    }
}