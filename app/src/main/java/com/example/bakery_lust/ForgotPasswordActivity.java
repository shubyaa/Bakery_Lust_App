package com.example.bakery_lust;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ConstraintLayout mailChecker;
    private ConstraintLayout passwordInputs;
    private Button continueButton, checkMail;
    private EditText email, newPassword, confirmPassword;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference emailUseresReferece;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //Connect all layout ids to the class
        mailChecker = findViewById(R.id.mailChecker);
        passwordInputs = findViewById(R.id.passwordInputs);
        continueButton = findViewById(R.id.continueButton);
        checkMail = findViewById(R.id.checkMail);
        email = findViewById(R.id.email);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        emailUseresReferece = database.getReference("Email");


        mailChecker.setVisibility(View.VISIBLE);
        passwordInputs.setVisibility(View.GONE);

        checkMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(email.getText().toString())) {
                    email.setError("enter email");
                } else {
                    checkMailInDatabase();
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(newPassword.getText().toString()) || TextUtils.isEmpty(confirmPassword.getText().toString())) {
                    newPassword.setError("Enter Valid Password");
                    confirmPassword.setError("Enter Valid Password");
                } else {
                    updateDatabasePassword();
                }
            }
        });
    }

    //Check if user exists in database
    private void checkMailInDatabase() {
        Query checkUser = emailUseresReferece.orderByChild("email").equalTo(email.getText().toString());

        checkUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(ForgotPasswordActivity.this, "User exists", Toast.LENGTH_SHORT).show();
                    mailChecker.setVisibility(View.GONE);
                    passwordInputs.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "User does not exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Change Password
    private void updateDatabasePassword() {
        if (newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
            emailUseresReferece.child(uniqueID(email.getText().toString())).child("password").setValue(MD5hash(confirmPassword.getText().toString()));
            Toast.makeText(this, "Password Updated", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {
            confirmPassword.setError("Confirm password not same");
        }
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}