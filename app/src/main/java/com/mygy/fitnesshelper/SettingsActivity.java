package com.mygy.fitnesshelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mygy.fitnesshelper.data.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private User user;
    private static SimpleDateFormat dateFormat= new SimpleDateFormat("d MMMM yyyy",new Locale("ru","RU"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        user = MainActivity.user;

        ImageButton backBtn = findViewById(R.id.settings_back);
        backBtn.setOnClickListener(v -> {
            finish();
        });

        if(user != null) {

            EditText name = findViewById(R.id.settings_name);
            name.setText(user.getName());

            TextView birthDateTxt = findViewById(R.id.settings_birthDateTxt);
            birthDateTxt.setText(dateFormat.format(user.getBirthDate()));

            ImageButton birthDateBtn = findViewById(R.id.settings_birthDateBtn);
            final Date[] selectedDate = new Date[1];
            birthDateBtn.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate[0] = new Date(year - 1900, month, dayOfMonth);
                        birthDateTxt.setText(dateFormat.format(selectedDate[0]));
                    }
                }, year, month, day);
                dpd.show();
            });

            Button saveBtn = findViewById(R.id.settings_saveBtn);
            saveBtn.setOnClickListener(v -> {
                if(name.getText().toString().length() > 0) {
                    user.setName(name.getText().toString());
                }else{
                    name.setText(user.getName());
                    //Toast.makeText(this, "Имя не введено!", Toast.LENGTH_SHORT).show();
                }
                if (selectedDate[0] != null) {
                    user.setBirthDate(selectedDate[0]);
                }else{
                    birthDateTxt.setText(dateFormat.format(user.getBirthDate()));
                    //Toast.makeText(this, "Дата рождения не введена!", Toast.LENGTH_SHORT).show();
                }
                finish();
            });

            Button signOutBtn = findViewById(R.id.settings_signOutBtn);
            signOutBtn.setOnClickListener(v -> {
                user.signOut(this);

                Intent intent = new Intent(this, EnteringActivity.class);
                startActivity(intent);
                finish();
            });
        }else{
            finish();
        }
    }
}