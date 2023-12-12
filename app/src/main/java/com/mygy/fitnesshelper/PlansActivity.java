package com.mygy.fitnesshelper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mygy.fitnesshelper.adapters.PlanRecyclerAdapter;
import com.mygy.fitnesshelper.adapters.ProductRecyclerAdapter;
import com.mygy.fitnesshelper.data.Plan;
import com.mygy.fitnesshelper.data.Product;
import com.mygy.fitnesshelper.data.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlansActivity extends AppCompatActivity {
    private TextView[] daysBtns;
    private User user;
    private Date todayDate, selectedDayDate;
    private PlanRecyclerAdapter recyclerAdapter;
    TextView plansInfo;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru", "RU"));
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat df = new SimpleDateFormat("EEE",new Locale("ru","RU"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        user = MainActivity.getUser();
        todayDate = new Date();
        selectedDayDate = todayDate;

        ImageButton backBtn = findViewById(R.id.plans_back);
        backBtn.setOnClickListener(v -> {
            finish();
        });

        plansInfo = findViewById(R.id.plans_sumText);

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
                        recyclerAdapter.setSource(user.getPlansAtDate(selectedDayDate));
                        updateData();
                    }
                }
            });
        }

        RecyclerView recycler = findViewById(R.id.rv);
        recyclerAdapter = new PlanRecyclerAdapter(this, user.getPlansAtDate(selectedDayDate));
        recycler.setAdapter(recyclerAdapter);

        FloatingActionButton addPlansBtn = findViewById(R.id.plans_addBtn);
        addPlansBtn.setOnClickListener( v -> {
            showAddPlanWindow();
        });

        updateData();
    }

    private void updateData(){
        todayDate = new Date();

        for(int i = daysBtns.length-1; i >= 0; i--){
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_MONTH, i-6);
            daysBtns[i].setText(df.format(day.getTime()));
        }

        plansInfo.setText("Всего планов на сегодня: " + user.getPlansAtDate(selectedDayDate).size());

    }
    @Override
    protected void onResume() {
        super.onResume();
        updateData();
        recyclerAdapter.notifyDataSetChanged();
    }
    private void showAddPlanWindow(){
        AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
        final View addPlanWindow = this.getLayoutInflater().inflate(R.layout.add_plan_window, null);

        EditText name = addPlanWindow.findViewById(R.id.addPlan_name);
        EditText description = addPlanWindow.findViewById(R.id.addPlan_description);

        final Date[] selectedDate = new Date[1];
        selectedDate[0] = new Date(selectedDayDate.getTime());

        TextView dateTxt = addPlanWindow.findViewById(R.id.addPlan_date);
        dateTxt.setText(dateFormat.format(selectedDate[0]));

        ImageButton dateBtn = addPlanWindow.findViewById(R.id.addPlan_dateBtn);
        dateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    selectedDate[0].setYear(year - 1900);
                    selectedDate[0].setMonth(month);
                    selectedDate[0].setDate(dayOfMonth);
                    dateTxt.setText(dateFormat.format(selectedDate[0]));
                }
            }, year, month, day);
            dpd.show();
        });

        TextView timeTxt = addPlanWindow.findViewById(R.id.addPlan_time);
        timeTxt.setText(timeFormat.format(selectedDate[0]));

        ImageButton timeBtn = addPlanWindow.findViewById(R.id.addPlan_timeBtn);
        timeBtn.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    selectedDate[0].setHours(hourOfDay);
                    selectedDate[0].setMinutes(minute);
                    timeTxt.setText(timeFormat.format(selectedDate[0]));
                }
            }, selectedDate[0].getHours(),selectedDate[0].getMinutes(),true);
            tpd.show();
        });

        ImageButton cancel = addPlanWindow.findViewById(R.id.addPlan_cancelBtn);
        ImageButton add = addPlanWindow.findViewById(R.id.addPlan_addBtn);

        a_builder.setView(addPlanWindow);
        AlertDialog dialog = a_builder.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        add.setOnClickListener(v -> {
            try {
                String n = name.getText().toString();
                String descr = description.getText().toString();
                if(n.length() == 0 )
                    throw new RuntimeException("Введите имя!!!");
                if (selectedDate[0] == null)
                    throw new RuntimeException("Выберите дату!!!");

                user.addPlan(selectedDate[0],new Plan(n,descr,selectedDate[0]));
                updateData();
                recyclerAdapter.notifyDataSetChanged();
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