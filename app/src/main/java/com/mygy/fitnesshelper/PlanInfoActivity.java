package com.mygy.fitnesshelper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mygy.fitnesshelper.data.Plan;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PlanInfoActivity extends AppCompatActivity {
    private static SimpleDateFormat dtf = new SimpleDateFormat("d MMMM yyyy | HH:mm",new Locale("ru","RU"));
    public static Plan plan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_info);

        ImageButton backBtn = findViewById(R.id.planInfo_back);
        backBtn.setOnClickListener(v -> {
            finish();
        });

        if(plan != null){
            TextView name = findViewById(R.id.planInfo_name);
            name.setText(plan.getName());

            TextView time = findViewById(R.id.planInfo_time);
            time.setText(dtf.format(plan.getTime()));

            TextView description = findViewById(R.id.planInfo_description);
            description.setText(plan.getDescription());

            CheckBox checkBox = findViewById(R.id.planInfo_checkBox);
            checkBox.setChecked(plan.isDone());

            checkBox.setOnClickListener(v -> {
                plan.setDone(checkBox.isChecked());
            });
        }
    }
}