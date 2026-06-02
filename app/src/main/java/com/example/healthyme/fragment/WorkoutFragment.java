package com.example.healthyme.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutFragment extends Fragment {

    private RecyclerView rvWorkout;
    private WorkoutAdapter adapter;
    private ProgressBar progressBar;
    private static final String API_KEY = "YOUR_API_NINJAS_KEY"; // Replace with real key if available

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        rvWorkout = view.findViewById(R.id.rv_workout);
        progressBar = view.findViewById(R.id.progress_bar);

        rvWorkout.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WorkoutAdapter(new ArrayList<>(), workout -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("workout", workout);
            startActivity(intent);
        });
        rvWorkout.setAdapter(adapter);

        fetchWorkouts();

        return view;
    }

    private void fetchWorkouts() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        
        apiService.getWorkouts(API_KEY, "chest", null).enqueue(new Callback<List<Workout>>() {
            @Override
            public void onResponse(Call<List<Workout>> call, Response<List<Workout>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    adapter.setWorkouts(response.body());
                } else {
                    // Jika API gagal (karena API Key kosong), tampilkan data dummy agar desain terlihat
                    showDummyData();
                }
            }

            @Override
            public void onFailure(Call<List<Workout>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showDummyData();
                Toast.makeText(getContext(), "Koneksi gagal, menampilkan data lokal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDummyData() {
        List<Workout> dummyList = new ArrayList<>();

        Workout w1 = new Workout();
        w1.setName("Barbell Bench Press");
        w1.setMuscle("Chest");
        w1.setType("Strength");
        w1.setDifficulty("Beginner");
        w1.setInstructions("Berbaring di bench, pegang barbel selebar bahu. Turunkan ke dada perlahan, tahan 1 detik, lalu dorong ke atas. 3 set x 10 repetisi.");

        Workout w2 = new Workout();
        w2.setName("Pull Up");
        w2.setMuscle("Back");
        w2.setType("Compound");
        w2.setDifficulty("Intermediate");
        w2.setInstructions("Gantungkan tubuh pada palang besi, tarik tubuh hingga dagu melewati palang, lalu turunkan perlahan.");

        Workout w3 = new Workout();
        w3.setName("Squat");
        w3.setMuscle("Legs");
        w3.setType("Compound");
        w3.setDifficulty("Beginner");
        w3.setInstructions("Berdiri tegak, turunkan pinggul seperti hendak duduk dengan punggung tetap lurus, lalu kembali berdiri.");

        dummyList.add(w1);
        dummyList.add(w2);
        dummyList.add(w3);

        adapter.setWorkouts(dummyList);
    }
}
