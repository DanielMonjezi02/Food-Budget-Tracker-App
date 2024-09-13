package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<String> {

    private List<String> categoriesList;
    private List<String> amountList;

    public CategoryAdapter(Context context, int resource, List<String> categoriesList, List<String> amountList) {
        super(context, resource, categoriesList);
        this.categoriesList = categoriesList;
        this.amountList = amountList;
    }

    @Override
    public int getCount() {
        return categoriesList.size() + 1; // Add 1 for the "Add Category" row
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.list_category_layout, parent, false);
            holder = new ViewHolder();
            holder.categoryTextView = view.findViewById(R.id.categoryTextView);
            holder.amountTextView = view.findViewById(R.id.amountTextView);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }


        if (position == categoriesList.size()) {
            holder.categoryTextView.setText("Add Category");
            holder.amountTextView.setText("");
            holder.categoryTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.semi_transparent_grey));
            holder.amountTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.semi_transparent_grey));
            holder.categoryTextView.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.categoryTextView.setTypeface(null, Typeface.NORMAL);
            holder.amountTextView.setTypeface(null, Typeface.NORMAL);
            holder.categoryTextView.setText(categoriesList.get(position));
            holder.amountTextView.setText(amountList.get(position));
            holder.categoryTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            holder.amountTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }

        return view;
    }

    private static class ViewHolder {
        TextView categoryTextView;
        TextView amountTextView;
    }
}