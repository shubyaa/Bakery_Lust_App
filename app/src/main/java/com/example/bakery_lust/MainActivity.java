package com.example.bakery_lust;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private TextView name, email;
    private ImageView profileImage;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private View headerView;

    //fragments
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private String name1, email1, phoneNo1, address1, profile_image1;
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
        phoneNo1 = preferences.getString("phoneNo", "NONE");
        address1 = preferences.getString("address", "NONE");
        profile_image1 = preferences.getString("profile_image", "NONE");

        //adding id to the code from UI

        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_bar);

        //Ids from drawer
        headerView = navigationView.getHeaderView(0);
        name = headerView.findViewById(R.id.name_drawer);
        email = headerView.findViewById(R.id.email_drawer);
        profileImage = headerView.findViewById(R.id.profile_image);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });

        checkUser();

        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Home");
        Log.i("title", toolbar.getTitle().toString());

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();

        //Add fragments to the layout
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, new HomeFragment());
        fragmentTransaction.commit();

    }

//???????????????????????????????????????????????????????????????????????????????????????????????????



    //check User
    private void checkUser() {
        if (name1.equals("NONE") || email1.equals("NONE")){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            Query checkUser = emailUsersReference.orderByChild("email").equalTo(email1);
            checkUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        String nameDb = snapshot.child(uniqueID(email1)).child("name").getValue(String.class);
                        String emailDb = snapshot.child(uniqueID(email1)).child("email").getValue(String.class);

                        name.setText(nameDb);
                        email.setText(emailDb);
                    }else {
                        Query checkUser = googleUsersReference.orderByChild("email").equalTo(email1);
                        checkUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String nameDb = snapshot.child(uniqueID(email1)).child("name").getValue(String.class);
                                    String emailDb = snapshot.child(uniqueID(email1)).child("email").getValue(String.class);

                                    name.setText(nameDb);
                                    email.setText(emailDb);
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
    }

    @Override
    public void onBackPressed() {
        finish();
    }


//???????????????????????????????????????????????????????????????????????????????????????????????????

    //Navigation view
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.Home){

            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, new HomeFragment());
            fragmentTransaction.commit();

        }else if (item.getItemId() == R.id.Orders){

            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, new OrderFragment());
            fragmentTransaction.commit();

        }else if (item.getItemId() == R.id.Settings){

        }else if (item.getItemId() == R.id.logout){
            SharedPreferences.Editor preferences = getSharedPreferences("Details", MODE_PRIVATE).edit();
            preferences.putString("name", "NONE");
            preferences.putString("email", "NONE");
            preferences.apply();
            finish();

        }else if (item.getItemId() == R.id.Exit){
            finish();
        }
        return true;
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
}