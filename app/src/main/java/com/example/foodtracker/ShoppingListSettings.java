package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ShoppingListSettings extends AppCompatActivity implements View.OnClickListener {

    private EditText inputEmail;
    private Button addUser, leaveShoppingList;
    private ImageView backButton;
    private static String shoppingListId;
    private Spinner permissionLevelSpinner;
    private ListView listView;
    private UserListAdapter adapter;
    private ArrayList<HashMap<String, String>> userList = new ArrayList<>();
    private String currentUserID;
    private String userPermission;
    private RelativeLayout addUserView;
    private TextView addUserHeading;
    private View addUserBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_settings);

        inputEmail = findViewById(R.id.inputEmail);
        addUser = findViewById(R.id.addUser);
        listView = findViewById(R.id.listview);
        addUserView = findViewById(R.id.addUserView);
        addUserHeading = findViewById(R.id.addUserHeading);
        addUserBar = findViewById(R.id.addUserBar);
        leaveShoppingList = findViewById(R.id.leaveShoppingList);
        backButton = findViewById(R.id.backButton);

        leaveShoppingList.setOnClickListener(this);
        backButton.setOnClickListener(this);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        permissionLevelSpinner = findViewById(R.id.permissionLevelSpinner);
        String[] permissionLevels = {"Read", "Write"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_layout, permissionLevels);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        permissionLevelSpinner.setAdapter(adapter);

        inputEmail.setOnClickListener(this);
        addUser.setOnClickListener(this);

        shoppingListId = getIntent().getStringExtra("shoppingListId");

        getCurrentUserPermission();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.addUser:
                String user = inputEmail.getText().toString();
                if(user == null || user.length() == 0){
                    Toast.makeText(this, "Please enter an email in the text box", Toast.LENGTH_SHORT).show();
                } else {
                    shareShoppingList(user);
                    inputEmail.setText("");
                }
                break;
            case R.id.leaveShoppingList:
                leaveShoppingList();
                break;
            case R.id.backButton:
                startActivity(new Intent(this, ShoppingList.class));
                break;
        }
    }

    public static void removeUser(String userId){
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List").child(shoppingListId);
        userRef.child("Accepted Permissions").child(userId).removeValue();
        userRef.child("Users").child(userId).removeValue();

    }

    public void getCurrentUserPermission()
    {

        Log.i(TAG, "Shopping List ID " + shoppingListId);
        DatabaseReference userPermissionRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List").child(shoppingListId).child("Users").child(currentUserID).child("Role");
        userPermissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userPermission = dataSnapshot.getValue(String.class);
                Log.i(TAG, "User permission " + userPermission);
                getShoppingListUsers();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
            }
        });

    }
    public void getShoppingListUsers(){
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List").child(shoppingListId).child("Users");
        Log.i(TAG, "usersRef " + usersRef);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userKey = userSnapshot.getKey();
                    String role = userSnapshot.getValue().toString();
                    String permission = Arrays.stream(role.split("=")).skip(1).findFirst().orElse("").replace("}", "");
                    if(userPermission.equals("Owner")){

                        lookupUserEmailAndAddToList(userKey, permission);

                    } else {
                        addUserView.setVisibility(View.INVISIBLE);
                        addUserHeading.setVisibility(View.INVISIBLE);
                        addUserBar.setVisibility(View.INVISIBLE);
                        leaveShoppingList.setVisibility(View.VISIBLE);
                        Log.i(TAG, "IT HAPPENED");
                        if(userSnapshot.getKey().contains(currentUserID) || "Owner".equals(permission)){


                            lookupUserEmailAndAddToList(userKey, permission);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    private void lookupUserEmailAndAddToList(String userKey, String permission) {
        DatabaseReference emailRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Users")
                .child(userKey)
                .child("email");

        emailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userEmail = snapshot.getValue(String.class);
                userList.add(new HashMap<String, String>() {{
                    put("email", userEmail);
                    put("permission", permission);
                    put("id", userKey);
                }});

                // Sort the arrayList so that owner is always at the top
                Collections.sort(userList, new Comparator<Map<String, String>>() {
                    @Override
                    public int compare(Map<String, String> o1, Map<String, String> o2) {
                        String p1 = o1.get("permission");
                        String p2 = o2.get("permission");
                        if (p1.equals("Owner") && !p2.equals("Owner")) {
                            return -1; // o1 is owner, so it should come first
                        } else if (!p1.equals("Owner") && p2.equals("Owner")) {
                            return 1; // o2 is owner, so it should come first
                        } else {
                            return 0;
                        }
                    }
                });
                adapter = new UserListAdapter(getApplicationContext(), userList, userPermission);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    public void leaveShoppingList(){
        if(!userPermission.equals("Owner")){
            DatabaseReference usersRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List").child(shoppingListId);
            usersRef.child("Accepted Permissions").child(currentUserID).removeValue();
            usersRef.child("Users").child(currentUserID).removeValue();

            // Build an alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You have left the shopping list. Please refresh this page to display your shopping list settings.")
                    .setPositiveButton("OK", null)
                    .show();

        }
    }


    public void shareShoppingList(String user){
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users");

        Query query = usersRef.orderByChild("email").equalTo(user);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { // email exists in the database
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String selectedPermission = (String) permissionLevelSpinner.getSelectedItem();
                        String addingUserId = userSnapshot.getKey();
                        Log.i(TAG, "addingUserId" + userSnapshot.getKey());
                        DatabaseReference shoppingListRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List").child(shoppingListId);
                        Log.i(TAG, "Current User " + addingUserId);
                        Log.i(TAG, "Current User  " + shoppingListRef.child("Users").child(addingUserId).getKey());
                        shoppingListRef.child("Users").child(addingUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if((snapshot.exists() && snapshot.getValue() != null)){
                                    // User is already in the shopping list
                                    Toast.makeText(ShoppingListSettings.this, user + " is already in the shopping list", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    // Add user to shopping list
                                    shoppingListRef.child("Users").child(addingUserId).child("Role").setValue(selectedPermission);
                                    shoppingListRef.child("Accepted Permissions").child(addingUserId).setValue(false);
                                    Toast.makeText(ShoppingListSettings.this, "Added: " + user, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    Toast.makeText(ShoppingListSettings.this, "User does not exist", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}