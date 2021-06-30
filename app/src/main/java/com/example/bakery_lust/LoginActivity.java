package com.example.bakery_lust;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {
    private Button login, signup, google;
    private TextView forgot_password;
    private EditText email, password;

    private String nameDb, emailDb, phoneNoDb, addressDb, profile_imageDb;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser user;
    private DatabaseReference googleUsersReference;
    private DatabaseReference emailUsersReference;
    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        onRequest();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        googleUsersReference = database.getReference("Google");
        emailUsersReference = database.getReference("Email");

        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup);
        google = findViewById(R.id.google);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        forgot_password = findViewById(R.id.forgot_password);

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidEmail(email.toString())) {
                    email.setError("Enter your email.");
                } else if (TextUtils.isEmpty(password.toString())) {
                    email.setError("Enter your password.");
                } else if (password.toString().length() < 6) {
                    email.setError("Enter a password greater than 6 characters");
                } else {
                    loginViaEmail();
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(in);
                LoginActivity.this.finish();
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInViaGoogle();
            }
        });

    }

    //request to google
    private void onRequest() {
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
        //Result returned from launching GoogleSignInApi.getSignInIntent...
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google sign in was successful and then authenticate with firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                //Check for user in our database without password because it is authenticated by google.
                Query checkUser = googleUsersReference.orderByChild("email").equalTo(account.getEmail());

                checkUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            nameDb = snapshot.child(uniqueID(account.getEmail())).child("name").getValue(String.class);
                            emailDb = snapshot.child(uniqueID(account.getEmail())).child("email").getValue(String.class);
                            phoneNoDb = snapshot.child(uniqueID(account.getEmail())).child("phoneNo").getValue(String.class);
                            addressDb = snapshot.child(uniqueID(account.getEmail())).child("address").getValue(String.class);
                            profile_imageDb = snapshot.child(uniqueID(account.getEmail())).child("profile_image").getValue(String.class);

                            SharedPreferences.Editor editor = getSharedPreferences("Details", MODE_PRIVATE).edit();
                            editor.putString("name", nameDb);
                            editor.putString("email", emailDb);
                            editor.putString("phoneNo", phoneNoDb);
                            editor.putString("address", addressDb);
                            editor.putString("profile_image", profile_imageDb);
                            editor.apply();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Log.i("IDToken", account.getIdToken());
            } catch (ApiException e) {
                Log.i("Error in loading task", e.getMessage());
            }
        }
    }

    //check for existence of a particular user in our database.
    private void loginViaEmail() {
        Query checkUser = emailUsersReference.orderByChild("email").equalTo(email.getText().toString());


        checkUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String passwordFromDb = snapshot.child(uniqueID(email.getText().toString())).child("password").getValue(String.class);

                    if (passwordFromDb.equals(MD5hash(password.getText().toString()))) {
                        nameDb = snapshot.child(uniqueID(email.getText().toString())).child("name").getValue(String.class);
                        emailDb = snapshot.child(uniqueID(email.getText().toString())).child("email").getValue(String.class);
                        phoneNoDb = snapshot.child(uniqueID(email.getText().toString())).child("phoneNo").getValue(String.class);
                        addressDb = snapshot.child(uniqueID(email.getText().toString())).child("address").getValue(String.class);
                        profile_imageDb = snapshot.child(uniqueID(email.getText().toString())).child("profile_image").getValue(String.class);

                        SharedPreferences.Editor editor = getSharedPreferences("Details", MODE_PRIVATE).edit();
                        editor.putString("name", nameDb);
                        editor.putString("email", emailDb);
                        editor.putString("phoneNo", phoneNoDb);
                        editor.putString("address", addressDb);
                        editor.putString("profile_image", profile_imageDb);
                        editor.apply();
                        mAuth.signInWithEmailAndPassword(email.toString(), password.toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.i("success", "Yes");
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                    } else {
                        Toast.makeText(LoginActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Username dose'nt exist!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Email Validation
    private boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    /*
    ####################################################################################################
     */
    //create a unique email substring and use it as an ID for each user which will also be easy to access about a particular user.
    private String uniqueID(String email) {
        if (email == null) {
            return "";
        } else {
            return email.substring(0, email.lastIndexOf("@"));
        }
    }
/*
####################################################################################################
 */

    //provide password encryption, do encryption to the password input and then compare it with database.
    private static String MD5hash(String password) {
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an password digest() return array of byte
            byte[] messageDigest = md.digest(password.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            StringBuilder hashtext = new StringBuilder(no.toString(16));
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    //Forgot Password
    private void forgotPassword() {
        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}