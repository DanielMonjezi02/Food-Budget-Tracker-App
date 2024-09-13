package com.example.foodtracker;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.ViewHolder> {

    private ArrayList<HashMap<String, String>> faqItems;

    public FaqAdapter(ArrayList<HashMap<String, String>> faqItems) {
        this.faqItems = faqItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faq_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> faqItem = faqItems.get(position);
        holder.questionTextView.setText(faqItem.get("question"));
        holder.answerTextView.setText(faqItem.get("answer"));
    }

    @Override
    public int getItemCount() {
        return faqItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView questionTextView;
        TextView answerTextView;

        ViewHolder(View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.question_textview);
            answerTextView = itemView.findViewById(R.id.answer_textview);
            questionTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (answerTextView.getVisibility() == View.GONE) {
                answerTextView.setVisibility(View.VISIBLE);
            } else {
                answerTextView.setVisibility(View.GONE);
            }
        }
    }
}