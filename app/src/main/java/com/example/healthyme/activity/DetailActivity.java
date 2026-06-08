package com.example.healthyme.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthyme.R;
import com.example.healthyme.database.DatabaseHelper;
import com.example.healthyme.model.Workout;
import com.example.healthyme.util.TranslationUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailActivity extends AppCompatActivity {

    private TextView tvName, tvInstructions;
    private TextView tvBarDifficulty, tvBarMuscle, tvBarType;
    private Button btnFavorite, btnStartWorkout;
    private LinearLayout btnBack;
    private Workout workout;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        btnBack = findViewById(R.id.btn_back);
        ViewCompat.setOnApplyWindowInsetsListener(btnBack, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
            params.topMargin = systemBars.top + (int) (16 * getResources().getDisplayMetrics().density);
            v.setLayoutParams(params);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        tvName = findViewById(R.id.tv_detail_name);
        tvInstructions = findViewById(R.id.tv_detail_instructions);
        tvBarDifficulty = findViewById(R.id.tv_bar_difficulty);
        tvBarMuscle = findViewById(R.id.tv_bar_muscle);
        tvBarType = findViewById(R.id.tv_bar_type);
        
        btnFavorite = findViewById(R.id.btn_favorite);
        btnStartWorkout = findViewById(R.id.btn_start_workout);

        workout = (Workout) getIntent().getSerializableExtra("workout");

        if (workout != null) {
            tvName.setText(workout.getName());
            
            // Menampilkan status loading terjemahan
            tvInstructions.setText("Menerjemahkan instruksi...");

            // Menerjemahkan instruksi menggunakan Google ML Kit secara otomatis
            TranslationUtils.translateWithMLKit(workout.getInstructions(), translatedText -> {
                tvInstructions.setText(translatedText);
            });
            
            // Menerjemahkan label kategori ke Bahasa Indonesia
            tvBarDifficulty.setText(TranslationUtils.translate(workout.getDifficulty()));
            tvBarMuscle.setText(TranslationUtils.translate(workout.getMuscle()));
            tvBarType.setText(TranslationUtils.translate(workout.getType()));

            checkIfFavorite();
        }

        btnFavorite.setOnClickListener(v -> {
            if (isLoggedIn()) toggleFavorite();
            else Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
        });
        
        btnBack.setOnClickListener(v -> finish());
        
        btnStartWorkout.setOnClickListener(v -> {
            if (isLoggedIn()) startWorkout();
            else Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isLoggedIn() {
        return sharedPreferences.contains("user_name");
    }

    private void startWorkout() {
        if (workout == null) return;
        executorService.execute(() -> {
            dbHelper.addToHistory(workout.getName());
            mainHandler.post(() -> Toast.makeText(this, "Workout " + workout.getName() + " selesai!", Toast.LENGTH_LONG).show());
        });
    }

    private void checkIfFavorite() {
        if (!isLoggedIn()) return;
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
                });
            } else {
                dbHelper.addFavorite(workout);
                mainHandler.post(() -> {
                    btnFavorite.setText("Hapus dari Favorit");
                    btnFavorite.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.btn_star_big_on, 0, 0, 0);
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
