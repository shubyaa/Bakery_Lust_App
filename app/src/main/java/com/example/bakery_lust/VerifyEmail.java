package com.example.bakery_lust;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VerifyEmail extends AppCompatActivity {
    private LottieAnimationView lottie;
    private Button verify;
    private TextView resend;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        //Link elements from Ui to the activity.
        verify = findViewById(R.id.verify);
        resend = findViewById(R.id.resend);
        lottie = findViewById(R.id.lottie);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


        lottie.setVisibility(View.VISIBLE);
        lottie.playAnimation();

        if (!user.isEmailVerified()){
            lottie.setVisibility(View.GONE);

            resend.setVisibility(View.VISIBLE);
            resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lottie.setVisibility(View.VISIBLE);
                    resendEmail();
                    lottie.setVisibility(View.INVISIBLE);
                }
            });

        }else {
            directToHome();
        }
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!user.isEmailVerified()){
                    lottie.setVisibility(View.VISIBLE);
                    resendEmail();
                    lottie.setVisibility(View.INVISIBLE);
                }else {
                    lottie.setVisibility(View.VISIBLE);
                    directToHome();
                    lottie.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void resendEmail(){
        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(VerifyEmail.this, "Verify again, Email is sent", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(VerifyEmail.this, "Failed to send email.", Toast.LENGTH_SHORT).show();
                Log.i("Error to send mail", e.getMessage());
            }
        });
    }

    private void directToHome(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}