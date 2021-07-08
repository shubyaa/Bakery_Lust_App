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
import android.widget.Toast;

import com.example.bakery_lust.User.EmailUser;
import com.example.bakery_lust.User.GoogleUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity {

    EditText name, email, password;
    Button register, google;
    private String Id = "";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 10;
    private FirebaseDatabase database;
    private DatabaseReference googleUsersReference;
    private DatabaseReference emailUsersReference;
    private String name_of_user;
    private String email_of_user;
    private String password_of_user;

    GoogleUser googleUser;
    EmailUser emailUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        onRequest();

        //Initializing Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        googleUsersReference = database.getReference("Google");
        emailUsersReference = database.getReference("Email");

        //adding id to the code from UI
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        google = findViewById(R.id.google);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name_of_user = name.getText().toString();
                email_of_user = email.getText().toString();
                password_of_user = password.getText().toString();

                if (!isValidEmail(email_of_user)) {
                    email.setError("Enter your email.");
                } else if (TextUtils.isEmpty(name_of_user)) {
                    email.setError("Enter your name.");
                } else if (TextUtils.isEmpty(password_of_user)) {
                    email.setError("Enter your password.");
                } else if (password_of_user.length() < 6) {
                    email.setError("Enter a password greater than 6 characters");
                }

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

        //Result retured from launching GoogleSignInApi.getSignInIntent...
        if (requestCode == RC_SIGN_IN) {
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
                        if (task.isSuccessful()) {
                            //SignIn Success
                            user = mAuth.getCurrentUser();

                            //Get values like name and email from google and add it in your database.
                            String name = user.getDisplayName();
                            String email = user.getEmail();

                            googleUser = new GoogleUser(name, email); // Making a new object
                            updateGoogleDatabase(user);

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);


                            SharedPreferences.Editor editor = getSharedPreferences("Details", MODE_PRIVATE).edit();
                            editor.putString("name", name);
                            editor.putString("email", email);
                            editor.apply();
                            finish();
                        } else {
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
    private void Register() {

        String encrypt_password = MD5hash(password_of_user);

        mAuth.createUserWithEmailAndPassword(email_of_user, password_of_user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //SignIn Success
                user = mAuth.getCurrentUser();
                emailUser = new EmailUser(name_of_user, email_of_user, encrypt_password);
                emailUsersReference.child(uniqueID(email_of_user)).setValue(emailUser);
                updateEmailDatabase(user);

                SharedPreferences.Editor editor = getSharedPreferences("Details", MODE_PRIVATE).edit();
                editor.putString("name", name_of_user);
                editor.putString("email", email_of_user);
                editor.apply();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
            }
        });


    }

    /*
    ####################################################################################################
     */
    //create a unique email substring and use it as an ID for each user which will also be easy to access about a particular user.
    private String uniqueID(String email) {
        return email.substring(0, email.lastIndexOf("@"));
    }

    //update Google Database
    private void updateGoogleDatabase(FirebaseUser firebaseUser) {

        Query checkUser = emailUsersReference.orderByChild("email").equalTo(email.getText().toString());
        checkUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!(snapshot.exists())) {

                    Query checkUser = googleUsersReference.orderByChild("email").equalTo(email.getText().toString());
                    checkUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(SignUpActivity.this, "User Already exists, Please Log In", Toast.LENGTH_SHORT).show();
                            } else {
                                Id = uniqueID(firebaseUser.getEmail());
                                googleUsersReference.child(Id).setValue(googleUser);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //update Email Database
    private void updateEmailDatabase(FirebaseUser firebaseUser) {

        Query checkUser = emailUsersReference.orderByChild("email").equalTo(email.getText().toString());
        checkUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!(snapshot.exists())) {
                    Query checkUser = googleUsersReference.orderByChild("email").equalTo(email.getText().toString());
                    checkUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(SignUpActivity.this, "User Already exists, Please Log In", Toast.LENGTH_SHORT).show();
                            } else {
                                Id = uniqueID(firebaseUser.getEmail());
                                emailUsersReference.child(Id).setValue(emailUser);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
/*
####################################################################################################
 */

    //Email Validation
    private boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    //provide password encryption
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


    //to redirect it to login page on back pressed.
    @Override
    public void onBackPressed() {
        this.finish();
        Intent in = new Intent(this, LoginActivity.class);
        startActivity(in);
    }
}