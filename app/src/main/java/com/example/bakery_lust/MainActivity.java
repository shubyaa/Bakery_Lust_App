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
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    TextView name, email;
    Button logout;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private View headerView;

    //fragments
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

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

        //adding id to the code from UI

        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_bar);

        headerView = navigationView.getHeaderView(0);
        name = headerView.findViewById(R.id.name_drawer);
        email = headerView.findViewById(R.id.email_drawer);

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
            name.setText(name1);
            email.setText(email1);
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

        }else if (item.getItemId() == R.id.Exit){

        }
        return true;
    }


    public String sendNameToFragment(){
        return name1;
    }

    public String sendEmailTOFragment(){
        return email1;
    }
}