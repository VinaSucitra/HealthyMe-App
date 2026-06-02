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

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workoutList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Workout workout);
    }

    public WorkoutAdapter(List<Workout> workoutList, OnItemClickListener listener) {
        this.workoutList = workoutList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        holder.tvName.setText(workout.getName());
        holder.tvMuscleInfo.setText(workout.getMuscle() + " - " + workout.getType());
        holder.tvBadgeDifficulty.setText(capitalize(workout.getDifficulty()));
        holder.tvBadgeType.setText(capitalize(workout.getType()));

        // Logic for icon can be added here if you have different icons for different workouts
        // holder.ivIcon.setImageResource(...);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(workout);
            }
        });
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public int getItemCount() {
        return workoutList != null ? workoutList.size() : 0;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workoutList = workouts;
        notifyDataSetChanged();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMuscleInfo, tvBadgeDifficulty, tvBadgeType;
        ImageView ivIcon, ivFavorite;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_workout_name);
            tvMuscleInfo = itemView.findViewById(R.id.tv_muscle_info);
            tvBadgeDifficulty = itemView.findViewById(R.id.tv_badge_difficulty);
            tvBadgeType = itemView.findViewById(R.id.tv_badge_type);
            ivIcon = itemView.findViewById(R.id.iv_workout_icon);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}
