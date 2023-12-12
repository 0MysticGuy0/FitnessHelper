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
import com.mygy.fitnesshelper.data.Product;

import java.text.SimpleDateFormat;
import java.util.List;

public class PlanRecyclerAdapter extends RecyclerView.Adapter<PlanRecyclerAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private List<Plan> plans;
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public PlanRecyclerAdapter(Context context, List<Plan> plans) {
        this.plans = plans;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public PlanRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.plan_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlanRecyclerAdapter.ViewHolder holder, int position) {
        Plan plan = plans.get(position);

        holder.name.setText(plan.getName());
        holder.time.setText(timeFormat.format(plan.getTime()));
        if(plan.getDescription().length() == 0)
            holder.hasDescriptionIco.setVisibility(View.INVISIBLE);

        holder.checkBox.setChecked(plan.isDone());

        holder.checkBox.setOnClickListener(v -> {
            plan.setDone( holder.checkBox.isChecked() );
            notifyDataSetChanged();
        });

        holder.root.setOnClickListener(v -> {
            Intent intent = new Intent(inflater.getContext(), PlanInfoActivity.class);
            PlanInfoActivity.plan = plan;
            inflater.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return plans.size();
    }
    public void setSource(List<Plan> plans){
        this.plans = plans;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, time;
        final ImageView hasDescriptionIco;
        final CheckBox checkBox;
        final CardView root;
        ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.plan_name);
            time = view.findViewById(R.id.plan_time);
            hasDescriptionIco = view.findViewById(R.id.plan_hasDescriptionIco);
            checkBox = view.findViewById(R.id.plan_checkBox);
            root = view.findViewById(R.id.plan_root);
        }
    }
}
