package com.example.planner;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.concurrent.Executors;

public class HabitsActivity extends BaseActivity {

    private LinearLayout habitsContainer;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAdd;

    private View dimOverlay;
    private LinearLayout modalPopup;
    private HabitDao habitDao;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        View rootView = findViewById(R.id.coordinatorLayout);
        bottomNav = findViewById(R.id.bottomNavigationView);

        setupAllInsets(rootView, bottomNav);

        AppDatabase db = AppDatabase.getInstance(this);
        habitDao = db.habitDao();

        initViews();
        setupBottomNavigation();
        setupModal();
        loadHabits();
    }

    private void addHabitCard(Habit habit) {

        View habitView = LayoutInflater.from(this)
                .inflate(R.layout.item_habit, habitsContainer, false);

        TextView tvName = habitView.findViewById(R.id.tvHabitName);
        TextView tvSchedule = habitView.findViewById(R.id.tvHabitSchedule);
        TextView tvProgress = habitView.findViewById(R.id.tvHabitProgress);
        TextView tvStreak = habitView.findViewById(R.id.tvStreak);
        TextView btnComplete = habitView.findViewById(R.id.btnComplete);

        View colorBar = habitView.findViewById(R.id.colorBar);

        LinearLayout contentLayout =
                habitView.findViewById(R.id.contentLayout);

        tvName.setText(habit.name);

        tvSchedule.setText(habit.schedule);

        // ПРОГРЕСС

        if (habit.targetDays > 0) {

            tvProgress.setText(
                    habit.completedDays + "/" +
                            habit.targetDays + " дней"
            );

        } else {

            tvProgress.setText(
                    "Серия: " + habit.streak
            );
        }

        // СТРАЙК

        if (habit.streak > 0) {

            tvStreak.setVisibility(View.VISIBLE);

            tvStreak.setText(
                    "🔥 " + habit.streak + " дней"
            );

        } else {

            tvStreak.setVisibility(View.GONE);
        }

        // КНОПКА

        boolean isCompletedToday = habit.completedToday;

        if (isCompletedToday) {

            btnComplete.setText("Выполнено");

            btnComplete.setBackgroundResource(
                    R.drawable.mark_button_background_completed
            );

            btnComplete.setTextColor(
                    ContextCompat.getColor(this, android.R.color.white)
            );

        } else {

            btnComplete.setText("Выполнить");

            btnComplete.setBackgroundResource(
                    R.drawable.mark_button_background
            );

            btnComplete.setTextColor(
                    ContextCompat.getColor(this, R.color.light_text)
            );
        }

        int habitColor = habit.color;

        String hexColor =
                String.format("#%06X", (0xFFFFFF & habitColor));

        String transparentColor =
                "#33" + hexColor.substring(1);

        colorBar.setBackgroundColor(habitColor);

        contentLayout.setBackgroundColor(
                Color.parseColor(transparentColor)
        );

        btnComplete.setOnClickListener(v -> {

            markComplete(habit);
        });

        // ОТКРЫТИЕ РЕДАКТОРА

        habitView.setOnClickListener(v -> {

            Intent intent =
                    new Intent(this, HabitEditorActivity.class);

            intent.putExtra("habit_id", habit.id);

            startActivityForResult(intent, 400);
        });

        habitsContainer.addView(habitView);
    }

    private void loadHabits() {

        Executors.newSingleThreadExecutor().execute(() -> {

            List<Habit> habits = habitDao.getAllHabits();

            runOnUiThread(() -> {

                habitsContainer.removeAllViews();

                if (habits.isEmpty()) {

                    TextView emptyView = new TextView(this);

                    emptyView.setText("Нет привычек");

                    emptyView.setTextSize(16);

                    emptyView.setTextColor(getColor(R.color.gray));

                    emptyView.setGravity(android.view.Gravity.CENTER);

                    emptyView.setPadding(0, dpToPx(100), 0, 0);

                    habitsContainer.addView(emptyView);

                    return;
                }

                for (Habit habit : habits) {

                    addHabitCard(habit);
                }
            });
        });
    }

    private void initViews() {
        habitsContainer = findViewById(R.id.habitsContainer);
        fabAdd = findViewById(R.id.fabAdd);

        dimOverlay = findViewById(R.id.dimOverlay);
        modalPopup = findViewById(R.id.modalPopup);
    }

    private void setupModal() {
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showModal());
        }
        if (dimOverlay != null) {
            dimOverlay.setOnClickListener(v -> hideModal());
        }
        setupModalButtons();
    }

    private void setupModalButtons() {
        if (modalPopup == null) return;

        TextView btnAddTask = modalPopup.findViewById(R.id.btnAddTask);
        TextView btnAddNote = modalPopup.findViewById(R.id.btnAddNote);
        TextView btnAddHabit = modalPopup.findViewById(R.id.btnAddHabit);

        btnAddTask.setOnClickListener(v -> {
            hideModal();
            showCreateTaskDialog();
        });

        btnAddNote.setOnClickListener(v -> {
            hideModal();
            showCreateNoteDialog();
        });

        btnAddHabit.setOnClickListener(v -> {
            hideModal();
            showCreateHabitDialog();
        });
    }

    private void showModal() {
        if (dimOverlay != null && modalPopup != null) {
            dimOverlay.setVisibility(View.VISIBLE);
            modalPopup.setVisibility(View.VISIBLE);
            dimOverlay.setAlpha(0f);
            modalPopup.setScaleX(0.8f);
            modalPopup.setScaleY(0.8f);
            modalPopup.setAlpha(0f);
            dimOverlay.animate().alpha(1f).setDuration(200).start();
            modalPopup.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).start();
        }

    }

    private void hideModal() {
        if (dimOverlay != null && modalPopup != null) {
            dimOverlay.animate().alpha(0f).setDuration(150)
                    .withEndAction(() -> dimOverlay.setVisibility(View.GONE)).start();
            modalPopup.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(150)
                    .withEndAction(() -> modalPopup.setVisibility(View.GONE)).start();
        }
    }

    private long getTimeMillis(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return cal.getTimeInMillis();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            loadHabits();
        }
    }

    @Override
    protected void onHabitCreated(String name, String schedule) {

        loadHabits();

        Toast.makeText(
                this,
                "Привычка создана",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void setupBottomNavigation() {
        if (bottomNav == null) return;

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
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_habits);
    }

    private void markComplete(Habit habit) {

        if (habit.completedToday) {

            Toast.makeText(
                    this,
                    "Уже выполнено сегодня",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {

            habit.completedToday = true;

            habit.completedDays++;

            habit.streak++;

            if (habit.streak > habit.bestStreak) {
                habit.bestStreak = habit.streak;
            }

            habitDao.update(habit);

            runOnUiThread(() -> {

                Toast.makeText(
                        this,
                        "✓ Выполнено",
                        Toast.LENGTH_SHORT
                ).show();

                loadHabits();
            });
        });
    }


    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}