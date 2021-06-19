package com.example.bakery_lust;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bakery_lust.User.User;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    EditText name, email, password;
    Button register, google;
    public String Id = "";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 10;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        onRequest();

        //Initializing Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");

        //adding id to the code from UI
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        google = findViewById(R.id.google);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInViaGoogle();
            }
        });
    }
/*
********************************************************
********************************************************
        HERE, REGISTRATION VIA GOOGLE STARTS
********************************************************
********************************************************
 */
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
                            SignUpActivity.this.finish();
                        }else {
                            Toast.makeText(SignUpActivity.this, "Failed to SignIn", Toast.LENGTH_SHORT).show();
                            Log.i("error in task", task.toString());
                        }

                    }
                });
    }

    /*
********************************************************
********************************************************
        HERE, REGISTRATION VIA GOOGLE ENDS
********************************************************
********************************************************
 */


/*
********************************************************
********************************************************
        HERE, REGISTRATION VIA EMAIL/PASSWORD STARTS
********************************************************
********************************************************
 */
    private void Register(){
        String name_of_user = name.getText().toString();
        String email_of_user = email.getText().toString();
        String password_of_user = password.getText().toString();

        if (!isValidEmail(email_of_user)){
            email.setError("Enter your email.");
        }else if (TextUtils.isEmpty(name_of_user)){
            email.setError("Enter your name.");
        }else if (TextUtils.isEmpty(password_of_user)){
            email.setError("Enter your password.");
        }else if (password_of_user.length()<6){
            email.setError("Enter a password greater than 6 characters");
        }

        //Makes an object of a new user


        mAuth.createUserWithEmailAndPassword(email_of_user, password_of_user).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(SignUpActivity.this, "Successfully registered", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    Intent in = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(in);
                    finish();
                }else {
                    Toast.makeText(SignUpActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Email Validation
    private boolean isValidEmail(CharSequence email){
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    //to redirect it to login page on back pressed.
    @Override
    public void onBackPressed() {
        this.finish();
        Intent in = new Intent(this, LoginActivity.class);
        startActivity(in);
    }
}