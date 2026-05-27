package com.example.planner;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.view.ViewOutlineProvider;
import android.graphics.Outline;

public class SettingsActivity extends BaseActivity {

    private ImageView profileImage;
    private TextView tvName, tvSurname, tvEmail, tvAbout;
    private EditText etName, etSurname, etEmail, etAbout;
    private ImageView btnEditName, btnSaveName, btnEditSurname, btnSaveSurname;
    private ImageView btnEditEmail, btnSaveEmail, btnEditAbout, btnSaveAbout;
    private View layoutNotifications, layoutPhoto;
    private BottomNavigationView bottomNav;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String PROFILE_IMAGE_NAME = "profile_image.jpg";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    // Лаунчер для выбора фото
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri sourceUri = result.getData().getData();
                    if (sourceUri != null) {
                        saveImageToInternalStorage(sourceUri);
                    }
                }
            });

    // Лаунчер для запроса разрешения на уведомления
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setNotificationsEnabled(true);
                    Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        View rootView = findViewById(R.id.coordinatorLayout);
        bottomNav = findViewById(R.id.bottomNavigationView);

        setupAllInsets(rootView, bottomNav);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadUserData();
        setupBottomNavigation();
        setupClickListeners();
        makeImageCircle();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        tvName = findViewById(R.id.tvName);
        tvSurname = findViewById(R.id.tvSurname);
        tvEmail = findViewById(R.id.tvEmail);
        tvAbout = findViewById(R.id.tvAbout);

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
        layoutPhoto = findViewById(R.id.layoutPhoto);
    }

    private void makeImageCircle() {
        profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        profileImage.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int size = Math.min(view.getWidth(), view.getHeight());
                outline.setRoundRect(0, 0, size, size, size / 2f);
            }
        });

        profileImage.setClipToOutline(true);
    }

    private void loadUserData() {
        String name = sharedPreferences.getString("name", "Елена");
        String surname = sharedPreferences.getString("surname", "Смирнова");
        String email = sharedPreferences.getString("email", "smirnovael@gmail.com");
        String about = sharedPreferences.getString("about", "Люблю планировать свои дела и ставить цели");

        tvName.setText(name);
        tvSurname.setText(surname);
        tvEmail.setText(email);
        tvAbout.setText(about);

        etName.setText(name);
        etSurname.setText(surname);
        etEmail.setText(email);
        etAbout.setText(about);

        btnEditEmail.setVisibility(View.GONE);
        btnSaveEmail.setVisibility(View.GONE);
        etEmail.setEnabled(false);

        loadProfileImage();
    }

    private void loadProfileImage() {
        File imageFile = new File(getFilesDir(), PROFILE_IMAGE_NAME);
        if (imageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
                return;
            }
        }
    }

    private void saveImageToInternalStorage(Uri sourceUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             FileOutputStream outputStream = new FileOutputStream(
                     new File(getFilesDir(), PROFILE_IMAGE_NAME))) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            Toast.makeText(this, "Фото сохранено", Toast.LENGTH_SHORT).show();
            loadProfileImage();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка сохранения фото", Toast.LENGTH_SHORT).show();
        }
    }

    // ====================== УВЕДОМЛЕНИЯ ======================

    private void setupClickListeners() {
        setupEditFunction(tvName, etName, btnEditName, btnSaveName, "name");
        setupEditFunction(tvSurname, etSurname, btnEditSurname, btnSaveSurname, "surname");
        setupEditFunction(tvEmail, etEmail, btnEditEmail, btnSaveEmail, "email");
        setupEditFunction(tvAbout, etAbout, btnEditAbout, btnSaveAbout, "about");

        layoutPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // === Обработка нажатия на уведомления ===
        layoutNotifications.setOnClickListener(v -> handleNotificationsClick());
    }

    private void handleNotificationsClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Первый запрос разрешения
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }

        // Если разрешение уже есть или Android < 13 — переключаем состояние
        boolean currentlyEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        setNotificationsEnabled(!currentlyEnabled);
    }

    private void setNotificationsEnabled(boolean enabled) {
        sharedPreferences.edit()
                .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
                .apply();

        if (enabled) {
            Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show();
            // Здесь в будущем можно будет включить все уведомления
        } else {
            Toast.makeText(this, "Уведомления выключены", Toast.LENGTH_SHORT).show();
            // Здесь можно отменить все запланированные уведомления
        }
    }

    // ====================== Остальные методы ======================

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

    private void setupBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_settings);

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Intent intent = null;

                if (itemId == R.id.nav_calendar) intent = new Intent(this, CalendarActivity.class);
                else if (itemId == R.id.nav_tasks) intent = new Intent(this, TasksActivity.class);
                else if (itemId == R.id.nav_notes) intent = new Intent(this, NotesActivity.class);
                else if (itemId == R.id.nav_habits) intent = new Intent(this, HabitsActivity.class);

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return itemId == R.id.nav_settings;
            });
        }
    }
}