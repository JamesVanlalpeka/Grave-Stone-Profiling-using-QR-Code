package com.adbu.qrgrave;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class userProfileLandingActivity extends AppCompatActivity {
    TextView mFullName, mAddress, mDob, mDod, mUserProfileLandingGender;
    ImageView mProfilePic;

    private Button gotoUserLandingImageViewBtn;
    private ProgressBar userProfileLandingProgressBar;

//    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String UID;

    // instance for firebase storage and StorageReference
    StorageReference storageReference;
    FirebaseStorage storage;

    public static TextView scanText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_landing);

        mFullName = findViewById(R.id.profileLandingFullName);
        mAddress = findViewById(R.id.profileLandingAddress);
        mDob = findViewById(R.id.profileLandingDateOfBirth);
        mDod = findViewById(R.id.profileLandingDateOfDeath);
        mProfilePic = findViewById(R.id.profileLandingProfilePic);
        gotoUserLandingImageViewBtn = findViewById(R.id.userProfileLandingGotoImageListBtn);
        userProfileLandingProgressBar = findViewById(R.id.userProfileLandingProgressBar);
        mUserProfileLandingGender = findViewById(R.id.profileLandingGender);


        fStore = FirebaseFirestore.getInstance();
        UID = getIntent().getStringExtra("userID");


        gotoUserLandingImageViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(userProfileLandingActivity.this, userProfileLandingImageListView.class);
                intent.putExtra("userID", UID.toString());
                startActivity(intent);
            }
        });


        try{
            DocumentReference docRef = fStore.collection("users").document(UID);
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
                            mUserProfileLandingGender.setText(document.getString("gender"));
                        }

                    }
                }
            });

        }catch (Exception e){
            startActivity(new Intent(getApplicationContext(),FirstActivity.class));
            Toast.makeText(this,"User Does not Exist", Toast.LENGTH_SHORT).show();

        }

        StorageReference imagesRef = FirebaseStorage.getInstance().getReference("users/"+UID+"profile.jpg");
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(mProfilePic);
                userProfileLandingProgressBar.setVisibility(View.INVISIBLE);
            }

        });


    }
}