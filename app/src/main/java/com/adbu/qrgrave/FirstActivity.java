package com.adbu.qrgrave;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FirstActivity extends AppCompatActivity {
    private TextView gotoSignUp;
    private Button gotoLogin, gotoQrScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        gotoLogin = findViewById(R.id.firstActivityGotoLogin);
        gotoSignUp = findViewById(R.id.firstActivityGotoSignUp);
        gotoQrScanner = findViewById(R.id.firstActivityScanQrCodeBtn);

//go to qr code scanner
        gotoQrScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), QRscanner.class));
                finish();
            }
        });

//go to login activity
        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

//goto register activity
        gotoSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Register.class));
                finish();
            }
        });

    }
}