package com.example.planner;

import android.graphics.Paint;
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
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarMonthFragment extends Fragment {
    private LinearLayout calendarGrid;
    private int currentYear, currentMonth;
    private TaskViewModel taskViewModel;
    private List<Task> allTasksCache = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_month, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        calendarGrid = view.findViewById(R.id.calendarGrid);

        Calendar currentCalendar = Calendar.getInstance();
        currentYear = currentCalendar.get(Calendar.YEAR);
        currentMonth = currentCalendar.get(Calendar.MONTH);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasksCache.clear();
            if (tasks != null) allTasksCache.addAll(tasks);
            drawCalendar();
        });
    }

    private void drawCalendar() {
        if (calendarGrid == null) return;
        calendarGrid.removeAllViews();

        Calendar cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, 1);
        int offset = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = 1; boolean nextMonthStarted = false;

        for (int row = 0; row < 6; row++) {
            LinearLayout weekRow = new LinearLayout(getContext());
            weekRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            weekRow.setOrientation(LinearLayout.HORIZONTAL);

            for (int col = 0; col < 7; col++) {
                View cellView = LayoutInflater.from(getContext()).inflate(R.layout.month_cell_layout, weekRow, false);
                TextView tvDayNumber = cellView.findViewById(R.id.tvDayNumber);
                LinearLayout tasksContainer = cellView.findViewById(R.id.tasksContainer);
                tasksContainer.removeAllViews();

                int dayNumber; boolean isCurrentMonth;
                if (row == 0 && col < offset) {
                    Calendar prevCal = (Calendar) cal.clone(); prevCal.add(Calendar.MONTH, -1);
                    dayNumber = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH) - (offset - col) + 1;
                    isCurrentMonth = false; tvDayNumber.setTextColor(ContextCompat.getColor(getContext(), R.color.gray));
                } else if (nextMonthStarted || currentDay > daysInMonth) {
                    if (!nextMonthStarted) { currentDay = 1; nextMonthStarted = true; }
                    dayNumber = currentDay; isCurrentMonth = false; tvDayNumber.setTextColor(ContextCompat.getColor(getContext(), R.color.gray));
                    currentDay++;
                } else {
                    dayNumber = currentDay; isCurrentMonth = true;
                    Calendar today = Calendar.getInstance();
                    if (today.get(Calendar.YEAR) == currentYear && today.get(Calendar.MONTH) == currentMonth && today.get(Calendar.DAY_OF_MONTH) == dayNumber)
                        tvDayNumber.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    else tvDayNumber.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
                    currentDay++;
                }
                tvDayNumber.setText(String.valueOf(dayNumber));

                if (isCurrentMonth) {
                    for (Task t : allTasksCache) {
                        Calendar taskCal = Calendar.getInstance(); taskCal.setTimeInMillis(t.date);
                        if (taskCal.get(Calendar.YEAR) == currentYear && taskCal.get(Calendar.MONTH) == currentMonth && taskCal.get(Calendar.DAY_OF_MONTH) == dayNumber) {
                            TextView taskView = new TextView(getContext());
                            taskView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            taskView.setTextSize(13);
                            taskView.setTextColor(ContextCompat.getColor(getContext(), R.color.light_text));
                            taskView.setMaxLines(1); taskView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                            taskView.setPadding(0, 4, 0, 4);

                            // ВИЗУАЛЬНОЕ ОБОЗНАЧЕНИЕ ВЫПОЛНЕННЫХ ЗАДАЧ
                            if (t.isCompleted) {
                                taskView.setText("✓ " + t.title);
                                taskView.setPaintFlags(taskView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                taskView.setAlpha(0.5f);
                            } else {
                                taskView.setText(t.title);
                                taskView.setPaintFlags(taskView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                taskView.setAlpha(1.0f);
                            }
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