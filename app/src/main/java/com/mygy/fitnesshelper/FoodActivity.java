package com.mygy.fitnesshelper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mygy.fitnesshelper.adapters.ProductRecyclerAdapter;
import com.mygy.fitnesshelper.data.Product;
import com.mygy.fitnesshelper.data.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FoodActivity extends AppCompatActivity {

    private TextView[] daysBtns;
    private User user;
    private Date todayDate, selectedDayDate;
    private ProductRecyclerAdapter recyclerAdapter;
    TextView proteins, fats, carbs, ccals;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru", "RU"));
    private static SimpleDateFormat df = new SimpleDateFormat("EEE",new Locale("ru","RU"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        user = MainActivity.getUser();
        todayDate = new Date();
        selectedDayDate = todayDate;

        proteins = findViewById(R.id.food_proteins);
        fats = findViewById(R.id.food_fats);
        carbs = findViewById(R.id.food_carbs);
        ccals = findViewById(R.id.food_ccal);

        ImageButton backBtn = findViewById(R.id.food_back);
        backBtn.setOnClickListener(v -> {
            finish();
        });

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
                        recyclerAdapter.setSource(user.getFoodAtDate(selectedDayDate));
                        updateData(selectedDayDate);
                    }
                }
            });
        }

        RecyclerView recycler = findViewById(R.id.rv);
        recyclerAdapter = new ProductRecyclerAdapter(this, user.getFoodAtDate(todayDate));
        recycler.setAdapter(recyclerAdapter);

        FloatingActionButton addFoodBtn = findViewById(R.id.food_addBtn);
        addFoodBtn.setOnClickListener( v -> {
            showAddFoodWindow();
        });

        updateData(todayDate);
    }

    private void updateData(Date date) {
        for(int i = daysBtns.length-1; i >= 0; i--){
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_MONTH, i-6);
            daysBtns[i].setText(df.format(day.getTime()));
        }

        Product sum = user.getFoodSumDataAtDate(date);

        proteins.setText(String.format("%2.1f",sum.getProteins()));
        fats.setText(String.format("%2.1f",sum.getFats()));
        carbs.setText(String.format("%2.1f",sum.getCarbs()));
        ccals.setText(String.format("%2.1f",sum.getCcals()));
    }
    private void showAddFoodWindow(){
        AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
        final View addFoodWindow = this.getLayoutInflater().inflate(R.layout.add_food_window, null);

        EditText name = addFoodWindow.findViewById(R.id.addFood_name);
        EditText proteins = addFoodWindow.findViewById(R.id.addFood_proteins);
        EditText fats = addFoodWindow.findViewById(R.id.addFood_fats);
        EditText carbs = addFoodWindow.findViewById(R.id.addFood_carbs);
        EditText ccals = addFoodWindow.findViewById(R.id.addFood_ccals);

        final Date[] selectedDate = new Date[1];
        selectedDate[0] = new Date(selectedDayDate.getTime());

        TextView dateTxt = addFoodWindow.findViewById(R.id.addFood_date);
        dateTxt.setText(dateFormat.format(selectedDate[0]));

        ImageButton dateBtn = addFoodWindow.findViewById(R.id.addFood_dateBtn);
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

        ImageButton cancel = addFoodWindow.findViewById(R.id.addSteps_cancelBtn);
        ImageButton add = addFoodWindow.findViewById(R.id.addSteps_addBtn);

        a_builder.setView(addFoodWindow);
        AlertDialog dialog = a_builder.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        add.setOnClickListener(v -> {
            try {
                String n = name.getText().toString();
                float p = Float.parseFloat(proteins.getText().toString());
                float f = Float.parseFloat(fats.getText().toString());
                float c = Float.parseFloat(carbs.getText().toString());
                float cc = Float.parseFloat(ccals.getText().toString());
                if(n.length() == 0 )
                    throw new RuntimeException("Введите имя!!!");
                if (selectedDate[0] == null)
                    throw new RuntimeException("Выберите дату!!!");
                if (selectedDate[0].after(new Date()))
                    throw new RuntimeException("Выбранный день еще не наступил! Выберите корректную дату!");

                user.addFood(selectedDate[0],new Product(n,p,f,c,cc));
                updateData(selectedDayDate);
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