package com.example.planner;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();
    }

    private void enableEdgeToEdge() {
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(
                getWindow(),
                getWindow().getDecorView()
        );

        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    protected void setupWindowInsets(View rootView) {
        if (rootView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });
    }

    protected void setupTopInsets(View view) {
        if (view == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );

            return insets;
        });
    }

    protected void setupBottomNavInsets(BottomNavigationView bottomNav) {
        if (bottomNav == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            // Устанавливаем только верхний отступ, нижний убираем
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    0
            );

            return insets;
        });
    }

    protected void setupAllInsets(View rootView, BottomNavigationView bottomNav) {
        setupWindowInsets(rootView);
        if (bottomNav != null) {
            setupBottomNavInsets(bottomNav);
        }
    }

    // Диалог создания задачи - открывает AddTaskActivity
    protected void showCreateTaskDialog() {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivityForResult(intent, 200);
    }

    // Диалог создания заметки - открывает AddNoteActivity
    protected void showCreateNoteDialog() {
        Intent intent = new Intent(this, AddNoteActivity.class);
        startActivityForResult(intent, 100);
    }

    // Диалог создания привычки - открывает AddHabitActivity
    protected void showCreateHabitDialog() {
        Intent intent = new Intent(this, AddHabitActivity.class);
        startActivityForResult(intent, 300);
    }

    // Методы для переопределения в дочерних активностях
    protected void onTaskCreated(String title, String startTime, String endTime, int year, int month, int day) {
        Toast.makeText(this, "Задача \"" + title + "\" создана", Toast.LENGTH_SHORT).show();
    }

    protected void onNoteCreated(String title, String content) {
        Toast.makeText(this, "Заметка \"" + title + "\" создана", Toast.LENGTH_SHORT).show();
    }

    protected void onHabitCreated(String name, String schedule) {
        Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show();
    }
}