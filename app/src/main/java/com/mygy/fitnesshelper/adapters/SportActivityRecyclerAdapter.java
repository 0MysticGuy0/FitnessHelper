package com.mygy.fitnesshelper.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mygy.fitnesshelper.R;
import com.mygy.fitnesshelper.data.SportActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SportActivityRecyclerAdapter extends RecyclerView.Adapter<SportActivityRecyclerAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private ArrayList<Map.Entry<SportActivity,Integer>> activities;

    public SportActivityRecyclerAdapter(Context context, HashMap<SportActivity,Integer> activities) {
        this.activities = new ArrayList<>();
        for(Map.Entry<SportActivity,Integer> a:activities.entrySet()){
            this.activities.add(a);
        }
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public SportActivityRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.sport_activity_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SportActivityRecyclerAdapter.ViewHolder holder, int position) {
        SportActivity activity = activities.get(position).getKey();
        int minutes = activities.get(position).getValue();

        holder.name.setText(activity.getName());
        holder.data.setText(minutes + " мин. - " + activity.getCcalsPerHour()/60*minutes + " ККал");
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public void setSource(HashMap<SportActivity,Integer> list){
        this.activities = new ArrayList<>();
        for(Map.Entry<SportActivity,Integer> a:list.entrySet()){
            this.activities.add(a);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, data;
        ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.activity_name);
            data = view.findViewById(R.id.activity_data);
        }
    }
}
