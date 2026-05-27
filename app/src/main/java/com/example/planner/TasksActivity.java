package com.example.planner;

import android.content.Intent;
import android.graphics.Color;
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
import androidx.lifecycle.ViewModelProvider;

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

    private TaskViewModel taskViewModel;
    private String currentFilter = "all";  // 🔥 Убран пробел
    private String currentSearchQuery = "";  // 🔥 Убран пробел

    // 🔥 Храним сырые данные из БД для повторной фильтрации
    private List<Task> allTasksFromDb = new ArrayList<>();

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));  // 🔥 Убран пробел
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", new Locale("ru"));
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ru"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tasks);

        View rootView = findViewById(R.id.coordinatorLayout);
        bottomNav = findViewById(R.id.bottomNavigationView);
        setupAllInsets(rootView, bottomNav);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // 🔥 Подписка: сохраняем данные и сразу отображаем
        taskViewModel.getAllTasks().observe(this, tasks -> {
            allTasksFromDb = tasks != null ? tasks : new ArrayList<>();
            applyFiltersAndDisplay();  // 🔥 Применяем текущие фильтры
        });

        initViews();
        setupFilters();
        setupSearch();
        setupBottomNavigation();
        setupModal();
    }

    // 🔥 НОВЫЙ метод: фильтрация + отображение
    private void applyFiltersAndDisplay() {
        List<Task> filtered = filterTasks(allTasksFromDb);
        displayFilteredTasks(filtered);
    }

    // 🔥 Фильтрация (исправлены опечатки)
    private List<Task> filterTasks(List<Task> tasks) {
        List<Task> result = new ArrayList<>();
        Calendar now = Calendar.getInstance();

        // 🔥 Обнуляем время для корректного сравнения только по дням
        Calendar startOfToday = Calendar.getInstance();
        startOfToday.set(Calendar.HOUR_OF_DAY, 0);
        startOfToday.set(Calendar.MINUTE, 0);
        startOfToday.set(Calendar.SECOND, 0);
        startOfToday.set(Calendar.MILLISECOND, 0);

        Calendar weekEnd = (Calendar) startOfToday.clone();
        weekEnd.add(Calendar.DAY_OF_MONTH, 7);
        weekEnd.set(Calendar.HOUR_OF_DAY, 23);
        weekEnd.set(Calendar.MINUTE, 59);
        weekEnd.set(Calendar.SECOND, 59);

        for (Task task : tasks) {
            // Фильтр по выполнению
            if (currentFilter.equals("completed")) {
                if (!task.isCompleted) continue;
            } else {
                if (task.isCompleted) continue;
            }

            Calendar taskDate = Calendar.getInstance();
            taskDate.setTimeInMillis(task.date);

            if (currentFilter.equals("today")) {
                if (!isSameDay(taskDate, now)) continue;
            } else if (currentFilter.equals("tomorrow")) {
                Calendar tomorrow = (Calendar) now.clone();
                tomorrow.add(Calendar.DAY_OF_MONTH, 1);
                if (!isSameDay(taskDate, tomorrow)) continue;
            } else if (currentFilter.equals("week")) {
                //  Теперь сегодня входит в "эту неделю"
                if (taskDate.before(startOfToday) || taskDate.after(weekEnd)) continue;
            }

            if (!currentSearchQuery.isEmpty() &&
                    !task.title.toLowerCase().contains(currentSearchQuery)) {
                continue;
            }

            result.add(task);
        }
        return result;
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    // 🔥 Отображение отфильтрованных задач
    private void displayFilteredTasks(List<Task> tasks) {
        tasksContainer.removeAllViews();

        if (tasks.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Task task : tasks) {
            View taskView = createTaskView(task);
            tasksContainer.addView(taskView);
        }
    }

    private void showEmptyState() {
        TextView emptyView = new TextView(this);
        String message = getEmptyMessage();
        emptyView.setText(message);
        emptyView.setTextSize(16);
        emptyView.setTextColor(ContextCompat.getColor(this, R.color.gray));
        emptyView.setGravity(android.view.Gravity.CENTER);
        emptyView.setPadding(0, dpToPx(100), 0, 0);
        tasksContainer.addView(emptyView);
    }

    private String getEmptyMessage() {
        switch (currentFilter) {
            case "completed": return "Нет выполненных задач";
            case "today": return "Нет задач на сегодня";
            case "tomorrow": return "Нет задач на завтра";
            case "week": return "Нет задач на эту неделю";
            default: return "Нет активных задач";
        }
    }

    // 🔥 Создание view задачи (исправлены опечатки)
    private View createTaskView(Task task) {
        View taskView = LayoutInflater.from(this)
                .inflate(R.layout.item_task, tasksContainer, false);

        String hexColor = String.format("#%06X", (0xFFFFFF & task.color));
        String transparentColor = "#26" + hexColor.substring(1);
        taskView.setBackgroundColor(Color.parseColor(transparentColor));

        TextView tvTitle = taskView.findViewById(R.id.tvTitle);
        TextView tvTimeRange = taskView.findViewById(R.id.tvTimeRange);
        TextView tvDate = taskView.findViewById(R.id.tvDate);
        TextView btnToggleComplete = taskView.findViewById(R.id.btnToggleComplete);
        View colorBar = taskView.findViewById(R.id.colorBar);

        tvTitle.setText(task.title);
        tvTitle.setAlpha(task.isCompleted ? 0.5f : 1f);

        String timeText = formatMinutes(task.startTime);
        if (task.endTime >= 0) {
            timeText += " – " + formatMinutes(task.endTime);
        }
        tvTimeRange.setText(timeText);
        tvTimeRange.setAlpha(task.isCompleted ? 0.5f : 1f);

        Calendar taskCal = Calendar.getInstance();
        taskCal.setTimeInMillis(task.date);
        tvDate.setText(formatTaskDate(taskCal));
        tvDate.setAlpha(task.isCompleted ? 0.5f : 1f);

        if (colorBar != null) colorBar.setBackgroundColor(task.color);

        btnToggleComplete.setText(task.isCompleted ? "Восстановить" : "Выполнить");
        btnToggleComplete.setBackgroundResource(
                task.isCompleted ? R.drawable.mark_button_background_cancel : R.drawable.mark_button_background);
        btnToggleComplete.setOnClickListener(v -> {
            taskViewModel.toggleCompletion(task.id, !task.isCompleted);
            String msg = task.isCompleted ? "✓ Задача восстановлена" : "✓ Задача выполнена";
            Toast.makeText(TasksActivity.this, msg, Toast.LENGTH_SHORT).show();
        });

        taskView.setOnClickListener(v -> {
            Intent intent = new Intent(TasksActivity.this, TaskEditorActivity.class);
            intent.putExtra("task_id", task.id);  // 🔥 Убран пробел
            startActivity(intent);
        });

        return taskView;
    }

    private String formatMinutes(int minutes) {
        if (minutes < 0) return "";
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private String formatTaskDate(Calendar cal) {
        Calendar today = Calendar.getInstance();
        if (isSameDay(cal, today)) return "Сегодня";

        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        if (isSameDay(cal, tomorrow)) return "Завтра";

        return dayFormat.format(cal.getTime()) + ", " + dateFormat.format(cal.getTime());
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

    // 🔥 Настройка фильтров (исправлены опечатки)
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
            // 🔥 Перезапускаем фильтрацию
            applyFiltersAndDisplay();
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
        chipAll.setBackgroundResource(unselectedBg); chipAll.setTextColor(unselectedColor);
        chipToday.setBackgroundResource(unselectedBg); chipToday.setTextColor(unselectedColor);
        chipTomorrow.setBackgroundResource(unselectedBg); chipTomorrow.setTextColor(unselectedColor);
        chipWeek.setBackgroundResource(unselectedBg); chipWeek.setTextColor(unselectedColor);
        chipCompleted.setBackgroundResource(unselectedBg); chipCompleted.setTextColor(unselectedColor);
    }

    // 🔥 Настройка поиска (исправлены опечатки)
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase();
                btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFiltersAndDisplay();  // 🔥 Перезапускаем фильтрацию
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            currentSearchQuery = "";
            btnClearSearch.setVisibility(View.GONE);
            applyFiltersAndDisplay();  // 🔥 Перезапускаем фильтрацию
        });
    }

    // 🔥 Остальные методы (модалка, навигация) — без изменений, только исправлены опечатки
    private void setupModal() {
        if (fabAdd != null) fabAdd.setOnClickListener(v -> showModal());
        if (dimOverlay != null) dimOverlay.setOnClickListener(v -> hideModal());
        setupModalButtons();
    }

    private void setupModalButtons() {
        if (modalPopup == null) return;
        modalPopup.findViewById(R.id.btnAddTask).setOnClickListener(v -> {
            hideModal();
            Intent intent = new Intent(TasksActivity.this, TaskEditorActivity.class);
            intent.putExtra("task_id", -1L);
            startActivity(intent);
        });
        modalPopup.findViewById(R.id.btnAddNote).setOnClickListener(v -> {
            hideModal();
            showCreateNoteDialog();
        });
        modalPopup.findViewById(R.id.btnAddHabit).setOnClickListener(v -> {
            hideModal();
            showCreateHabitDialog();
        });
    }

    private void showModal() {
        if (dimOverlay != null && modalPopup != null) {
            dimOverlay.setVisibility(View.VISIBLE);
            modalPopup.setVisibility(View.VISIBLE);
            dimOverlay.setAlpha(0f);
            modalPopup.setScaleX(0.8f); modalPopup.setScaleY(0.8f); modalPopup.setAlpha(0f);
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

    private void setupBottomNavigation() {
        if (bottomNav == null) return;
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) { startActivity(new Intent(this, CalendarActivity.class)); overridePendingTransition(0,0); finish(); return true; }
            else if (id == R.id.nav_tasks) { return true; }
            else if (id == R.id.nav_notes) { startActivity(new Intent(this, NotesActivity.class)); overridePendingTransition(0,0); finish(); return true; }
            else if (id == R.id.nav_habits) { startActivity(new Intent(this, HabitsActivity.class)); overridePendingTransition(0,0); finish(); return true; }
            else if (id == R.id.nav_settings) { startActivity(new Intent(this, SettingsActivity.class)); overridePendingTransition(0,0); return true; }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_tasks);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onTaskCreated(String title, String startTime, String endTime, int year, int month, int day) {
        // Больше не нужно — задачи создаются напрямую в БД
    }

    @Override
    protected void onNoteCreated(String title, String content) {
        showCreateNoteDialog();
    }

    @Override
    protected void onHabitCreated(String name, String schedule) {
        Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show();
    }
}