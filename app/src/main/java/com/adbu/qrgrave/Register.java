package com.adbu.qrgrave;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mFullName, mEmail, mPassword, mConfirmPassword;
    private Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mRegisterBtn = findViewById(R.id.gotoUploadProfileInfoPage);
        mLoginBtn = findViewById(R.id.gotoLoginPage);
        mConfirmPassword = findViewById(R.id.registerConfirmPassword);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String fullName = mFullName.getText().toString();
                String confirmPassword = mConfirmPassword.getText().toString();


                if(TextUtils.isEmpty(fullName)) {
                    mFullName.setError("Full Name is Required");
                    return;
                }
                if(TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required");
                    return;
                }
                if(TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required");
                    return;
                }
                if(TextUtils.isEmpty(confirmPassword)) {
                    mConfirmPassword.setError("Confirm Password is Required");
                    return;
                }
                if(!password.equals(confirmPassword)) {
                    mConfirmPassword.setError("Entry not same with Password");
                    return;
                }
                if(password.length() < 6) {
                    mPassword.setError("Password must be more than 6 characters");
                    return;
                }

                Intent intent = new Intent(Register.this, uploadProfileInfo.class);
                intent.putExtra("fullName", fullName);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                startActivity(intent);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
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
            startActivity(new Intent(getApplicationContext(),FirstActivity.class));
            finish();

        }
        pressedTime = System.currentTimeMillis();
    }
}