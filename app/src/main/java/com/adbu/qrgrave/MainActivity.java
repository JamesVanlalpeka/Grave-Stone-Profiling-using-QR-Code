package com.adbu.qrgrave;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {
    TextView mFullName, mAddress, mDob, mDod, mGender;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    ImageView mProfilePic;
    private Button mMainMenuGotoPhotoListDisplay;

//    navigation
    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;

//    dialog qr code popup
    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userId = fAuth.getCurrentUser().getUid();

        mFullName = findViewById(R.id.mainFullName);
        mAddress = findViewById(R.id.mainAddress);
        mDob = findViewById(R.id.mainDateOfBirth);
        mDod = findViewById(R.id.mainDateOfDeath);
        mProfilePic = findViewById(R.id.mainProfilePic);
        mMainMenuGotoPhotoListDisplay = findViewById(R.id.mainGotoPhotoBtn);
        mGender = findViewById(R.id.mainGender);

        mMainMenuGotoPhotoListDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), PhotoListDisplay.class));
                finish();
            }
        });

//        qr code dialog popup
        myDialog = new Dialog(this);


//        navigation
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nav=(NavigationView)findViewById(R.id.navmenu);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer);

        toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_qr_code: //when qr code generator is click
                        userId = fAuth.getCurrentUser().getUid();

                        Intent intent = new Intent(MainActivity.this, QRgenerator.class);
                        intent.putExtra("userID", userId);
                        startActivity(intent);
                        break;

                    case R.id.menu_logout:  //when logout is click
                        //logout function
                        FirebaseAuth.getInstance().signOut(); //logout
                        startActivity(new Intent(getApplicationContext(),FirstActivity.class)); //send back to login activity
                        finishAffinity();
                        break;

                    case R.id.menu_edit_profile: //when edit profile is click
                        startActivity(new Intent(getApplicationContext(),EditProfile.class)); //send back to login activity
                        finish();
                        break;

                    case R.id.menu_delete_user:
                        startActivity(new Intent(getApplicationContext(),DeleteActivity.class));
                        finish();
                        break;
                }
                return true;
            }
        });

        DocumentReference docRef = fStore.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        mFullName.setText(document.getString("fName"));
                        mAddress.setText(document.getString("address"));
                        mDob.setText(document.getString("dob"));
                        mDod.setText(document.getString("dod"));
                        mGender.setText(document.getString("gender"));
                    }
                }
            }
        });

        StorageReference imagesRef = FirebaseStorage.getInstance().getReference("users/"+userId+"profile.jpg");
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(mProfilePic);
            }
        });
    }

    //    on back key press
    private long pressedTime;
    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            FirebaseAuth.getInstance().signOut(); //logout
            startActivity(new Intent(getApplicationContext(),FirstActivity.class)); //send back to login activity
            finishAffinity();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to Logout", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

}