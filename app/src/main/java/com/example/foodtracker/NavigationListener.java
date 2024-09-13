package com.example.foodtracker;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationBarView;

public class NavigationListener implements NavigationBarView.OnItemSelectedListener {

    private final Context context;

    public NavigationListener(Context context) {
        this.context = context;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.home:
                context.startActivity(new Intent(context, Homepage.class));
                break;
            case R.id.diary:
                context.startActivity(new Intent(context, Diary.class));
                break;
            case R.id.shoppingList:
                context.startActivity(new Intent(context, ShoppingList.class));
                break;
            case R.id.settings:
                context.startActivity(new Intent(context, UserSettings.class));
                break;
        }
        return true;
    }
}