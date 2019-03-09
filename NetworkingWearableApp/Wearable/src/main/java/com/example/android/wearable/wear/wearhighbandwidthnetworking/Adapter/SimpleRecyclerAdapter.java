package com.example.android.wearable.wear.wearhighbandwidthnetworking.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.wearable.wear.wearhighbandwidthnetworking.R;

import java.util.ArrayList;
import java.util.List;

public class SimpleRecyclerAdapter extends RecyclerView.Adapter<SimpleRecyclerAdapter.SimpleViewHolder>{

    List<Integer> dataSource;
    public SimpleRecyclerAdapter() {
        dataSource = new ArrayList<>();
        for (int i=0;i<=10;i++)
            dataSource.add(i);
    }

    @NonNull
    @Override
    public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item,parent,false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position) {
        holder.textView.setText(String.valueOf(dataSource.get(position)));

    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder{


        public TextView textView;
        public SimpleViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.textView);
        }
    }
}