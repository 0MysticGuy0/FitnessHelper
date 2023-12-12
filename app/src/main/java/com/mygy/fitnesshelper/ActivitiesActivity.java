package com.mygy.fitnesshelper;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mygy.fitnesshelper.adapters.SportActivityRecyclerAdapter;
import com.mygy.fitnesshelper.data.SportActivity;
import com.mygy.fitnesshelper.data.User;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ActivitiesActivity extends AppCompatActivity {

    private TextView[] daysBtns;
    private User user;
    private Date todayDate, selectedDayDate;
    private SportActivityRecyclerAdapter recyclerAdapter;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru", "RU"));
    private static SimpleDateFormat df = new SimpleDateFormat("EEE",new Locale("ru","RU"));
    private TextView sumTxt;
    private static String[] activitiesNames;
    private ArrayAdapter<String> spinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        user = MainActivity.getUser();
        todayDate = new Date();
        selectedDayDate = todayDate;
        activitiesNames = SportActivity.getAllActivities().stream().map(SportActivity::getName).sorted().toArray(String[]::new);
        spinnerAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, activitiesNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ImageButton backBtn = findViewById(R.id.steps_back);
        backBtn.setOnClickListener(v -> {
            finish();
        });

        sumTxt = findViewById(R.id.activities_sumText);

        daysBtns = new TextView[7];
        daysBtns[0] = findViewById(R.id.food_d1Btn);
        daysBtns[1] = findViewById(R.id.food_d2Btn);
        daysBtns[2] = findViewById(R.id.food_d3Btn);
        daysBtns[3] = findViewById(R.id.food_d4Btn);
        daysBtns[4] = findViewById(R.id.food_d5Btn);
        daysBtns[5] = findViewById(R.id.food_d6Btn);
        daysBtns[6] = findViewById(R.id.food_d7Btn);

        for (TextView btn : daysBtns) {
            btn.setOnClickListener(v -> {
                for (int i = 0; i < daysBtns.length; i++) {
                    TextView b = daysBtns[i];
                    if (btn != b)
                        b.setBackgroundResource(R.color.gray);
                    else {
                        b.setBackgroundResource(R.color.white);
                        Calendar day = Calendar.getInstance();
                        day.add(Calendar.DAY_OF_MONTH, i-6);
                        selectedDayDate = day.getTime();
                        recyclerAdapter.setSource(user.getActivitiesAtDate(selectedDayDate));
                        updateData();
                    }
                }
            });
        }

        RecyclerView recycler = findViewById(R.id.rv);
        recyclerAdapter = new SportActivityRecyclerAdapter(this, user.getActivitiesAtDate(selectedDayDate));
        recycler.setAdapter(recyclerAdapter);

        FloatingActionButton addFoodBtn = findViewById(R.id.activities_addBtn);
        addFoodBtn.setOnClickListener( v -> {
            showAddActivityWindow();
        });

        updateData();
    }

    private void updateData() {
        todayDate = new Date();

        recyclerAdapter.setSource(user.getActivitiesAtDate(selectedDayDate));

        for(int i = daysBtns.length-1; i >= 0; i--){
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_MONTH, i-6);
            daysBtns[i].setText(df.format(day.getTime()));
        }

        int activitiesTime = 0;
        float activitiesCcal = 0;
        for(Map.Entry<SportActivity,Integer> a: user.getActivitiesAtDate(selectedDayDate).entrySet()){
            activitiesTime += a.getValue();
            activitiesCcal += a.getKey().getCcalsPerHour()/60f * a.getValue();
        }
       sumTxt.setText(activitiesTime + " мин. - " + activitiesCcal + " ККал");
    }

    private void showAddActivityWindow(){
        AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
        final View addActivityWindow = this.getLayoutInflater().inflate(R.layout.add_activity_window, null);

        EditText minutesET = addActivityWindow.findViewById(R.id.addActivity_time);

        final SportActivity[] selectedActivity = {null};
        Spinner spinner = addActivityWindow.findViewById(R.id.addActivity_spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String a_name = activitiesNames[position];
                selectedActivity[0] = SportActivity.getActivityByName(a_name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Date[] selectedDate = new Date[1];
        selectedDate[0] = new Date(selectedDayDate.getTime());

        TextView dateTxt = addActivityWindow.findViewById(R.id.addActivity_date);
        dateTxt.setText(dateFormat.format(selectedDate[0]));

        ImageButton dateBtn = addActivityWindow.findViewById(R.id.addActivity_dateBtn);
        dateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    selectedDate[0] = new Date(year - 1900, month, dayOfMonth);
                    dateTxt.setText(dateFormat.format(selectedDate[0]));
                }
            }, year, month, day);
            dpd.show();
        });

        ImageButton cancel = addActivityWindow.findViewById(R.id.addActivity_cancelBtn);
        ImageButton add = addActivityWindow.findViewById(R.id.addActivity_addBtn);

        a_builder.setView(addActivityWindow);
        AlertDialog dialog = a_builder.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        add.setOnClickListener(v -> {
            try {
                int time = Integer.parseInt(minutesET.getText().toString());
                if (selectedDate[0] == null)
                    throw new RuntimeException("Выберите дату!!!");
                if (selectedDate[0].after(new Date()))
                    throw new RuntimeException("Выбранный день еще не наступил! Выберите корректную дату!");
                if(selectedActivity[0] == null)
                    throw new RuntimeException("Выберите активность из списка!");

                user.addActivity(selectedDate[0],new AbstractMap.SimpleEntry<>(selectedActivity[0],time));
                updateData();
                Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show();


                dialog.cancel();
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Некорректный ввод!!!", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}