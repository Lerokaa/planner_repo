package com.example.planner;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends BaseActivity {
    private View dimOverlay;
    private LinearLayout modalPopup;
    private FloatingActionButton fabAdd;
    private TextView tvViewValue;
    private LinearLayout layoutViewSelector;
    private LinearLayout calendarGrid;
    private LinearLayout weekGrid;
    private TextView tvMonth;
    private TextView tvMonthYear;
    private LinearLayout dateStrip;
    private HorizontalScrollView dateScrollView;
    private BottomNavigationView bottomNav;

    private String currentViewType = "День";
    private Calendar currentCalendar;
    private int currentYear, currentMonth, currentDay;

    // Room Integration
    private TaskViewModel taskViewModel;
    private List<Task> allTasksCache = new ArrayList<>();

    private static class CalendarItem {
        long taskId;
        String title;
        String startTime;
        String endTime;
        boolean isCompleted;
        int startHour, startMinute, endHour, endMinute;
        int column = 0;
        int totalColumns = 1;

        CalendarItem(long taskId, String title, String startTime, String endTime, boolean isCompleted) {
            this.taskId = taskId;
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
            this.isCompleted = isCompleted;
            try {
                String[] startParts = startTime.split(":");
                String[] endParts = endTime.split(":");
                this.startHour = Integer.parseInt(startParts[0]);
                this.startMinute = Integer.parseInt(startParts[1]);
                this.endHour = Integer.parseInt(endParts[0]);
                this.endMinute = Integer.parseInt(endParts[1]);
            } catch (Exception e) {
                this.startHour = 9; this.startMinute = 0;
                this.endHour = 10; this.endMinute = 0;
            }
        }

        int getStartTotalMinutes() { return startHour * 60 + startMinute; }
        int getEndTotalMinutes() { return endHour * 60 + endMinute; }
        boolean overlaps(CalendarItem other) {
            return getStartTotalMinutes() < other.getEndTotalMinutes() && getEndTotalMinutes() > other.getStartTotalMinutes();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentCalendar = Calendar.getInstance();
        currentYear = currentCalendar.get(Calendar.YEAR);
        currentMonth = currentCalendar.get(Calendar.MONTH);
        currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // ЕДИНЫЙ источник данных для всех дней. LiveData автоматически обновляет кэш при изменении БД.
        taskViewModel.getAllTasks().observe(this, tasks -> {
            allTasksCache.clear();
            if (tasks != null) allTasksCache.addAll(tasks);
            refreshCurrentView();
        });

        showDayCalendar();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (modalPopup != null && modalPopup.getVisibility() == View.VISIBLE) {
                    hideModal();
                } else {
                    finish();
                }
            }
        });
    }

    private void refreshCurrentView() {
        runOnUiThread(() -> {
            if ("День".equals(currentViewType)) {
                LinearLayout hourlySchedule = findViewById(R.id.hourlySchedule);
                if (hourlySchedule != null) {
                    hourlySchedule.removeAllViews();
                    drawHourlySchedule();
                }
            } else if ("Неделя".equals(currentViewType)) {
                drawWeekView();
            } else if ("Месяц".equals(currentViewType)) {
                drawMonthCalendar();
            }
        });
    }

    // ЕДИНЫЙ метод получения задач для любого дня. Исключает рассинхронизацию.
    public List<CalendarItem> getCalendarItemsForDate(int year, int month, int day) {
        List<CalendarItem> result = new ArrayList<>();
        for (Task t : allTasksCache) {
            Calendar taskCal = Calendar.getInstance();
            taskCal.setTimeInMillis(t.date);
            if (taskCal.get(Calendar.YEAR) == year &&
                    taskCal.get(Calendar.MONTH) == month &&
                    taskCal.get(Calendar.DAY_OF_MONTH) == day) {

                String endTimeStr = t.getEndTimeString();
                if (endTimeStr.isEmpty() && !t.getStartTimeString().isEmpty()) {
                    try {
                        String[] parts = t.getStartTimeString().split(":");
                        int h = Integer.parseInt(parts[0]) + 1;
                        if (h >= 24) h = 23;
                        endTimeStr = String.format("%02d:%s", h, parts[1]);
                    } catch (Exception e) { endTimeStr = t.getStartTimeString(); }
                }
                result.add(new CalendarItem(t.id, t.title, t.getStartTimeString(), endTimeStr, t.isCompleted));
            }
        }
        return result;
    }

    // --- UI INITIALIZATION ---
    private void showDayCalendar() {
        currentViewType = "День";
        setContentView(R.layout.calendar_day);
        initDayViews();
        drawDayView();
        setupBottomNavigation();
        setupModalButtons();
    }

    private void showWeekCalendar() {
        currentViewType = "Неделя";
        setContentView(R.layout.calendar_week);
        initWeekViews();
        drawWeekView();
        setupBottomNavigation();
        setupModalButtons();
    }

    private void showMonthCalendar() {
        currentViewType = "Месяц";
        setContentView(R.layout.calendar_month);
        initMonthViews();
        drawMonthCalendar();
        setupBottomNavigation();
        setupModalButtons();
    }

    private void initDayViews() {
        View root = findViewById(R.id.coordinatorLayout);
        if (root != null) setupInsets(root, false);
        dimOverlay = findViewById(R.id.dimOverlay);
        modalPopup = findViewById(R.id.modalPopup);
        fabAdd = findViewById(R.id.fabAdd);
        tvViewValue = findViewById(R.id.tvViewValue);
        layoutViewSelector = findViewById(R.id.layoutViewSelector);
        tvMonth = findViewById(R.id.tvMonth);
        dateStrip = findViewById(R.id.dateStrip);
        dateScrollView = findViewById(R.id.dateScrollView);
        bottomNav = findViewById(R.id.bottomNavigationView);

        if (tvViewValue != null) tvViewValue.setText("День");
        if (fabAdd != null) fabAdd.setOnClickListener(v -> showModal());
        if (dimOverlay != null) dimOverlay.setOnClickListener(v -> hideModal());
        if (layoutViewSelector != null) layoutViewSelector.setOnClickListener(v -> showViewTypeDialog());
    }

    private void initWeekViews() {
        View root = findViewById(R.id.coordinatorLayout);
        if (root != null) setupInsets(root, false);
        dimOverlay = findViewById(R.id.dimOverlay);
        modalPopup = findViewById(R.id.modalPopup);
        fabAdd = findViewById(R.id.fabAdd);
        tvViewValue = findViewById(R.id.tvViewValue);
        layoutViewSelector = findViewById(R.id.layoutViewSelector);
        weekGrid = findViewById(R.id.weekGrid);
        tvMonth = findViewById(R.id.tvMonth);
        dateStrip = findViewById(R.id.dateStrip);
        dateScrollView = findViewById(R.id.dateScrollView);
        bottomNav = findViewById(R.id.bottomNavigationView);

        if (tvViewValue != null) tvViewValue.setText("Неделя");
        if (fabAdd != null) fabAdd.setOnClickListener(v -> showModal());
        if (dimOverlay != null) dimOverlay.setOnClickListener(v -> hideModal());
        if (layoutViewSelector != null) layoutViewSelector.setOnClickListener(v -> showViewTypeDialog());
    }

    private void initMonthViews() {
        View root = findViewById(R.id.coordinatorLayout);
        if (root != null) setupInsets(root, false);
        dimOverlay = findViewById(R.id.dimOverlay);
        modalPopup = findViewById(R.id.modalPopup);
        fabAdd = findViewById(R.id.fabAdd);
        tvViewValue = findViewById(R.id.tvViewValue);
        layoutViewSelector = findViewById(R.id.layoutViewSelector);
        calendarGrid = findViewById(R.id.calendarGrid);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        dateStrip = findViewById(R.id.dateStrip);
        dateScrollView = findViewById(R.id.dateScrollView);
        bottomNav = findViewById(R.id.bottomNavigationView);

        if (tvViewValue != null) tvViewValue.setText("Месяц");
        if (fabAdd != null) fabAdd.setOnClickListener(v -> showModal());
        if (dimOverlay != null) dimOverlay.setOnClickListener(v -> hideModal());
        if (layoutViewSelector != null) layoutViewSelector.setOnClickListener(v -> showViewTypeDialog());
        updateMonthHeader();
    }

    private void setupInsets(View view, boolean includeBottom) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, includeBottom ? bars.bottom : 0);
            return insets;
        });
    }

    private void drawDayView() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("ru"));
        if (tvMonth != null) {
            String month = monthFormat.format(currentCalendar.getTime());
            month = month.substring(0, 1).toUpperCase() + month.substring(1);
            tvMonth.setText(month);
        }
        drawDayDateStrip();
        drawHourlySchedule();
    }

    private void drawDayDateStrip() {
        if (dateStrip == null) return;
        dateStrip.removeAllViews();
        Calendar startCal = (Calendar) currentCalendar.clone();
        startCal.add(Calendar.DAY_OF_MONTH, -14);
        String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        int selectedIndex = -1;
        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 28; i++) {
            int year = startCal.get(Calendar.YEAR);
            int month = startCal.get(Calendar.MONTH);
            int day = startCal.get(Calendar.DAY_OF_MONTH);
            int dayOfWeekIndex = (startCal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7;
            LinearLayout dayLayout = createDayCell(dayNames[dayOfWeekIndex], day, year, month, day, today);

            if (currentYear == year && currentMonth == month && currentDay == day) {
                selectedIndex = i;
                dayLayout.setBackgroundResource(R.drawable.selected_day_background);
            }

            final int fy = year, fm = month, fd = day;
            dayLayout.setOnClickListener(v -> {
                currentYear = fy; currentMonth = fm; currentDay = fd;
                currentCalendar.set(fy, fm, fd);

                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("ru"));
                String mName = monthFormat.format(currentCalendar.getTime());
                if (tvMonth != null) tvMonth.setText(mName.substring(0, 1).toUpperCase() + mName.substring(1));

                LinearLayout hourlySchedule = findViewById(R.id.hourlySchedule);
                if (hourlySchedule != null) { hourlySchedule.removeAllViews(); drawHourlySchedule(); }

                for (int j = 0; j < dateStrip.getChildCount(); j++) dateStrip.getChildAt(j).setBackgroundResource(android.R.color.transparent);
                dayLayout.setBackgroundResource(R.drawable.selected_day_background);
                runOnUiThread(() -> { if (hourlySchedule != null) hourlySchedule.invalidate(); dateStrip.invalidate(); });
            });

            dateStrip.addView(dayLayout);
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        scrollToSelectedDay(selectedIndex);
    }

    private void drawWeekDateStrip() {
        if (dateStrip == null) return;
        dateStrip.removeAllViews();
        Calendar startCal = (Calendar) currentCalendar.clone();
        startCal.add(Calendar.DAY_OF_MONTH, -7);
        String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        int selectedIndex = -1;
        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 21; i++) {
            int year = startCal.get(Calendar.YEAR);
            int month = startCal.get(Calendar.MONTH);
            int day = startCal.get(Calendar.DAY_OF_MONTH);
            int dayOfWeekIndex = (startCal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7;
            LinearLayout dayLayout = createDayCell(dayNames[dayOfWeekIndex], day, year, month, day, today);

            if (currentYear == year && currentMonth == month && currentDay == day) {
                selectedIndex = i;
                dayLayout.setBackgroundResource(R.drawable.selected_day_background);
            }

            final int fy = year, fm = month, fd = day;
            dayLayout.setOnClickListener(v -> {
                currentYear = fy; currentMonth = fm; currentDay = fd;
                currentCalendar.set(fy, fm, fd);
                drawWeekView();
            });
            dateStrip.addView(dayLayout);
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        scrollToSelectedDay(selectedIndex);
    }

    private void drawMonthDateStrip() {
        if (dateStrip == null) return;
        dateStrip.removeAllViews();
        Calendar startCal = (Calendar) currentCalendar.clone();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        startCal.add(Calendar.DAY_OF_MONTH, -7);
        String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        int selectedIndex = -1;
        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 56; i++) {
            int year = startCal.get(Calendar.YEAR);
            int month = startCal.get(Calendar.MONTH);
            int day = startCal.get(Calendar.DAY_OF_MONTH);
            int dayOfWeekIndex = (startCal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7;
            LinearLayout dayLayout = createMonthDayCell(dayNames[dayOfWeekIndex], day, year, month, day, today);

            if (currentYear == year && currentMonth == month && currentDay == day) {
                selectedIndex = i;
                dayLayout.setBackgroundResource(R.drawable.selected_day_background);
            }

            final int fy = year, fm = month, fd = day;
            dayLayout.setOnClickListener(v -> {
                currentYear = fy; currentMonth = fm; currentDay = fd;
                currentCalendar.set(fy, fm, fd);
                drawMonthCalendar();
                drawMonthDateStrip();
            });
            dateStrip.addView(dayLayout);
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        scrollToSelectedMonthDay(selectedIndex);
    }

    private LinearLayout createDayCell(String dayName, int dayNum, int year, int month, int day, Calendar today) {
        LinearLayout dayLayout = new LinearLayout(this);
        dayLayout.setOrientation(LinearLayout.VERTICAL);
        dayLayout.setGravity(Gravity.CENTER);
        dayLayout.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(60), LinearLayout.LayoutParams.WRAP_CONTENT));
        dayLayout.setPadding(0, dpToPx(8), 0, dpToPx(8));
        dayLayout.setBackgroundResource(android.R.color.transparent);

        TextView tvWeekday = new TextView(this);
        tvWeekday.setText(dayName);
        tvWeekday.setTextColor(ContextCompat.getColor(this, R.color.gray));
        tvWeekday.setTextSize(12);
        tvWeekday.setGravity(Gravity.CENTER);

        TextView tvDayNum = new TextView(this);
        tvDayNum.setText(String.valueOf(dayNum));
        tvDayNum.setTextSize(18);
        tvDayNum.setGravity(Gravity.CENTER);
        tvDayNum.setTypeface(tvDayNum.getTypeface(), android.graphics.Typeface.BOLD);

        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) == day) {
            tvDayNum.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {
            tvDayNum.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
        }

        dayLayout.addView(tvWeekday);
        dayLayout.addView(tvDayNum);
        return dayLayout;
    }

    private LinearLayout createMonthDayCell(String dayName, int dayNum, int year, int month, int day, Calendar today) {
        LinearLayout dayLayout = new LinearLayout(this);
        dayLayout.setOrientation(LinearLayout.VERTICAL);
        dayLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(50), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        dayLayout.setLayoutParams(params);
        dayLayout.setPadding(0, dpToPx(8), 0, dpToPx(8));
        dayLayout.setBackgroundResource(android.R.color.transparent);

        TextView tvWeekday = new TextView(this);
        tvWeekday.setText(dayName);
        tvWeekday.setTextColor(ContextCompat.getColor(this, R.color.gray));
        tvWeekday.setTextSize(11);
        tvWeekday.setGravity(Gravity.CENTER);

        TextView tvDayNum = new TextView(this);
        tvDayNum.setText(String.valueOf(dayNum));
        tvDayNum.setTextSize(16);
        tvDayNum.setGravity(Gravity.CENTER);
        tvDayNum.setTypeface(tvDayNum.getTypeface(), android.graphics.Typeface.BOLD);

        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) == day) {
            tvDayNum.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {
            tvDayNum.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
        }

        dayLayout.addView(tvWeekday);
        dayLayout.addView(tvDayNum);
        return dayLayout;
    }

    private void scrollToSelectedDay(int selectedIndex) {
        if (selectedIndex != -1 && dateScrollView != null) {
            final int scrollPosition = selectedIndex * dpToPx(60);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (dateScrollView != null) {
                    int targetScroll = scrollPosition - (dateScrollView.getWidth() / 2) + (dpToPx(60) / 2);
                    dateScrollView.scrollTo(Math.max(0, targetScroll), 0);
                }
            }, 100);
        }
    }

    private void scrollToSelectedMonthDay(int selectedIndex) {
        if (selectedIndex != -1 && dateScrollView != null) {
            final int scrollPosition = selectedIndex * (dpToPx(50) + dpToPx(8));
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (dateScrollView != null) {
                    int targetScroll = scrollPosition - (dateScrollView.getWidth() / 2) + (dpToPx(50) / 2);
                    dateScrollView.scrollTo(Math.max(0, targetScroll), 0);
                }
            }, 100);
        }
    }

    private void drawHourlySchedule() {
        LinearLayout hourlySchedule = findViewById(R.id.hourlySchedule);
        if (hourlySchedule == null) return;

        List<CalendarItem> items = getCalendarItemsForDate(currentYear, currentMonth, currentDay);
        items.sort((e1, e2) -> Integer.compare(e1.getStartTotalMinutes(), e2.getStartTotalMinutes()));
        calculateItemColumns(items);

        int hourHeightPx = dpToPx(60);
        int dividerHeightPx = dpToPx(1);
        int totalDayHeightPx = (hourHeightPx + dividerHeightPx) * 24 + hourHeightPx;
        int leftMargin = dpToPx(60);
        int columnGap = dpToPx(4);

        int maxColumns = 1;
        for (CalendarItem item : items) maxColumns = Math.max(maxColumns, item.totalColumns);

        int availableWidth = getResources().getDisplayMetrics().widthPixels - leftMargin - dpToPx(16);
        int columnWidth = (availableWidth - (columnGap * Math.max(0, maxColumns - 1))) / Math.max(1, maxColumns);

        LinearLayout timeContainer = new LinearLayout(this);
        timeContainer.setOrientation(LinearLayout.VERTICAL);
        timeContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, totalDayHeightPx));

        for (int hour = 0; hour <= 24; hour++) {
            LinearLayout timeRow = new LinearLayout(this);
            timeRow.setOrientation(LinearLayout.HORIZONTAL);
            timeRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, hourHeightPx));

            TextView tvTime = new TextView(this);
            tvTime.setText(String.format("%02d:00", hour));
            tvTime.setTextColor(ContextCompat.getColor(this, R.color.gray));
            tvTime.setTextSize(12);
            tvTime.setGravity(Gravity.TOP | Gravity.START);
            tvTime.setPadding(0, dpToPx(2), dpToPx(12), 0);
            tvTime.setLayoutParams(new LinearLayout.LayoutParams(leftMargin, hourHeightPx));
            timeRow.addView(tvTime);
            timeContainer.addView(timeRow);

            if (hour < 24) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dividerHeightPx));
                divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
                timeContainer.addView(divider);
            }
        }

        android.widget.FrameLayout wrapper = new android.widget.FrameLayout(this);
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, totalDayHeightPx));
        wrapper.addView(timeContainer);

        for (CalendarItem item : items) {
            View itemView = createCalendarItemView(item, columnWidth);
            int startMinutes = item.getStartTotalMinutes();
            int endMinutes = item.getEndTotalMinutes();
            int topMargin = Math.round((startMinutes / 60f) * (hourHeightPx + dividerHeightPx));
            int durationMinutes = endMinutes - startMinutes;
            int height = Math.round((durationMinutes / 60f) * (hourHeightPx + dividerHeightPx));
            int minHeight = dpToPx(24);
            if (height < minHeight) {
                height = minHeight;
                if (topMargin + height > totalDayHeightPx) topMargin = totalDayHeightPx - height;
            }

            int leftPos = leftMargin + (item.column * (columnWidth + columnGap));
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(columnWidth, height);
            params.topMargin = topMargin;
            params.leftMargin = leftPos;
            itemView.setLayoutParams(params);
            wrapper.addView(itemView);
        }
        hourlySchedule.addView(wrapper);
    }

    private void calculateItemColumns(List<CalendarItem> items) {
        if (items.isEmpty()) return;
        items.sort((e1, e2) -> {
            int cmp = Integer.compare(e1.getStartTotalMinutes(), e2.getStartTotalMinutes());
            return (cmp != 0) ? cmp : Integer.compare(e1.getEndTotalMinutes(), e2.getEndTotalMinutes());
        });

        for (CalendarItem item : items) { item.column = 0; item.totalColumns = 1; }

        for (int i = 0; i < items.size(); i++) {
            CalendarItem current = items.get(i);
            for (int col = 0; col < items.size(); col++) {
                boolean canUseColumn = true;
                for (int j = 0; j < i; j++) {
                    CalendarItem prev = items.get(j);
                    if (prev.column == col && current.overlaps(prev)) { canUseColumn = false; break; }
                }
                if (canUseColumn) { current.column = col; break; }
            }
        }
        for (int i = 0; i < items.size(); i++) {
            CalendarItem current = items.get(i);
            int maxCol = 0;
            for (int j = 0; j < items.size(); j++) {
                if (current.overlaps(items.get(j))) maxCol = Math.max(maxCol, items.get(j).column);
            }
            current.totalColumns = maxCol + 1;
        }
    }

    private View createCalendarItemView(CalendarItem item, int columnWidth) {
        int colorInt = item.taskId != 0 ? getTaskColor(item.taskId) : Color.GRAY;

        // нормальный полупрозрачный фон
        int bgColor = Color.argb(
                25,
                Color.red(colorInt),
                Color.green(colorInt),
                Color.blue(colorInt)
        );

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        View colorBar = new View(this);
        colorBar.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(4), LinearLayout.LayoutParams.MATCH_PARENT));
        colorBar.setBackgroundColor(colorInt);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        textContainer.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        textContainer.setGravity(Gravity.TOP);

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dpToPx(8));
        gd.setColor(bgColor);
        textContainer.setBackground(gd);

        TextView titleView = new TextView(this);
        titleView.setTextSize(13);
        titleView.setTextColor(Color.parseColor("#1C1C1E"));
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);
        titleView.setMaxLines(2);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView timeView = new TextView(this);
        timeView.setText(item.startTime + " – " + item.endTime);
        timeView.setTextSize(10);
        timeView.setTextColor(Color.parseColor("#666666"));

        // ВИЗУАЛЬНОЕ ОБОЗНАЧЕНИЕ ВЫПОЛНЕННЫХ ЗАДАЧ
        if (item.isCompleted) {
            titleView.setText("✓ " + item.title);
            titleView.setPaintFlags(titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            titleView.setAlpha(0.5f);
            timeView.setAlpha(0.5f);
            colorBar.setAlpha(0.4f);
        } else {
            titleView.setText(item.title);
            titleView.setPaintFlags(titleView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            titleView.setAlpha(1.0f);
            timeView.setAlpha(1.0f);
            colorBar.setAlpha(1.0f);
        }

        textContainer.addView(titleView);
        textContainer.addView(timeView);
        container.addView(colorBar);
        container.addView(textContainer);

        container.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, TaskEditorActivity.class);
            intent.putExtra("task_id", item.taskId);
            intent.putExtra("task_title", item.title);
            intent.putExtra("task_description", "");
            intent.putExtra("task_color", colorInt);
            intent.putExtra("task_date", getStartOfDayTimestamp(currentYear, currentMonth, currentDay));
            intent.putExtra("task_start_time", System.currentTimeMillis());
            intent.putExtra("task_end_time", -1L);
            intent.putExtra("is_edit", true);
            startActivityForResult(intent, 300);
        });
        return container;
    }

    private void drawWeekView() {
        if (weekGrid == null) return;
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("ru"));
        if (tvMonth != null) {
            String month = monthFormat.format(currentCalendar.getTime());
            tvMonth.setText(month.substring(0, 1).toUpperCase() + month.substring(1));
        }
        drawWeekDateStrip();
        weekGrid.removeAllViews();
        Calendar weekStart = (Calendar) currentCalendar.clone();
        weekStart.add(Calendar.DAY_OF_MONTH, -((weekStart.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7));
        String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};

        LinearLayout row1 = new LinearLayout(this); row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1)); row1.setPadding(0, 0, 0, dpToPx(16));
        for (int i = 0; i < 3; i++) row1.addView(createWeekDayCell(weekStart, i, dayNames[i]));
        weekGrid.addView(row1);

        LinearLayout row2 = new LinearLayout(this); row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1)); row2.setPadding(0, 0, 0, dpToPx(16));
        for (int i = 3; i < 6; i++) row2.addView(createWeekDayCell(weekStart, i, dayNames[i]));
        weekGrid.addView(row2);

        LinearLayout row3 = new LinearLayout(this); row3.setOrientation(LinearLayout.HORIZONTAL);
        row3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row3.addView(createWeekDayCell(weekStart, 6, dayNames[6]));
        weekGrid.addView(row3);
    }

    private LinearLayout createWeekDayCell(Calendar weekStart, int index, String dayName) {
        Calendar dayCal = (Calendar) weekStart.clone();
        dayCal.add(Calendar.DAY_OF_MONTH, index);
        int year = dayCal.get(Calendar.YEAR), month = dayCal.get(Calendar.MONTH), day = dayCal.get(Calendar.DAY_OF_MONTH);

        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setBackgroundResource(R.drawable.week_day_background);
        cell.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        if (index == 6) params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, dpToPx(12), 0);
        if (index == 2 || index == 5) params.setMargins(0, 0, 0, 0);
        cell.setLayoutParams(params);

        TextView tvDayHeader = new TextView(this); tvDayHeader.setText(dayName);
        tvDayHeader.setTextColor(ContextCompat.getColor(this, R.color.light_text)); tvDayHeader.setTextSize(14); tvDayHeader.setTypeface(tvDayHeader.getTypeface(), android.graphics.Typeface.BOLD);

        TextView tvDayNumber = new TextView(this); tvDayNumber.setText(String.valueOf(day));
        tvDayNumber.setTextSize(24); tvDayNumber.setTypeface(tvDayNumber.getTypeface(), android.graphics.Typeface.BOLD); tvDayNumber.setPadding(0, dpToPx(4), 0, dpToPx(12));
        Calendar realToday = Calendar.getInstance();
        if (realToday.get(Calendar.YEAR) == year && realToday.get(Calendar.MONTH) == month && realToday.get(Calendar.DAY_OF_MONTH) == day) tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.red));
        else tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.dark_text));

        LinearLayout itemsLayout = new LinearLayout(this); itemsLayout.setOrientation(LinearLayout.VERTICAL);
        itemsLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        for (CalendarItem item : getCalendarItemsForDate(year, month, day)) itemsLayout.addView(createCompactItemView(item));

        cell.addView(tvDayHeader); cell.addView(tvDayNumber); cell.addView(itemsLayout);

        final int fy = year, fm = month, fd = day;
        cell.setOnClickListener(v -> {
            currentYear = fy; currentMonth = fm; currentDay = fd;
            currentCalendar.set(fy, fm, fd);
            showDayCalendar();
        });
        return cell;
    }

    private int getTaskColor(long taskId) {
        for (Task t : allTasksCache) {
            if (t.id == taskId) return t.color;
        }
        return Color.GRAY;
    }

    private LinearLayout createCompactItemView(CalendarItem item) {
        int colorInt = item.taskId != 0 ? getTaskColor(item.taskId) : Color.GRAY;

        int bgColor = Color.argb(
                25,
                Color.red(colorInt),
                Color.green(colorInt),
                Color.blue(colorInt)
        );

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        container.setPadding(0, dpToPx(4), 0, dpToPx(4));

        View colorBar = new View(this);
        colorBar.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(3), LinearLayout.LayoutParams.MATCH_PARENT));
        colorBar.setBackgroundColor(colorInt);

        TextView itemView = new TextView(this);
        itemView.setTextSize(12);
        itemView.setTextColor(Color.parseColor("#1C1C1E"));
        itemView.setMaxLines(2);
        itemView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        itemView.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        itemView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // ВИЗУАЛЬНОЕ ОБОЗНАЧЕНИЕ ВЫПОЛНЕННЫХ ЗАДАЧ
        if (item.isCompleted) {
            itemView.setText("✓ " + item.title);
            itemView.setPaintFlags(itemView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            itemView.setAlpha(0.5f);
            colorBar.setAlpha(0.4f);
        } else {
            itemView.setText(item.title);
            itemView.setPaintFlags(itemView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            itemView.setAlpha(1.0f);
            colorBar.setAlpha(1.0f);
        }

        GradientDrawable gd = new GradientDrawable(); gd.setCornerRadius(dpToPx(6)); gd.setColor(bgColor);
        itemView.setBackground(gd);
        container.addView(colorBar); container.addView(itemView);
        container.setOnClickListener(v -> Toast.makeText(this, item.title + "\n" + item.startTime + " - " + item.endTime, Toast.LENGTH_SHORT).show());
        return container;
    }

    private void drawMonthCalendar() {
        if (calendarGrid == null) return;
        drawMonthDateStrip();
        calendarGrid.removeAllViews();

        Calendar cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, 1);
        int offset = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDayNum = 1; boolean nextMonthStarted = false;
        Calendar today = Calendar.getInstance();

        for (int row = 0; row < 6; row++) {
            LinearLayout weekRow = new LinearLayout(this);
            weekRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
            weekRow.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 0; col < 7; col++) {
                View cellView = LayoutInflater.from(this).inflate(R.layout.month_cell_layout, weekRow, false);
                TextView tvDayNumber = cellView.findViewById(R.id.tvDayNumber);
                LinearLayout tasksContainer = cellView.findViewById(R.id.tasksContainer);
                tasksContainer.removeAllViews();

                int dayNumber; boolean isCurrentMonth; int dy = currentYear, dm = currentMonth;
                if (row == 0 && col < offset) {
                    Calendar prevCal = (Calendar) cal.clone(); prevCal.add(Calendar.MONTH, -1);
                    dayNumber = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH) - (offset - col) + 1;
                    isCurrentMonth = false; dy = prevCal.get(Calendar.YEAR); dm = prevCal.get(Calendar.MONTH);
                    tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.gray));
                } else if (nextMonthStarted || currentDayNum > daysInMonth) {
                    if (!nextMonthStarted) { currentDayNum = 1; nextMonthStarted = true; }
                    dayNumber = currentDayNum; isCurrentMonth = false;
                    Calendar nextCal = (Calendar) cal.clone(); nextCal.add(Calendar.MONTH, 1);
                    dy = nextCal.get(Calendar.YEAR); dm = nextCal.get(Calendar.MONTH);
                    tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.gray)); currentDayNum++;
                } else {
                    dayNumber = currentDayNum; isCurrentMonth = true;
                    if (today.get(Calendar.YEAR) == currentYear && today.get(Calendar.MONTH) == currentMonth && today.get(Calendar.DAY_OF_MONTH) == dayNumber)
                        tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.red));
                    else tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
                    currentDayNum++;
                }
                tvDayNumber.setText(String.valueOf(dayNumber));

                if (isCurrentMonth) {
                    for (CalendarItem item : getCalendarItemsForDate(dy, dm, dayNumber)) {
                        if (tasksContainer.getChildCount() >= 2) {
                            TextView more = new TextView(this); more.setText("+ ещё " + (getCalendarItemsForDate(dy, dm, dayNumber).size() - 2));
                            more.setTextSize(10); more.setTextColor(ContextCompat.getColor(this, R.color.light_text)); more.setPadding(0, dpToPx(2), 0, dpToPx(2));
                            tasksContainer.addView(more); break;
                        }
                        tasksContainer.addView(createCompactItemView(item));
                    }
                }

                final int fy = dy, fm = dm, fd = dayNumber, fIs = isCurrentMonth ? 1 : 0;
                cellView.setOnClickListener(v -> {
                    if (fIs == 1) { currentYear = fy; currentMonth = fm; currentDay = fd; currentCalendar.set(fy, fm, fd); showDayCalendar(); }
                });
                weekRow.addView(cellView);
            }
            calendarGrid.addView(weekRow);
        }
    }

    private void updateMonthHeader() {
        if (tvMonthYear != null) {
            SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", new Locale("ru"));
            String txt = fmt.format(currentCalendar.getTime());
            tvMonthYear.setText(txt.substring(0, 1).toUpperCase() + txt.substring(1));
        }
    }

    private void showViewTypeDialog() {
        final String[] viewTypes = {"День", "Неделя", "Месяц"};
        new AlertDialog.Builder(this).setTitle("Выберите вид")
                .setItems(viewTypes, (dialog, which) -> {
                    currentViewType = viewTypes[which];
                    if (tvViewValue != null) tvViewValue.setText(currentViewType);
                    if (which == 0) showDayCalendar(); else if (which == 1) showWeekCalendar(); else showMonthCalendar();
                }).create().show();
    }

    private void setupBottomNavigation() {
        if (bottomNav == null) return;
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) return true;
            if (id == R.id.nav_tasks) { startActivity(new Intent(this, TasksActivity.class)); finish(); return true; }
            if (id == R.id.nav_notes) { startActivity(new Intent(this, NotesActivity.class)); finish(); return true; }
            if (id == R.id.nav_habits) { startActivity(new Intent(this, HabitsActivity.class)); finish(); return true; }
            if (id == R.id.nav_settings) { startActivity(new Intent(this, SettingsActivity.class)); return true; }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_calendar);
    }

    private void setupModalButtons() {
        if (modalPopup == null) return;
        modalPopup.findViewById(R.id.btnAddTask).setOnClickListener(v -> { hideModal(); showCreateTaskDialog(); });
        modalPopup.findViewById(R.id.btnAddNote).setOnClickListener(v -> { hideModal(); showCreateNoteDialog(); });
        modalPopup.findViewById(R.id.btnAddHabit).setOnClickListener(v -> { hideModal(); showCreateHabitDialog(); });
    }

    private void showModal() {
        if (dimOverlay != null && modalPopup != null) {
            dimOverlay.setVisibility(View.VISIBLE); modalPopup.setVisibility(View.VISIBLE);
            dimOverlay.setAlpha(0f); modalPopup.setScaleX(0.8f); modalPopup.setScaleY(0.8f); modalPopup.setAlpha(0f);
            dimOverlay.animate().alpha(1f).setDuration(200).start();
            modalPopup.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).start();
        }
    }

    private void hideModal() {
        if (dimOverlay != null && modalPopup != null) {
            dimOverlay.animate().alpha(0f).setDuration(150).withEndAction(() -> dimOverlay.setVisibility(View.GONE)).start();
            modalPopup.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(150).withEndAction(() -> modalPopup.setVisibility(View.GONE)).start();
        }
    }

    private int dpToPx(int dp) { return (int) (dp * getResources().getDisplayMetrics().density); }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("habit_name");
            if (name != null) Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onTaskCreated(String title, String startTime, String endTime, int year, int month, int day) {
        long dateTimestamp = getStartOfDayTimestamp(year, month, day);
        int startMin = parseTimeToMinutes(startTime);
        int endMin = parseTimeToMinutes(endTime);
        if (endMin <= startMin) endMin = startMin + 60;

        Task newTask = new Task();
        newTask.title = title; newTask.date = dateTimestamp; newTask.startTime = startMin; newTask.endTime = endMin;
        newTask.isCompleted = false; newTask.color = ColorHelper.getColorForTitle(title); newTask.description = "";

        taskViewModel.insert(newTask);
        Toast.makeText(this, "Задача \"" + title + "\" сохранена", Toast.LENGTH_SHORT).show();
    }

    private int parseTimeToMinutes(String time) {
        if (time == null || time.trim().isEmpty()) return -1;
        try { String[] p = time.split(":"); return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]); } catch (Exception e) { return -1; }
    }

    private long getStartOfDayTimestamp(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    @Override protected void onNoteCreated(String title, String content) { startActivityForResult(new Intent(this, AddNoteActivity.class), 100); }
    @Override protected void onHabitCreated(String name, String schedule) { Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show(); }
}