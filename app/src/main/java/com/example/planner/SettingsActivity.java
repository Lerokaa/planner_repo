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
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileNotFoundException;
import java.io.InputStream;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Находим корневой View и BottomNavigationView
        View rootView = findViewById(R.id.coordinatorLayout);
        bottomNav = findViewById(R.id.bottomNavigationView);

        // Применяем отступы для системных баров
        setupAllInsets(rootView, bottomNav);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadUserData();
        setupBottomNavigation();
        setupClickListeners();
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

    private void loadUserData() {
        String name = sharedPreferences.getString("name", "Елена");
        String surname = sharedPreferences.getString("surname", "Смирнова");
        String email = sharedPreferences.getString("email", "smirnovael@gmail.com");
        String about = sharedPreferences.getString("about", "Люблю планировать свои дела и ставить цели");
        String imageUri = sharedPreferences.getString("profile_image", null);

        tvName.setText(name);
        tvSurname.setText(surname);
        tvEmail.setText(email);
        tvAbout.setText(about);

        etName.setText(name);
        etSurname.setText(surname);
        etEmail.setText(email);
        etAbout.setText(about);

        // Скрываем возможность редактирования почты
        btnEditEmail.setVisibility(View.GONE);
        btnSaveEmail.setVisibility(View.GONE);
        etEmail.setEnabled(false);

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
    }

    private void setupBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_settings);

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_calendar) {
                    startActivity(new Intent(this, CalendarActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_tasks) {
                    startActivity(new Intent(this, TasksActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_notes) {
                    startActivity(new Intent(this, NotesActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_habits) {
                    startActivity(new Intent(this, HabitsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    return true;
                }
                return false;
            });
        }
    }
}