package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {

    private Button registerUserButton;
    private ImageButton backButton;
    private TextInputLayout firstnameInput, emailAddressInput, passwordInput, surenameInput;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        firstnameInput = findViewById(R.id.firstNameInput);
        surenameInput = findViewById(R.id.surenameInput);
        emailAddressInput = findViewById(R.id.emailAddressInput);
        passwordInput = findViewById(R.id.passwordInput);
        backButton = findViewById(R.id.backButton);
        registerUserButton = findViewById(R.id.registerUserButton);

        registerUserButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.registerUserButton:
                registerUser();
                break;
            case R.id.backButton:
                startActivity(new Intent(this, GuestPage.class));
                break;
        }
    }

    private void registerUser(){
        String firstname = firstnameInput.getEditText().getEditableText().toString().trim();
        String surename = surenameInput.getEditText().getEditableText().toString().trim();
        String emailAddress = emailAddressInput.getEditText().getEditableText().toString().trim();
        String password = passwordInput.getEditText().getEditableText().toString().trim();

        firstnameInput.setError(null);
        surenameInput.setError(null);
        emailAddressInput.setError(null);
        passwordInput.setError(null);


        if(firstname.isEmpty()){
            firstnameInput.setError("First Name is required!");
            firstnameInput.requestFocus();
            return;
        }

        if(surename.isEmpty()){
            surenameInput.setError("Surename is required!");
            surenameInput.requestFocus();
            return;
        }

        if(emailAddress.isEmpty()){
            emailAddressInput.setError("Email Address is required!");
            emailAddressInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()){
            emailAddressInput.setError("Please provide a valid email!");
            emailAddressInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required!");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password length should be 6 characters or more!");
            passwordInput.requestFocus();
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            passwordInput.setError("Password must contain at least one capital letter!");
            passwordInput.requestFocus();
            return;
        }

        if (!password.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?].*")) {
            passwordInput.setError("Password must contain at least one special character!");
            passwordInput.requestFocus();
            return;
        }

        // Ensure first letters are capitalized in first name and surename
        firstname = firstname.substring(0, 1).toUpperCase() + firstname.substring(1);
        surename = surename.substring(0, 1).toUpperCase() + surename.substring(1);
        String fullName = firstname + " " + surename;
        mAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            User user = new User(fullName, emailAddress);

                            // create the user node
                            DatabaseReference userRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
                                    .child(currentUserUid);
                            userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Create a category node for the user
                                        DatabaseReference shoppingListRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Shopping List");
                                        String key = shoppingListRef.push().getKey();
                                        shoppingListRef.child(key).child("Items").setValue(true);
                                        shoppingListRef.child(key).child("Users").child(currentUserUid).child("Role").setValue("Owner");

                                        DatabaseReference categoriesRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserUid);

                                        // Create category nodes to store under "Categories
                                        categoriesRef.child("Snacks").setValue(true);
                                        categoriesRef.child("Vegetables").setValue(true);
                                        categoriesRef.child("Fruits").setValue(true);
                                        categoriesRef.child("Drinks").setValue(true);
                                        categoriesRef.child("Meats").setValue(true);

                                        Toast.makeText(RegisterUser.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(RegisterUser.this, "Failed to register, try again!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterUser.this, "An account with this email address already exists", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}