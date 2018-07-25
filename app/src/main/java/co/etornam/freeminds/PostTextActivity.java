package co.etornam.freeminds;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import co.etornam.freeminds.authenticate.SignupActivity;

public class PostTextActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    ProgressDialog mProgressDialog;
    EditText editText;
    TextView txtCount;
    Button btnSubmit;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_text);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mProgressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        editText = findViewById(R.id.edtPost);
        btnSubmit = findViewById(R.id.btnSubmit);
        txtCount = findViewById(R.id.txtCount);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Date date = new Date();
                final SimpleDateFormat dateFt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                if (!TextUtils.isEmpty(editText.getText())){
                    Map<String, Object> objectMap =  new HashMap<>();
                    objectMap.put("postText",editText.getText().toString());
                    objectMap.put("current_userId",firebaseUser.getUid());
                    objectMap.put("timeStamp", dateFt.format(date));
                    objectMap.put("thumbnail","");
                    objectMap.put("datePosted",FieldValue.serverTimestamp());
                    mFirestore.collection("Posts").document().set(objectMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()){
                                   Toast.makeText(PostTextActivity.this, "Post Submitted!", Toast.LENGTH_SHORT).show();
                                   startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                   finish();
                               }else{
                                   Toast.makeText(PostTextActivity.this, "Couldn't Submit Post", Toast.LENGTH_SHORT).show();
                               }
                                }
                            });
                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()/140 == 0){
                    txtCount.setText("140");
                    txtCount.setTextColor(getResources().getColor(R.color.lightGreen));
                }else{
                    txtCount.setText("140/"+String.valueOf((s.length()/140)+1));
                    txtCount.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser == null){
            startActivity(new Intent(getApplicationContext(),SignupActivity.class));
            finish();
        }
    }
}
