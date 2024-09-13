package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserSettings extends AppCompatActivity implements View.OnClickListener  {

    private TextView fullName;
    private String currentUserId;
    private RelativeLayout logoutRow, spendingsRow, messageRow, faqRow, securityRow;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        fullName = findViewById(R.id.fullName);
        logoutRow = findViewById(R.id.logoutRow);
        spendingsRow = findViewById(R.id.spendingsRow);
        backButton = findViewById(R.id.backButton);
        messageRow = findViewById(R.id.messageRow);
        securityRow = findViewById(R.id.securityRow);
        faqRow = findViewById(R.id.faqRow);

        logoutRow.setOnClickListener(this);
        spendingsRow.setOnClickListener(this);
        backButton.setOnClickListener(this);
        messageRow.setOnClickListener(this);
        securityRow.setOnClickListener(this);
        faqRow.setOnClickListener(this);


        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getUserFullName();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.logoutRow:
                FirebaseAuth.getInstance().signOut();
                SharedPreferences sharedPreferences = getSharedPreferences("my_app", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("is_logged_in");
                editor.apply();
                startActivity(new Intent(this, GuestPage.class));
                break;
            case R.id.spendingsRow:
                startActivity(new Intent(this, Spendings.class));
                break;
            case R.id.backButton:
                startActivity(new Intent(this, Homepage.class));
                break;
            case R.id.messageRow:
                startActivity(new Intent(this, MessageUs.class));
                break;
            case R.id.faqRow:
                startActivity(new Intent(this, Faq.class));
                break;
            case R.id.securityRow:
                startActivity(new Intent(this, SecurityAndPrivacy.class));
                break;
        }
    }

    private void getUserFullName(){
        DatabaseReference userFullNameRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users").child(currentUserId).child("fullName");

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
                fullName.setText(userFullName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }


}