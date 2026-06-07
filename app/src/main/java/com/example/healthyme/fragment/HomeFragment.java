package com.example.healthyme.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthyme.R;
import com.example.healthyme.activity.DetailActivity;
import com.example.healthyme.model.Workout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private TextView tvUserName, tvHomeAvatar;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        
        // Inisialisasi View Profil
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvHomeAvatar = view.findViewById(R.id.tv_home_avatar);

        // Klik avatar atau nama untuk pindah ke tab Profile
        View.OnClickListener goToProfile = v -> {
            BottomNavigationView navView = requireActivity().findViewById(R.id.bottom_navigation);
            if (navView != null) {
                navView.setSelectedItemId(R.id.navigation_profile);
            }
        };
        tvHomeAvatar.setOnClickListener(goToProfile);
        tvUserName.setOnClickListener(goToProfile);

        // Load data profil
        loadUserProfile();

        // Inisialisasi Tips
        View tipHydration = view.findViewById(R.id.tip_hydration);
        View tipSleep = view.findViewById(R.id.tip_sleep);
        View tipNutrition = view.findViewById(R.id.tip_nutrition);

        tipHydration.setOnClickListener(v -> showToast(getString(R.string.tip_hydration_msg)));
        tipSleep.setOnClickListener(v -> showToast(getString(R.string.tip_sleep_msg)));
        tipNutrition.setOnClickListener(v -> showToast(getString(R.string.tip_nutrition_msg)));

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

    private void loadUserProfile() {
        if (sharedPreferences == null) return;
        
        String name = sharedPreferences.getString("user_name", getString(R.string.guest_user));
        tvUserName.setText(name);
        
        // Set Avatar (Inisial Nama)
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 2); i++) {
                if (!parts[i].isEmpty()) {
                    initials.append(parts[i].charAt(0));
                }
            }
            tvHomeAvatar.setText(initials.toString().toUpperCase());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
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
