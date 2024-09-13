package com.example.foodtracker;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ItemsListAdapter extends ArrayAdapter<String> {

    ArrayList<String> itemsList;
    Context context;

    private com.example.foodtracker.ItemsListAdapter userListAdapter;

    public ItemsListAdapter(Context context, ArrayList<String> itemsList){
        super(context, R.layout.shopping_list_items_design, itemsList);
        this.context = context;
        this.itemsList = itemsList;


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.shopping_list_items_design, null);

            TextView number = convertView.findViewById(R.id.number);
            number.setText(position + 1 + ".");

            TextView name = convertView.findViewById(R.id.name);
            name.setText(itemsList.get(position));

            ImageView remove = convertView.findViewById(R.id.remove);


            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShoppingList.removeItem(itemsList.get(position));
                }
            });
        }
        return convertView;
    }

}
