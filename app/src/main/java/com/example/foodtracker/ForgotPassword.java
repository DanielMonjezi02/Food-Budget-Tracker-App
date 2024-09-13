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
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout emailAddressInput;
    private Button resetPasswordButton;
    private ImageButton backButton;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailAddressInput = findViewById(R.id.emailAddressInput);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        backButton = findViewById(R.id.backButton);

        auth = FirebaseAuth.getInstance();

        resetPasswordButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.resetPasswordButton:
                resetPassword();
                break;
            case R.id.backButton:
                startActivity(new Intent(this, LoginUser.class));
                break;
        }
    }

    private void resetPassword(){
        String emailAddress = emailAddressInput.getEditText().getEditableText().toString().trim();

        emailAddressInput.setError(null);

        if(emailAddress.isEmpty()){
            emailAddressInput.setError("Email is required!");
            emailAddressInput.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()){
            emailAddressInput.setError("Please provide a valid email!");
            emailAddressInput.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this, "Check your email to reset your password!", Toast.LENGTH_LONG).show();
                } else{
                    Toast.makeText(ForgotPassword.this, "Please provide an email that is registered!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}