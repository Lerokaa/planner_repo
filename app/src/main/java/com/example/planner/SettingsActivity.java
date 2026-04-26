package com.example.planner;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SettingsActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView tvName, tvSurname, tvEmail, tvAbout, tvThemeValue;
    private EditText etName, etSurname, etEmail, etAbout;
    private ImageView btnEditName, btnSaveName, btnEditSurname, btnSaveSurname;
    private ImageView btnEditEmail, btnSaveEmail, btnEditAbout, btnSaveAbout;
    private View layoutNotifications, layoutTheme, layoutPhoto;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        profileImage.setImageBitmap(bitmap);
                        sharedPreferences.edit().putString("profile_image", imageUri.toString()).apply();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Не удалось загрузить фото", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinatorLayout), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadUserData();
        setupBottomNavigation();
        setupClickListeners();

        // Делаем фото круглым
        makeImageCircle();
    }

    private void makeImageCircle() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(getColor(R.color.light_text));
        profileImage.setBackground(drawable);
        profileImage.setClipToOutline(true);
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        tvName = findViewById(R.id.tvName);
        tvSurname = findViewById(R.id.tvSurname);
        tvEmail = findViewById(R.id.tvEmail);
        tvAbout = findViewById(R.id.tvAbout);
        tvThemeValue = findViewById(R.id.tvThemeValue);

        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etEmail = findViewById(R.id.etEmail);
        etAbout = findViewById(R.id.etAbout);

        btnEditName = findViewById(R.id.btnEditName);
        btnSaveName = findViewById(R.id.btnSaveName);
        btnEditSurname = findViewById(R.id.btnEditSurname);
        btnSaveSurname = findViewById(R.id.btnSaveSurname);
        btnEditEmail = findViewById(R.id.btnEditEmail);
        btnSaveEmail = findViewById(R.id.btnSaveEmail);
        btnEditAbout = findViewById(R.id.btnEditAbout);
        btnSaveAbout = findViewById(R.id.btnSaveAbout);

        layoutNotifications = findViewById(R.id.layoutNotifications);
        layoutTheme = findViewById(R.id.layoutTheme);
        layoutPhoto = findViewById(R.id.layoutPhoto);
    }

    private void loadUserData() {
        String name = sharedPreferences.getString("name", "Елена");
        String surname = sharedPreferences.getString("surname", "Смирнова");
        String email = sharedPreferences.getString("email", "smirnovael@gmail.com");
        String about = sharedPreferences.getString("about", "Люблю планировать свои дела и ставить цели");
        String theme = sharedPreferences.getString("theme", "Светлая");
        String imageUri = sharedPreferences.getString("profile_image", null);

        tvName.setText(name);
        tvSurname.setText(surname);
        tvEmail.setText(email);
        tvAbout.setText(about);
        tvThemeValue.setText(theme);

        etName.setText(name);
        etSurname.setText(surname);
        etEmail.setText(email);
        etAbout.setText(about);

        if (imageUri != null && !imageUri.isEmpty()) {
            try {
                Uri uri = Uri.parse(imageUri);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                profileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupEditFunction(TextView tv, EditText et, ImageView btnEdit, ImageView btnSave, String key) {
        btnEdit.setOnClickListener(v -> {
            tv.setVisibility(View.GONE);
            et.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
            et.requestFocus();
        });

        btnSave.setOnClickListener(v -> {
            String newValue = et.getText().toString().trim();
            if (!newValue.isEmpty()) {
                tv.setText(newValue);
                sharedPreferences.edit().putString(key, newValue).apply();
                Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
            }
            tv.setVisibility(View.VISIBLE);
            et.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
        });
    }

    private void setupClickListeners() {
        setupEditFunction(tvName, etName, btnEditName, btnSaveName, "name");
        setupEditFunction(tvSurname, etSurname, btnEditSurname, btnSaveSurname, "surname");
        setupEditFunction(tvEmail, etEmail, btnEditEmail, btnSaveEmail, "email");
        setupEditFunction(tvAbout, etAbout, btnEditAbout, btnSaveAbout, "about");

        layoutPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        layoutNotifications.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "Настройки уведомлений (в разработке)", Toast.LENGTH_SHORT).show();
        });

        layoutTheme.setOnClickListener(v -> {
            String[] themes = {"Светлая", "Темная", "Системная"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Выберите тему")
                    .setItems(themes, (dialog, which) -> {
                        String selectedTheme = themes[which];
                        tvThemeValue.setText(selectedTheme);
                        sharedPreferences.edit().putString("theme", selectedTheme).apply();
                        Toast.makeText(this, "Тема изменена на " + selectedTheme + " (в разработке)", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_settings);

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_calendar) {
                    // Возвращаемся в MainActivity и показываем календарь
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_tasks) {
                    // Возвращаемся в MainActivity и показываем задачи
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("navigate_to", "tasks"); // Добавляем параметр
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_notes) {
                    Toast.makeText(SettingsActivity.this, "Заметки (в разработке)", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_habits) {
                    Toast.makeText(SettingsActivity.this, "Привычки (в разработке)", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    return true;
                }
                return false;
            });
        }
    }
}