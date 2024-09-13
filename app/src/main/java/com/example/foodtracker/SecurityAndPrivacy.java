package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SecurityAndPrivacy extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout passwordInput;
    private Button submitButton;
    private ImageButton backButton;
    private FirebaseAuth mAuth;
    private TableLayout userDetails;
    private TextView banner, firstNameText, surenameText, emailAddressText, passwordText;
    private TableRow firstNameRow, surenameRow, emailAddressRow, passwordRow;
    private String currentUserID, fullName, emailAddress, userPassword;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_and_privacy);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users").child(currentUserID);

        passwordInput = findViewById(R.id.passwordInput);
        submitButton = findViewById(R.id.submitButton);
        userDetails = findViewById(R.id.userDetails);
        backButton = findViewById(R.id.backButton);
        firstNameText = findViewById(R.id.firstNameText);
        surenameText = findViewById(R.id.surenameText);
        emailAddressText = findViewById(R.id.emailAddressText);
        passwordText = findViewById(R.id.passwordText);
        firstNameRow = findViewById(R.id.firstNameRow);
        surenameRow = findViewById(R.id.surenameRow);
        emailAddressRow = findViewById(R.id.emailAddressRow);
        passwordRow = findViewById(R.id.passwordRow);
        banner = findViewById(R.id.banner);


        submitButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        firstNameRow.setOnClickListener(this);
        surenameRow.setOnClickListener(this);
        emailAddressRow.setOnClickListener(this);
        passwordRow.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submitButton:
                displayRenterPassword();
                break;
            case R.id.backButton:
                startActivity(new Intent(SecurityAndPrivacy.this, UserSettings.class));
                break;
            case R.id.firstNameRow:
                editFirstName();
                break;
            case R.id.surenameRow:
                editSurename();
                break;
            case R.id.emailAddressRow:
                editEmailAddress();
                break;
            case R.id.passwordRow:
                editPassword();
                break;
        }
    }

    private void displayRenterPassword() {
        String password = passwordInput.getEditText().getEditableText().toString().trim();

        passwordInput.setError(null);

        if (password.isEmpty()) {
            passwordInput.setError("Please enter your password!");
            passwordInput.requestFocus();
            return;
        } else {
            // Get the currently signed-in user
            String emailAddress = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            mAuth.signInWithEmailAndPassword(emailAddress,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        userPassword = password;
                        passwordInput.setVisibility(View.GONE);
                        displayDetails();
                    } else{
                        Toast.makeText(SecurityAndPrivacy.this, "Password is incorrect!", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }

    private void displayDetails(){
        banner.setText("Edit Details");
        userDetails.setVisibility(View.VISIBLE);
        passwordText.setText("**********");

        // Get user email
        userRef.child("email").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emailAddress = snapshot.getValue().toString();
                emailAddressText.setText(emailAddress);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userRef.child("fullName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullName = snapshot.getValue().toString();
                String[] nameParts = fullName.split(" ");
                String firstName = nameParts[0];
                String lastName = nameParts[nameParts.length - 1];
                firstNameText.setText(firstName);
                surenameText.setText(lastName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void editFirstName(){
        View dialogView = LayoutInflater.from(SecurityAndPrivacy.this).inflate(R.layout.input_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(SecurityAndPrivacy.this, R.style.AlertDialog);
        builder.setTitle("Change first name");
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.input);
        input.setHint("First Name");
        final TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Enter first name:");


        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the input text and add it to the categories list
                String firstName;
                String inputString = input.getText().toString();
                if(inputString.length() < 3 || inputString.isEmpty()){
                    Toast.makeText(SecurityAndPrivacy.this, "First name must be at least 3 characters long", Toast.LENGTH_SHORT).show();
                } else{
                    firstName = inputString.substring(0, 1).toUpperCase() + inputString.substring(1);
                    String[] nameParts = fullName.split(" ");
                    String lastName = nameParts[nameParts.length - 1];
                    userRef.child("fullName").setValue(firstName + " " + lastName);
                    Toast.makeText(SecurityAndPrivacy.this, "First name has been changed", Toast.LENGTH_SHORT).show();
                }
                return;

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

    private void editSurename(){
        View dialogView = LayoutInflater.from(SecurityAndPrivacy.this).inflate(R.layout.input_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(SecurityAndPrivacy.this, R.style.AlertDialog);
        builder.setTitle("Change surename");
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.input);
        input.setHint("Surename");
        final TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Enter surename:");


        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the input text and add it to the categories list
                String surename;
                String inputString = input.getText().toString();
                if(inputString.length() < 3 || inputString.isEmpty()){
                    Toast.makeText(SecurityAndPrivacy.this, "First name must be at least 3 characters long", Toast.LENGTH_SHORT).show();
                } else{
                    surename = inputString.substring(0, 1).toUpperCase() + inputString.substring(1);
                    String[] nameParts = fullName.split(" ");
                    String firstName = nameParts[0];
                    userRef.child("fullName").setValue(firstName + " " + surename);
                    Toast.makeText(SecurityAndPrivacy.this, "Surename has been changed", Toast.LENGTH_SHORT).show();
                }
                return;

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

    private void editEmailAddress(){
        View dialogView = LayoutInflater.from(SecurityAndPrivacy.this).inflate(R.layout.input_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(SecurityAndPrivacy.this, R.style.AlertDialog);
        builder.setTitle("Change email address");
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.input);
        input.setHint("Email Address");
        final TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Enter email address:");


        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the input text and add it to the categories list
                String inputString = input.getText().toString();
                if(!Patterns.EMAIL_ADDRESS.matcher(inputString).matches() || inputString.isEmpty()){
                    Toast.makeText(SecurityAndPrivacy.this, "Please enter a valid email address!", Toast.LENGTH_SHORT).show();
                } else{
                    DatabaseReference usersRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users");
                    Query query = usersRef.orderByChild("email").equalTo(inputString);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(SecurityAndPrivacy.this, "An account with this email address already exists", Toast.LENGTH_SHORT).show();
                            } else {
                                userRef.child("email").setValue(inputString).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SecurityAndPrivacy.this, "Email Address has been changed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(SecurityAndPrivacy.this, "Failed to change email address", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SecurityAndPrivacy.this, "Failed to check if email address already exists", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return;

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

    private void editPassword(){
        View dialogView = LayoutInflater.from(SecurityAndPrivacy.this).inflate(R.layout.input_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(SecurityAndPrivacy.this, R.style.AlertDialog);
        builder.setTitle("Change password");
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.input);
        input.setHint("Password");
        final TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Enter password:");


        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the input text and add it to the categories list
                String inputString = input.getText().toString();

                if (inputString.isEmpty()) {
                    Toast.makeText(SecurityAndPrivacy.this, "Please input a password!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (inputString.length() < 6) {
                    Toast.makeText(SecurityAndPrivacy.this, "Password should be at least 6 characters long!", Toast.LENGTH_SHORT).show();
                    return;
                }else if (!inputString.matches(".*[A-Z].*")) {
                    Toast.makeText(SecurityAndPrivacy.this, "Password must contain at least one capital leter!", Toast.LENGTH_SHORT).show();
                    return;
                }else if (!inputString.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?].*")) {
                    Toast.makeText(SecurityAndPrivacy.this, "Password must contain at least one special character!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    Log.i(TAG, "Email Address " + emailAddressText.getText().toString());
                    Log.i(TAG, "User Password " + userPassword);
                    // Authenticate the user with their current email and password
                    AuthCredential credential = EmailAuthProvider.getCredential(emailAddressText.getText().toString(), userPassword);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Update the user's password// replace with the user's new password
                                        user.updatePassword(inputString)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "Password updated successfully");
                                                            // Log user out after changing password
                                                            FirebaseAuth.getInstance().signOut();
                                                            SharedPreferences sharedPreferences = getSharedPreferences("my_app", Context.MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.remove("is_logged_in");
                                                            editor.apply();
                                                            startActivity(new Intent(SecurityAndPrivacy.this, GuestPage.class));
                                                        } else {
                                                            Log.d(TAG, "Error updating password", task.getException());
                                                        }
                                                    }
                                                });
                                    } else {
                                        Log.d(TAG, "Authentication failed", task.getException());
                                    }
                                }
                            });
                }
                return;

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

}