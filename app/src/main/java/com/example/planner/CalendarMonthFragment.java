package com.example.planner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarMonthFragment extends Fragment {

    private LinearLayout calendarGrid;
    private int currentYear, currentMonth;
    private Map<String, List<String>> tasksMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_month, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarGrid = view.findViewById(R.id.calendarGrid);

        // Инициализация
        Calendar currentCalendar = Calendar.getInstance();
        currentYear = currentCalendar.get(Calendar.YEAR);
        currentMonth = currentCalendar.get(Calendar.MONTH);

        // Тестовые задачи
        initTestTasks();

        // Отрисовка календаря
        drawCalendar();
    }

    private void initTestTasks() {
        tasksMap = new HashMap<>();

        // Задачи для февраля 2026 (месяц 1 = февраль, так как январь = 0)
        addTask(2026, 1, 2, "Купить продукты");
        addTask(2026, 1, 2, "Созвониться");
        addTask(2026, 1, 2, "Прогуляться");
        addTask(2026, 1, 3, "Приготовить");
        addTask(2026, 1, 3, "Уборка");
        addTask(2026, 1, 4, "Купить подарок");
        addTask(2026, 1, 4, "Учеба");
        addTask(2026, 1, 5, "Купить");
        addTask(2026, 1, 5, "Уборка");
        addTask(2026, 1, 5, "Гости");
        addTask(2026, 1, 5, "Учеба");
        addTask(2026, 1, 6, "Купить");
        addTask(2026, 1, 6, "Прогулка");
        addTask(2026, 1, 7, "Купить");
        addTask(2026, 1, 7, "Учеба");
        addTask(2026, 1, 8, "Купить");
        addTask(2026, 1, 9, "Купить");
        addTask(2026, 1, 9, "Учеба");
        addTask(2026, 1, 10, "Купить");
        addTask(2026, 1, 10, "Забрать");
        addTask(2026, 1, 11, "Купить");
        addTask(2026, 1, 11, "Прогулка");
        addTask(2026, 1, 12, "Купить");
        addTask(2026, 1, 12, "Учеба");
        addTask(2026, 1, 13, "Купить");
        addTask(2026, 1, 13, "Созвониться");
        addTask(2026, 1, 14, "Купить");
        addTask(2026, 1, 15, "Купить");
        addTask(2026, 1, 16, "Купить");
        addTask(2026, 1, 16, "Созвониться");
        addTask(2026, 1, 16, "Прогулка");
        addTask(2026, 1, 17, "Купить");
        addTask(2026, 1, 17, "Созвониться");
        addTask(2026, 1, 17, "Уборка");
        addTask(2026, 1, 18, "Купить");
        addTask(2026, 1, 18, "Приготовить");
        addTask(2026, 1, 18, "Учеба");
        addTask(2026, 1, 19, "Купить");
        addTask(2026, 1, 19, "Учеба");
        addTask(2026, 1, 20, "Купить");
        addTask(2026, 1, 20, "Уборка");
        addTask(2026, 1, 20, "Гости");
        addTask(2026, 1, 20, "Учеба");
        addTask(2026, 1, 21, "Купить");
        addTask(2026, 1, 22, "Купить");
        addTask(2026, 1, 23, "Купить");
        addTask(2026, 1, 24, "Здоровье");
        addTask(2026, 1, 24, "Купить");
        addTask(2026, 1, 25, "Здоровье");
        addTask(2026, 1, 25, "Купить");
        addTask(2026, 1, 26, "Здоровье");
        addTask(2026, 1, 26, "Купить");
        addTask(2026, 1, 27, "Здоровье");
        addTask(2026, 1, 27, "Купить");
        addTask(2026, 1, 28, "Здоровье");
        addTask(2026, 1, 28, "Купить");
    }

    private void addTask(int year, int month, int day, String task) {
        String key = year + "-" + month + "-" + day;
        if (!tasksMap.containsKey(key)) {
            tasksMap.put(key, new ArrayList<>());
        }
        tasksMap.get(key).add(task);
    }

    private void drawCalendar() {
        calendarGrid.removeAllViews();

        // Получаем первый день месяца
        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        // Сдвигаем чтобы понедельник был первым
        int offset = (firstDayOfWeek - 2 + 7) % 7;

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int currentDay = 1;
        boolean nextMonthStarted = false;

        // 6 строк для 6 возможных недель в месяце
        for (int row = 0; row < 6; row++) {
            LinearLayout weekRow = new LinearLayout(getContext());
            weekRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            weekRow.setOrientation(LinearLayout.HORIZONTAL);

            for (int col = 0; col < 7; col++) {
                View cellView = LayoutInflater.from(getContext())
                        .inflate(R.layout.month_cell_layout, weekRow, false);

                TextView tvDayNumber = cellView.findViewById(R.id.tvDayNumber);
                LinearLayout tasksContainer = cellView.findViewById(R.id.tasksContainer);

                int dayNumber;
                boolean isCurrentMonth;

                if (row == 0 && col < offset) {
                    // Дни предыдущего месяца
                    Calendar prevCal = (Calendar) cal.clone();
                    prevCal.add(Calendar.MONTH, -1);
                    int prevDaysInMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    dayNumber = prevDaysInMonth - (offset - col) + 1;
                    isCurrentMonth = false;
                    tvDayNumber.setTextColor(ContextCompat.getColor(getContext(), R.color.gray));
                } else if (nextMonthStarted || currentDay > daysInMonth) {
                    // Дни следующего месяца
                    if (!nextMonthStarted) {
                        currentDay = 1;
                        nextMonthStarted = true;
                    }
                    dayNumber = currentDay;
                    isCurrentMonth = false;
                    tvDayNumber.setTextColor(ContextCompat.getColor(getContext(), R.color.gray));
                    currentDay++;
                } else {
                    // Дни текущего месяца
                    dayNumber = currentDay;
                    isCurrentMonth = true;

                    // Проверяем текущую дату
                    Calendar today = Calendar.getInstance();
                    if (today.get(Calendar.YEAR) == currentYear &&
                            today.get(Calendar.MONTH) == currentMonth &&
                            today.get(Calendar.DAY_OF_MONTH) == dayNumber) {
                        tvDayNumber.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    } else {
                        tvDayNumber.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
                    }
                    currentDay++;
                }

                tvDayNumber.setText(String.valueOf(dayNumber));

                // Добавляем задачи для этого дня
                if (isCurrentMonth) {
                    String key = currentYear + "-" + currentMonth + "-" + dayNumber;
                    List<String> tasks = tasksMap.get(key);
                    if (tasks != null) {
                        for (String task : tasks) {
                            TextView taskView = new TextView(getContext());
                            taskView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            taskView.setText(task);
                            taskView.setTextSize(13);
                            taskView.setTextColor(ContextCompat.getColor(getContext(), R.color.light_text));
                            taskView.setMaxLines(1);
                            taskView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                            taskView.setPadding(0, 4, 0, 4);
                            tasksContainer.addView(taskView);
                        }
                    }
                }

                weekRow.addView(cellView);
            }

            calendarGrid.addView(weekRow);
        }
    }

    public void updateCalendar(int year, int month) {
        this.currentYear = year;
        this.currentMonth = month;
        drawCalendar();
    }
}