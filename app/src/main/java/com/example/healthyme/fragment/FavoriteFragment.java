package com.example.healthyme.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private TextView tvStatFavorites, tvStatThisWeek, tvStatTotalSessions;
    private TextView tvActiveFilter;
    private ImageView btnFilter;
    private List<Workout> allFavorites = new ArrayList<>();
    private String currentFilter = "Semua";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        rvFavorite = view.findViewById(R.id.rv_favorite);
        tvStatFavorites = view.findViewById(R.id.tv_stat_favorites);
        tvStatThisWeek = view.findViewById(R.id.tv_stat_this_week);
        tvStatTotalSessions = view.findViewById(R.id.tv_stat_total_sessions);
        tvActiveFilter = view.findViewById(R.id.tv_active_filter);
        btnFilter = view.findViewById(R.id.btn_filter_favorite);

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
                        loadFavoritesAndStats();
                        Toast.makeText(getContext(), R.string.removed_from_favorite, Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
        
        rvFavorite.setAdapter(adapter);

        // Logic Filter
        btnFilter.setOnClickListener(this::showFilterMenu);
        
        return view;
    }

    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add("Semua");
        popup.getMenu().add("Beginner");
        popup.getMenu().add("Intermediate");
        popup.getMenu().add("Expert");

        popup.setOnMenuItemClickListener(item -> {
            currentFilter = item.getTitle().toString();
            applyFilter();
            return true;
        });
        popup.show();
    }

    private void applyFilter() {
        if (currentFilter.equals("Semua")) {
            tvActiveFilter.setVisibility(View.GONE);
            adapter.setFavorites(allFavorites);
        } else {
            tvActiveFilter.setVisibility(View.VISIBLE);
            tvActiveFilter.setText("Filter: " + currentFilter);
            
            List<Workout> filtered = new ArrayList<>();
            for (Workout w : allFavorites) {
                if (w.getDifficulty() != null && w.getDifficulty().equalsIgnoreCase(currentFilter)) {
                    filtered.add(w);
                }
            }
            adapter.setFavorites(filtered);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        View header = view.findViewById(R.id.header_favorite);
        if (header != null) {
            ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top + (int) (24 * getResources().getDisplayMetrics().density), v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavoritesAndStats();
    }

    private void loadFavoritesAndStats() {
        if (dbHelper == null) return;
        
        executorService.execute(() -> {
            List<Workout> favorites = dbHelper.getAllFavorites();
            int totalSessions = dbHelper.getHistoryCount();
            int thisWeekSessions = dbHelper.getHistoryCountThisWeek();
            
            mainHandler.post(() -> {
                if (isAdded()) {
                    allFavorites = favorites;
                    tvStatFavorites.setText(String.valueOf(favorites.size()));
                    tvStatTotalSessions.setText(String.valueOf(totalSessions));
                    tvStatThisWeek.setText(String.valueOf(thisWeekSessions));
                    
                    // Pertahankan filter yang sedang aktif
                    applyFilter();
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
