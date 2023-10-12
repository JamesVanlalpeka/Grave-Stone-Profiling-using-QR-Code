package com.adbu.qrgrave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
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

public class uploadPhoto extends AppCompatActivity {

    ImageView mUploadPhotoImage;
    TextView mUploadPhotoCaption;
    Button mUploadPhotoUploadBtn;
    ProgressBar mUploadPhotoProgressBar;

    final int PICK_IMAGE_CODE = 12;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId, uploadImageUri;

//    realtime database
    DatabaseReference mDatabaseRef;

    // instance for firebase storage and StorageReference
    StorageReference storageReference;
    FirebaseStorage storage;

    StorageTask mUploadTask;

    String caption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

//firebase storage
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

//        realtime database
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(userId);

// get the Firebase storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mUploadPhotoCaption = findViewById(R.id.uploadPhotoCaption);
        mUploadPhotoImage = findViewById(R.id.uploadPhotoImage);
        mUploadPhotoUploadBtn = findViewById(R.id.uploadPhotoUploadBtn);
        mUploadPhotoProgressBar = findViewById(R.id.uploadPhotoProgressBar);

//Setting image uri from another activity to this activity
        Bundle b = getIntent().getExtras();

        String uri_Str= b.getString("imageUri");
        Uri imageUri = Uri.parse(uri_Str);

        mUploadPhotoImage.setImageURI(imageUri);

        mUploadPhotoUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                caption = mUploadPhotoCaption.getText().toString();

                if(TextUtils.isEmpty(caption)){
                    mUploadPhotoCaption.setError("Caption is Required");
                }else{
                    if(mUploadTask != null && mUploadTask.isInProgress()) {
                        Toast.makeText(uploadPhoto.this,"Upload in progress", Toast.LENGTH_SHORT).show();
                    } else{
                        uploadImageToFirebase(imageUri);
                    }
                }
            }
        });

    }


    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

//upload image to firebase storage
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null){
            StorageReference imagesRef = storageReference.child(userId+"/"+System.currentTimeMillis());

            mUploadTask= imagesRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mUploadPhotoProgressBar.setProgress(0);
                                }
                            },500);

                            Picasso.get().load(uri).into(mUploadPhotoImage);
                        }
                    });

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    Upload upload = new Upload(mUploadPhotoCaption.getText().toString().trim(),downloadUrl.toString());

                    String uploadId = mDatabaseRef.push().getKey();
                    mDatabaseRef.child(uploadId).setValue(upload);

                    Toast.makeText(uploadPhoto.this,"Image Upload Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),PhotoListDisplay.class));
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(uploadPhoto.this,"Image Upload Failed",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 + snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    mUploadPhotoProgressBar.setProgress((int) progress);

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
            startActivity(new Intent(getApplicationContext(),PhotoListDisplay.class));
            finish();

        }
        pressedTime = System.currentTimeMillis();
    }
}