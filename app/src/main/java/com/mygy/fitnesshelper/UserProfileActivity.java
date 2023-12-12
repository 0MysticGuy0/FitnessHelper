package com.mygy.fitnesshelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mygy.fitnesshelper.adapters.StatsRecyclerAdapter;
import com.mygy.fitnesshelper.data.Product;
import com.mygy.fitnesshelper.data.SportActivity;
import com.mygy.fitnesshelper.data.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserProfileActivity extends AppCompatActivity {

    public static User user;
    private Date selectedDate;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private String[][] stats;
    private TextView nameTV;
    private TextView ageTV;
    private  StatsRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        selectedDate = new Date();
        initializeStats();

        ImageButton backBtn = findViewById(R.id.profile_back);
        backBtn.setOnClickListener(v -> {
            finish();
        });

        if(user != null){
            nameTV = findViewById(R.id.profile_name);
            ageTV = findViewById(R.id.profile_ageInfo);

            RecyclerView recyclerView = findViewById(R.id.profile_recycler);
            adapter = new StatsRecyclerAdapter(this,stats);
            recyclerView.setAdapter(adapter);

            Button dateBtn = findViewById(R.id.profile_dateBtn);
            dateBtn.setText(dateFormat.format(selectedDate));
            dateBtn.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate = new Date(year - 1900, month, dayOfMonth);
                        dateBtn.setText(dateFormat.format(selectedDate));

                        updateStatsData();
                        //adapter.setSource(stats);
                    }
                    }, year, month, day);
                dpd.show();

            });

            ImageButton shareBtn = findViewById(R.id.profile_shareBtn);
            shareBtn.setOnClickListener(v -> {
                shareStats();
            });

            ImageButton settingsBtn = findViewById(R.id.profile_settings);
            settingsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            });

            updateData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData();
    }

    private void updateData(){
        nameTV.setText(user.getName());
        long age = TimeUnit.DAYS.convert( Math.abs(new Date().getTime() - user.getBirthDate().getTime()), TimeUnit.MILLISECONDS)/365;
        ageTV.setText(age + " лет");
        updateStatsData();
    }
    private void initializeStats(){
        stats = new String[5][2];
        stats[0][0] = "Шаги:";
        stats[1][0] = "Вода:";
        stats[2][0] = "Активность:";
        stats[3][0] = "Питание:";
        stats[4][0] = "Планы:";
    }
    private void updateStatsData(){
        stats[0][1] = user.getStepsAtDate(selectedDate)+" из "+user.getStepsTarget();
        stats[1][1] = user.getWaterAtDate(selectedDate)+" из "+user.getWaterTarget()+" мл.";

        int activitiesTime = 0;
        float activitiesCcal = 0;
        for(Map.Entry<SportActivity,Integer> a: user.getActivitiesAtDate(selectedDate).entrySet()){
            activitiesTime += a.getValue();
            activitiesCcal += a.getKey().getCcalsPerHour()/60f * a.getValue();
        }
        stats[2][1] = activitiesTime + " мин. - " + activitiesCcal + " ККал";

        Product sum = user.getFoodSumDataAtDate(selectedDate);
        stats[3][1] = String.format("Белки: %2.1f\nЖиры: %2.1f\nУглеводы: %2.1f\nКкал: %2.1f",sum.getProteins(),sum.getFats(),sum.getCarbs(),sum.getCcals());

        stats[4][1] = "Выполнено планов - "+user.getPlansAtDate(selectedDate).stream().filter(p -> p.isDone()).count()+" из "+user.getPlansAtDate(selectedDate).size();

        adapter.notifyDataSetChanged();
    }
    private void shareStats() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        //intent.setPackage(packageName); //имя пакета приложения

        StringBuilder statsToSend = new StringBuilder();
        statsToSend.append("Статистика от ")
                .append(user.getName())
                .append(" за ")
                .append(dateFormat.format(selectedDate))
                .append("\n==========\n");
        for(String[] info:stats){
            statsToSend.append("-| ")
                    .append(info[0])
                    .append(" |-\n")
                    .append(info[1])
                    .append("\n==========\n");
        }
        intent.putExtra(Intent.EXTRA_TEXT, statsToSend.toString()); // текст отправки
        startActivity(Intent.createChooser(intent, "Поделиться с"));
    }
}