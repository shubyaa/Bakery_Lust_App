package com.example.bakery_lust.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakery_lust.Model.ParentModel;
import com.example.bakery_lust.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ParentAdapter extends RecyclerView.Adapter<ParentAdapter.MyViewHolder> {
    public RecyclerView.RecycledViewPool viewPool;
    public ArrayList<ParentModel> parentModelArrayList;
    public Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView parentName;
        public TextView description;
        public TextView cost;
        public TextView rating;
        public ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
            rating = itemView.findViewById(R.id.rating);
            cost= itemView.findViewById(R.id.cost);
            parentName = itemView.findViewById(R.id.child_name);
            imageView = itemView.findViewById(R.id.child_image);
        }
    }

    public ParentAdapter(ArrayList<ParentModel> parentModelArrayList, Context context) {
        this.parentModelArrayList = parentModelArrayList;
        this.context = context;

        viewPool = new RecyclerView.RecycledViewPool();
    }

    @NonNull
    @Override
    public ParentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_items, parent, false);
        Log.i("parent", "inflated");
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentAdapter.MyViewHolder holder, int position) {

        ParentModel categoryModel = parentModelArrayList.get(position);

        holder.parentName.setText(categoryModel.getCategoryName());
        holder.cost.setText(String.valueOf(categoryModel.getCost()));
        holder.rating.setText(categoryModel.getRating());
        holder.description.setText(des(categoryModel.getDescription()));
        Picasso.get().load(categoryModel.getCategoryId()).fit().into(holder.imageView);

    }

    private String des(String string){
        if (string.length() > 17){
            return string.substring(0, 17)+"...";
        }else {
            return string;
        }
    }

    @Override
    public int getItemCount() {
        return parentModelArrayList.size();
    }
}
