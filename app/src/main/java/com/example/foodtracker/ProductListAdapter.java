package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.util.List;

public class ProductListAdapter extends ArrayAdapter<String> {

    private List<String> productIdList;
    private List<String> productNameList;
    private List<String> productDateList;
    private List<String> productPriceList;
    private List<String> productStoreName;
    private List<String> productCategory;
    private CategoryProductList categoryProductListClass;
    Context context;

    public ProductListAdapter(Context context, int resource, CategoryProductList categoryProductListClass, List<String> productIdList, List<String> productNameList, List<String> productDateList, List<String> productPriceList, List<String> productStoreName, List<String> productCategory) {
        super(context, resource, productIdList);
        this.context = context;
        this.categoryProductListClass = categoryProductListClass;
        this.productIdList = productIdList;
        this.productNameList = productNameList;
        this.productDateList = productDateList;
        this.productPriceList = productPriceList;
        this.productStoreName = productStoreName;
        this.productCategory = productCategory;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.product_list_design, parent, false);
            holder = new ViewHolder();
            holder.productName = view.findViewById(R.id.productName);
            holder.productDate = view.findViewById(R.id.productDate);
            holder.productPrice = view.findViewById(R.id.productPrice);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.productName.setText(productNameList.get(position));
        holder.productDate.setText(productDateList.get(position));
        holder.productPrice.setText("£" + productPriceList.get(position));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditProductDetails.class);
                intent.putExtra("productId", productIdList.get(position))
                        .putExtra("productName", productNameList.get(position))
                        .putExtra("productDate", productDateList.get(position))
                        .putExtra("productPrice", productPriceList.get(position))
                        .putExtra("productStore", productStoreName.get(position))
                        .putExtra("productCategory", productCategory.get(position));
                getContext().startActivity(intent);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                categoryProductListClass.removeProduct(productIdList.get(position));
                return true;
            }
        });



        return view;
    }

    private static class ViewHolder {
        TextView productName;
        TextView productDate;
        TextView productPrice;
    }
}
