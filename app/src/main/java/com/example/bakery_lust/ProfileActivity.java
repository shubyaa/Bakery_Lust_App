package com.example.bakery_lust;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.util.HashMap;


public class ProfileActivity extends AppCompatActivity {
    private String name, email, phoneNo, address, profile_image1;

    private Button update, add_profile;
    private EditText name_editText, address_editText, phoneNo_editText;
    private ImageView profile_image;
    private TextView name_view, phoneNo_view, address_view, email_view, edit_profile;
    private RelativeLayout view_profile_segment, edit_profile_segment;

    private FirebaseDatabase database;
    private DatabaseReference emailUsersReference;
    private DatabaseReference googleUsersReference;

    private Uri imageUri;
    private FirebaseStorage storage;
    private String myProfileUri;
    private StorageReference profile_image_reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        profile_image_reference = storage.getReference("profile_images");
        emailUsersReference = database.getReference("Email");
        googleUsersReference = database.getReference("Google");

        //getIds from the layout
        update = findViewById(R.id.update);
        name_editText = findViewById(R.id.name_edit);
        address_editText = findViewById(R.id.address_edit);
        phoneNo_editText = findViewById(R.id.phoneNo_edit);
        add_profile = findViewById(R.id.add_profile);

        name_view = findViewById(R.id.name);
        email_view = findViewById(R.id.email);
        address_view = findViewById(R.id.address);
        phoneNo_view = findViewById(R.id.phoneNo);
        view_profile_segment = findViewById(R.id.view_profile_segment);
        edit_profile_segment = findViewById(R.id.edit_profile_segment);

        profile_image = findViewById(R.id.profile_image);
        edit_profile = findViewById(R.id.edit_profile);

        add_profile.setVisibility(View.GONE);
        edit_profile_segment.setVisibility(View.GONE);
        view_profile_segment.setVisibility(View.VISIBLE);

        //get Strings using Shared preferences
        getUserSharePreferences();

        //Check if user exists
        checkUser();

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_profile.setVisibility(View.VISIBLE);
                edit_profile_segment.setVisibility(View.VISIBLE);
                view_profile_segment.setVisibility(View.GONE);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDetails();
                getUserSharePreferences();
                add_profile.setVisibility(View.GONE);
                edit_profile_segment.setVisibility(View.GONE);
                view_profile_segment.setVisibility(View.VISIBLE);
            }
        });

        add_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void uploadImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadProfileImage();
        }
    }

    private void uploadProfileImage() {
        final ProgressDialog loading = new ProgressDialog(this);
        loading.setTitle("Uploading Image...");
        loading.show();

        // Create a reference
        StorageReference profile = profile_image_reference.child(uniqueID(email) + "." + getFileExtension(imageUri));
        profile.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                myProfileUri = uri.toString();
                                profile_image.setImageURI(imageUri);
                            }
                        });
                        loading.dismiss();
                        Toast.makeText(ProfileActivity.this, "Profile Image Updated!!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loading.dismiss();
                        Toast.makeText(ProfileActivity.this, "Failed to upload.", Toast.LENGTH_SHORT).show();
                        Log.i("failUpload", e.getMessage());
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double percent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        loading.setMessage("Progress : " + (int) percent + "%");
                    }
                });
    }

    //Get Shared Preferences
    private void getUserSharePreferences() {
        SharedPreferences preferences = getSharedPreferences("Details", MODE_PRIVATE);
        name = preferences.getString("name", "NONE");
        email = preferences.getString("email", "NONE");
    }

    //For File extension
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //Update details of the user
    private void updateDetails() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        String newName = name_editText.getText().toString();
        String newPhoneNo = phoneNo_editText.getText().toString();
        String newAddress = address_editText.getText().toString();

        HashMap<String, Object> map = new HashMap<>();
        map.put("profile_image", myProfileUri);

        Query checkUser = emailUsersReference.orderByChild("email").equalTo(email);
        checkUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    emailUsersReference.child(uniqueID(email)).updateChildren(map);

                    SharedPreferences.Editor editor = getSharedPreferences("Details", MODE_PRIVATE).edit();
                    if (!(newName.equals(""))) {
                        editor.putString("name", newName);
                        emailUsersReference.child(uniqueID(email)).child("name").setValue(newName);
                    }
                    if (!(newPhoneNo.equals(""))) {
                        editor.putString("phoneNo", newPhoneNo);
                        emailUsersReference.child(uniqueID(email)).child("phoneNo").setValue(newPhoneNo);
                    }
                    if (!(newAddress.equals(""))) {
                        editor.putString("address", newAddress);
                        emailUsersReference.child(uniqueID(email)).child("address").setValue(newAddress);
                    }
                    editor.apply();
                    progressDialog.dismiss();
                } else {
                    googleUsersReference.child(uniqueID(email)).updateChildren(map);

                    SharedPreferences.Editor editor = getSharedPreferences("Details", MODE_PRIVATE).edit();
                    if (!(newName.equals(""))) {
                        editor.putString("name", newName);
                        googleUsersReference.child(uniqueID(email)).child("name").setValue(newName);
                    }
                    if (!(newPhoneNo.equals(""))) {
                        editor.putString("phoneNo", newPhoneNo);
                        googleUsersReference.child(uniqueID(email)).child("phoneNo").setValue(newPhoneNo);
                    }
                    if (!(newAddress.equals(""))) {
                        editor.putString("address", newAddress);
                        googleUsersReference.child(uniqueID(email)).child("phoneNo").setValue(newPhoneNo);
                    }
                    editor.apply();
                    getUserSharePreferences();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //checkUser
    private void checkUser() {
        if (email.equals("NONE")) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Query checkProfile = emailUsersReference.orderByChild("email").equalTo(email);
            checkProfile.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        name = snapshot.child(uniqueID(email)).child("name").getValue(String.class);
                        email = snapshot.child(uniqueID(email)).child("email").getValue(String.class);
                        phoneNo = snapshot.child(uniqueID(email)).child("phoneNo").getValue(String.class);
                        profile_image1 = snapshot.child(uniqueID(email)).child("profile_image").getValue(String.class);
                        address = snapshot.child(uniqueID(email)).child("address").getValue(String.class);

                        name_view.setText(name);
                        email_view.setText(email);
                        address_view.setText(address);
                        phoneNo_view.setText(phoneNo);

                        //Check whether user has profile image or not
                        String image = snapshot.child(uniqueID(email)).child("profile_image").getValue(String.class);
                        if (image==null){
                            profile_image.setImageResource(R.drawable.ic_user);
                        }else {
                            Glide.with(ProfileActivity.this).load(image).into(profile_image);
                            //Picasso.get().load(image).into(profileImage);
                        }
                    } else {
                        Query checkProfile = googleUsersReference.orderByChild("email").equalTo(email);
                        checkProfile.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    name = snapshot.child(uniqueID(email)).child("name").getValue(String.class);
                                    email = snapshot.child(uniqueID(email)).child("email").getValue(String.class);
                                    phoneNo = snapshot.child(uniqueID(email)).child("phoneNo").getValue(String.class);
                                    profile_image1 = snapshot.child(uniqueID(email)).child("profile_image").getValue(String.class);
                                    address = snapshot.child(uniqueID(email)).child("address").getValue(String.class);

                                    name_view.setText(name);
                                    email_view.setText(email);
                                    address_view.setText(address);
                                    phoneNo_view.setText(phoneNo);

                                    //Check whether user has profile image or not
                                    String image = snapshot.child(uniqueID(email)).child("profile_image").getValue(String.class);
                                    if (image==null){
                                        profile_image.setImageResource(R.drawable.ic_user);
                                    }else {
                                        Glide.with(ProfileActivity.this).load(image).into(profile_image);
                                        //Picasso.get().load(image).into(profileImage);
                                    }
                                    Toast.makeText(ProfileActivity.this, "Getting Image", Toast.LENGTH_SHORT).show();
                                    Glide.with(getApplicationContext()).load(image).into(profile_image);
                                    //Picasso.get().load(image).into(profile_image);

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
        updateDetails();
        super.onBackPressed();
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