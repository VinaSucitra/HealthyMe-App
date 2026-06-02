package com.example.healthyme.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthyme.R;
import com.example.healthyme.database.DatabaseHelper;
import com.example.healthyme.model.Workout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailActivity extends AppCompatActivity {

    private TextView tvName, tvInstructions;
    private TextView tvBarDifficulty, tvBarMuscle, tvBarType;
    private Button btnFavorite, btnStartWorkout;
    private LinearLayout btnBack;
    private Workout workout;
    private DatabaseHelper dbHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dbHelper = new DatabaseHelper(this);

        tvName = findViewById(R.id.tv_detail_name);
        tvInstructions = findViewById(R.id.tv_detail_instructions);
        tvBarDifficulty = findViewById(R.id.tv_bar_difficulty);
        tvBarMuscle = findViewById(R.id.tv_bar_muscle);
        tvBarType = findViewById(R.id.tv_bar_type);
        
        btnFavorite = findViewById(R.id.btn_favorite);
        btnStartWorkout = findViewById(R.id.btn_start_workout);
        btnBack = findViewById(R.id.btn_back);

        workout = (Workout) getIntent().getSerializableExtra("workout");

        if (workout != null) {
            tvName.setText(workout.getName());
            tvInstructions.setText(workout.getInstructions());
            
            tvBarDifficulty.setText(capitalize(workout.getDifficulty()));
            tvBarMuscle.setText(capitalize(workout.getMuscle()));
            tvBarType.setText(capitalize(workout.getType()));

            checkIfFavorite();
        }

        btnFavorite.setOnClickListener(v -> toggleFavorite());
        
        btnBack.setOnClickListener(v -> finish());
        
        btnStartWorkout.setOnClickListener(v -> {
            startWorkout();
        });
    }

    private void startWorkout() {
        if (workout == null) return;
        
        executorService.execute(() -> {
            dbHelper.addToHistory(workout.getName());
            mainHandler.post(() -> {
                Toast.makeText(this, "Workout " + workout.getName() + " telah dicatat di riwayat!", Toast.LENGTH_LONG).show();
            });
        });
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void checkIfFavorite() {
        executorService.execute(() -> {
            boolean isFav = dbHelper.isFavorite(workout.getName());
            mainHandler.post(() -> {
                if (isFav) {
                    btnFavorite.setText("Hapus dari Favorit");
                    btnFavorite.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.btn_star_big_on, 0, 0, 0);
                } else {
                    btnFavorite.setText("Tambah ke Favorit");
                    btnFavorite.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.btn_star_big_off, 0, 0, 0);
                }
            });
        });
    }

    private void toggleFavorite() {
        executorService.execute(() -> {
            boolean isFav = dbHelper.isFavorite(workout.getName());
            if (isFav) {
                dbHelper.deleteFavorite(workout.getName());
                mainHandler.post(() -> {
                    btnFavorite.setText("Tambah ke Favorit");
                    btnFavorite.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.btn_star_big_off, 0, 0, 0);
                    Toast.makeText(DetailActivity.this, "Dihapus dari Favorit", Toast.LENGTH_SHORT).show();
                });
            } else {
                dbHelper.addFavorite(workout);
                mainHandler.post(() -> {
                    btnFavorite.setText("Hapus dari Favorit");
                    btnFavorite.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.btn_star_big_on, 0, 0, 0);
                    Toast.makeText(DetailActivity.this, "Ditambahkan ke Favorit", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
