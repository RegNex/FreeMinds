package co.etornam.freeminds;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import co.etornam.freeminds.authenticate.LoginActivity;
import co.etornam.freeminds.authenticate.SignupActivity;
import id.zelory.compressor.Compressor;

public class CompleteProfileActivity extends AppCompatActivity {
    private static final String TAG = "CompleteProfileActivity";
    private EditText edtUser,edtPhone,edtAddress;
private RadioButton maleBtn,femaleBtn;
private RadioGroup radioGroup;
private Button continueBtn;
private ImageButton imgSelector;
private FirebaseAuth mAuth;
private FirebaseUser mUser;
private FirebaseFirestore mDatabase;
private StorageReference mStorage,imageRef;
private Uri resultUri = null;
private ProgressDialog mProgressDialog;
private String uid;
private Bitmap compressedImageFile;
    Task<Uri> urlTask,thumbTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);
        mProgressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mDatabase.setFirestoreSettings(settings);
        edtUser = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        maleBtn = findViewById(R.id.radioMale);
        radioGroup = findViewById(R.id.radioGrp);
        femaleBtn = findViewById(R.id.radioFemale);
        imgSelector = findViewById(R.id.userImg);
        continueBtn = findViewById(R.id.btnComplete);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton gender = group.findViewById(checkedId);
                if (null != gender && checkedId > -1) {
                    //  Toast.makeText(ProfileActivity.this, diabeticRb.getText(), Toast
                    //        .LENGTH_SHORT).show();
                }
            }
        });

        imgSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(CompleteProfileActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(CompleteProfileActivity.this, "Permission Denied!", Toast
                                .LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(CompleteProfileActivity.this, new String[]
                                {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(CompleteProfileActivity.this);
                    }
                }else{
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(CompleteProfileActivity.this);
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = edtUser.getText().toString();
                final String phone = edtPhone.getText().toString();
                final String address = edtAddress.getText().toString();
                final RadioButton gender = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                final String mGender = gender.getText().toString();
                if (!username.isEmpty() && resultUri != null) {
                    mProgressDialog.setMessage("Completing...");
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    imageRef = mStorage.child("profile_images").child("original").child(uid + ".jpg");


                    UploadTask uploadTask = imageRef.putFile(resultUri);
                    urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return imageRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                File newImageFile = new File(Objects.requireNonNull(resultUri.getPath()));
                                try {
                                    compressedImageFile = new Compressor(CompleteProfileActivity.this)
                                            .setMaxHeight(50)
                                            .setMaxWidth(50)
                                            .setQuality(2)
                                            .compressToBitmap(newImageFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 2, byteArrayOutputStream);
                                byte[] data = byteArrayOutputStream.toByteArray();

                                UploadTask thumbUploadTask = mStorage.child("profile_images").child("thumb").child(uid + ".jpg").putBytes(data);
                                thumbTask = thumbUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }
                                        // Continue with the task to get the download URL
                                        return imageRef.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()){

                                            Uri downloadThumbUri = task.getResult();
                                            Map<String, Object> mUserUpload = new HashMap<>();
                                            mUserUpload.put("imageUrl",downloadThumbUri.toString());
                                            mUserUpload.put("username",username);
                                            mUserUpload.put("phone",phone);
                                            mUserUpload.put("gender",mGender);
                                            mUserUpload.put("address",address);
                                            mUserUpload.put("timeStamp", FieldValue.serverTimestamp());
                                            mDatabase.collection("Users").document(uid).set(mUserUpload)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(CompleteProfileActivity.this, "All set. Welcome "+username, Toast
                                                                        .LENGTH_SHORT).show();

                                                            }else{
                                                                Toast.makeText(CompleteProfileActivity.this, "Something went wrong. Try again!", Toast.LENGTH_SHORT).show();
                                                                Log.d(TAG, "onComplete: "+task.getException().getMessage());
                                                            }
                                                        }
                                                    });
                                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                            finish();
                                        }else{
                                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                        }
                                        mProgressDialog.dismiss();
                                    }
                                });
                            } else {
                                 Toast.makeText(CompleteProfileActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            mProgressDialog.dismiss();
                        }
                    });
                }else{
                        edtUser.setError("Cannot be Empty");
                    Toast.makeText(CompleteProfileActivity.this, "You must select a picture", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                imgSelector.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "onActivityResult: "+error);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null){
            startActivity(new Intent(getApplicationContext(), SignupActivity.class));
            finish();
        }else{
            uid = mAuth.getCurrentUser().getUid();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }
}
