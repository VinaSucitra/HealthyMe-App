package com.example.healthyme.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.healthyme.R;
import com.example.healthyme.activity.HistoryActivity;
import com.example.healthyme.activity.SetupProfileActivity;
import com.example.healthyme.database.DatabaseHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private TextView tvCountFavorite, tvCountSession, tvCountStreak, tvStreakBadge;
    private TextView tvProfileName, tvProfileAvatar, tvProfileJoinDate;
    private TextView tvLogoutTitle, tvLogoutSubtitle;
    private View btnLogout, btnHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        dbHelper = new DatabaseHelper(getContext());
        
        // Inisialisasi View Profil
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileAvatar = view.findViewById(R.id.tv_profile_avatar);
        tvProfileJoinDate = view.findViewById(R.id.tv_profile_join_date);
        
        // Inisialisasi View Statistik
        tvCountFavorite = view.findViewById(R.id.tv_count_favorite);
        tvCountSession = view.findViewById(R.id.tv_count_session);
        tvCountStreak = view.findViewById(R.id.tv_count_streak);
        tvStreakBadge = view.findViewById(R.id.tv_streak_badge);

        // Inisialisasi View Pengaturan
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        SwitchMaterial switchNotifications = view.findViewById(R.id.switch_notifications);
        View btnDarkMode = view.findViewById(R.id.btn_dark_mode);
        View btnNotifications = view.findViewById(R.id.btn_notifications);
        btnHistory = view.findViewById(R.id.btn_history);
        btnLogout = view.findViewById(R.id.btn_logout);
        tvLogoutTitle = view.findViewById(R.id.tv_logout_title);
        tvLogoutSubtitle = view.findViewById(R.id.tv_logout_subtitle);

        // Load Profil & Statistik
        loadUserProfile();
        updateStats();

        // Klik nama/avatar untuk edit atau login
        View.OnClickListener loginOrEdit = v -> {
            if (isLoggedIn()) {
                showEditNameDialog();
            } else {
                startActivity(new Intent(getActivity(), SetupProfileActivity.class));
            }
        };
        tvProfileName.setOnClickListener(loginOrEdit);
        tvProfileAvatar.setOnClickListener(loginOrEdit);

        // --- Logika Dark Mode ---
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);
        btnDarkMode.setOnClickListener(v -> switchDarkMode.setChecked(!switchDarkMode.isChecked()));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == sharedPreferences.getBoolean("dark_mode", false)) return;
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                AppCompatDelegate.setDefaultNightMode(isChecked ? 
                        AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }, 150);
        });

        // --- Logika Notifikasi ---
        boolean isNotifyEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(isNotifyEnabled);
        btnNotifications.setOnClickListener(v -> switchNotifications.setChecked(!switchNotifications.isChecked()));
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply();
            String message = getString(isChecked ? R.string.notify_enabled : R.string.notify_disabled);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });

        // --- Navigasi ke Riwayat ---
        btnHistory.setOnClickListener(v -> {
            if (isLoggedIn()) {
                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), R.string.please_login, Toast.LENGTH_SHORT).show();
            }
        });

        // --- Logika Logout / Login ---
        btnLogout.setOnClickListener(v -> {
            if (isLoggedIn()) {
                showLogoutConfirmation();
            } else {
                startActivity(new Intent(getActivity(), SetupProfileActivity.class));
            }
        });

        return view;
    }

    private boolean isLoggedIn() {
        return sharedPreferences.contains("user_name");
    }

    private void loadUserProfile() {
        String name = sharedPreferences.getString("user_name", getString(R.string.guest_user));
        tvProfileName.setText(name);
        
        if (isLoggedIn()) {
            tvProfileJoinDate.setVisibility(View.VISIBLE);
            tvLogoutTitle.setText(R.string.logout_positive);
            tvLogoutSubtitle.setText("Keluar dari akun Anda");
        } else {
            tvProfileJoinDate.setVisibility(View.GONE);
            tvLogoutTitle.setText(R.string.login_register);
            tvLogoutSubtitle.setText("Masuk untuk simpan data");
        }
        
        // Set Avatar (Inisial Nama)
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 2); i++) {
                if (!parts[i].isEmpty()) {
                    initials.append(parts[i].charAt(0));
                }
            }
            tvProfileAvatar.setText(initials.toString().toUpperCase());
        }
    }

    private void showEditNameDialog() {
        // Simple dialog to edit name if logged in
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.edit_name_title);

        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint(R.string.hint_enter_name);
        input.setText(tvProfileName.getText());
        
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 20, padding, 0);
        container.addView(input);
        
        builder.setView(container);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                sharedPreferences.edit().putString("user_name", newName).apply();
                loadUserProfile();
                Toast.makeText(getContext(), "Nama diperbarui", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.logout_positive, (dialog, which) -> {
                    sharedPreferences.edit().remove("user_name").apply();
                    loadUserProfile();
                    updateStats();
                    Toast.makeText(getContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.logout_negative, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStats();
        loadUserProfile();
    }

    private void updateStats() {
        if (dbHelper == null) return;
        
        if (isLoggedIn()) {
            int favCount = dbHelper.getAllFavorites().size();
            int sessionCount = dbHelper.getHistoryCount();
            
            tvCountFavorite.setText(String.valueOf(favCount));
            tvCountSession.setText(String.valueOf(sessionCount));
            tvCountStreak.setText(String.valueOf(sessionCount));
            tvStreakBadge.setText(getString(R.string.streak_format, sessionCount));
        } else {
            tvCountFavorite.setText("0");
            tvCountSession.setText("0");
            tvCountStreak.setText("0");
            tvStreakBadge.setText(getString(R.string.streak_format, 0));
        }
    }
}
