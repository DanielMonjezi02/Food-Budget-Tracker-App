package com.example.foodtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashMap;

public class Faq extends AppCompatActivity {

    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        backButton = findViewById(R.id.backButton);

        ArrayList<HashMap<String, String>> faqList = new ArrayList<>();

        String[] questions = {"How do I delete a category?", "How can I delete a product that I've added to my diary?", "How can I change product details that I added?", "I have experienced a bug, what can I do?"};
        String[] answers = {"Go to the diary page, press and hold on a category which will delete it.", "Go to the diary page, click on the category that the product is in, press and hold on the product which will delete it.", "Go to the diary page, click on a category that the product is in, click on the product you would like to edit and edit the details.", "If you have experienced a bug, please contact us and we will do our best to assist you and resolve the issue. " + "\n" + "Email: FoodTracker@suppoer.com"};
        for (int i = 0; i < questions.length; i++) {
            HashMap<String, String> faqItem = new HashMap<>();
            faqItem.put("question", questions[i]);
            faqItem.put("answer", answers[i]);
            faqList.add(faqItem);
        }

        RecyclerView faqRecyclerView = findViewById(R.id.recyclerView);
        faqRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        faqRecyclerView.setAdapter(new FaqAdapter(faqList));


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Faq.this, UserSettings.class));
            }
        });

    }


}