package com.example.foodtracker;

import java.util.ArrayList;

public interface UserCategoriesCallback {
    void onUserCategoriesReceived(ArrayList<String> userCategories);
}