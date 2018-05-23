package com.example.rgain.extractapk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rgain on 2/12/2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<listItemModel> packages;
    private Context context;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position, String model);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public MyAdapter(List<listItemModel> packages, Context context, ArrayList<listItemModel> apksModel) {
        this.packages = packages;
        this.context = context;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_model, parent, false);
        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(final MyAdapter.ViewHolder holder, final int position) {
        final listItemModel model = packages.get(position);
        final listItemModel listItem = packages.get(position);
        holder.ocl = mListener;
        holder.txtName.setText(listItem.getAppname());
        holder.txtPackageName.setText(listItem.getPname());
        holder.txtSize.setText(listItem.getVersionName());
        holder.imgApp.setImageDrawable(listItem.getIcon());



        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.ocl.onItemClick(position, model.getPname());
            }
        });
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgApp;
        TextView txtName, txtPackageName, txtSize;
        CardView cardView;
        OnItemClickListener ocl;

        public ViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardItem);
            txtName = itemView.findViewById(R.id.textName);
            txtPackageName = itemView.findViewById(R.id.textPackageName);
            txtSize = itemView.findViewById(R.id.textSize);
            imgApp = itemView.findViewById(R.id.img);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        String clickedName = txtPackageName.toString();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position, clickedName);
                        }
                    }
                }
            });

        }
    }
}
