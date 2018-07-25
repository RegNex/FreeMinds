package co.etornam.freeminds;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import co.etornam.freeminds.authenticate.SignupActivity;
import id.zelory.compressor.Compressor;

public class PictureBlogActivity extends AppCompatActivity {
    private static final String TAG = "PictureBlog";
    ImageView mImage;
EditText imgDesc;
FirebaseAuth mAuth;
FirebaseFirestore mFirestore;
StorageReference mStorage;
String uid;
    Uri resultUri = null;
    StorageReference imagesRef;
    ProgressDialog mProgressDialog;
    Bitmap compressedImageFile;
    Task<Uri> urlTask,thumbTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_blog);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mProgressDialog = new ProgressDialog(this);
        mImage = findViewById(R.id.imgView);
        imgDesc = findViewById(R.id.imgDesc);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();


        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(PictureBlogActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(PictureBlogActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(PictureBlogActivity.this, new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }else{
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(PictureBlogActivity.this);
                }
            }else{
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(PictureBlogActivity.this);
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
                mImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "onActivityResult: "+error);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_post)
            postImage();
        return super.onOptionsItemSelected(item);
    }

    //post new Image
    private void postImage() {
        final Date date = new Date();
        final SimpleDateFormat dateFt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault());
        final String imageDesc = imgDesc.getText().toString();
        if (!imageDesc.isEmpty() && resultUri != null){
            mProgressDialog.setMessage("Uploading...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            final String random = UUID.randomUUID().toString();
            imagesRef = mStorage.child("post_images").child("original").child(random + ".jpg");

            UploadTask uploadTask = imagesRef.putFile(resultUri);
            urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return imagesRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull final Task<Uri> task) {
                    final Uri downloadUrl = task.getResult();
                    if (task.isSuccessful()) {
                        File newImageFile = new File(Objects.requireNonNull(resultUri.getPath()));
                        try {
                            compressedImageFile = new Compressor(PictureBlogActivity.this)
                                    .setMaxHeight(100)
                                    .setMaxWidth(100)
                                    .setQuality(2)
                                    .compressToBitmap(newImageFile);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 2, byteArrayOutputStream);
                        byte[] data = byteArrayOutputStream.toByteArray();

                        UploadTask thumbUploadTask = mStorage.child("post_images").child("thumb").child(random + ".jpg").putBytes(data);
                        thumbTask = thumbUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                // Continue with the task to get the download URL
                                return imagesRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    Uri downloadThumbUri = task.getResult();
                                    Map<String, Object> mPostUpload = new HashMap<>();
                                    mPostUpload.put("imageUrl",downloadUrl.toString());
                                    mPostUpload.put("thumbnail",downloadThumbUri.toString());
                                    mPostUpload.put("imgDesc",imageDesc);
                                    mPostUpload.put("current_userId",uid);
                                    mPostUpload.put("timeStamp", dateFt.format(date));
                                    mPostUpload.put("datePosted",FieldValue.serverTimestamp());


                                    mFirestore.collection("Posts").document().set(mPostUpload)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(PictureBlogActivity.this, "Post Added!", Toast
                                                                .LENGTH_SHORT).show();
                                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                        finish();
                                                    }else{
                                                        Toast.makeText(PictureBlogActivity.this, "Something went " +
                                                                "wrong. Try again!", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "onComplete: "+task.getException().getMessage());
                                                    }
                                                }
                                            });
                                }else{
                                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                }
                                mProgressDialog.dismiss();
                            }
                        });

                    } else {
                        Toast.makeText(PictureBlogActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        initAuth();
    }

    private void initAuth() {
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
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}
