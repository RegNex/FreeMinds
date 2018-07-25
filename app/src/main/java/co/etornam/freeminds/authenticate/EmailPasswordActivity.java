package co.etornam.freeminds.authenticate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import co.etornam.freeminds.CompleteProfileActivity;
import co.etornam.freeminds.R;

public class EmailPasswordActivity extends AppCompatActivity {
EditText edtEmail,edtPassword,edtComfirmPass;
Button btnSignupEmail;
FirebaseAuth mAuth;
String TAG = "EmailPasswordActivity";
ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password);

        mAuth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtComfirmPass = findViewById(R.id.edtPassConfirm);
        progressDialog = new ProgressDialog(this);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
    }

    private void signup() {
        progressDialog.setMessage("Setting you up...");
        progressDialog.show();
        if (edtEmail.getText().toString().isEmpty()){
            edtEmail.setError("Enter your Email");
        }if (edtPassword.getText().toString().isEmpty()){
            edtPassword.setError("Enter Password");
        }if (edtComfirmPass.getText().toString().isEmpty()){
            edtComfirmPass.setError("Confirm Password");
        }if (!edtPassword.getText().toString().equals(edtComfirmPass.getText().toString())){
            edtComfirmPass.setError("Password does not Match");
            edtPassword.setError("Password does not Match");
        }if (!edtEmail.getText().toString().isEmpty() && !edtPassword.getText().toString()
                .isEmpty() && !edtComfirmPass.getText().toString().isEmpty()){
            final String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        progressDialog.dismiss();
                        startActivity(new Intent(getApplicationContext(),CompleteProfileActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "createUserWithEmail:failure", task.getException());
                        progressDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "Couldn't set you up",
                                Snackbar.LENGTH_LONG).setAction("OK", null).show();
                    }
                }
            });
        }
    }

    public void gotoLogin(View view) {
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }
}
