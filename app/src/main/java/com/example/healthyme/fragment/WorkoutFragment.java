package com.example.healthyme.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    private List<Workout> allWorkouts = new ArrayList<>();
    private EditText etSearch;
    private ImageView btnSearchToggle;

    private static final String API_KEY = "CxnrnztRIFd8rdGvlwTMZftJSXktzQaSYNJAKxNC";
    private TextView currentSelectedChip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        RecyclerView rvWorkout = view.findViewById(R.id.rv_workout);
        progressBar = view.findViewById(R.id.progress_bar);
        etSearch = view.findViewById(R.id.et_search_workout);
        btnSearchToggle = view.findViewById(R.id.btn_search_workout);

        rvWorkout.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WorkoutAdapter(new ArrayList<>(), workout -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("workout", workout);
            startActivity(intent);
        });
        rvWorkout.setAdapter(adapter);

        // Toggle Search Bar
        btnSearchToggle.setOnClickListener(v -> {
            if (etSearch.getVisibility() == View.GONE) {
                etSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
            } else {
                etSearch.setVisibility(View.GONE);
                etSearch.setText("");
            }
        });

        // Search Logic
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWorkouts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupChips(view);
        
        // Memuat "Semua" secara default (mengirim null agar difilter by beginner oleh logic fetchWorkouts)
        fetchWorkouts(null);

        return view;
    }

    private void filterWorkouts(String query) {
        List<Workout> filteredList = new ArrayList<>();
        for (Workout workout : allWorkouts) {
            if (workout.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(workout);
            }
        }
        adapter.setWorkouts(filteredList);
    }

    private void setupChips(View view) {
        TextView chipAll = view.findViewById(R.id.chip_all);
        TextView chipChest = view.findViewById(R.id.chip_chest);
        TextView chipBack = view.findViewById(R.id.chip_back);
        TextView chipLegs = view.findViewById(R.id.chip_legs);
        TextView chipAbs = view.findViewById(R.id.chip_abdominals);

        chipAll.setText(R.string.category_all);
        chipChest.setText(R.string.category_chest);
        chipBack.setText(R.string.category_back);
        chipLegs.setText(R.string.category_legs);
        chipAbs.setText(R.string.category_core);

        // Set default terpilih: Semua (diubah dari Chest)
        currentSelectedChip = chipAll;
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
            // Jika id adalah chip_all, muscle tetap null
            
            etSearch.setText("");
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
            // Untuk mode "Semua", kita tampilkan Beginner sebagai filter default (karena API wajib ada 1 filter)
            options.put("difficulty", "beginner");
        }

        apiService.getWorkouts(API_KEY, options).enqueue(new Callback<List<Workout>>() {
            @Override
            public void onResponse(@NonNull Call<List<Workout>> call, @NonNull Response<List<Workout>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allWorkouts = response.body();
                    adapter.setWorkouts(allWorkouts);
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Workout>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                showToast(getString(R.string.network_error));
            }
        });
    }

    private void showToast(String message) {
        if (getContext() != null) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View header = view.findViewById(R.id.header_workout);
        if (header != null) {
            ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top + (int) (24 * getResources().getDisplayMetrics().density), v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }
}
