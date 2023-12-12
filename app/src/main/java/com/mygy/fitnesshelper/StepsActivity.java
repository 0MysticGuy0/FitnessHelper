package com.mygy.fitnesshelper;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.mygy.fitnesshelper.data.Store;
import com.mygy.fitnesshelper.data.User;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StepsActivity extends AppCompatActivity {

    private User user;
    private TextView stepsDone;
    private TextView stepsNeed;
    private TextView date;
    private static SimpleDateFormat dateFormat= new SimpleDateFormat("d MMMM yyyy",new Locale("ru","RU"));
    private BarChart barChart;
    private Date todayDate,selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);

        user = MainActivity.getUser();
        todayDate = new Date();
        selectedDate = todayDate;

        ImageButton backBtn = findViewById(R.id.steps_back);
        backBtn.setOnClickListener(v -> {
            finish();
        });
        ImageButton settingsBtn = findViewById(R.id.steps_settings);
        settingsBtn.setOnClickListener(v -> {
            User.showChangeTagretWindow(this,"шагов",user.getStepsTarget(), inp -> {
                user.setStepsTarget((int)inp);
                updateInfoData();
            });
        });

        stepsDone = findViewById(R.id.steps_stepsDone);
        stepsNeed = findViewById(R.id.steps_stepsNeed);
        date = findViewById(R.id.steps_date);
        barChart = findViewById(R.id.steps_barChart);

        setUpBarChart();
        updateInfoData();
        updateBarData();

        ImageButton addSteps = findViewById(R.id.steps_addSteps);
        addSteps.setOnClickListener(v -> {
            User.showAddValueWindow(this,"шаги",(date,val) -> {
                user.addSteps(date,(int)val);
                //user.addActivity(date,new AbstractMap.SimpleEntry<>(Store.walking,time));
                updateInfoData();
                updateBarData();
            });
        });
    }

    private void setUpBarChart(){
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                Calendar day = Calendar.getInstance();
                day.add(Calendar.DAY_OF_MONTH,-6);
                day.add(Calendar.DAY_OF_MONTH,(int)value);
                SimpleDateFormat df = new SimpleDateFormat("EEE",new Locale("ru","RU"));
                return df.format(day.getTime());
            }
        });
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawAxisLine(false);

        barChart.setDrawValueAboveBar(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);

        barChart.setHighlightPerDragEnabled(false);//disable drags

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int selectedIndx = (int)e.getX();
                Calendar day = Calendar.getInstance();
                day.add(Calendar.DAY_OF_MONTH,-6);
                day.add(Calendar.DAY_OF_MONTH,selectedIndx);
                selectedDate = day.getTime();
                updateInfoData();
            }
            @Override
            public void onNothingSelected() {
            }
        });
    }
    private void updateInfoData(){
        todayDate = new Date();

        date.setText(dateFormat.format(selectedDate));
        stepsDone.setText(Integer.toString(user.getStepsAtDate(selectedDate)));
        stepsNeed.setText(Integer.toString(user.getStepsTarget()));
    }
    private void updateBarData(){
        List<BarEntry> barEntries = new ArrayList<>();
        for(int i = 0; i < 7; i++){
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_MONTH,-6);
            day.add(Calendar.DAY_OF_MONTH,i);
            barEntries.add(new BarEntry(i,user.getStepsAtDate(day.getTime())));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries,"steps count");
        barDataSet.setColor(Color.RED);
        barDataSet.setValueTextSize(12f);
        barDataSet.setDrawValues(false);
        BarData barData = new BarData(barDataSet);
        //barData.setDrawValues(false);
        barChart.setData(barData);

        barChart.invalidate();
    }

}