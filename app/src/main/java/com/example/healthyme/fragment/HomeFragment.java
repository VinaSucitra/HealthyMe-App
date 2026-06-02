package com.example.healthyme.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthyme.R;
import com.example.healthyme.activity.DetailActivity;
import com.example.healthyme.model.Workout;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inisialisasi Tips
        View tipHydration = view.findViewById(R.id.tip_hydration);
        View tipSleep = view.findViewById(R.id.tip_sleep);
        View tipNutrition = view.findViewById(R.id.tip_nutrition);

        tipHydration.setOnClickListener(v -> showToast("Minum air putih membantu metabolisme tubuh!"));
        tipSleep.setOnClickListener(v -> showToast("Tidur cukup meningkatkan pemulihan otot."));
        tipNutrition.setOnClickListener(v -> showToast("Nutrisi seimbang adalah kunci energi maksimal."));

        // Inisialisasi Tombol Workout
        Button btnWorkout1 = view.findViewById(R.id.btn_start_workout1);
        Button btnWorkout2 = view.findViewById(R.id.btn_start_workout2);

        btnWorkout1.setOnClickListener(v -> {
            Workout workout = new Workout();
            workout.setName("Bench Press");
            workout.setType("Strength");
            workout.setMuscle("Chest");
            workout.setDifficulty("Beginner");
            workout.setInstructions("1. Berbaring di bangku.\n2. Pegang palang sedikit lebih lebar dari bahu.\n3. Turunkan palang ke dada.\n4. Dorong kembali ke atas.");
            navigateToDetail(workout);
        });

        btnWorkout2.setOnClickListener(v -> {
            Workout workout = new Workout();
            workout.setName("Jump Squat");
            workout.setType("Plyometrics");
            workout.setMuscle("Legs");
            workout.setDifficulty("Beginner");
            workout.setInstructions("1. Berdiri dengan kaki selebar bahu.\n2. Lakukan squat biasa.\n3. Melompatlah secara eksplosif ke atas.\n4. Mendaratlah dengan lembut.");
            navigateToDetail(workout);
        });

        return view;
    }

    private void navigateToDetail(Workout workout) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("workout", workout);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
