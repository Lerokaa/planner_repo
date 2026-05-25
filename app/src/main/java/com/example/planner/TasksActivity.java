package com.example.planner;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TasksActivity extends BaseActivity {

    private LinearLayout tasksContainer;
    private EditText etSearch;
    private ImageView btnClearSearch;
    private TextView tvSectionHeader;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAdd;

    private View dimOverlay;
    private LinearLayout modalPopup;

    private TextView chipAll, chipToday, chipTomorrow, chipWeek, chipCompleted;

    private List<CalendarTask> allTasks = new ArrayList<>();
    private List<CalendarTask> filteredTasks = new ArrayList<>();

    private String currentFilter = "all";
    private String currentSearchQuery = "";

    public static class CalendarTask {
        String title;
        String description;
        String startTime;
        String endTime;
        int year, month, day;
        boolean isCompleted;
        int color;
        long timestamp;
        String attachedNoteTitle;
        String attachedNoteContent;
        long attachedNoteId;

        public CalendarTask(String title, String startTime, String endTime, int year, int month, int day, boolean isCompleted, int color) {
            this.title = title;
            this.description = "";
            this.startTime = startTime;
            this.endTime = endTime;
            this.year = year;
            this.month = month;
            this.day = day;
            this.isCompleted = isCompleted;
            this.color = color;
            this.attachedNoteTitle = null;
            this.attachedNoteContent = null;
            this.attachedNoteId = -1;

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            this.timestamp = cal.getTimeInMillis();
        }

        public String getDateString() {
            Calendar today = Calendar.getInstance();
            Calendar taskDate = Calendar.getInstance();
            taskDate.set(year, month, day);

            if (taskDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    taskDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                return "Сегодня";
            }

            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);
            if (taskDate.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                    taskDate.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)) {
                return "Завтра";
            }

            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("d MMMM", new java.util.Locale("ru"));
            java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEEE", new java.util.Locale("ru"));
            return dayFormat.format(taskDate.getTime()) + ", " + dateFormat.format(taskDate.getTime());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("habit_name");
            Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatTime(long time) {
        if (time == -1) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new java.util.Date(time));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tasks);

        View rootView = findViewById(R.id.coordinatorLayout);
        bottomNav = findViewById(R.id.bottomNavigationView);

        setupAllInsets(rootView, bottomNav);

        initViews();
        loadTasksFromCalendar();
        setupFilters();
        setupSearch();
        setupBottomNavigation();
        applyFilters();
        setupModal();
    }

    private void initViews() {
        tasksContainer = findViewById(R.id.tasksContainer);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        tvSectionHeader = findViewById(R.id.tvSectionHeader);
        fabAdd = findViewById(R.id.fabAdd);

        dimOverlay = findViewById(R.id.dimOverlay);
        modalPopup = findViewById(R.id.modalPopup);

        chipAll = findViewById(R.id.chipAll);
        chipToday = findViewById(R.id.chipToday);
        chipTomorrow = findViewById(R.id.chipTomorrow);
        chipWeek = findViewById(R.id.chipWeek);
        chipCompleted = findViewById(R.id.chipCompleted);
    }

    private void loadTasksFromCalendar() {
        allTasks.clear();

        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        addTaskFromCalendar("Купить продукты", "10:00", "11:00", currentYear, currentMonth, currentDay, false);
        addTaskFromCalendar("Созвониться с клиентом", "12:00", "12:30", currentYear, currentMonth, currentDay, false);
        addTaskFromCalendar("Прогуляться в парке", "15:00", "16:00", currentYear, currentMonth, currentDay, false);
        addTaskFromCalendar("Позвонить родителям", "18:00", "18:30", currentYear, currentMonth, currentDay, false);
        addTaskFromCalendar("Сделать уборку", "19:00", "20:00", currentYear, currentMonth, currentDay, false);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        addTaskFromCalendar("Сходить в магазин", "09:00", "10:00",
                tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH), false);
        addTaskFromCalendar("Подготовить отчет", "14:00", "16:00",
                tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH), false);
        addTaskFromCalendar("Встреча с партнерами", "11:00", "12:00",
                tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH), false);

        Calendar afterTomorrow = Calendar.getInstance();
        afterTomorrow.add(Calendar.DAY_OF_MONTH, 2);
        addTaskFromCalendar("Купить продукты", "10:00", "11:00",
                afterTomorrow.get(Calendar.YEAR), afterTomorrow.get(Calendar.MONTH), afterTomorrow.get(Calendar.DAY_OF_MONTH), false);
        addTaskFromCalendar("Созвониться", "15:00", "15:30",
                afterTomorrow.get(Calendar.YEAR), afterTomorrow.get(Calendar.MONTH), afterTomorrow.get(Calendar.DAY_OF_MONTH), false);

        Calendar day25 = Calendar.getInstance();
        day25.add(Calendar.DAY_OF_MONTH, 3);
        addTaskFromCalendar("Приготовить обед", "12:00", "13:00",
                day25.get(Calendar.YEAR), day25.get(Calendar.MONTH), day25.get(Calendar.DAY_OF_MONTH), false);

        Calendar day26 = Calendar.getInstance();
        day26.add(Calendar.DAY_OF_MONTH, 4);
        addTaskFromCalendar("Купить подарок", "16:00", "17:00",
                day26.get(Calendar.YEAR), day26.get(Calendar.MONTH), day26.get(Calendar.DAY_OF_MONTH), false);

        Calendar day27 = Calendar.getInstance();
        day27.add(Calendar.DAY_OF_MONTH, 5);
        addTaskFromCalendar("Сдать отчет", "09:00", "11:00",
                day27.get(Calendar.YEAR), day27.get(Calendar.MONTH), day27.get(Calendar.DAY_OF_MONTH), false);

        Calendar day28 = Calendar.getInstance();
        day28.add(Calendar.DAY_OF_MONTH, 6);
        addTaskFromCalendar("Встреча с клиентом", "14:00", "15:00",
                day28.get(Calendar.YEAR), day28.get(Calendar.MONTH), day28.get(Calendar.DAY_OF_MONTH), false);

        Calendar completedDate = Calendar.getInstance();
        completedDate.add(Calendar.DAY_OF_MONTH, -2);
        addTaskFromCalendar("Купить хлеб", "09:00", "10:00",
                completedDate.get(Calendar.YEAR), completedDate.get(Calendar.MONTH), completedDate.get(Calendar.DAY_OF_MONTH), true);
        addTaskFromCalendar("Помыть машину", "10:00", "11:00",
                completedDate.get(Calendar.YEAR), completedDate.get(Calendar.MONTH), completedDate.get(Calendar.DAY_OF_MONTH), true);
    }

    private void addTaskFromCalendar(String title, String startTime, String endTime, int year, int month, int day, boolean isCompleted) {
        int color = ColorHelper.getColorForTitle(title);
        allTasks.add(new CalendarTask(title, startTime, endTime, year, month, day, isCompleted, color));
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

    @Override
    protected void onTaskCreated(String title, String startTime, String endTime, int year, int month, int day) {
        addTaskFromCalendar(title, startTime, endTime, year, month, day, false);
        applyFilters();
        Toast.makeText(this, "Задача \"" + title + "\" добавлена", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onNoteCreated(String title, String content) {
        Intent intent = new Intent(this, AddNoteActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onHabitCreated(String name, String schedule) {
        Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show();
    }

    private void setupFilters() {
        View.OnClickListener filterListener = v -> {
            resetChips();
            if (v == chipAll) {
                currentFilter = "all";
                chipAll.setBackgroundResource(R.drawable.chip_selected);
                chipAll.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                tvSectionHeader.setText("Все задачи");
            } else if (v == chipToday) {
                currentFilter = "today";
                chipToday.setBackgroundResource(R.drawable.chip_selected);
                chipToday.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                tvSectionHeader.setText("На сегодня");
            } else if (v == chipTomorrow) {
                currentFilter = "tomorrow";
                chipTomorrow.setBackgroundResource(R.drawable.chip_selected);
                chipTomorrow.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                tvSectionHeader.setText("На завтра");
            } else if (v == chipWeek) {
                currentFilter = "week";
                chipWeek.setBackgroundResource(R.drawable.chip_selected);
                chipWeek.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                tvSectionHeader.setText("На эту неделю");
            } else if (v == chipCompleted) {
                currentFilter = "completed";
                chipCompleted.setBackgroundResource(R.drawable.chip_selected);
                chipCompleted.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                tvSectionHeader.setText("Выполненные задачи");
            }
            applyFilters();
        };

        chipAll.setOnClickListener(filterListener);
        chipToday.setOnClickListener(filterListener);
        chipTomorrow.setOnClickListener(filterListener);
        chipWeek.setOnClickListener(filterListener);
        chipCompleted.setOnClickListener(filterListener);

        currentFilter = "all";
        chipAll.setBackgroundResource(R.drawable.chip_selected);
        chipAll.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        tvSectionHeader.setText("Все задачи");
    }

    private void resetChips() {
        int unselectedBg = R.drawable.chip_unselected;
        int unselectedColor = ContextCompat.getColor(this, R.color.light_text);

        chipAll.setBackgroundResource(unselectedBg);
        chipAll.setTextColor(unselectedColor);
        chipToday.setBackgroundResource(unselectedBg);
        chipToday.setTextColor(unselectedColor);
        chipTomorrow.setBackgroundResource(unselectedBg);
        chipTomorrow.setTextColor(unselectedColor);
        chipWeek.setBackgroundResource(unselectedBg);
        chipWeek.setTextColor(unselectedColor);
        chipCompleted.setBackgroundResource(unselectedBg);
        chipCompleted.setTextColor(unselectedColor);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase();
                btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            currentSearchQuery = "";
            btnClearSearch.setVisibility(View.GONE);
            applyFilters();
        });
    }

    private void applyFilters() {
        filteredTasks.clear();
        Calendar now = Calendar.getInstance();
        Calendar weekLater = (Calendar) now.clone();
        weekLater.add(Calendar.DAY_OF_MONTH, 7);

        for (CalendarTask task : allTasks) {
            if (currentFilter.equals("completed")) {
                if (!task.isCompleted) continue;
            } else {
                if (task.isCompleted) continue;

                Calendar taskDate = Calendar.getInstance();
                taskDate.set(task.year, task.month, task.day);

                if (currentFilter.equals("today")) {
                    if (taskDate.get(Calendar.YEAR) != now.get(Calendar.YEAR) ||
                            taskDate.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR)) {
                        continue;
                    }
                }
                if (currentFilter.equals("tomorrow")) {
                    Calendar tomorrow = (Calendar) now.clone();
                    tomorrow.add(Calendar.DAY_OF_MONTH, 1);
                    if (taskDate.get(Calendar.YEAR) != tomorrow.get(Calendar.YEAR) ||
                            taskDate.get(Calendar.DAY_OF_YEAR) != tomorrow.get(Calendar.DAY_OF_YEAR)) {
                        continue;
                    }
                }
                if (currentFilter.equals("week")) {
                    if (taskDate.before(now) || taskDate.after(weekLater)) {
                        continue;
                    }
                }
            }

            if (!currentSearchQuery.isEmpty()) {
                if (!task.title.toLowerCase().contains(currentSearchQuery)) {
                    continue;
                }
            }
            filteredTasks.add(task);
        }
        displayTasks();
    }

    private void displayTasks() {
        tasksContainer.removeAllViews();

        if (filteredTasks.isEmpty()) {
            TextView emptyView = new TextView(this);
            String emptyMessage = "";
            if (currentFilter.equals("completed")) {
                emptyMessage = "Нет выполненных задач";
            } else if (currentFilter.equals("today")) {
                emptyMessage = "Нет задач на сегодня";
            } else if (currentFilter.equals("tomorrow")) {
                emptyMessage = "Нет задач на завтра";
            } else if (currentFilter.equals("week")) {
                emptyMessage = "Нет задач на эту неделю";
            } else {
                emptyMessage = "Нет активных задач";
            }
            emptyView.setText(emptyMessage);
            emptyView.setTextSize(16);
            emptyView.setTextColor(ContextCompat.getColor(this, R.color.gray));
            emptyView.setGravity(android.view.Gravity.CENTER);
            emptyView.setPadding(0, dpToPx(100), 0, 0);
            tasksContainer.addView(emptyView);
            return;
        }

        for (CalendarTask task : filteredTasks) {
            View taskView = createTaskView(task);
            tasksContainer.addView(taskView);
        }
    }

    private View createTaskView(CalendarTask task) {
        View taskView = LayoutInflater.from(this)
                .inflate(R.layout.item_task, tasksContainer, false);

        TextView tvTitle = taskView.findViewById(R.id.tvTitle);
        TextView tvTimeRange = taskView.findViewById(R.id.tvTimeRange);
        TextView tvDate = taskView.findViewById(R.id.tvDate);
        TextView tvTime = taskView.findViewById(R.id.tvTime);
        LinearLayout layoutTime = taskView.findViewById(R.id.layoutTime);
        TextView btnToggleComplete = taskView.findViewById(R.id.btnToggleComplete);

        View colorBar = taskView.findViewById(R.id.colorBar);
        LinearLayout contentLayout = taskView.findViewById(R.id.contentLayout);

        int taskColor = task.color;
        String hexColor = String.format("#%06X", (0xFFFFFF & taskColor));
        String transparentColor = "#33" + hexColor.substring(1);

        if (colorBar != null) {
            colorBar.setBackgroundColor(taskColor);
        }

        if (contentLayout != null) {
            contentLayout.setBackgroundColor(android.graphics.Color.parseColor(transparentColor));
        }

        tvTitle.setText(task.title);
        tvTitle.setAlpha(task.isCompleted ? 0.5f : 1.0f);

        tvTimeRange.setText(task.startTime + " – " + task.endTime);
        tvTimeRange.setAlpha(task.isCompleted ? 0.5f : 1.0f);

        tvDate.setText(task.getDateString());
        tvDate.setAlpha(task.isCompleted ? 0.5f : 1.0f);
        layoutTime.setVisibility(View.GONE);

        if (task.isCompleted) {
            btnToggleComplete.setText("Восстановить");
            btnToggleComplete.setBackgroundResource(R.drawable.mark_button_background_cancel);
            btnToggleComplete.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            btnToggleComplete.setText("Выполнить");
            btnToggleComplete.setBackgroundResource(R.drawable.mark_button_background);
            btnToggleComplete.setTextColor(ContextCompat.getColor(this, R.color.light_text));
        }

        btnToggleComplete.setOnClickListener(v -> {
            task.isCompleted = !task.isCompleted;
            applyFilters();

            String message = task.isCompleted ? "✓ Задача выполнена" : "↺ Задача восстановлена";
            Toast.makeText(TasksActivity.this, message, Toast.LENGTH_SHORT).show();
        });

        // Открытие редактора при клике
        taskView.setOnClickListener(v -> {
            Intent intent = new Intent(TasksActivity.this, TaskEditorActivity.class);
            intent.putExtra("task_title", task.title);
            intent.putExtra("task_description", task.description);
            intent.putExtra("task_color", task.color);
            intent.putExtra("task_date", task.timestamp);
            intent.putExtra("task_start_time", getStartTimeMillis(task));
            intent.putExtra("task_end_time", getEndTimeMillis(task));
            intent.putExtra("task_id", task.hashCode());
            startActivityForResult(intent, 200);
        });

        return taskView;
    }


    private long getStartTimeMillis(CalendarTask task) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(task.year, task.month, task.day);
            String[] timeParts = task.startTime.split(":");
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private long getEndTimeMillis(CalendarTask task) {
        if (task.endTime == null || task.endTime.isEmpty()) return -1;
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(task.year, task.month, task.day);
            String[] timeParts = task.endTime.split(":");
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return -1;
        }
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
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_tasks);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}