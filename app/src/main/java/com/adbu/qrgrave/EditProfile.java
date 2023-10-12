package com.adbu.qrgrave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    DatePickerDialog.OnDateSetListener mDateSetListener, mDateOfDeathListener;

    TextView fullName, address, dob, dod;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    private Button saveBtn;
    ImageView profileImageView;
    final int PICK_IMAGE_CODE = 12;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioButton;
    private String genderValue;

    // instance for firebase storage and StorageReference
    StorageReference storageReference;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

//firebase storage
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();


        fullName = findViewById(R.id.editProfileFullName);
        address = findViewById(R.id.editProfileAddress);
        dob = findViewById(R.id.editProfileDateOfBirth);
        dod = findViewById(R.id.editProfileDateOfDeath);
        saveBtn = findViewById(R.id.editProfileSaveBtn);
        profileImageView = findViewById(R.id.profileImageView);
        mRadioGroup = findViewById(R.id.editProfileGender);

        DocumentReference docRef = fStore.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        fullName.setText(document.getString("fName"));
                        dob.setText(document.getString("dob"));
                        dod.setText(document.getString("dod"));
                        address.setText(document.getString("address"));
                        genderValue = document.getString("gender");


                    }
                }
            }
        });

// get the Firebase storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        StorageReference imagesRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "profile.jpg");
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImageView);
            }
        });


        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(EditProfile.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = month + "/" + day + "/" + year;
                dob.setText(date);
            }
        };


        dod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(EditProfile.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateOfDeathListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateOfDeathListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = month + "/" + day + "/" + year;
                dod.setText(date);
            }
        };

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkP();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fullName.getText().toString().isEmpty() || address.getText().toString().isEmpty() || dob.getText().toString().isEmpty() || dod.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfile.this, "One or Many fields are empty,", Toast.LENGTH_SHORT).show();
                    return;
                }

                int radioID = mRadioGroup.getCheckedRadioButtonId();
                mRadioButton = findViewById(radioID);

                if (mRadioButton != null) {

                    Log.d("hello", mRadioButton.getText().toString());

                    String fFullName = fullName.getText().toString();
                    String fAddress = address.getText().toString();
                    String fDob = dob.getText().toString();
                    String fDod = dod.getText().toString();
                    String fGender = mRadioButton.getText().toString();

                    userId = fAuth.getCurrentUser().getUid();

                    DocumentReference documentReference = fStore.collection( "users").document(userId);


                    Map<String,Object> updateUserData = new HashMap<>();
                    updateUserData.put("fName",fFullName);
                    updateUserData.put("dob",fDob);
                    updateUserData.put("dod",fDod);
                    updateUserData.put("address",fAddress);
                    updateUserData.put("gender",fGender);
                    documentReference.update(updateUserData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(EditProfile.this, "Profile Update Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfile.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{

                    String fGender = genderValue;
                    String fFullName = fullName.getText().toString();
                    String fAddress = address.getText().toString();
                    String fDob = dob.getText().toString();
                    String fDod = dod.getText().toString();
                    //    String fGender = mRadioButton.getText().toString();

                    userId = fAuth.getCurrentUser().getUid();


                    DocumentReference documentReference = fStore.collection( "users").document(userId);


                    Map<String,Object> updateUserData = new HashMap<>();
                    updateUserData.put("fName",fFullName);
                    updateUserData.put("dob",fDob);
                    updateUserData.put("dod",fDod);
                    updateUserData.put("address",fAddress);
                    updateUserData.put("gender",fGender);
                    documentReference.update(updateUserData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(EditProfile.this, "Profile Update Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfile.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

    }

//crop image
    private void checkP() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Intent intent = new Intent();
//                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, PICK_IMAGE_CODE);
//                        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."),PICK_IMAGE_CODE);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
            if(data!=null) {
                resizeImage(data.getData());

            }
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri imageUri = result.getUri();
//            profileImageView.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null){

            StorageReference imagesRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"profile.jpg");
            imagesRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(EditProfile.this,"Image Upload Successfully", Toast.LENGTH_SHORT).show();
                    imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(profileImageView);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfile.this,"Image Upload Failed",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void resizeImage(Uri data) {
        CropImage.activity(data)
                .setMultiTouchEnabled(true)
                .setAspectRatio(1,1)
                .setMaxCropResultSize(5000,5000)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setOutputCompressQuality(50)
                .start(this);
    }

    //    on back key press
    private long pressedTime;
    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();

        }
        pressedTime = System.currentTimeMillis();
    }

}