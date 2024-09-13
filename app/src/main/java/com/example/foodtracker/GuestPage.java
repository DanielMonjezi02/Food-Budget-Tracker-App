package com.example.foodtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class GuestPage extends AppCompatActivity implements View.OnClickListener{

    private TextView registerButton;
    private TextView loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guestpage);

        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);

        registerButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.registerButton:
                startActivity(new Intent(this, RegisterUser.class));
                break;
            case R.id.loginButton:
                startActivity(new Intent(this, LoginUser.class));
                break;
        }
    }
}