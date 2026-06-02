package com.example.healthyme.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthyme.R;
import com.example.healthyme.model.Workout;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<Workout> favoriteList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Workout workout);
        void onDeleteClick(Workout workout);
    }

    public FavoriteAdapter(List<Workout> favoriteList, OnItemClickListener listener) {
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Workout workout = favoriteList.get(position);
        holder.tvName.setText(workout.getName());
        holder.tvInfo.setText(workout.getMuscle() + " - " + workout.getDifficulty());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(workout);
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(workout);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList != null ? favoriteList.size() : 0;
    }

    public void setFavorites(List<Workout> favorites) {
        this.favoriteList = favorites;
        notifyDataSetChanged();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;
        ImageView ivIcon, ivDelete;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_workout_name);
            tvInfo = itemView.findViewById(R.id.tv_muscle_info);
            ivIcon = itemView.findViewById(R.id.iv_workout_icon);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
