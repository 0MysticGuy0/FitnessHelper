package com.mygy.fitnesshelper.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mygy.fitnesshelper.R;
import com.mygy.fitnesshelper.data.Product;

import java.util.List;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private List<Product> products;

    public ProductRecyclerAdapter(Context context, List<Product> products) {
        this.products = products;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public ProductRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.product_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductRecyclerAdapter.ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.name.setText(product.getName());
        holder.proteins.setText(String.format("%2.1f",product.getProteins()));
        holder.fats.setText(String.format("%2.1f",product.getFats()));
        holder.carbs.setText(String.format("%2.1f",product.getCarbs()));
        holder.ccals.setText(String.format("%2.1f",product.getCcals()));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
    public void setSource(List<Product> food){
        products = food;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, proteins, fats, carbs, ccals;
        ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.product_name);
            proteins = view.findViewById(R.id.product_proteins);
            fats = view.findViewById(R.id.product_fats);
            carbs = view.findViewById(R.id.product_carbs);
            ccals = view.findViewById(R.id.product_ccals);
        }
    }
}
