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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workoutList;
    private final OnItemClickListener listener;
    private static final Map<String, String> translationMap = new HashMap<>();

    static {
        // Pemetaan Otot
        translationMap.put("abdominals", "Perut");
        translationMap.put("abductors", "Paha Luar");
        translationMap.put("adductors", "Paha Dalam");
        translationMap.put("biceps", "Bisep");
        translationMap.put("calves", "Betis");
        translationMap.put("chest", "Dada");
        translationMap.put("forearms", "Lengan Bawah");
        translationMap.put("glutes", "Bokong");
        translationMap.put("hamstrings", "Paha Belakang");
        translationMap.put("lats", "Punggung Samping");
        translationMap.put("lower_back", "Pinggang");
        translationMap.put("middle_back", "Punggung Tengah");
        translationMap.put("neck", "Leher");
        translationMap.put("quadriceps", "Paha Depan");
        translationMap.put("traps", "Bahu Atas");
        translationMap.put("triceps", "Trisep");

        // Pemetaan Kesulitan
        translationMap.put("beginner", "Pemula");
        translationMap.put("intermediate", "Menengah");
        translationMap.put("expert", "Ahli");

        // Pemetaan Tipe
        translationMap.put("strength", "Kekuatan");
        translationMap.put("cardio", "Kardio");
        translationMap.put("stretching", "Peregangan");
        translationMap.put("plyometrics", "Pliometrik");
    }

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
        
        String muscleIndo = translate(workout.getMuscle());
        String typeIndo = translate(workout.getType());
        String diffIndo = translate(workout.getDifficulty());

        holder.tvName.setText(workout.getName());
        holder.tvMuscleInfo.setText(muscleIndo + " - " + typeIndo);
        holder.tvBadgeDifficulty.setText(diffIndo);
        holder.tvBadgeType.setText(typeIndo);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Kirim data asli ke detail (untuk instruksi)
                listener.onItemClick(workout);
            }
        });
    }

    private String translate(String key) {
        if (key == null) return "N/A";
        String lowerKey = key.toLowerCase();
        return translationMap.getOrDefault(lowerKey, capitalize(key));
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
