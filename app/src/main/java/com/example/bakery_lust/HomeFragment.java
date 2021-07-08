package com.example.bakery_lust;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bakery_lust.Adapters.ParentAdapter;
import com.example.bakery_lust.Model.ParentModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ParentAdapter parentAdapter;

    private ArrayList<ParentModel> parentModelArrayList;

    private FirebaseDatabase database;
    private DatabaseReference DataReference;

    private RecyclerView.LayoutManager manager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        DataReference = database.getReference("Database");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.RV_parent);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        populateData();

        return view;
    }


    //fetch data from db
    private void populateData() {
        DataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parentModelArrayList = new ArrayList<>();
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    String name, image, description, rating;
                    Long cost;
                    name = snapshot1.child("name").getValue(String.class);
                    image = snapshot1.child("image").getValue(String.class);
                    description = snapshot1.child("description").getValue(String.class);
                    rating = snapshot1.child("rating").getValue(String.class);
                    cost = Long.parseLong(snapshot1.child("cost").getValue(String.class));

                    ParentModel parentModel = new ParentModel(name, image, cost, description, rating);
                    parentModelArrayList.add(parentModel);
                }

                parentAdapter = new ParentAdapter(parentModelArrayList, getContext());
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(parentAdapter);
                parentAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
