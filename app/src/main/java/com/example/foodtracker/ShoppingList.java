package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

public class ShoppingList extends AppCompatActivity implements View.OnClickListener {

    static ListView listView;
    static ArrayList<String> items;
    static ItemsListAdapter adapter;

    private BottomNavigationView bottomNavigationView;
    private EditText inputItem;
    private ImageView addItem;
    private ImageButton settings;
    public static String shoppingListId;
    private boolean sharedShoppingList;
    private static String currentUserID;
    private static Context applicationContext;
    private String ownerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        listView = findViewById(R.id.listview);
        inputItem = findViewById(R.id.inputItem);
        addItem = findViewById(R.id.addItem);
        settings = findViewById(R.id.settings);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(new NavigationListener(this));
        inputItem.setOnClickListener(this);
        addItem.setOnClickListener(this);
        settings.setOnClickListener(this);

        items = new ArrayList<>();
        items.clear();

        applicationContext = getApplicationContext();

        adapter = new ItemsListAdapter(getApplicationContext(), items);
        listView.setAdapter(adapter);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference shoppingListRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List");
        shoppingListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot shoppingListSnapshot : snapshot.getChildren()) {
                    if (!sharedShoppingList == true) {

                        // Check if the current user logged in is within any shared shopping lists
                        Log.i(TAG, "Shopping List Key: " + shoppingListSnapshot.getKey());
                        Log.i(TAG, "Value: " + shoppingListSnapshot.child("Users").child(currentUserID).child("Role").getValue());
                        // Check if any of the shopping lists have the current user and the user role is read/write
                        if (shoppingListSnapshot.child("Users").hasChild(currentUserID) && ("Read".equals(shoppingListSnapshot.child("Users").child(currentUserID).child("Role").getValue()) || "Write".equals(shoppingListSnapshot.child("Users").child(currentUserID).child("Role").getValue()))) {
                            if (shoppingListSnapshot.child("Accepted Permissions").child(currentUserID).getValue() != null && shoppingListSnapshot.child("Accepted Permissions").child(currentUserID).getValue().equals(false)) {
                                // User is in a shopping list but hasn't accepted the permission
                                AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingList.this);
                                shoppingListId = shoppingListSnapshot.getKey();
                                getOwnerName(ownerName -> {
                                    Log.i(TAG, "Owner name: " + ownerName);
                                    builder.setMessage(ownerName + " has shared their shopping list with you. Would you like to join it?" + "\n" + "\n" + "Pressing 'Yes' will result in you not being able to use your own shopping list. " + "\n" + "You can leave their shopping list at any point in time and continue to use yours by heading over to the settings tab located in the top right.");
                                    builder.setCancelable(true);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    sharedShoppingList = true;
                                                    shoppingListId = shoppingListSnapshot.getKey();
                                                    Log.i(TAG, "true" + shoppingListId);
                                                    shoppingListRef.child(shoppingListId).child("Accepted Permissions").child(currentUserID).setValue(true);
                                                    displaySharedShoppingList();
                                                }
                                            });

                                    // User denies joining shared shopping list
                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    sharedShoppingList = false;
                                                    shoppingListRef.child(shoppingListId).child("Accepted Permissions").child(currentUserID).removeValue();
                                                    shoppingListRef.child(shoppingListId).child("Users").child(currentUserID).removeValue();
                                                    shoppingListId = null;
                                                    getShoppingListId();

                                                }
                                            });

                                    AlertDialog acceptPermissionAlert = builder.create();
                                    acceptPermissionAlert.show();
                                });
                            } else {
                                // User is in a shared shopping list but has already accepted the permission
                                sharedShoppingList = true;
                                Log.i(TAG, "User is within shared shopping list but has already accepted permission");
                                Log.i(TAG, "Accepted Permission true: " + shoppingListSnapshot.getKey());
                                shoppingListId = shoppingListSnapshot.getKey();
                                displaySharedShoppingList();
                            }
                            break;

                        }
                    }
                }
                // User is not in a shared shopping list at all
                if(sharedShoppingList == false){
                    getShoppingListId();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addItem:
                String text = inputItem.getText().toString();
                if (text == null || text.length() == 0) {
                    Toast.makeText(this, "Enter an item in the text box.", Toast.LENGTH_SHORT);
                } else {
                    addItem(text);
                    inputItem.setText("");
                    Toast.makeText(this, "Added: " + text, Toast.LENGTH_SHORT);
                }
                break;
            case R.id.settings:
                Intent intent = new Intent(this, ShoppingListSettings.class);
                intent.putExtra("shoppingListId", shoppingListId);
                startActivity(intent);
                break;

        }
    }

    private void getOwnerName(Consumer<String> callback){
        DatabaseReference shoppingListRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List").child(shoppingListId).child("Users");
        shoppingListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userKey = userSnapshot.getKey();
                    String permission = userSnapshot.child("Role").getValue().toString();
                    Log.i(TAG, "userKey " + userKey);
                    Log.i(TAG, "permission " + userKey);
                    if(permission.equals("Owner")){
                        DatabaseReference fullNameRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("Users")
                                .child(userKey)
                                .child("fullName");

                        fullNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ownerName = snapshot.getValue(String.class);
                                callback.accept(ownerName);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    // Adds item to shopping list
    public static void addItem(String item) {

        DatabaseReference itemsRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List").child(shoppingListId);
        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.child("Users").child(currentUserID).getValue().toString();
                String permission = Arrays.stream(role.split("=")).skip(1).findFirst().orElse("").replace("}", "");
                if("Write".equals(permission) || "Owner".equals(permission))
                {
                    itemsRef.child("Items").child(item).setValue(currentUserID);
                }else{
                    Toast.makeText(applicationContext, "Error: You don't have permission to add an item to the shopping list", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }

    // Removes item from shopping list
    public static void removeItem(String item) {

        DatabaseReference itemsRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List").child(shoppingListId);
        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.child("Users").child(currentUserID).getValue().toString();
                String permission = Arrays.stream(role.split("=")).skip(1).findFirst().orElse("").replace("}", "");
                Log.i(TAG, "Permission: " + permission);
                if("Write".equals(permission) || "Owner".equals(permission))
                {
                    Log.i(TAG, "Removed item allowed: ");
                    itemsRef.child("Items").child(item).removeValue();
                }else{
                    Toast.makeText(applicationContext, "Error: You don't have permission to remove an item from the shopping list", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }

    public void getShoppingListId() {
        DatabaseReference shoppingListRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List");

        Log.i(TAG, "ITS FALSE " + shoppingListRef);
        shoppingListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot shoppingListSnapshot : snapshot.getChildren()) {
                    Log.i(TAG, "FOUND " + shoppingListSnapshot.child(currentUserID));
                    if(shoppingListSnapshot.child("Users").hasChild(currentUserID)){
                        shoppingListId = shoppingListSnapshot.getKey();
                        displaySharedShoppingList();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    public void displaySharedShoppingList() {

        DatabaseReference shoppingListRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List").child(shoppingListId).child("Items");
        shoppingListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Log.d("TAG", "Item name: " + itemSnapshot.getKey());
                    items.add(itemSnapshot.getKey());
                }
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}


