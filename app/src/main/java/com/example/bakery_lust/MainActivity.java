package com.example.bakery_lust;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.internal.DialogRedirect;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    TextView name, email;
    Button logout;

    private String LOGOUT = "LOGOUT";
    private String name1;
    private String email1;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference googleUsersReference;
    private DatabaseReference emailUsersReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get instances from firebase database
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        googleUsersReference = database.getReference("Google");
        emailUsersReference = database.getReference("Email");

        //Get strings from other activities
        SharedPreferences preferences = getSharedPreferences("Details", MODE_PRIVATE);
        name1 = preferences.getString("name", "NONE");
        email1 = preferences.getString("email", "NONE");

        checkUser();

        //adding id to the code from UI
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        logout = findViewById(R.id.logout);


        //Adding logout function to the button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDialog(MainActivity.this);
                SharedPreferences.Editor editor = getSharedPreferences("Details", MODE_PRIVATE).edit();
                editor.putString("name", "NONE");
                editor.putString("email", "NONE");
                editor.apply();
            }
        });
    }

    //get details of the user.
    private void getDetails() {
        name.setText(name1);
        email.setText(email1);
    }


    //AlertDialog box for logout
    private void logoutDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Are you sure?")
                .setTitle("Logout")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        Intent in = new Intent(activity, LoginActivity.class);
                        startActivity(in);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //check User
    private void checkUser() {
        if (name1.equals("NONE") || email1.equals("NONE")){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getDetails();
    }
}