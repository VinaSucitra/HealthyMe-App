package com.example.healthyme.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        dbHelper = new DatabaseHelper(this);

        // UI Components
        ImageButton btnClose = findViewById(R.id.btn_close);
        etName = findViewById(R.id.et_setup_name);
        etPassword = findViewById(R.id.et_setup_password);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvSwitchMode = findViewById(R.id.tv_switch_mode);
        btnAction = findViewById(R.id.btn_setup_finish);

        // Menangani Insets agar tombol tidak tertutup jam/status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_setup_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            
            if (btnClose != null) {
                android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) btnClose.getLayoutParams();
                params.topMargin = systemBars.top + (int) (16 * getResources().getDisplayMetrics().density);
                btnClose.setLayoutParams(params);
            }
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
                Toast.makeText(this, "Silakan isi semua kolom", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Kata sandi minimal 6 karakter", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                // Logic: LOGIN
                if (dbHelper.checkUser(name, password)) {
                    saveSessionAndProceed(name, sharedPreferences);
                } else {
                    Toast.makeText(this, "Nama pengguna atau kata sandi salah", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Logic: REGISTER
                if (dbHelper.registerUser(name, password)) {
                    Toast.makeText(this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show();
                    saveSessionAndProceed(name, sharedPreferences);
                } else {
                    Toast.makeText(this, "Pendaftaran gagal. Nama mungkin sudah digunakan.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI() {
        if (isLoginMode) {
            tvTitle.setText("Masuk");
            tvSubtitle.setText("Lanjutkan progres latihan Anda");
            btnAction.setText("Masuk Sekarang");
            tvSwitchMode.setText("Belum punya akun? Daftar di sini");
        } else {
            tvTitle.setText("Buat Akun");
            tvSubtitle.setText("Simpan progres latihan Anda selamanya");
            btnAction.setText("Daftar Sekarang");
            tvSwitchMode.setText("Sudah punya akun? Masuk di sini");
        }
    }

    private void saveSessionAndProceed(String name, SharedPreferences prefs) {
        prefs.edit().putString("user_name", name).apply();
        
        if (isTaskRoot()) {
            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
            startActivity(intent);
        }
        
        Toast.makeText(this, "Selamat datang, " + name, Toast.LENGTH_SHORT).show();
        finish();
    }
}
