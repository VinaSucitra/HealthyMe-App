package com.example.healthyme.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthyme.R;
import com.example.healthyme.activity.DetailActivity;
import com.example.healthyme.adapter.FavoriteAdapter;
import com.example.healthyme.database.DatabaseHelper;
import com.example.healthyme.model.Workout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteFragment extends Fragment {

    private RecyclerView rvFavorite;
    private FavoriteAdapter adapter;
    private DatabaseHelper dbHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        rvFavorite = view.findViewById(R.id.rv_favorite);
        dbHelper = new DatabaseHelper(getContext());

        rvFavorite.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new FavoriteAdapter(new ArrayList<>(), new FavoriteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Workout workout) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("workout", workout);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Workout workout) {
                executorService.execute(() -> {
                    dbHelper.deleteFavorite(workout.getName());
                    mainHandler.post(() -> {
                        loadFavorites();
                        Toast.makeText(getContext(), "Dihapus dari favorit", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
        
        rvFavorite.setAdapter(adapter);
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        executorService.execute(() -> {
            List<Workout> favorites = dbHelper.getAllFavorites();
            mainHandler.post(() -> {
                adapter.setFavorites(favorites);
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
