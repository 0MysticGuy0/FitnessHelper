package com.mygy.fitnesshelper.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mygy.fitnesshelper.PlanInfoActivity;
import com.mygy.fitnesshelper.R;
import com.mygy.fitnesshelper.data.Plan;

import java.text.SimpleDateFormat;
import java.util.List;

public class StatsRecyclerAdapter extends RecyclerView.Adapter<StatsRecyclerAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private String[][] stats;

    public StatsRecyclerAdapter(Context context, String[][] stats) {
        this.stats = stats;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public StatsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.user_stats_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StatsRecyclerAdapter.ViewHolder holder, int position) {
        String[] info = stats[position];

        holder.name.setText(info[0]);
        holder.info.setText(info[1]);
    }

    @Override
    public int getItemCount() {
        return stats.length;
    }
    public void setSource(String[][] stats ){
        this.stats = stats;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, info;
        ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.stats_name);
            info = view.findViewById(R.id.stats_info);
        }
    }
}
