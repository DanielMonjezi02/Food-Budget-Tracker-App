package com.example.foodtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginUser extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout emailAddressInput, passwordInput;
    private Button loginButton;
    private ImageButton backButton;
    private TextView forgotPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        mAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        backButton = findViewById(R.id.backButton);

        loginButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        backButton.setOnClickListener(this);

        emailAddressInput = findViewById(R.id.emailAddressInput);
        passwordInput = findViewById(R.id.passwordInput);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.loginButton:
                userLogin();
                break;
            case R.id.forgotPassword:
                startActivity(new Intent(this, ForgotPassword.class));
                break;
            case R.id.backButton:
                startActivity(new Intent(this, GuestPage.class));
                break;
        }
    }

    private void userLogin(){
        String emailAddress = emailAddressInput.getEditText().getEditableText().toString().trim();
        String password = passwordInput.getEditText().getEditableText().toString().trim();

        emailAddressInput.setError(null);
        passwordInput.setError(null);

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

        if(password.isEmpty()) {
            passwordInput.setError("Password is required!");
            passwordInput.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(emailAddress,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    SharedPreferences preferences = getSharedPreferences("my_app", MODE_PRIVATE);

                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putBoolean("is_logged_in", true);
                    editor.apply();
                    startActivity(new Intent(LoginUser.this, Homepage.class));
                } else{
                    Toast.makeText(LoginUser.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}