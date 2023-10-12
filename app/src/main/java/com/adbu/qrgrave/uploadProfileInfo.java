package com.adbu.qrgrave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class uploadProfileInfo extends AppCompatActivity {

    TextView mDateOfBirth, mDateOfDeath, mAddress;
    DatePickerDialog.OnDateSetListener mDateSetListener, mDateOfDeathListener;

    private RadioGroup mRadioGroup;
    private RadioButton mRadioButton;

    private static final String TAG = "TAG";
    String email,fullName,password;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private Button mRegisterProfileBtn;
    String userID;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_info);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        fullName = getIntent().getStringExtra("fullName");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        progressBar = findViewById(R.id.uploadProfileInfoProgressBar);

        mAddress = findViewById(R.id.uploadProfileInfoAddress);
        mDateOfBirth = findViewById(R.id.dateOfBirth);
        mDateOfDeath = findViewById(R.id.dateOfDeath);

        mRegisterProfileBtn = findViewById(R.id.RegisterProfileBtn);
        mRadioGroup = findViewById(R.id.upload_profile_info_gender);

        mDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(uploadProfileInfo.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = month+ "/" +day+ "/" +year;
                mDateOfBirth.setText(date);
            }
        };

        mDateOfDeath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(uploadProfileInfo.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateOfDeathListener, year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateOfDeathListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = month+ "/" +day+ "/" +year;
                mDateOfDeath.setText(date);
            }
        };

        mRegisterProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radioID = mRadioGroup.getCheckedRadioButtonId();
                mRadioButton = findViewById(radioID);

                String fGender = mRadioButton.getText().toString();
                String fDateOfBirth = mDateOfBirth.getText().toString();
                String fDateOfDeath = mDateOfDeath.getText().toString();
                String fAddress = mAddress.getText().toString();

//              register the user in firebase and upload data in firebase
                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(uploadProfileInfo.this,"User Created", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection( "users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("fName",fullName);
                            user.put("email",email);
                            user.put("dob",fDateOfBirth);
                            user.put("dod",fDateOfDeath);
                            user.put("address",fAddress);
                            user.put("gender",fGender);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "OnSuccess: user Profile is created for "+ userID);
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),EmailVerificationActivity.class));
                        }
                        else {
                            Toast.makeText( uploadProfileInfo.this, "Error " +task.getException().getMessage(), Toast.LENGTH_SHORT).show();                        }
                            progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    //    on back key press
    private long pressedTime;
    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            startActivity(new Intent(getApplicationContext(),Register.class));
            finish();
        }
        pressedTime = System.currentTimeMillis();
    }
}