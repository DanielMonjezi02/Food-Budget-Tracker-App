package com.example.foodtracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

class UserListAdapter extends SimpleAdapter {
    private Context context;
    private String userPermission;
    private ArrayList<HashMap<String, String>> userList;

    public UserListAdapter(Context context, ArrayList<HashMap<String, String>> userList, String userPermission) {
        super(context, userList, R.layout.user_list_row, new String[]{"email", "permission"}, new int[]{R.id.userEmail, R.id.userPermission});
        this.context = context;
        this.userList = userList;
        this.userPermission = userPermission;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.user_list_row, parent, false);

        TextView emailTextView = rowView.findViewById(R.id.userEmail);
        TextView permissionTextView = rowView.findViewById(R.id.userPermission);
        ImageView remove = rowView.findViewById(R.id.remove);

        HashMap<String, String> user = userList.get(position);
        String email = user.get("email");
        String permission = user.get("permission");

        emailTextView.setText(email);
        emailTextView.setTextColor(Color.WHITE);
        permissionTextView.setText(permission);
        permissionTextView.setTextColor(Color.WHITE);

        if (userPermission.equals("Owner")) {
            // set visibility to visible if the user has owner permission
            if (permission != null && permission.equals("Owner")) {
                // If the user is an owner, hide the remove button
                remove.setVisibility(View.GONE);
            } else {
                // Otherwise, show the remove button
                remove.setVisibility(View.VISIBLE);
            }
        } else {
            // set visibility to gone if the user does not have owner permission
            remove.setVisibility(View.GONE);
        }


        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = userList.get(position).get("id");
                ShoppingListSettings.removeUser(userId);
            }
        });

        return rowView;
    }
}