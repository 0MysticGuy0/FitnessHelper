package com.mygy.fitnesshelper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mygy.fitnesshelper.data.AddWindowResult;
import com.mygy.fitnesshelper.data.Plan;
import com.mygy.fitnesshelper.data.Product;
import com.mygy.fitnesshelper.data.Store;
import com.mygy.fitnesshelper.data.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    public static User user;

    private TextView stepsDoneTV;
    private TextView stepsNeedTV;
    private TextView waterDoneTV;
    private TextView waterNeedTV;
    private TextView waterPercentTV;
    private TextView activityTimeTV;
    private TextView foodProteinsTV;
    private TextView foodFatsTV;
    private TextView foodCarbsTV;
    private TextView foodCcalsTV;
    private TextView planTimeTV;
    private TextView planNameTV;
    private TextView plansNumTV;
    private LinearLayout nearestPlanInfoPanel;

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(user != null) {
            System.out.println(Store.auth);

            stepsDoneTV = findViewById(R.id.main_stepsDone);
            stepsNeedTV = findViewById(R.id.main_stepsNeed);
            waterDoneTV = findViewById(R.id.mian_waterDone);
            waterNeedTV = findViewById(R.id.main_waterNeed);
            waterPercentTV = findViewById(R.id.main_waterPercent);
            activityTimeTV = findViewById(R.id.main_activityTime);
            foodProteinsTV = findViewById(R.id.main_foodProteins);
            foodFatsTV = findViewById(R.id.main_foodFats);
            foodCarbsTV = findViewById(R.id.main_foodCarbs);
            foodCcalsTV = findViewById(R.id.main_foodCcals);
            planNameTV = findViewById(R.id.main_planName);
            planTimeTV = findViewById(R.id.main_planTime);
            plansNumTV = findViewById(R.id.main_plansNum);
            nearestPlanInfoPanel = findViewById(R.id.main_nearestPlanInfoPanel);

            updateData();

            TextView profileRoot = findViewById(R.id.main_profileRoot);
            profileRoot.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                UserProfileActivity.user = user;
                startActivity(intent);
            });

            LinearLayout stepsRoot = findViewById(R.id.main_stepsRoot);
            stepsRoot.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, StepsActivity.class);
                startActivity(intent);
            });

            LinearLayout waterRoot = findViewById(R.id.main_waterRoot);
            waterRoot.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WaterActivity.class);
                startActivity(intent);
            });

            LinearLayout activityRoot = findViewById(R.id.main_activityRoot);
            activityRoot.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ActivitiesActivity.class);
                startActivity(intent);
            });

            LinearLayout foodRoot = findViewById(R.id.main_foodRoot);
            foodRoot.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, FoodActivity.class);
                startActivity(intent);
            });

            LinearLayout plansRoot = findViewById(R.id.main_plansRoot);
            plansRoot.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, PlansActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData();
    }

    private void updateData(){
        if(user!= null) {
            Date todayDate = new Date();
            stepsNeedTV.setText(Integer.toString(user.getStepsTarget()));
            stepsDoneTV.setText(Integer.toString(user.getStepsAtDate(todayDate)));

            int waterNeed = user.getWaterTarget();
            int waterDone = user.getWaterAtDate(todayDate);
            waterNeedTV.setText(Integer.toString(waterNeed));
            waterDoneTV.setText(Integer.toString(waterDone));
            waterPercentTV.setText(Integer.toString((int) ((float) waterDone / waterNeed * 100f)) + "%");

            int activitiesTime = 0;
            for (int t : user.getActivitiesAtDate(todayDate).values()) {
                activitiesTime += t;
            }
            activityTimeTV.setText(Integer.toString(activitiesTime));

            Product sum = user.getFoodSumDataAtDate(todayDate);
            foodProteinsTV.setText(String.format("%2.1f", sum.getProteins()));
            foodFatsTV.setText(String.format("%2.1f", sum.getFats()));
            foodCarbsTV.setText(String.format("%2.1f", sum.getCarbs()));
            foodCcalsTV.setText(String.format("%2.1f", sum.getCcals()));


            List<Plan> activePlans = user.getPlansAtDate(todayDate).stream().filter(p -> !p.isDone()).collect(Collectors.toList());
            int activePlansNum = Integer.max(0, activePlans.toArray().length - 1);
            Optional<Plan> nearestPlan = activePlans.stream()
                    .filter(p -> !p.getTime().before(new Date()))
                    .findFirst();
            if (nearestPlan.isPresent()) {
                nearestPlanInfoPanel.setVisibility(View.VISIBLE);
                Plan p = nearestPlan.get();
                planNameTV.setText(p.getName());
                planTimeTV.setText(timeFormat.format(p.getTime()));
            } else {
                nearestPlanInfoPanel.setVisibility(View.INVISIBLE);
                System.out.println("FFFFFFFFFFfff");
            }

            plansNumTV.setText(Integer.toString(activePlansNum));
        }
    }

    public static User getUser() {
        return user;
    }



}