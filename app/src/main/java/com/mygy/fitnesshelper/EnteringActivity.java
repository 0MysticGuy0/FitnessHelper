package com.mygy.fitnesshelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mygy.fitnesshelper.data.Constants;
import com.mygy.fitnesshelper.data.Store;
import com.mygy.fitnesshelper.data.User;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import pl.droidsonroids.gif.GifImageView;

public class EnteringActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore usersBase;
    private User user;
    private GifImageView loadingGIF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entering);

        loadingGIF = findViewById(R.id.enter_load);

        auth = FirebaseAuth.getInstance();
        usersBase = FirebaseFirestore.getInstance();

        String[] savedLoginData = User.readLoginDataFromFile(this);
        if(savedLoginData != null && !savedLoginData[0].equals("") && !savedLoginData[1].equals("")){
            logIn(savedLoginData[0], savedLoginData[1]);
        }

        Button loginBtn = findViewById(R.id.enter_login);
        loginBtn.setOnClickListener(v -> {
            showLoginWindow();
        });

        Button regBtn = findViewById(R.id.enter_reg);
        regBtn.setOnClickListener(v -> {
            showRegisterWindow();
        });
    }


    private void logIn(String email,String passwd){
        loadingGIF.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email,passwd)
                .addOnSuccessListener(res -> {

                    usersBase.collection(Constants.USERS_BASE)
                            .whereEqualTo(Constants.USER_EMAIL,email)
                            .get()
                            .addOnCompleteListener( task -> {
                                if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                    try {
                                        HashMap<String, Object> hm = (HashMap<String, Object>) documentSnapshot.getData();
                                        System.out.println("Loaded==============\n"+hm+"\n===============");
                                        System.out.println(Store.auth);

                                        user = new User(hm);
                                        if(user.getDocID() == null){
                                            user.setDocID(documentSnapshot.getId());
                                        }
                                        user.saveLoginDataToFile(this);
                                        MainActivity.user = user;
                                        startActivity(new Intent(EnteringActivity.this,MainActivity.class));
                                        finish();
                                    }
                                    catch (NullPointerException ex){
                                        ex.printStackTrace();
                                        System.out.println("++++++++++++LOGINERROR+++++++++++++"+ex.getMessage());
                                        loadingGIF.setVisibility(View.GONE);
                                        throw new RuntimeException();
                                    }

                                }
                            }).addOnFailureListener( ex -> {
                                System.out.println("++++++++++++LOGINERROR2+++++++++++++"+ex.getMessage());
                                Toast.makeText(getApplicationContext(),"Не удалось получить данные о пользователе! "+ex.getMessage(),Toast.LENGTH_SHORT).show();
                                loadingGIF.setVisibility(View.GONE);
                                //throw new RuntimeException();
                            });
                })
                .addOnFailureListener(ex -> {
                    System.out.println("++++++++++++LOGINERROR3+++++++++++++"+ex.getMessage());
                    Toast.makeText(getApplicationContext(),"Ошибка авторизаци! "+ex.getMessage(),Toast.LENGTH_SHORT).show();
                    loadingGIF.setVisibility(View.GONE);
                });

    }
    private void showLoginWindow(){
        AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
        final View loginView = getLayoutInflater().inflate(R.layout.login_window,null);

        EditText emailET = loginView.findViewById(R.id.login_email);
        EditText passwdET = loginView.findViewById(R.id.login_password);

        ImageButton cancel = loginView.findViewById(R.id.login_cancelBtn);
        ImageButton add = loginView.findViewById(R.id.login_loginBtn);

        a_builder.setView(loginView);
        AlertDialog dialog = a_builder.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        add.setOnClickListener(v -> {
            String email = emailET.getText().toString();
            String passwd = passwdET.getText().toString();

            if( email.length()==0 || passwd.length() == 0) {
                Toast.makeText(getApplicationContext(),"Введите все данные!",Toast.LENGTH_SHORT).show();
                return;
            }
            logIn(email,passwd);
            dialog.hide();
        });
    }
    private void showRegisterWindow(){
        AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
        final View regestrationView = getLayoutInflater().inflate(R.layout.register_window,null);

        EditText nameET = regestrationView.findViewById(R.id.register_name);
        EditText emailET = regestrationView.findViewById(R.id.register_email);
        EditText passwdET = regestrationView.findViewById(R.id.register_passwd);
        EditText passwdConfirmET = regestrationView.findViewById(R.id.register_passwdConfirm);
        TextView birthDateTxt = regestrationView.findViewById(R.id.register_birthDateTxt);

        ImageButton birthDateBtn = regestrationView.findViewById(R.id.register_birthDateBtn);
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
                    birthDateTxt.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                }
            }, year, month, day);
            dpd.show();
        });

        ImageButton cancel = regestrationView.findViewById(R.id.regiser_cancelBtn);
        ImageButton add = regestrationView.findViewById(R.id.register_regBtn);

        a_builder.setView(regestrationView);
        AlertDialog dialog = a_builder.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        add.setOnClickListener(v -> {
            String email = emailET.getText().toString();
            String name = nameET.getText().toString();
            String passwd = passwdET.getText().toString();
            String passwdConfirm = passwdConfirmET.getText().toString();

            if(email.length()==0 ||passwd.length() == 0 || passwdConfirm.length() == 0 || name.length() == 0 || selectedDate[0]==null) {
                Toast.makeText(getApplicationContext(),"Введите все данные!",Toast.LENGTH_SHORT).show();
                return;
            }if(passwd.length() < 6) {
                Toast.makeText(getApplicationContext(), "Минимальная длина пароля - 6 символов!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!passwd.equals(passwdConfirm)) {
                Toast.makeText(getApplicationContext(), "Пароль и подтвержденный пароль отличаются!!", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email,passwd)
                    .addOnSuccessListener(res -> {
                        User user = new User(name,email,passwd,selectedDate[0]);
                        MainActivity.user=user;

                        usersBase.collection(Constants.USERS_BASE).add(user.getUserDoc())
                                .addOnSuccessListener( docRef -> {
                                    user.setDocID(docRef.getId());
                                    user.saveLoginDataToFile(this);

                                    Toast.makeText(this,"Успешно зарегистрирован",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(EnteringActivity.this,MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(ex -> {
                                    Toast.makeText(this,ex.getMessage(),Toast.LENGTH_SHORT).show();
                                    loadingGIF.setVisibility(View.GONE);
                                });
                        dialog.cancel();
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println(e.getMessage());
                            Toast.makeText(emailET.getContext().getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                            loadingGIF.setVisibility(View.GONE);
                        }
                    });

            //dialog.cancel();
            dialog.hide();
            loadingGIF.setVisibility(View.VISIBLE);
        });
    }
}