package com.example.healthyme.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthyme.R;
import com.example.healthyme.activity.DetailActivity;
import com.example.healthyme.api.ApiClient;
import com.example.healthyme.api.ApiService;
import com.example.healthyme.database.DatabaseHelper;
import com.example.healthyme.model.Workout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvUserName, tvHomeAvatar;
    private TextView tvWeeklyStatus, tvWeeklyPercentage;
    private ProgressBar pbWeeklyProgram;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;

    // Water Tracker
    private TextView tvWaterCount;
    private int waterCount = 0;
    private static final int TARGET_WATER = 8;

    // Recommendations
    private LinearLayout layoutRecommendations;
    private ProgressBar pbRecommendations;
    private static final String API_KEY = "CxnrnztRIFd8rdGvlwTMZftJSXktzQaSYNJAKxNC";

    // Motivation
    private TextView tvMotivationQuote;
    private final String[] quotes = {
        "“Kesehatan adalah kekayaan yang paling berharga.”",
        "“Jangan berhenti saat lelah, berhentilah saat selesai.”",
        "“Masa depanmu dibentuk oleh apa yang kamu lakukan hari ini, bukan besok.”",
        "“Disiplin adalah jembatan antara tujuan dan pencapaian.”",
        "“Kesehatan mentalmu adalah prioritas. Kebahagiaanmu itu penting.”",
        "“Setiap langkah kecil membawamu lebih dekat ke versi terbaik dirimu.”",
        "“Tubuhmu adalah satu-satunya tempat tinggalmu. Jagalah baik-baik.”"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        dbHelper = new DatabaseHelper(getContext());
        
        // Inisialisasi View
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvHomeAvatar = view.findViewById(R.id.tv_home_avatar);
        tvMotivationQuote = view.findViewById(R.id.tv_motivation_quote);

        // Water Tracker Views
        tvWaterCount = view.findViewById(R.id.tv_water_count);
        ImageView btnAddWater = view.findViewById(R.id.btn_add_water);
        ImageView btnRemoveWater = view.findViewById(R.id.btn_remove_water);

        // Recommendations
        layoutRecommendations = view.findViewById(R.id.layout_recommendations);
        pbRecommendations = view.findViewById(R.id.pb_recommendations);

        // Logika Reset Air Harian
        checkDailyWaterReset();
        updateWaterUI();

        btnAddWater.setOnClickListener(v -> {
            if (waterCount < 20) {
                waterCount++;
                saveWaterData();
                updateWaterUI();
                if (waterCount == TARGET_WATER) {
                    Toast.makeText(getContext(), "Hebat! Target air tercapai! 💧", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRemoveWater.setOnClickListener(v -> {
            if (waterCount > 0) {
                waterCount--;
                saveWaterData();
                updateWaterUI();
            }
        });

        setRandomQuote();
        setupNavigation(view);
        setupTips(view);
        loadUserProfile();
        updateWeeklyProgram();
        fetchRecommendations();

        return view;
    }

    private void checkDailyWaterReset() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = sharedPreferences.getString("water_last_date", "");
        if (!today.equals(lastDate)) {
            waterCount = 0;
            sharedPreferences.edit().putInt("water_count", 0).putString("water_last_date", today).apply();
        } else {
            waterCount = sharedPreferences.getInt("water_count", 0);
        }
    }

    private void saveWaterData() {
        sharedPreferences.edit().putInt("water_count", waterCount).apply();
    }

    private void updateWaterUI() {
        if (tvWaterCount != null) {
            tvWaterCount.setText(waterCount + " dari " + TARGET_WATER + " gelas");
        }
    }

    private void setRandomQuote() {
        if (tvMotivationQuote != null) {
            int randomIndex = new Random().nextInt(quotes.length);
            tvMotivationQuote.setText(quotes[randomIndex]);
        }
    }

    private void setupNavigation(View view) {
        View.OnClickListener goToProfile = v -> {
            BottomNavigationView navView = requireActivity().findViewById(R.id.bottom_navigation);
            if (navView != null) navView.setSelectedItemId(R.id.navigation_profile);
        };
        if (tvHomeAvatar != null) tvHomeAvatar.setOnClickListener(goToProfile);
        if (tvUserName != null) tvUserName.setOnClickListener(goToProfile);

        View btnSeeAll = view.findViewById(R.id.btn_see_all);
        if (btnSeeAll != null) {
            btnSeeAll.setOnClickListener(v -> {
                BottomNavigationView navView = requireActivity().findViewById(R.id.bottom_navigation);
                if (navView != null) navView.setSelectedItemId(R.id.navigation_workout);
            });
        }
    }

    private void setupTips(View view) {
        LinearLayout layoutTips = view.findViewById(R.id.layout_tips);
        if (layoutTips == null) return;
        
        layoutTips.removeAllViews();
        String[] tipTitles = {"Hidrasi", "Tidur", "Nutrisi", "Pemanasan", "Konsistensi", "Recovery"};
        String[] tipSubtitles = {"8 gelas/hari", "7-8 jam", "Sayur & Buah", "5-10 menit", "Jadwal rutin", "Istirahatkan otot"};
        int[] tipIcons = {R.drawable.ic_water, R.drawable.ic_sleep, R.drawable.ic_nutrition, R.drawable.ic_heart_pulse, R.drawable.ic_history, R.drawable.ic_moon};
        for (int i = 0; i < tipTitles.length; i++) {
            View tipView = getLayoutInflater().inflate(R.layout.item_tip_card, layoutTips, false);
            ((ImageView) tipView.findViewById(R.id.iv_tip_icon)).setImageResource(tipIcons[i]);
            ((TextView) tipView.findViewById(R.id.tv_tip_title)).setText(tipTitles[i]);
            ((TextView) tipView.findViewById(R.id.tv_tip_subtitle)).setText(tipSubtitles[i]);
            layoutTips.addView(tipView);
        }
    }

    private void fetchRecommendations() {
        if (pbRecommendations != null) pbRecommendations.setVisibility(View.VISIBLE);
        if (layoutRecommendations != null) {
            layoutRecommendations.removeAllViews();
            if (pbRecommendations != null) layoutRecommendations.addView(pbRecommendations);
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Map<String, String> options = new HashMap<>();
        options.put("difficulty", "beginner");
        apiService.getWorkouts(API_KEY, options).enqueue(new Callback<List<Workout>>() {
            @Override
            public void onResponse(@NonNull Call<List<Workout>> call, @NonNull Response<List<Workout>> response) {
                if (!isAdded()) return;
                if (pbRecommendations != null) pbRecommendations.setVisibility(View.GONE);
                if (layoutRecommendations != null) layoutRecommendations.removeView(pbRecommendations);
                if (response.isSuccessful() && response.body() != null) {
                    List<Workout> list = response.body();
                    for (int i = 0; i < Math.min(list.size(), 3); i++) {
                        addRecommendationCard(list.get(i));
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Workout>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (pbRecommendations != null) pbRecommendations.setVisibility(View.GONE);
            }
        });
    }

    private void addRecommendationCard(Workout workout) {
        if (layoutRecommendations == null) return;
        View card = getLayoutInflater().inflate(R.layout.item_workout_recommendation, layoutRecommendations, false);
        ((TextView) card.findViewById(R.id.tv_rec_title)).setText(workout.getName());
        ((TextView) card.findViewById(R.id.tv_rec_subtitle)).setText(workout.getMuscle() + " • " + workout.getType());
        View.OnClickListener listener = v -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("workout", workout);
            startActivity(intent);
        };
        card.setOnClickListener(listener);
        card.findViewById(R.id.btn_rec_start).setOnClickListener(listener);
        layoutRecommendations.addView(card);
    }

    private void loadUserProfile() {
        if (sharedPreferences == null) return;
        String name = sharedPreferences.getString("user_name", getString(R.string.guest_user));
        if (tvUserName != null) tvUserName.setText(name);
        if (name != null && !name.trim().isEmpty() && tvHomeAvatar != null) {
            String[] parts = name.trim().split("\\s+");
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 2); i++) {
                if (!parts[i].isEmpty()) initials.append(parts[i].charAt(0));
            }
            tvHomeAvatar.setText(initials.toString().toUpperCase());
        }
    }

    private void updateWeeklyProgram() {
        if (dbHelper == null) return;
        int uniqueDays = dbHelper.getUniqueDaysCountThisWeek();
        int targetDays = 7;
        if (uniqueDays > targetDays) uniqueDays = targetDays;
        int percentage = (int) ((uniqueDays / (float) targetDays) * 100);
        
        if (tvWeeklyStatus != null) {
            tvWeeklyStatus.setText(uniqueDays + " dari " + targetDays + " hari selesai");
        }
        if (tvWeeklyPercentage != null) {
            tvWeeklyPercentage.setText(percentage + "%");
        }
        if (pbWeeklyProgram != null) {
            pbWeeklyProgram.setProgress(percentage);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        updateWeeklyProgram();
        checkDailyWaterReset();
        updateWaterUI();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
