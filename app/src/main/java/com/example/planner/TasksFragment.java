package com.example.planner;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment {

    private LinearLayout tasksContainer;
    private EditText etSearch;
    private ImageView btnClearSearch;
    private TextView tvSectionHeader;

    private TextView chipAll, chipToday, chipTomorrow, chipWeek, chipCompleted, chipUncompleted;

    private List<TaskItem> allTasks = new ArrayList<>();
    private List<TaskItem> filteredTasks = new ArrayList<>();

    private String currentFilter = "all";
    private String currentSearchQuery = "";

    private Calendar today = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", new Locale("ru"));
    private SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ru"));

    public static class TaskItem {
        String title;
        String description;
        String date;
        String time;
        boolean isCompleted;
        long timestamp;

        public TaskItem(String title, String description, String date, String time, boolean isCompleted, long timestamp) {
            this.title = title;
            this.description = description;
            this.date = date;
            this.time = time;
            this.isCompleted = isCompleted;
            this.timestamp = timestamp;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация элементов интерфейса
        tasksContainer = view.findViewById(R.id.tasksContainer);
        etSearch = view.findViewById(R.id.etSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        tvSectionHeader = view.findViewById(R.id.tvSectionHeader);

        chipAll = view.findViewById(R.id.chipAll);
        chipToday = view.findViewById(R.id.chipToday);
        chipTomorrow = view.findViewById(R.id.chipTomorrow);
        chipWeek = view.findViewById(R.id.chipWeek);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipUncompleted = view.findViewById(R.id.chipUncompleted);

        // Скрываем FAB в родительской активности (он есть в оболочке)
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            com.google.android.material.floatingactionbutton.FloatingActionButton fab =
                    activity.findViewById(R.id.fabAdd);
            if (fab != null) {
                fab.setVisibility(View.GONE);
            }
        }

        initTestTasks();
        setupFilters();
        setupSearch();
        applyFilters();

        // Навигация обрабатывается ТОЛЬКО в MainActivity - не добавляем здесь!
    }

    private void initTestTasks() {
        Calendar cal = Calendar.getInstance();

        // Сегодняшние задачи
        allTasks.add(new TaskItem("Сходить за продуктами",
                "В магазине \"Магнит\" купить:\n- Макароны спагетти\n- Томатная паста",
                "Сегодня", "", false, cal.getTimeInMillis()));
        allTasks.add(new TaskItem("Утренний созвон", "Без описания",
                "Сегодня", "12:00", false, cal.getTimeInMillis()));
        allTasks.add(new TaskItem("Запись к стоматологу",
                "ул. Голубкова, д. 44, не забыть прийти на прием!!!",
                "Сегодня", "15:00", false, cal.getTimeInMillis()));

        // Завтрашние задачи
        cal.add(Calendar.DAY_OF_MONTH, 1);
        allTasks.add(new TaskItem("Утренний созвон", "Без описания",
                "Завтра", "11:00", false, cal.getTimeInMillis()));
        allTasks.add(new TaskItem("Приготовить обед",
                "Рецепт пасты болоньезе на 3–4 порции:\n- макароны — 300 г\n- фарш — 400 г",
                "Завтра", "12:00", false, cal.getTimeInMillis()));

        // Задачи на неделю
        cal.add(Calendar.DAY_OF_MONTH, 2);
        allTasks.add(new TaskItem("Позвонить родителям", "Узнать как дела",
                formatDate(cal), "19:00", false, cal.getTimeInMillis()));
        cal.add(Calendar.DAY_OF_MONTH, 3);
        allTasks.add(new TaskItem("Сдать отчет", "Подготовить квартальный отчет",
                formatDate(cal), "14:00", false, cal.getTimeInMillis()));

        // Выполненные задачи
        allTasks.add(new TaskItem("Купить хлеб", "", "Вчера", "", true, cal.getTimeInMillis()));
        allTasks.add(new TaskItem("Помыть машину", "", "Вчера", "10:00", true, cal.getTimeInMillis()));
    }

    private String formatDate(Calendar cal) {
        if (isSameDay(cal, Calendar.getInstance())) return "Сегодня";
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        if (isSameDay(cal, tomorrow)) return "Завтра";
        return dayFormat.format(cal.getTime()) + ", " + dateFormat.format(cal.getTime());
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void setupFilters() {
        View.OnClickListener filterListener = v -> {
            resetChips();
            if (v == chipAll) {
                currentFilter = "all";
                chipAll.setBackgroundResource(R.drawable.chip_selected);
                chipAll.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                tvSectionHeader.setText("По текущей дате...");
            } else if (v == chipToday) {
                currentFilter = "today";
                chipToday.setBackgroundResource(R.drawable.chip_selected);
                chipToday.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                tvSectionHeader.setText("На сегодня");
            } else if (v == chipTomorrow) {
                currentFilter = "tomorrow";
                chipTomorrow.setBackgroundResource(R.drawable.chip_selected);
                chipTomorrow.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                tvSectionHeader.setText("На завтра");
            } else if (v == chipWeek) {
                currentFilter = "week";
                chipWeek.setBackgroundResource(R.drawable.chip_selected);
                chipWeek.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                tvSectionHeader.setText("На эту неделю");
            } else if (v == chipCompleted) {
                currentFilter = "completed";
                chipCompleted.setBackgroundResource(R.drawable.chip_selected);
                chipCompleted.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                tvSectionHeader.setText("Выполненные задачи");
            } else if (v == chipUncompleted) {
                currentFilter = "uncompleted";
                chipUncompleted.setBackgroundResource(R.drawable.chip_selected);
                chipUncompleted.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                tvSectionHeader.setText("Активные задачи");
            }
            applyFilters();
        };

        chipAll.setOnClickListener(filterListener);
        chipToday.setOnClickListener(filterListener);
        chipTomorrow.setOnClickListener(filterListener);
        chipWeek.setOnClickListener(filterListener);
        chipCompleted.setOnClickListener(filterListener);
        chipUncompleted.setOnClickListener(filterListener);
    }

    private void resetChips() {
        int unselectedBg = R.drawable.chip_unselected;
        int unselectedColor = ContextCompat.getColor(requireContext(), R.color.light_text);

        chipAll.setBackgroundResource(unselectedBg); chipAll.setTextColor(unselectedColor);
        chipToday.setBackgroundResource(unselectedBg); chipToday.setTextColor(unselectedColor);
        chipTomorrow.setBackgroundResource(unselectedBg); chipTomorrow.setTextColor(unselectedColor);
        chipWeek.setBackgroundResource(unselectedBg); chipWeek.setTextColor(unselectedColor);
        chipCompleted.setBackgroundResource(unselectedBg); chipCompleted.setTextColor(unselectedColor);
        chipUncompleted.setBackgroundResource(unselectedBg); chipUncompleted.setTextColor(unselectedColor);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase();
                btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
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

        for (TaskItem task : allTasks) {
            if (currentFilter.equals("completed") && !task.isCompleted) continue;
            if (currentFilter.equals("uncompleted") && task.isCompleted) continue;
            if (currentFilter.equals("today") && !task.date.equals("Сегодня")) continue;
            if (currentFilter.equals("tomorrow") && !task.date.equals("Завтра")) continue;

            if (currentFilter.equals("week")) {
                if (!task.date.equals("Сегодня") && !task.date.equals("Завтра")) {
                    if (!(task.timestamp > now.getTimeInMillis() && task.timestamp <= weekLater.getTimeInMillis())) {
                        continue;
                    }
                }
            }

            if (!currentSearchQuery.isEmpty()) {
                if (!task.title.toLowerCase().contains(currentSearchQuery) &&
                        !task.description.toLowerCase().contains(currentSearchQuery)) {
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
            TextView emptyView = new TextView(getContext());
            emptyView.setText("Нет задач");
            emptyView.setTextSize(16);
            emptyView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            emptyView.setGravity(android.view.Gravity.CENTER);
            emptyView.setPadding(0, dpToPx(100), 0, 0);
            tasksContainer.addView(emptyView);
            return;
        }

        for (TaskItem task : filteredTasks) {
            View taskView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_task, tasksContainer, false);

            CheckBox cbCompleted = taskView.findViewById(R.id.cbCompleted);
            TextView tvTitle = taskView.findViewById(R.id.tvTitle);
            TextView tvDescription = taskView.findViewById(R.id.tvDescription);
            TextView tvDate = taskView.findViewById(R.id.tvDate);
            TextView tvTime = taskView.findViewById(R.id.tvTime);
            LinearLayout layoutTime = taskView.findViewById(R.id.layoutTime);
            TextView btnMarkComplete = taskView.findViewById(R.id.btnMarkComplete);

            cbCompleted.setChecked(task.isCompleted);
            tvTitle.setText(task.title);
            tvTitle.setAlpha(task.isCompleted ? 0.5f : 1.0f);

            if (task.description.isEmpty()) {
                tvDescription.setVisibility(View.GONE);
            } else {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(task.description);
                tvDescription.setAlpha(task.isCompleted ? 0.5f : 1.0f);
            }

            tvDate.setText(task.date);
            tvDate.setAlpha(task.isCompleted ? 0.5f : 1.0f);

            if (task.time.isEmpty()) {
                layoutTime.setVisibility(View.GONE);
            } else {
                layoutTime.setVisibility(View.VISIBLE);
                tvTime.setText(task.time);
                tvTime.setAlpha(task.isCompleted ? 0.5f : 1.0f);
            }

            if (task.isCompleted) {
                btnMarkComplete.setText("Выполнено");
                btnMarkComplete.setEnabled(false);
                btnMarkComplete.setAlpha(0.5f);
            } else {
                btnMarkComplete.setText("Отметить как выполненное");
                btnMarkComplete.setEnabled(true);
                btnMarkComplete.setAlpha(1.0f);
            }

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.isCompleted = isChecked;
                applyFilters();
            });

            btnMarkComplete.setOnClickListener(v -> {
                task.isCompleted = true;
                applyFilters();
            });

            tasksContainer.addView(taskView);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}