package co.etornam.freeminds.authenticate;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

import co.etornam.freeminds.CompleteProfileActivity;
import co.etornam.freeminds.MainActivity;
import co.etornam.freeminds.R;

public class SignupActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    FirebaseAuth mAuth;
    private static final String PATH_TOS = "https://developerbryte.wordpress.com/portfolio/";
    FirebaseFirestore mDatabase,mUser;
    String TAG = "SignUpActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();

    }

    public void googleLogin(View view) {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setTosUrl(PATH_TOS)
                .build(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                loginUser();
            }
            if(resultCode == RESULT_CANCELED){
                Snackbar.make(findViewById(android.R.id.content), "Process Cancelled", Snackbar
                        .LENGTH_LONG)
                        .setAction("OK", null).show();
            }
            return;
        }
        Log.d(TAG, "onActivityResult: "+resultCode);
    }

    private void loginUser() {
        Intent loginIntent = new Intent(getApplicationContext(), CompleteProfileActivity.class);
        startActivity(loginIntent);
        finish();
    }

    //email password login
    public void emailPasswordLogin(View view) {
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }


    //facebook login
    public void facebookLogin(View view) {
        Toast.makeText(this, "feature not yet done", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initAuth();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initAuth();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initAuth();
    }

    private void initAuth() {
        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
    }
}
