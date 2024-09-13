package com.example.foodtracker;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DateValueFormatter extends ValueFormatter {
    private final ArrayList<Long> mTimestamps;

    public DateValueFormatter(ArrayList<Long> timestamps) {
        mTimestamps = timestamps;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        int index = (int) value;
        if (index >= 0 && index < mTimestamps.size()) {
            Date date = new Date(mTimestamps.get(index));
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return sdf.format(date);
        } else {
            return "";
        }
    }
}





