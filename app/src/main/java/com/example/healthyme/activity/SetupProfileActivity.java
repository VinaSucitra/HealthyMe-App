package com.example.healthyme.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthyme.MainActivity;
import com.example.healthyme.R;
import com.example.healthyme.database.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class SetupProfileActivity extends AppCompatActivity {

    private boolean isLoginMode = false; // Default: Register mode
    private DatabaseHelper dbHelper;
    private TextInputEditText etName, etPassword;
    private TextView tvTitle, tvSubtitle, tvSwitchMode;
    private Button btnAction;
    private ImageButton btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Mengaktifkan tampilan penuh ke atas (Edge-to-Edge)
        setContentView(R.layout.activity_setup_profile);

        // Mengatur status bar agar transparan sehingga background biru header terlihat sampai ujung atas
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        dbHelper = new DatabaseHelper(this);

        // UI Components
        btnClose = findViewById(R.id.btn_close);
        etName = findViewById(R.id.et_setup_name);
        etPassword = findViewById(R.id.et_setup_password);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvSwitchMode = findViewById(R.id.tv_switch_mode);
        btnAction = findViewById(R.id.btn_setup_finish);

        // Menangani Insets untuk tombol close agar turun di bawah jam/status bar
        // Tidak memberi padding pada layout utama agar background biru tetap penuh ke atas
        ViewCompat.setOnApplyWindowInsetsListener(btnClose, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
            // Memberikan margin top dinamis sesuai tinggi status bar + 16dp
            params.topMargin = systemBars.top + (int) (16 * getResources().getDisplayMetrics().density);
            v.setLayoutParams(params);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        btnClose.setOnClickListener(v -> finish());

        tvSwitchMode.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUI();
        });

        btnAction.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, R.string.error_password_short, Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                // LOGIN
                if (dbHelper.checkUser(name, password)) {
                    saveSessionAndProceed(name, sharedPreferences);
                } else {
                    Toast.makeText(this, R.string.error_login_failed, Toast.LENGTH_SHORT).show();
                }
            } else {
                // REGISTER
                if (dbHelper.registerUser(name, password)) {
                    Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                    saveSessionAndProceed(name, sharedPreferences);
                } else {
                    Toast.makeText(this, R.string.error_register_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI() {
        if (isLoginMode) {
            tvTitle.setText(R.string.login_title);
            tvSubtitle.setText(R.string.login_subtitle);
            btnAction.setText(R.string.btn_login);
            tvSwitchMode.setText(R.string.switch_to_register);
        } else {
            tvTitle.setText(R.string.register_title);
            tvSubtitle.setText(R.string.register_subtitle);
            btnAction.setText(R.string.btn_register);
            tvSwitchMode.setText(R.string.switch_to_login);
        }
    }

    private void saveSessionAndProceed(String name, SharedPreferences prefs) {
        prefs.edit().putString("user_name", name).apply();
        
        if (isTaskRoot()) {
            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
            startActivity(intent);
        }
        
        Toast.makeText(this, getString(R.string.welcome_message, name), Toast.LENGTH_SHORT).show();
        finish();
    }
}
