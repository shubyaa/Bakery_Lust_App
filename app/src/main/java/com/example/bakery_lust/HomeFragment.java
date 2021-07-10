package com.example.bakery_lust;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.bakery_lust.Adapters.ParentAdapter;
import com.example.bakery_lust.Interfaces.RecyclerViewOnClickListener;
import com.example.bakery_lust.Model.ParentModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements RecyclerViewOnClickListener {
    private RecyclerView recyclerView;
    private ScrollView scrollView;
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
        scrollView = view.findViewById(R.id.scroll);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        populateData();

        return view;
    }


    //fetch data from db
    private void populateData() {
        parentModelArrayList = new ArrayList<>();
        DataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    String name, image, description, rating, id;
                    long cost;
                    name = snapshot1.child("name").getValue(String.class);
                    image = snapshot1.child("image").getValue(String.class);
                    description = snapshot1.child("description").getValue(String.class);
                    rating = snapshot1.child("rating").getValue(String.class);
                    cost = Long.parseLong(snapshot1.child("cost").getValue(String.class));
                    id = snapshot1.getKey();

                    ParentModel parentModel = new ParentModel(name, image, cost, description, rating, id);
                    parentModelArrayList.add(parentModel);
                }

                parentAdapter = new ParentAdapter(parentModelArrayList, getContext(), HomeFragment.this);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(parentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onItemClick(int position) {
        ParentModel parentModel = parentModelArrayList.get(position);
        String id = parentModel.getId();
        Toast.makeText(getActivity(), position, Toast.LENGTH_SHORT).show();
       /* Intent intent = new Intent(getActivity(), OrderActivity.class);
        intent.putExtra("orderId", id);
        startActivity(intent);
        getActivity().finish();
        */
        Log.i("Orderid", id);
    }
}
