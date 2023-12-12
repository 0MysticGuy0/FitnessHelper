package com.mygy.fitnesshelper.data;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mygy.fitnesshelper.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class User implements Serializable{
    private String name;
    private Date birthDate;
    private HashMap<String, Object> userDoc;
    private String docID;
    private String email,passwd;

    private int stepsTarget;
    private HashMap<String, Integer> stepsData;
    private int waterTarget;
    private HashMap<String, Integer> waterData;
    private HashMap<String, ArrayList<Product>> foodData;
    private HashMap<String, HashMap<SportActivity,Integer>> activityData;
    private HashMap<String, ArrayList<Plan>> plansData;
    private static final SimpleDateFormat dateFormat;
    private static final SimpleDateFormat dateFormat2;
    private static final FirebaseFirestore usersBase;
    private Plan.OnPlanDoneChanged onPlanDoneChanged;

    static{
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat2 = new SimpleDateFormat("d MMMM yyyy", new Locale("ru", "RU"));
        usersBase = FirebaseFirestore.getInstance();
    }

    public User(String name, String email,String passwd, Date birthDate) {
        this.name = name;
        this.email = email;
        this.passwd = passwd;
        this.birthDate = birthDate;

        stepsData = new HashMap<>();
        stepsTarget = 10000;
        waterData = new HashMap<>();
        waterTarget = 2000;
        activityData = new HashMap<>();
        foodData = new HashMap<>();
        plansData = new HashMap<>();

        userDoc = new HashMap<>();
        docID = null;
        updateAllUserDoc();

        onPlanDoneChanged = (p -> {
            Plan.sortPlansList(getPlansAtDate(p.getTime()));
            saveDataToBase();
        });
        Plan.setOnPlanDataChanged(onPlanDoneChanged);
    }

    public User(HashMap<String,Object> document){
        userDoc = document;

        email = (String) userDoc.get(Constants.USER_EMAIL);
        passwd = (String) userDoc.get(Constants.USER_PASSWORD);
        name = (String) userDoc.get(Constants.USER_NAME);
        docID = (String) userDoc.get(Constants.USER_DOCK_ID);
        try {
            birthDate = ((Timestamp) userDoc.get(Constants.USER_BIRTHDATE)).toDate();
        }catch (NullPointerException ex){
            birthDate = new Date();
        }
        try {
            stepsTarget =  ((Long)userDoc.get(Constants.USER_STEPS_TARGET)).intValue();
        }catch (NullPointerException ex) {
            stepsTarget = 10000;
        }
        try {
        waterTarget = ((Long) userDoc.get(Constants.USER_WATER_TARGET)).intValue();
        }catch (NullPointerException ex) {
            waterTarget = 2000;
        }

        stepsData = new HashMap<>();
        for(Map.Entry<String,Long> sd:((HashMap<String, Long>) userDoc.get(Constants.USER_STEPS_DOC)).entrySet()){
                 stepsData.put(sd.getKey(),sd.getValue().intValue());
        }
        waterData = new HashMap<>();
        for(Map.Entry<String,Long> sd:((HashMap<String, Long>) userDoc.get(Constants.USER_WATER_DOC)).entrySet()){
            waterData.put(sd.getKey(),sd.getValue().intValue());
        }

        foodData = new HashMap<>();
        HashMap<String,ArrayList<HashMap<String,Object>>> food_hm = (HashMap<String, ArrayList<HashMap<String,Object>>>) userDoc.get(Constants.USER_FOOD_DOC);
        for(Map.Entry<String,ArrayList<HashMap<String,Object>>> el:food_hm.entrySet()){
            ArrayList<Product> dayList = new ArrayList<>();
            foodData.put(el.getKey(),dayList);
            for(HashMap<String,Object> p:el.getValue()){
                dayList.add(new Product(p));
            }
        }

        plansData = new HashMap<>();
        HashMap<String,ArrayList<HashMap<String,Object>>> plans_hm = (HashMap<String, ArrayList<HashMap<String,Object>>>) userDoc.get(Constants.USER_PLANS_DOC);
        for(Map.Entry<String,ArrayList<HashMap<String,Object>>> el:plans_hm.entrySet()){
            ArrayList<Plan> dayList = new ArrayList<>();
            plansData.put(el.getKey(),dayList);
            for(HashMap<String,Object> p:el.getValue()){
                dayList.add(new Plan(p));
            }
        }

        activityData = new HashMap<>();
        HashMap<String,ArrayList<HashMap<String,Object>>> activity_hm = (HashMap<String, ArrayList<HashMap<String,Object>>>) userDoc.get(Constants.USER_ACTIVITY_DOC);
        for(Map.Entry<String,ArrayList<HashMap<String,Object>>> el:activity_hm.entrySet()){
            HashMap<SportActivity,Integer> dayList = new HashMap<>();
            activityData.put(el.getKey(),dayList);
            for(HashMap<String,Object> p:el.getValue()){
                SportActivity sActivity = SportActivity.getActivityByName((String)p.get("key"));
                dayList.put(sActivity, ((Long)p.get("value")).intValue());
            }
        }

        onPlanDoneChanged = (p -> {
            Plan.sortPlansList(getPlansAtDate(p.getTime()));
            saveDataToBase();
        });
        Plan.setOnPlanDataChanged(onPlanDoneChanged);
    }

    public HashMap<String, Object> getUserDoc() {
        return userDoc;
    }
    private void updateAllUserDoc(){
        userDoc.put(Constants.USER_EMAIL,email);
        userDoc.put(Constants.USER_PASSWORD,passwd);
        userDoc.put(Constants.USER_NAME,name);
        userDoc.put(Constants.USER_DOCK_ID,docID);
        userDoc.put(Constants.USER_BIRTHDATE,birthDate);

        userDoc.put(Constants.USER_STEPS_TARGET,stepsTarget);
        userDoc.put(Constants.USER_STEPS_DOC,stepsData);
        userDoc.put(Constants.USER_WATER_TARGET,waterTarget);
        userDoc.put(Constants.USER_WATER_DOC,waterData);
        saveSportActivityDataToDoc();
        userDoc.put(Constants.USER_FOOD_DOC,foodData);
        userDoc.put(Constants.USER_PLANS_DOC,plansData);
        saveDataToBase();
    }
    public void saveDataToBase() {
        if (docID != null) {
            usersBase.collection(Constants.USERS_BASE)
                    .document(this.getDocID())
                    .update(this.getUserDoc())
                    .addOnSuccessListener(docRef -> {
                        System.out.println("----------Сохранил пользователя" + this.getEmail() + "-----------");
                    })
                    .addOnFailureListener(ex -> {
                        System.out.println("----------Ошибка сохранения" + this.getEmail() + "-----------");
                    });
        }
    }
    private void saveSportActivityDataToDoc(){
        HashMap<String,ArrayList<Map.Entry<String,Integer>>> data = new HashMap<>();
        for(Map.Entry<String,HashMap<SportActivity,Integer>> ad: activityData.entrySet()){
            ArrayList<Map.Entry<String,Integer>> dayList = new ArrayList<>();
            data.put(ad.getKey(),dayList);

            for(Map.Entry<SportActivity,Integer> ac: ad.getValue().entrySet()){
                Map.Entry<String,Integer> a_data = new AbstractMap.SimpleEntry<>(ac.getKey().getName(),ac.getValue());
                dayList.add(a_data);
            }
        }


        userDoc.put(Constants.USER_ACTIVITY_DOC,data);
    }

    public String getEmail() {
        return email;
    }
    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getName() {
        return name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public int getStepsTarget() {
        return stepsTarget;
    }

    public int getStepsAtDate(Date date) {
        String dateKey = dateFormat.format(date);
        Integer res = stepsData.get(dateKey);
        if (res == null) {
            stepsData.put(dateKey, 0);
            res = 0;
        }
        return res;
    }

    public int getWaterAtDate(Date date) {
        String dateKey = dateFormat.format(date);
        Integer res = waterData.get(dateKey);
        if (res == null) {
            waterData.put(dateKey, 0);
            res = 0;
        }
        return res;
    }

    public HashMap<SportActivity,Integer> getActivitiesAtDate(Date date) {
        String dateKey = dateFormat.format(date);
        if (!activityData.containsKey(dateKey)) {
            activityData.put(dateKey, new HashMap<>());
        }
        return activityData.get(dateKey);
    }
    public ArrayList<Product> getFoodAtDate(Date date) {
        String dateKey = dateFormat.format(date);
        if (!foodData.containsKey(dateKey)) {
            foodData.put(dateKey, new ArrayList<>());
        }

        return foodData.get(dateKey);
    }
    public ArrayList<Plan> getPlansAtDate(Date date) {
        String dateKey = dateFormat.format(date);
        if (!plansData.containsKey(dateKey)) {
            plansData.put(dateKey, new ArrayList<>());
        }

        return plansData.get(dateKey);
    }

    public void addSteps(Date date, int steps) {
        int stepsDone = 0;
        String dateKey = dateFormat.format(date);
        if (stepsData.containsKey(dateKey)) stepsDone = stepsData.get(dateKey);

        stepsData.put(dateKey, stepsDone + steps);
        userDoc.put(Constants.USER_STEPS_DOC,stepsData);
        saveDataToBase();
    }

    public void addWater(Date date, int milliliters) {
        int waterDone = 0;
        String dateKey = dateFormat.format(date);
        if (waterData.containsKey(dateKey)) waterDone = waterData.get(dateKey);

        waterData.put(dateKey, waterDone + milliliters);
        userDoc.put(Constants.USER_WATER_DOC,waterData);
        saveDataToBase();
    }

    public void addActivity(Date date, Map.Entry<SportActivity,Integer> activity) {
        int activityDone = 0;
        String dateKey = dateFormat.format(date);
        if (!activityData.containsKey(dateKey)) {
            activityData.put(dateKey, new HashMap<>());
        }
        if (activityData.get(dateKey).containsKey(activity.getKey())) {
            activityDone = activityData.get(dateKey).get(activity.getKey());
        }
        activityData.get(dateKey).put(activity.getKey(),activity.getValue() + activityDone);

        saveSportActivityDataToDoc();
        saveDataToBase();
    }
    public void addFood(Date date, Product product) {
        String dateKey = dateFormat.format(date);
        if(!foodData.containsKey(dateKey)){
            foodData.put(dateKey,new ArrayList<>());
        }
        ArrayList<Product> foodDone = foodData.get(dateKey);
        foodDone.add(product);

        userDoc.put(Constants.USER_FOOD_DOC,foodData);
        saveDataToBase();
    }
    public void addPlan(Date date, Plan plan) {
        String dateKey = dateFormat.format(date);
        if(!plansData.containsKey(dateKey)){
            plansData.put(dateKey,new ArrayList<>());
        }
        ArrayList<Plan> plansList = plansData.get(dateKey);
        plansList.add(plan);

        Plan.sortPlansList(plansList);

        userDoc.put(Constants.USER_PLANS_DOC,plansData);
        saveDataToBase();
    }

    public Product getFoodSumDataAtDate(Date date){
        ArrayList<Product> list = getFoodAtDate(date);
        float p = 0f;
        float f = 0f;
        float c = 0f;
        float cc = 0f;
        for(Product product:list){
            p+=product.getProteins();
            f += product.getFats();
            c += product.getCarbs();
            cc += product.getCcals();
        }
        return new Product("result",p,f,c,cc);
    }


    public void setStepsTarget(int stepsTarget) {
        this.stepsTarget = stepsTarget;
        userDoc.put(Constants.USER_STEPS_TARGET,stepsTarget);
        saveDataToBase();
    }

    public int getWaterTarget() {
        return waterTarget;
    }

    public void setWaterTarget(int waterTarget) {
        this.waterTarget = waterTarget;
        userDoc.put(Constants.USER_WATER_TARGET,waterTarget);
        saveDataToBase();
    }

    public void setName(String name) {
        this.name = name;
        userDoc.put(Constants.USER_NAME,name);
        saveDataToBase();
    }
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
        userDoc.put(Constants.USER_BIRTHDATE,birthDate);
        saveDataToBase();
    }

    /////////////////////////////////
    public static void showAddValueWindow(AppCompatActivity activity, String valueName, AddWindowResult resultProcessor) {
        AlertDialog.Builder a_builder = new AlertDialog.Builder(activity);
        final View addValueWindow = activity.getLayoutInflater().inflate(R.layout.add_steps_window, null);

        TextView title = addValueWindow.findViewById(R.id.addSteps_title);
        title.setText("Добавить " + valueName);
        EditText steps = addValueWindow.findViewById(R.id.addSteps_steps);

        final Date[] selectedDate = new Date[1];
        selectedDate[0] = new Date();

        TextView dateTxt = addValueWindow.findViewById(R.id.addSteps_date);
        dateTxt.setText(dateFormat2.format(selectedDate[0]));

        ImageButton dateBtn = addValueWindow.findViewById(R.id.addSteps_dateBtn);
        dateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog dpd = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    selectedDate[0] = new Date(year - 1900, month, dayOfMonth);
                    dateTxt.setText(dateFormat2.format(selectedDate[0]));
                }
            }, year, month, day);
            dpd.show();
        });

        ImageButton cancel = addValueWindow.findViewById(R.id.addSteps_cancelBtn);
        ImageButton add = addValueWindow.findViewById(R.id.addSteps_addBtn);

        a_builder.setView(addValueWindow);
        AlertDialog dialog = a_builder.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        add.setOnClickListener(v -> {
            try {
                int input = Integer.parseInt(steps.getText().toString());
                if (selectedDate[0] == null)
                    throw new RuntimeException("Выберите дату!!!");
                if (selectedDate[0].after(new Date()))
                    throw new RuntimeException("Выбранный день еще не наступил! Выберите корректную дату!");

                resultProcessor.processResult(selectedDate[0], input);
                Toast.makeText(activity, "Данные обновлены", Toast.LENGTH_SHORT).show();

                dialog.cancel();
            } catch (NumberFormatException ex) {
                Toast.makeText(activity, "Некорректный ввод!!!", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException ex) {
                Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showChangeTagretWindow(AppCompatActivity activity, String valueName, float oldVal, ChangeTargetWindowResult resultProcessor) {
        AlertDialog.Builder a_builder = new AlertDialog.Builder(activity);
        final View changeTargetWindow = activity.getLayoutInflater().inflate(R.layout.change_target_value_window, null);

        TextView title = changeTargetWindow.findViewById(R.id.changeTarget_title);
        title.setText("Изменить цель для " + valueName);
        EditText valET = changeTargetWindow.findViewById(R.id.changeTarget_val);
        valET.setText(Float.toString(oldVal));

        ImageButton cancel = changeTargetWindow.findViewById(R.id.changeTarget_cancelBtn);
        ImageButton add = changeTargetWindow.findViewById(R.id.changeTarget_addBtn);

        a_builder.setView(changeTargetWindow);
        AlertDialog dialog = a_builder.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        add.setOnClickListener(v -> {
            try {
                float input = Float.parseFloat(valET.getText().toString());

                resultProcessor.processResult(input);
                Toast.makeText(activity, "Данные обновлены", Toast.LENGTH_SHORT).show();

                dialog.cancel();
            } catch (NumberFormatException ex) {
                Toast.makeText(activity, "Некорректный ввод!!!", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException ex) {
                Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //////////////////////////////////////
    public void saveLoginDataToFile(Context context) {
        try (FileOutputStream fos = context.openFileOutput(Constants.LOGIN_FILE_NAME, Context.MODE_PRIVATE)) {
            Properties properties = new Properties();
            properties.setProperty(Constants.USER_EMAIL, email);
            properties.setProperty(Constants.USER_PASSWORD, passwd);
            properties.store(fos, "login data");
            System.out.println("\n======================================SAVED LOGIN DATA=======================================\n");
        } catch (FileNotFoundException ex) {
            System.out.println("!!!!!!!!!!!!!!!\nNO FILE TO SAVE LOGIN DATA!\n!!!!!!!!!!!!!!!!!");
            throw new RuntimeException();
        } catch (IOException ex) {
            System.out.println("ERROR SAVING LOGIN DATA!!!!!!!!!!!!!");
            throw new RuntimeException();
        }
    }

    public static String[] readLoginDataFromFile(Context context) {
        try(FileInputStream fis = context.openFileInput(Constants.LOGIN_FILE_NAME)){
            Properties properties = new Properties();
            properties.load(fis);
            String[] data = new String[2];
            data[0] = properties.getProperty(Constants.USER_EMAIL);
            data[1] = properties.getProperty(Constants.USER_PASSWORD);
            return data;
        } catch (FileNotFoundException ex) {
            return  null;
        } catch (IOException ex) {
            System.out.println("ERROR READING LOGIN DATA!!!!!!!!!!!!!\n"+ex.getMessage());
            throw new RuntimeException();
        }
    }
    public void signOut(Context context){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        try (FileOutputStream fos = context.openFileOutput(Constants.LOGIN_FILE_NAME, Context.MODE_PRIVATE)) {
            Properties properties = new Properties();
            properties.setProperty(Constants.USER_EMAIL, "");
            properties.setProperty(Constants.USER_PASSWORD, "");
            properties.store(fos, "login data");
            System.out.println("\n======================================REMOVED LOGIN DATA=======================================\n");
        } catch (FileNotFoundException ex) {
            System.out.println("!!!!!!!!!!!!!!!\nNO FILE TO REMOVE LOGIN DATA!\n!!!!!!!!!!!!!!!!!");
            throw new RuntimeException();
        } catch (IOException ex) {
            System.out.println("ERROR REMOVING LOGIN DATA!!!!!!!!!!!!!");
            throw new RuntimeException();
        }
    }
}
