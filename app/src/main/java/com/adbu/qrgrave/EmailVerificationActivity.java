package com.adbu.qrgrave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class EmailVerificationActivity extends AppCompatActivity {
    Button mEmailVerficationBtn;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        fAuth = FirebaseAuth.getInstance();

        mEmailVerficationBtn = findViewById(R.id.emailVerificationBtn);

        if(fAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
        else{
            mEmailVerficationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    fAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(EmailVerificationActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();

                            }else{
                                Toast.makeText(EmailVerificationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }
                            startActivity(new Intent(getApplicationContext(),FirstActivity.class));
                            finish();
                        }
                    });
                }
            });
        }
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