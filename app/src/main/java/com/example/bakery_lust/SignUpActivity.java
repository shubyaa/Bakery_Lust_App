package com.example.bakery_lust;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity {

    Button SignUp, google, facebook;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        onRequest();

        //Initializing Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //adding id to the code from UI
        SignUp = findViewById(R.id.signup);
        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);


        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInViaGoogle();
            }
        });
    }

    // Check requests to google client
    private void onRequest(){
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    //SignIn function
    private void signInViaGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result retured from launching GoogleSignInApi.getSignInIntent...
        if (requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google sign in was successful and then authenticate with firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Log.i("IDToken", account.getIdToken());
            } catch (ApiException e) {
                Log.i("Error in loading task", e.getMessage());
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //SignIn Success
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent in = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(in);

                        }else {
                            Toast.makeText(SignUpActivity.this, "Failed to SignIn", Toast.LENGTH_SHORT).show();
                            Log.i("error in task", task.toString());
                        }

                    }
                });
    }

    //to redirect it to login page on back pressed.
    @Override
    public void onBackPressed() {
        this.finish();
        Intent in = new Intent(this, LoginActivity.class);
        startActivity(in);
    }
}