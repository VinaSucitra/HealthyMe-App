package com.example.healthyme.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthyme.R;
import com.example.healthyme.activity.DetailActivity;
import com.example.healthyme.adapter.WorkoutAdapter;
import com.example.healthyme.api.ApiClient;
import com.example.healthyme.api.ApiService;
import com.example.healthyme.model.Workout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutFragment extends Fragment {

    private WorkoutAdapter adapter;
    private ProgressBar progressBar;

    // API Key Anda dari api-ninjas.com
    private static final String API_KEY = "CxnrnztRIFd8rdGvlwTMZftJSXktzQaSYNJAKxNC";

    private TextView currentSelectedChip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        // Handle Window Insets (Status Bar)
        RelativeLayout header = view.findViewById(R.id.header_workout);
        if (header == null) {
            // Jika id tidak ada di XML, kita cari RelativeLayout pertama
            header = (RelativeLayout) ((ViewGroup)view).getChildAt(0).findViewById(R.id.header_workout);
        }
        
        // Let's check the XML again. It doesn't have an ID for the RelativeLayout.
        // I should probably add an ID to the XML or find it by type if it's the first child.
        // Actually, I'll modify the XML first to make it cleaner.

        RecyclerView rvWorkout = view.findViewById(R.id.rv_workout);
        progressBar = view.findViewById(R.id.progress_bar);

        rvWorkout.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WorkoutAdapter(new ArrayList<>(), workout -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("workout", workout);
            startActivity(intent);
        });
        rvWorkout.setAdapter(adapter);

        setupChips(view);

        // Load awal: kategori Dada (Chest) sebagai default
        fetchWorkouts("chest");

        return view;
    }

    private void setupChips(View view) {
        TextView chipAll = view.findViewById(R.id.chip_all);
        TextView chipChest = view.findViewById(R.id.chip_chest);
        TextView chipBack = view.findViewById(R.id.chip_back);
        TextView chipLegs = view.findViewById(R.id.chip_legs);
        TextView chipAbs = view.findViewById(R.id.chip_abdominals);

        // Mengatur teks dari strings.xml
        chipAll.setText(R.string.category_all);
        chipChest.setText(R.string.category_chest);
        chipBack.setText(R.string.category_back);
        chipLegs.setText(R.string.category_legs);
        chipAbs.setText(R.string.category_core);

        // Set default terpilih: Dada
        currentSelectedChip = chipChest;
        updateChipUI(currentSelectedChip, true);

        View.OnClickListener chipListener = v -> {
            TextView clickedChip = (TextView) v;
            if (clickedChip == currentSelectedChip) return;

            updateChipUI(currentSelectedChip, false);
            updateChipUI(clickedChip, true);
            currentSelectedChip = clickedChip;

            String muscle = null;
            int id = v.getId();
            if (id == R.id.chip_chest) muscle = "chest";
            else if (id == R.id.chip_back) muscle = "back";
            else if (id == R.id.chip_legs) muscle = "legs";
            else if (id == R.id.chip_abdominals) muscle = "abdominals";
                // Jika "Semua", kita kirim null agar fetchWorkouts menangani defaultnya
            else if (id == R.id.chip_all) muscle = null;

            fetchWorkouts(muscle);
        };

        chipAll.setOnClickListener(chipListener);
        chipChest.setOnClickListener(chipListener);
        chipBack.setOnClickListener(chipListener);
        chipLegs.setOnClickListener(chipListener);
        chipAbs.setOnClickListener(chipListener);
    }

    private void updateChipUI(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_absolute));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void fetchWorkouts(String muscle) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Map<String, String> options = new HashMap<>();
        if (muscle != null) {
            options.put("muscle", muscle);
        } else {
            // API ini wajib ada 1 filter. Jika "Semua", kita tampilkan Beginner sebagai default.
            options.put("difficulty", "beginner");
        }

        apiService.getWorkouts(API_KEY, options).enqueue(new Callback<List<Workout>>() {
            @Override
            public void onResponse(@NonNull Call<List<Workout>> call, @NonNull Response<List<Workout>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    adapter.setWorkouts(response.body());
                    if (response.body().isEmpty()) {
                        showToast(getString(R.string.no_data_found));
                    }
                } else {
                    String message = "Server Error: " + response.code();
                    if (response.code() == 401) message = "API Key Tidak Valid";
                    else if (response.code() == 400) message = "Filter Wajib Tidak Ada (400)";

                    showToast(message);
                    showDummyData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Workout>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                showToast(getString(R.string.network_error));
                showDummyData();
            }
        });
    }

    private void showDummyData() {
        List<Workout> dummyList = new ArrayList<>();
        Workout w = new Workout();
        w.setName("Latihan (Offline)");
        w.setMuscle("n/a");
        w.setType("n/a");
        w.setDifficulty("n/a");
        w.setInstructions("Mohon periksa internet atau API Key Anda untuk melihat data asli.");
        dummyList.add(w);
        adapter.setWorkouts(dummyList);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Menangani Insets agar tidak tertutup Status Bar (Jam)
        View header = view.findViewById(R.id.header_workout);
        if (header != null) {
            ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }
}