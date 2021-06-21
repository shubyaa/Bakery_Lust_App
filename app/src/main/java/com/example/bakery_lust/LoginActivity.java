package com.example.bakery_lust;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
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

public class LoginActivity extends AppCompatActivity {
    Button login, signup, google;
    EditText email, password;
    private String nameDb;
    private String emailDb;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
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
        database = FirebaseDatabase.getInstance();
        googleUsersReference = database.getReference("Google");
        emailUsersReference = database.getReference("Email");

        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup);
        google = findViewById(R.id.google);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        checkLogIn();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginViaEmail();
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
        //Result returned from launching GoogleSignInApi.getSignInIntent...
        if (requestCode==RC_SIGN_IN){
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

                            Log.i("name", nameDb);
                            Log.i("email", emailDb);

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("name", nameDb);
                            intent.putExtra("email", emailDb);
                            startActivity(intent);
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

    private void checkLogIn(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null){
            Intent in = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(in);
            finish();
        }
    }


    //check for existence of a particualar user in our database.
    private void loginViaEmail(){
        Query checkUser = emailUsersReference.orderByChild("email").equalTo(email.getText().toString());

        checkUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String passwordFromDb = snapshot.child(uniqueID(email.getText().toString())).child("password").getValue(String.class);

                    if (passwordFromDb.equals(MD5hash(password.getText().toString()))){
                         nameDb = snapshot.child(uniqueID(email.getText().toString())).child("name").getValue(String.class);
                         emailDb = snapshot.child(uniqueID(email.getText().toString())).child("email").getValue(String.class);

                        Log.i("name", nameDb);
                        Log.i("email", emailDb);

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("name", nameDb);
                        intent.putExtra("email", emailDb);
                        startActivity(intent);
                    }else {
                        Toast.makeText(LoginActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "Username dose'nt exist!", Toast.LENGTH_SHORT).show();
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
    //create a unique email substring and use it as an ID for each user which will also be easy to access about a particular user.
    private String uniqueID(String email){
        return email.substring(0, email.lastIndexOf("@"));
    }
/*
####################################################################################################
 */

    //provide password encryption, do encryption to the password input and then compare it with database.
    private static String MD5hash(String password){
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}