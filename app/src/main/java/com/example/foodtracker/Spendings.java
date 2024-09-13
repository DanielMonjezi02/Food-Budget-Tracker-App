package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class Spendings extends AppCompatActivity implements View.OnClickListener {

    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private Button pickDate;
    private ImageButton backButton;
    private TextView mShowSelectedDateText;
    private String currentUserID;
    private LocalDate selectedStartDate, selectedEndDate;
    private MaterialDatePicker materialDatePicker;
    private LineData lineData = new LineData();
    private TextView barChartText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spendings);

        pickDate = findViewById(R.id.pickDate);
        mShowSelectedDateText = findViewById(R.id.selectedDate);
        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);
        backButton = findViewById(R.id.backButton);
        barChart = findViewById(R.id.barChart);
        barChartText = findViewById(R.id.barChartText);

        pieChart.setNoDataText("Please select a date to display a pie chart");
        barChart.setNoDataText("Please select a category from the pie chart to display a bar chart");

        backButton.setOnClickListener(this);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();



        displayDatePicker();
        displayPieChart();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backButton:
                startActivity(new Intent(this, UserSettings.class));
                break;
        }
    }

    private void displayDatePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker();

        // now define the properties of the
        // materialDateBuilder
        materialDateBuilder.setTitleText("SELECT A DATE");

        // now create the instance of the material date
        // picker
        materialDatePicker = materialDateBuilder.build();

        // handle select date button which opens the
        // material design date picker
        pickDate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // getSupportFragmentManager() to
                        // interact with the fragments
                        // associated with the material design
                        // date picker tag is to get any error
                        // in logcat
                        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");

                    }
                });

        // now handle the positive button click from the
        // material design date picker
        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                    @Override
                    public void onPositiveButtonClick(Pair<Long, Long> selection) {
                        String startDate = DateFormat.getDateInstance().format(selection.first);
                        String endDate = DateFormat.getDateInstance().format(selection.second);

                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
                        selectedStartDate = LocalDate.parse(startDate, inputFormatter);
                        selectedEndDate = LocalDate.parse(endDate, inputFormatter);

                        mShowSelectedDateText.setText(materialDatePicker.getHeaderText());

                        displayPieChart();

                    }
                });
    }

    private void displayPieChart() {
        if (selectedStartDate != null && selectedEndDate != null) {
            ArrayList<PieEntry> entries = new ArrayList<>();
            DatabaseReference categoriesRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID);

            categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        String category = categorySnapshot.getKey();

                        double totalAmount = 0.0;
                        for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                            Date startDate = Date.from(selectedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                            Date endDate = Date.from(selectedEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                            Date productDate = null;
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            String startDateString = dateFormat.format(startDate);
                            String endDateString = dateFormat.format(endDate);
                            String dateString = productSnapshot.child("Date").getValue(String.class);

                            try {
                                productDate = dateFormat.parse(dateString);
                                startDate = dateFormat.parse(startDateString);
                                endDate = dateFormat.parse(endDateString);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }


                            if (dateString != null && (productDate.equals(startDate) || productDate.after(startDate)) && (productDate.equals(endDate) || productDate.before(endDate))) {
                                Double productAmount = Double.parseDouble(productSnapshot.child("Price").getValue(String.class));
                                if (productAmount > 0) {
                                    totalAmount = totalAmount + productAmount;

                                }
                            }
                        }
                        if (totalAmount > 0) {
                            entries.add(new PieEntry((float) totalAmount, category));
                        }
                    }

                    PieDataSet dataSet = new PieDataSet(entries, null);
                    dataSet.setValueTextSize(12f); // Set the font size of the value text
                    DecimalFormat priceFormat = new DecimalFormat("0.00");
                    dataSet.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            String formattedPrice = priceFormat.format(value); // Format the value using the DecimalFormat object
                            return "£" + formattedPrice;
                        }
                    });

                    dataSet.setValueLineVariableLength(true);
                    dataSet.setValueLinePart1OffsetPercentage(80.f);
                    dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                    dataSet.setValueLineColor(Color.WHITE);
                    dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                    Legend legend = pieChart.getLegend();
                    legend.setEnabled(false);

                    pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry entry, Highlight highlight) {
                            String selectedCategory = ((PieEntry) entry).getLabel();
                            Log.i(TAG, "Selected slice: " + selectedCategory);
                            displayDataChart(selectedCategory);
                        }

                        @Override
                        public void onNothingSelected() {
                            // This is called when no pie slice is selected
                        }
                    });

                    PieData data = new PieData(dataSet);
                    pieChart.setData(data);
                    pieChart.invalidate();
                    pieChart.setEntryLabelColor(Color.WHITE);
                    pieChart.getDescription().setEnabled(false);
                    pieChart.setCenterText("");
                    pieChart.setTouchEnabled(true);



                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error retrieving categories: " + error.getMessage());
                }
            });
        }
    }

    private void displayLinearChart() {
        if (selectedStartDate != null && selectedEndDate != null) {
            ArrayList<Entry> entries = new ArrayList<>();
            Map<String, ArrayList<Entry>> categoryEntries = new HashMap<>();
            DatabaseReference categoriesRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID);

            categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        String category = categorySnapshot.getKey();

                        Map<String, Double> dailyTotals = new HashMap<>();
                        ArrayList<Entry> entries = new ArrayList<>();

                        for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                            Date startDate = Date.from(selectedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                            Date endDate = Date.from(selectedEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                            Date productDate = null;
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            String dateString = productSnapshot.child("Date").getValue(String.class);

                            try {
                                productDate = dateFormat.parse(dateString);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }

                            // Check if the product falls within the selected date range
                            if (dateString != null && (productDate.equals(startDate) || productDate.after(startDate)) && (productDate.equals(endDate) || productDate.before(endDate))) {
                                Double productAmount = Double.parseDouble(productSnapshot.child("Price").getValue(String.class));
                                String dailyKey = dateFormat.format(productDate);
                                Double dailyTotal = dailyTotals.getOrDefault(dailyKey, 0.0);
                                dailyTotal += productAmount;
                                dailyTotals.put(String.valueOf(productDate), dailyTotal);

                            }
                        }
                        // Add the daily totals to the entry list for this category
                        int i = 0;
                        for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
                            entries.add(new Entry(i++, entry.getValue().floatValue()));
                        }

                        // Add the entry list to the map for this category
                        categoryEntries.put(category, entries);

                    }
                    LineData lineData = new LineData();
                    for (Map.Entry<String, ArrayList<Entry>> entry : categoryEntries.entrySet()) {
                        String category = entry.getKey();
                        ArrayList<Entry> entries = entry.getValue();
                        LineDataSet dataSet = new LineDataSet(entries, category);
                        lineData.addDataSet(dataSet);
                    }


                    lineChart.setData(lineData);
                    XAxis xAxis = lineChart.getXAxis();
                    // xAxis.setValueFormatter(new IndexAxisValueFormatter(getDateLabels(selectedStartDate, selectedEndDate)));
                    xAxis.setGranularityEnabled(true);
                    lineChart.invalidate();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void displayDataChart(String category) {
        if (selectedStartDate != null && selectedEndDate != null) {
            ArrayList<Entry> entries = new ArrayList<>();
            Map<String, ArrayList<Entry>> categoryEntries = new HashMap<>();
            DatabaseReference categoriesRef = FirebaseDatabase.getInstance("https://food-tracker-e3aa7-default-rtdb.europe-west1.firebasedatabase.app").getReference("User Categories").child(currentUserID).child(category);

            categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double totalAmount = 0.0;

                    Map<String, Double> dailyTotals = new HashMap<>();

                    for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                        Date startDate = Date.from(selectedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        Date endDate = Date.from(selectedEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        Date productDate = null;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        String dateString = productSnapshot.child("Date").getValue(String.class);

                        try {
                            productDate = dateFormat.parse(dateString);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        // Check if the product falls within the selected date range
                        if (dateString != null && (productDate.equals(startDate) || productDate.after(startDate)) && (productDate.equals(endDate) || productDate.before(endDate))) {
                            Double productAmount = Double.parseDouble(productSnapshot.child("Price").getValue(String.class));
                            if (productAmount > 0) {
                                // Get the date string in the format "dd/MM/yyyy"
                                String dailyKey = dateFormat.format(productDate);
                                // Get the current total for this day or initialize it to 0.0 if it doesn't exist yet
                                Double dailyTotal = dailyTotals.getOrDefault(dailyKey, 0.0);
                                // Add the product amount to the daily total
                                dailyTotal += productAmount;
                                // Store the updated daily total in the map
                                dailyTotals.put(dailyKey, dailyTotal);
                            }
                        }
                    }
                    // Sort dailyTotals by its keys (dates)
                    TreeMap<String, Double> sortedDailyTotals = new TreeMap<>(dailyTotals);

                    ArrayList<BarEntry> barEntries = new ArrayList<>();
                    int i = 0;
                    for (Map.Entry<String, Double> entry : sortedDailyTotals.entrySet()) {
                        barEntries.add(new BarEntry(i++, entry.getValue().floatValue(), entry.getKey()));
                    }
                    BarDataSet dataSet = new BarDataSet(barEntries, null);
                    dataSet.setValueTextSize(12f);
                    dataSet.setValueTextColor(Color.WHITE);
                    DecimalFormat priceFormat = new DecimalFormat("0.00");
                    dataSet.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            String formattedPrice = priceFormat.format(value);
                            return "£" + formattedPrice;
                        }
                    });
                    dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                    dataSet.setValueTextSize(12f);
                    dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                    YAxis yAxisRight = barChart.getAxisRight();
                    yAxisRight.setEnabled(false);


                    YAxis yAxisLeft = barChart.getAxisLeft();
                    yAxisLeft.setTextColor(Color.WHITE);

                    XAxis xAxisBottom = barChart.getXAxis();
                    xAxisBottom.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxisBottom.setGranularity(1f);
                    xAxisBottom.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value;
                            if (index >= 0 && index < barEntries.size()) {
                                return barEntries.get(index).getData().toString();
                            } else {
                                return "";
                            }
                        }
                    });
                    xAxisBottom.setTextColor(Color.WHITE);
                    xAxisBottom.setEnabled(true);


                    BarData barData = new BarData(dataSet);
                    barChart.setData(barData);
                    barChart.setDescription(null);
                    barChartText.setText(category + " Bar Chart");
                    barChart.getLegend().setEnabled(false);
                    barChart.invalidate();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private long parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        Date date;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        return date.getTime();
    }

    private String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }
}