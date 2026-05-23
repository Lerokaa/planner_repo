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

public class HabitsActivity extends BaseActivity {

    private LinearLayout habitsContainer;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAdd;

    private View dimOverlay;
    private LinearLayout modalPopup;

    private List<HabitItem> allHabits = new ArrayList<>();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static class HabitItem {
        String name;
        String description;
        String schedule;
        int color;
        boolean[] days;
        boolean hasTime;
        long time;
        int targetDays;
        int completedDays;
        int streak;
        int bestStreak;
        Map<String, Boolean> completedDates;

        public HabitItem(String name, String description, String schedule, int color,
                         boolean[] days, boolean hasTime, long time, int targetDays) {
            this.name = name;
            this.description = description;
            this.schedule = schedule;
            this.color = color;
            this.days = days;
            this.hasTime = hasTime;
            this.time = time;
            this.targetDays = targetDays;
            this.completedDays = 0;
            this.streak = 0;
            this.bestStreak = 0;
            this.completedDates = new HashMap<>();
        }

        public boolean hasTarget() {
            return targetDays > 0;
        }

        public boolean isCompletedToday() {
            String today = dateFormat.format(new Date());
            return completedDates.containsKey(today) && completedDates.get(today);
        }

        public int getProgressPercent() {
            if (targetDays <= 0) return 0;
            return (int) ((float) completedDays / targetDays * 100);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        View rootView = findViewById(R.id.coordinatorLayout);
        bottomNav = findViewById(R.id.bottomNavigationView);

        setupAllInsets(rootView, bottomNav);

        initViews();
        setupBottomNavigation();
        setupModal();
        loadDemoHabits();
        displayHabits();
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

    private void loadDemoHabits() {
        boolean[] days1 = {true, false, true, false, true, false, false};
        boolean[] days2 = {true, true, true, true, true, false, false};
        boolean[] days3 = {true, true, true, true, true, true, true};

        HabitItem habit1 = new HabitItem("Утренняя зарядка", "Зарядка по утрам", "Пн, Ср, Пт в 07:00",
                Color.parseColor("#FF6B6B"), days1, true, getTimeMillis(7, 0), 30);
        HabitItem habit2 = new HabitItem("Чтение книги", "30 минут чтения", "Пн-Пт в 21:00",
                Color.parseColor("#4ECDC4"), days2, true, getTimeMillis(21, 0), 7);
        HabitItem habit3 = new HabitItem("Медитация", "10 минут медитации", "Каждый день в 08:00",
                Color.parseColor("#96CEB4"), days3, true, getTimeMillis(8, 0), 0);

        initDemoStats(habit1, 5);
        initDemoStats(habit2, 3);
        initDemoStats(habit3, 2);

        allHabits.add(habit1);
        allHabits.add(habit2);
        allHabits.add(habit3);
    }

    private void initDemoStats(HabitItem habit, int daysCompleted) {
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < daysCompleted; i++) {
            String date = dateFormat.format(cal.getTime());
            habit.completedDates.put(date, true);
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        habit.completedDays = daysCompleted;
        habit.streak = daysCompleted;
        habit.bestStreak = daysCompleted;
    }

    private long getTimeMillis(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return cal.getTimeInMillis();
    }

    private boolean shouldCompleteToday(HabitItem habit) {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int index = (dayOfWeek + 5) % 7;
        if (index < 0 || index >= habit.days.length) return false;
        return habit.days[index];
    }

    private void markComplete(HabitItem habit) {
        String today = dateFormat.format(new Date());

        if (habit.completedDates.containsKey(today) && habit.completedDates.get(today)) {
            new AlertDialog.Builder(this)
                    .setTitle("Отменить выполнение?")
                    .setMessage("Вы уже отметили эту привычку сегодня. Отменить?")
                    .setPositiveButton("Отменить", (dialog, which) -> undoComplete(habit))
                    .setNegativeButton("Нет", null)
                    .show();
            return;
        }

        habit.completedDates.put(today, true);
        habit.completedDays++;
        updateStreak(habit);

        String message = "✓ " + habit.name + " выполнена! ";
        if (habit.streak > 0) {
            message += habit.streak + " дней подряд!";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        displayHabits();
    }

    private void undoComplete(HabitItem habit) {
        String today = dateFormat.format(new Date());

        if (habit.completedDates.containsKey(today) && habit.completedDates.get(today)) {
            habit.completedDates.remove(today);
            habit.completedDays--;
            updateStreak(habit);
            Toast.makeText(this, "↺ Выполнение отменено", Toast.LENGTH_SHORT).show();
            displayHabits();
        }
    }

    private void updateStreak(HabitItem habit) {
        Calendar cal = Calendar.getInstance();
        int currentStreak = 0;

        for (int i = 0; i < 30; i++) {
            String date = dateFormat.format(cal.getTime());

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int index = (dayOfWeek + 5) % 7;
            if (index >= 0 && index < habit.days.length) {
                boolean shouldComplete = habit.days[index];
                if (shouldComplete) {
                    if (habit.completedDates.containsKey(date) && habit.completedDates.get(date)) {
                        currentStreak++;
                    } else {
                        break;
                    }
                }
            }
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        habit.streak = currentStreak;
        if (currentStreak > habit.bestStreak) {
            habit.bestStreak = currentStreak;
        }
    }

    private void displayHabits() {
        habitsContainer.removeAllViews();

        if (allHabits.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Нет привычек");
            emptyView.setTextSize(16);
            emptyView.setTextColor(getColor(R.color.gray));
            emptyView.setGravity(android.view.Gravity.CENTER);
            emptyView.setPadding(0, dpToPx(100), 0, 0);
            habitsContainer.addView(emptyView);
            return;
        }

        for (HabitItem habit : allHabits) {
            View habitView = createHabitView(habit);
            habitsContainer.addView(habitView);
        }
    }

    private View createHabitView(HabitItem habit) {
        View habitView = LayoutInflater.from(this)
                .inflate(R.layout.item_habit, habitsContainer, false);

        TextView tvName = habitView.findViewById(R.id.tvHabitName);
        TextView tvSchedule = habitView.findViewById(R.id.tvHabitSchedule);
        TextView tvProgress = habitView.findViewById(R.id.tvHabitProgress);
        TextView tvStreak = habitView.findViewById(R.id.tvStreak);
        TextView btnComplete = habitView.findViewById(R.id.btnComplete);
        View colorBar = habitView.findViewById(R.id.colorBar);
        LinearLayout contentLayout = habitView.findViewById(R.id.contentLayout);

        int habitColor = habit.color;
        String hexColor = String.format("#%06X", (0xFFFFFF & habitColor));
        String transparentColor = "#33" + hexColor.substring(1);

        if (colorBar != null) {
            colorBar.setBackgroundColor(habitColor);
        }

        if (contentLayout != null) {
            contentLayout.setBackgroundColor(Color.parseColor(transparentColor));
        }

        tvName.setText(habit.name);
        tvSchedule.setText(habit.schedule);

        // Прогресс
        if (habit.hasTarget()) {
            int percent = habit.getProgressPercent();
            tvProgress.setText(habit.completedDays + "/" + habit.targetDays + " дней (" + percent + "%)");
        } else {
            tvProgress.setText("Выполнено " + habit.completedDays + " раз");
        }

        // Серия
        if (habit.streak > 0) {
            tvStreak.setText("🔥 " + habit.streak + " дней");
            tvStreak.setVisibility(View.VISIBLE);
        } else {
            tvStreak.setVisibility(View.GONE);
        }

        // Кнопка выполнения
        boolean isCompletedToday = habit.isCompletedToday();
        boolean shouldComplete = shouldCompleteToday(habit);

        if (isCompletedToday) {
            btnComplete.setText("Выполнено");
            btnComplete.setBackgroundResource(R.drawable.mark_button_background_completed);
            btnComplete.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else if (!shouldComplete) {
            btnComplete.setText("⏸ Сегодня выходной");
            btnComplete.setBackgroundResource(R.drawable.mark_button_background_cancel);
            btnComplete.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            btnComplete.setText("Выполнить");
            btnComplete.setBackgroundResource(R.drawable.mark_button_background);
            btnComplete.setTextColor(ContextCompat.getColor(this, R.color.light_text));
        }

        btnComplete.setOnClickListener(v -> markComplete(habit));

        // 👇 ДОБАВЛЯЕМ ОБРАБОТЧИК КЛИКА НА ВСЮ КАРТОЧКУ
        habitView.setOnClickListener(v -> {
            Intent intent = new Intent(HabitsActivity.this, HabitEditorActivity.class);
            intent.putExtra("habit_name", habit.name);
            intent.putExtra("habit_description", habit.description);
            intent.putExtra("habit_color", habit.color);
            intent.putExtra("habit_schedule", habit.schedule);
            intent.putExtra("habit_days", habit.days);
            intent.putExtra("habit_has_time", habit.hasTime);
            intent.putExtra("habit_time", habit.time);
            intent.putExtra("habit_target_days", habit.targetDays);
            intent.putExtra("habit_completed", habit.completedDays);
            intent.putExtra("habit_streak", habit.streak);
            intent.putExtra("habit_best_streak", habit.bestStreak);
            startActivityForResult(intent, 400);
        });

        return habitView;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("habit_name");
            String description = data.getStringExtra("habit_description");
            int color = data.getIntExtra("habit_color", ColorHelper.getColorForTitle(name));
            boolean[] days = data.getBooleanArrayExtra("habit_days");
            boolean hasTime = data.getBooleanExtra("habit_has_time", false);
            long time = data.getLongExtra("habit_time", -1);
            int targetDays = data.getIntExtra("habit_target_days", 0);

            // Формируем расписание для отображения
            StringBuilder daysStr = new StringBuilder();
            String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            for (int i = 0; i < days.length; i++) {
                if (days[i]) {
                    if (daysStr.length() > 0) daysStr.append(", ");
                    daysStr.append(dayNames[i]);
                }
            }
            String schedule = daysStr.length() > 0 ? daysStr.toString() : "Не выбрано";
            if (hasTime) {
                java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                schedule += " в " + timeFormat.format(new java.util.Date(time));
            }

            HabitItem newHabit = new HabitItem(name, description, schedule, color, days, hasTime, time, targetDays);
            allHabits.add(newHabit);
            displayHabits();
            Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onHabitCreated(String name, String schedule) {
        boolean[] days = {true, true, true, true, true, true, true};
        HabitItem newHabit = new HabitItem(name, "", "Каждый день", ColorHelper.getColorForTitle(name), days, false, -1, 0);
        allHabits.add(newHabit);
        displayHabits();
        Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show();
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


    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}