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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.nihaskalam.progressbuttonlibrary.CircularProgressButton;

import co.etornam.freeminds.CompleteProfileActivity;
import co.etornam.freeminds.MainActivity;
import co.etornam.freeminds.R;

public class LoginActivity extends AppCompatActivity {
    EditText edtEmail,edtPassword;
    Button btnLoginEmail;
    FirebaseAuth mAuth;
    String TAG = "EmailPasswordActivity";
ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edtEmailL);
        edtPassword = findViewById(R.id.edtPasswordL);
        btnLoginEmail = findViewById(R.id.loginBtn);
        btnLoginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    logInUser();

            }
        });
    }

    private void logInUser() {
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        if (edtEmail.getText().toString().isEmpty()){
            edtEmail.setError("Enter your Email");
        }if (edtPassword.getText().toString().isEmpty()){
            edtPassword.setError("Enter Password");
        }if (!edtEmail.getText().toString().isEmpty() && !edtPassword.getText().toString()
                .isEmpty()) {
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                progressDialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.d(TAG, "signInWithEmail:failure", task.getException());
                                progressDialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content), "Couldn't Log you in",
                                        Snackbar.LENGTH_LONG).setAction("OK", null).show();
                            }
                        }
                    });
        }

    }

    public void gotoSignup(View view) {
        startActivity(new Intent(getApplicationContext(),EmailPasswordActivity.class));
        finish();
    }
}
