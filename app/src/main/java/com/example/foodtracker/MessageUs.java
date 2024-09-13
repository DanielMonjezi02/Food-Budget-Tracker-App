package com.example.foodtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputLayout;

public class MessageUs extends AppCompatActivity {

    private Button messageButton;
    private ImageButton backButton;
    private TextInputLayout messageInput, subjectInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_us);

        messageButton = findViewById(R.id.messageButton);
        backButton = findViewById(R.id.backButton);
        messageInput = findViewById(R.id.messageInput);
        subjectInput = findViewById(R.id.subjectInput);

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMail();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageUs.this, UserSettings.class));
            }
        });
    }

    private void sendMail(){
        String subject = subjectInput.getEditText().getEditableText().toString().trim();
        String message = messageInput.getEditText().getEditableText().toString().trim();

        subjectInput.setError(null);
        messageInput.setError(null);

        if(subject.isEmpty()){
            subjectInput.setError("Subject is required!");
            subjectInput.requestFocus();
            return;
        }


        if(message.isEmpty()) {
            messageInput.setError("Password is required!");
            messageInput.requestFocus();
            return;
        }

        if(message.length() < 6) {
            messageInput.setError("Message must be at least 10 words long");
            messageInput.requestFocus();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "FoodTracker@support.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);


        startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
    }

}