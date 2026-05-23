package com.example.planner;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private Map<String, List<CalendarItem>> calendarItemsMap;
    private Map<String, Integer> itemColorMap;
    private Calendar currentCalendar;
    private int currentYear, currentMonth, currentDay;

    private static class CalendarItem {
        String title;
        String startTime;
        String endTime;
        int startHour;
        int startMinute;
        int endHour;
        int endMinute;
        int column = 0;
        int totalColumns = 1;

        CalendarItem(String title, String startTime, String endTime) {
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
            try {
                String[] startParts = startTime.split(":");
                String[] endParts = endTime.split(":");
                this.startHour = Integer.parseInt(startParts[0]);
                this.startMinute = Integer.parseInt(startParts[1]);
                this.endHour = Integer.parseInt(endParts[0]);
                this.endMinute = Integer.parseInt(endParts[1]);
            } catch (Exception e) {
                this.startHour = 9;
                this.startMinute = 0;
                this.endHour = 10;
                this.endMinute = 0;
            }
        }

        int getStartTotalMinutes() {
            return startHour * 60 + startMinute;
        }

        int getEndTotalMinutes() {
            return endHour * 60 + endMinute;
        }

        boolean overlaps(CalendarItem other) {
            return (getStartTotalMinutes() < other.getEndTotalMinutes() &&
                    getEndTotalMinutes() > other.getStartTotalMinutes());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentCalendar = Calendar.getInstance();
        currentYear = currentCalendar.get(Calendar.YEAR);
        currentMonth = currentCalendar.get(Calendar.MONTH);
        currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

        initTestData();
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

    private void initTestData() {
        calendarItemsMap = new HashMap<>();
        itemColorMap = new HashMap<>();

        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        // Сегодня
        addCalendarItem(currentYear, currentMonth, currentDay, "Купить продукты", "10:00", "11:00");
        addCalendarItem(currentYear, currentMonth, currentDay, "Созвониться с клиентом", "12:00", "12:30");
        addCalendarItem(currentYear, currentMonth, currentDay, "Прогуляться в парке", "15:00", "16:00");
        addCalendarItem(currentYear, currentMonth, currentDay, "Позвонить родителям", "18:00", "18:30");
        addCalendarItem(currentYear, currentMonth, currentDay, "Сделать уборку", "19:00", "20:00");

        // Завтра
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        addCalendarItem(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH),
                "Сходить в магазин", "09:00", "10:00");
        addCalendarItem(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH),
                "Подготовить отчет", "14:00", "16:00");
        addCalendarItem(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH),
                "Встреча с партнерами", "11:00", "12:00");

        // Послезавтра
        Calendar afterTomorrow = Calendar.getInstance();
        afterTomorrow.add(Calendar.DAY_OF_MONTH, 2);
        addCalendarItem(afterTomorrow.get(Calendar.YEAR), afterTomorrow.get(Calendar.MONTH), afterTomorrow.get(Calendar.DAY_OF_MONTH),
                "Купить продукты", "10:00", "11:00");
        addCalendarItem(afterTomorrow.get(Calendar.YEAR), afterTomorrow.get(Calendar.MONTH), afterTomorrow.get(Calendar.DAY_OF_MONTH),
                "Созвониться", "15:00", "15:30");

        // 25 мая
        Calendar day25 = Calendar.getInstance();
        day25.add(Calendar.DAY_OF_MONTH, 3);
        addCalendarItem(day25.get(Calendar.YEAR), day25.get(Calendar.MONTH), day25.get(Calendar.DAY_OF_MONTH),
                "Приготовить обед", "12:00", "13:00");

        // 26 мая
        Calendar day26 = Calendar.getInstance();
        day26.add(Calendar.DAY_OF_MONTH, 4);
        addCalendarItem(day26.get(Calendar.YEAR), day26.get(Calendar.MONTH), day26.get(Calendar.DAY_OF_MONTH),
                "Купить подарок", "16:00", "17:00");

        // 27 мая
        Calendar day27 = Calendar.getInstance();
        day27.add(Calendar.DAY_OF_MONTH, 5);
        addCalendarItem(day27.get(Calendar.YEAR), day27.get(Calendar.MONTH), day27.get(Calendar.DAY_OF_MONTH),
                "Сдать отчет", "09:00", "11:00");

        // 28 мая
        Calendar day28 = Calendar.getInstance();
        day28.add(Calendar.DAY_OF_MONTH, 6);
        addCalendarItem(day28.get(Calendar.YEAR), day28.get(Calendar.MONTH), day28.get(Calendar.DAY_OF_MONTH),
                "Встреча с клиентом", "14:00", "15:00");
    }

    private void addCalendarItem(int year, int month, int day, String title, String startTime, String endTime) {
        String key = year + "-" + month + "-" + day;
        if (!calendarItemsMap.containsKey(key)) {
            calendarItemsMap.put(key, new ArrayList<>());
        }
        calendarItemsMap.get(key).add(new CalendarItem(title, startTime, endTime));

        String colorKey = key + "_" + title;
        int color = ColorHelper.getColorForTitle(title);
        itemColorMap.put(colorKey, color);
    }

    private int getColorForTitle(String title) {
        return ColorHelper.getColorForTitle(title);
    }

    public List<CalendarItem> getCalendarItemsForDate(int year, int month, int day) {
        String key = year + "-" + month + "-" + day;
        List<CalendarItem> items = calendarItemsMap.get(key);
        return items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

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
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                );
                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        0  // Убираем нижний отступ
                );
                return insets;
            });
        }
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
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                );
                // Убираем нижний отступ
                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        0
                );
                return insets;
            });
        }
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
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                );
                // Убираем нижний отступ
                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        0
                );
                return insets;
            });
        }
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
            int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
            int dayOfWeekIndex = (dayOfWeek - 2 + 7) % 7;

            LinearLayout dayLayout = createDayCell(dayNames[dayOfWeekIndex], day, year, month, day, today);

            if (currentYear == year && currentMonth == month && currentDay == day) {
                selectedIndex = i;
                dayLayout.setBackgroundResource(R.drawable.selected_day_background);
            }

            final int finalYear = year;
            final int finalMonth = month;
            final int finalDay = day;

            dayLayout.setOnClickListener(v -> {
                currentYear = finalYear;
                currentMonth = finalMonth;
                currentDay = finalDay;
                currentCalendar.set(finalYear, finalMonth, finalDay);

                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("ru"));
                String monthName = monthFormat.format(currentCalendar.getTime());
                monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
                if (tvMonth != null) {
                    tvMonth.setText(monthName);
                }

                LinearLayout hourlySchedule = findViewById(R.id.hourlySchedule);
                if (hourlySchedule != null) {
                    hourlySchedule.removeAllViews();
                    drawHourlySchedule();
                }

                for (int j = 0; j < dateStrip.getChildCount(); j++) {
                    View child = dateStrip.getChildAt(j);
                    child.setBackgroundResource(android.R.color.transparent);
                }
                dayLayout.setBackgroundResource(R.drawable.selected_day_background);

                runOnUiThread(() -> {
                    if (hourlySchedule != null) hourlySchedule.invalidate();
                    dateStrip.invalidate();
                });
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
            int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
            int dayOfWeekIndex = (dayOfWeek - 2 + 7) % 7;

            LinearLayout dayLayout = createDayCell(dayNames[dayOfWeekIndex], day, year, month, day, today);

            if (currentYear == year && currentMonth == month && currentDay == day) {
                selectedIndex = i;
                dayLayout.setBackgroundResource(R.drawable.selected_day_background);
            }

            final int finalYear = year;
            final int finalMonth = month;
            final int finalDay = day;
            dayLayout.setOnClickListener(v -> {
                currentYear = finalYear;
                currentMonth = finalMonth;
                currentDay = finalDay;
                currentCalendar.set(finalYear, finalMonth, finalDay);
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
            int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
            int dayOfWeekIndex = (dayOfWeek - 2 + 7) % 7;

            LinearLayout dayLayout = createMonthDayCell(dayNames[dayOfWeekIndex], day, year, month, day, today);

            if (currentYear == year && currentMonth == month && currentDay == day) {
                selectedIndex = i;
                dayLayout.setBackgroundResource(R.drawable.selected_day_background);
            }

            final int finalYear = year;
            final int finalMonth = month;
            final int finalDay = day;
            dayLayout.setOnClickListener(v -> {
                currentYear = finalYear;
                currentMonth = finalMonth;
                currentDay = finalDay;
                currentCalendar.set(finalYear, finalMonth, finalDay);
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

        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month &&
                today.get(Calendar.DAY_OF_MONTH) == day) {
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

        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month &&
                today.get(Calendar.DAY_OF_MONTH) == day) {
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
        for (CalendarItem item : items) {
            maxColumns = Math.max(maxColumns, item.totalColumns);
        }

        int availableWidth = getResources().getDisplayMetrics().widthPixels - leftMargin - dpToPx(16);
        int columnWidth = (availableWidth - (columnGap * Math.max(0, maxColumns - 1))) / Math.max(1, maxColumns);

        LinearLayout timeContainer = new LinearLayout(this);
        timeContainer.setOrientation(LinearLayout.VERTICAL);
        timeContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, totalDayHeightPx));

        for (int hour = 0; hour <= 24; hour++) {
            LinearLayout timeRow = new LinearLayout(this);
            timeRow.setOrientation(LinearLayout.HORIZONTAL);
            timeRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, hourHeightPx));

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
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dividerHeightPx));
                divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
                timeContainer.addView(divider);
            }
        }

        android.widget.FrameLayout wrapper = new android.widget.FrameLayout(this);
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, totalDayHeightPx));
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
                if (topMargin + height > totalDayHeightPx) {
                    topMargin = totalDayHeightPx - height;
                }
            }

            int leftPos = leftMargin + (item.column * (columnWidth + columnGap));

            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                    columnWidth, height);
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

        for (CalendarItem item : items) {
            item.column = 0;
            item.totalColumns = 1;
        }

        for (int i = 0; i < items.size(); i++) {
            CalendarItem current = items.get(i);

            for (int col = 0; col < items.size(); col++) {
                boolean canUseColumn = true;

                for (int j = 0; j < i; j++) {
                    CalendarItem prev = items.get(j);
                    if (prev.column == col && current.overlaps(prev)) {
                        canUseColumn = false;
                        break;
                    }
                }

                if (canUseColumn) {
                    current.column = col;
                    break;
                }
            }
        }

        for (int i = 0; i < items.size(); i++) {
            CalendarItem current = items.get(i);
            int maxCol = 0;

            for (int j = 0; j < items.size(); j++) {
                CalendarItem other = items.get(j);
                if (current.overlaps(other)) {
                    maxCol = Math.max(maxCol, other.column);
                }
            }
            current.totalColumns = maxCol + 1;
        }
    }

    private View createCalendarItemView(CalendarItem item, int columnWidth) {
        String dateKey = currentYear + "-" + currentMonth + "-" + currentDay;
        String colorKey = dateKey + "_" + item.title;
        int colorInt = itemColorMap.containsKey(colorKey) ? itemColorMap.get(colorKey) : getColorForTitle(item.title);
        String color = String.format("#%06X", (0xFFFFFF & colorInt));
        String transparentColor = "#99" + color.substring(1);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        View colorBar = new View(this);
        colorBar.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(4), LinearLayout.LayoutParams.MATCH_PARENT));
        colorBar.setBackgroundColor(Color.parseColor(color));

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        textContainer.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        textContainer.setGravity(Gravity.TOP);

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dpToPx(8));
        gd.setColor(Color.parseColor(transparentColor));
        textContainer.setBackground(gd);

        TextView titleView = new TextView(this);
        titleView.setText(item.title);
        titleView.setTextSize(13);
        titleView.setTextColor(Color.parseColor("#1C1C1E"));
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);
        titleView.setMaxLines(2);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView timeView = new TextView(this);
        timeView.setText(item.startTime + " – " + item.endTime);
        timeView.setTextSize(10);
        timeView.setTextColor(Color.parseColor("#666666"));

        textContainer.addView(titleView);
        textContainer.addView(timeView);

        container.addView(colorBar);
        container.addView(textContainer);

        // Добавляем обработчик нажатия для открытия задачи в редакторе
        container.setOnClickListener(v -> {
            // Получаем дату из calendarItem (нужно сохранить дату в item)
            // Создаем Intent для открытия TaskEditorActivity
            Intent intent = new Intent(CalendarActivity.this, TaskEditorActivity.class);
            intent.putExtra("task_title", item.title);
            intent.putExtra("task_description", "");
            intent.putExtra("task_color", colorInt);

            // Создаем дату из currentYear, currentMonth, currentDay
            Calendar cal = Calendar.getInstance();
            cal.set(currentYear, currentMonth, currentDay, 0, 0, 0);
            intent.putExtra("task_date", cal.getTimeInMillis());

            // Время из item
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                java.util.Date startDate = sdf.parse(item.startTime);
                java.util.Date endDate = sdf.parse(item.endTime);
                cal.set(Calendar.HOUR_OF_DAY, startDate.getHours());
                cal.set(Calendar.MINUTE, startDate.getMinutes());
                intent.putExtra("task_start_time", cal.getTimeInMillis());

                cal.set(Calendar.HOUR_OF_DAY, endDate.getHours());
                cal.set(Calendar.MINUTE, endDate.getMinutes());
                intent.putExtra("task_end_time", cal.getTimeInMillis());
            } catch (Exception e) {
                intent.putExtra("task_start_time", System.currentTimeMillis());
                intent.putExtra("task_end_time", -1L);
            }

            intent.putExtra("task_id", item.title.hashCode());
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
            month = month.substring(0, 1).toUpperCase() + month.substring(1);
            tvMonth.setText(month);
        }

        drawWeekDateStrip();

        weekGrid.removeAllViews();
        Calendar weekStart = (Calendar) currentCalendar.clone();
        int dayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
        int daysToMonday = (dayOfWeek - 2 + 7) % 7;
        weekStart.add(Calendar.DAY_OF_MONTH, -daysToMonday);

        String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        row1.setPadding(0, 0, 0, dpToPx(16));
        for (int i = 0; i < 3; i++) {
            row1.addView(createWeekDayCell(weekStart, i, dayNames[i]));
        }
        weekGrid.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        row2.setPadding(0, 0, 0, dpToPx(16));
        for (int i = 3; i < 6; i++) {
            row2.addView(createWeekDayCell(weekStart, i, dayNames[i]));
        }
        weekGrid.addView(row2);

        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        row3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row3.addView(createWeekDayCell(weekStart, 6, dayNames[6]));
        weekGrid.addView(row3);
    }

    private LinearLayout createWeekDayCell(Calendar weekStart, int index, String dayName) {
        Calendar dayCal = (Calendar) weekStart.clone();
        dayCal.add(Calendar.DAY_OF_MONTH, index);
        int year = dayCal.get(Calendar.YEAR);
        int month = dayCal.get(Calendar.MONTH);
        int day = dayCal.get(Calendar.DAY_OF_MONTH);

        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setBackgroundResource(R.drawable.week_day_background);
        cell.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        if (index == 6) {
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        params.setMargins(0, 0, dpToPx(12), 0);
        if (index == 2 || index == 5) {
            params.setMargins(0, 0, 0, 0);
        }
        cell.setLayoutParams(params);

        TextView tvDayHeader = new TextView(this);
        tvDayHeader.setText(dayName);
        tvDayHeader.setTextColor(ContextCompat.getColor(this, R.color.light_text));
        tvDayHeader.setTextSize(14);
        tvDayHeader.setTypeface(tvDayHeader.getTypeface(), android.graphics.Typeface.BOLD);

        TextView tvDayNumber = new TextView(this);
        tvDayNumber.setText(String.valueOf(day));
        tvDayNumber.setTextSize(24);
        tvDayNumber.setTypeface(tvDayNumber.getTypeface(), android.graphics.Typeface.BOLD);
        tvDayNumber.setPadding(0, dpToPx(4), 0, dpToPx(12));

        Calendar realToday = Calendar.getInstance();
        if (realToday.get(Calendar.YEAR) == year && realToday.get(Calendar.MONTH) == month &&
                realToday.get(Calendar.DAY_OF_MONTH) == day) {
            tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {
            tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
        }

        LinearLayout itemsLayout = new LinearLayout(this);
        itemsLayout.setOrientation(LinearLayout.VERTICAL);
        itemsLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        List<CalendarItem> items = getCalendarItemsForDate(year, month, day);
        if (items != null) {
            for (CalendarItem item : items) {
                LinearLayout itemContainer = createCompactItemView(item);
                itemsLayout.addView(itemContainer);
            }
        }

        cell.addView(tvDayHeader);
        cell.addView(tvDayNumber);
        cell.addView(itemsLayout);

        final int finalYear = year;
        final int finalMonth = month;
        final int finalDay = day;
        cell.setOnClickListener(v -> {
            currentYear = finalYear;
            currentMonth = finalMonth;
            currentDay = finalDay;
            currentCalendar.set(finalYear, finalMonth, finalDay);
            showDayCalendar();
        });

        return cell;
    }

    private LinearLayout createCompactItemView(CalendarItem item) {
        String dateKey = currentYear + "-" + currentMonth + "-" + currentDay;
        String colorKey = dateKey + "_" + item.title;
        int colorInt = itemColorMap.containsKey(colorKey) ? itemColorMap.get(colorKey) : getColorForTitle(item.title);
        String color = String.format("#%06X", (0xFFFFFF & colorInt));
        String transparentColor = "#80" + color.substring(1);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        container.setPadding(0, dpToPx(4), 0, dpToPx(4));

        View colorBar = new View(this);
        colorBar.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(3), LinearLayout.LayoutParams.MATCH_PARENT));
        colorBar.setBackgroundColor(colorInt);

        TextView itemView = new TextView(this);
        itemView.setText(item.title);
        itemView.setTextSize(12);
        itemView.setTextColor(Color.parseColor("#1C1C1E"));
        itemView.setMaxLines(2);
        itemView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        itemView.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        itemView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dpToPx(6));
        gd.setColor(Color.parseColor(transparentColor));
        itemView.setBackground(gd);

        container.addView(colorBar);
        container.addView(itemView);

        container.setOnClickListener(v -> Toast.makeText(this, item.title + "\n" + item.startTime + " - " + item.endTime, Toast.LENGTH_SHORT).show());

        return container;
    }

    private void drawMonthCalendar() {
        if (calendarGrid == null) return;

        drawMonthDateStrip();

        calendarGrid.removeAllViews();

        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek - 2 + 7) % 7;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int currentDayNum = 1;
        boolean nextMonthStarted = false;
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

                int dayNumber;
                boolean isCurrentMonth;
                int displayYear = currentYear;
                int displayMonth = currentMonth;

                if (row == 0 && col < offset) {
                    Calendar prevCal = (Calendar) cal.clone();
                    prevCal.add(Calendar.MONTH, -1);
                    int prevDaysInMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    dayNumber = prevDaysInMonth - (offset - col) + 1;
                    isCurrentMonth = false;
                    displayYear = prevCal.get(Calendar.YEAR);
                    displayMonth = prevCal.get(Calendar.MONTH);
                    tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.gray));
                } else if (nextMonthStarted || currentDayNum > daysInMonth) {
                    if (!nextMonthStarted) { currentDayNum = 1; nextMonthStarted = true; }
                    dayNumber = currentDayNum;
                    isCurrentMonth = false;
                    Calendar nextCal = (Calendar) cal.clone();
                    nextCal.add(Calendar.MONTH, 1);
                    displayYear = nextCal.get(Calendar.YEAR);
                    displayMonth = nextCal.get(Calendar.MONTH);
                    tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.gray));
                    currentDayNum++;
                } else {
                    dayNumber = currentDayNum;
                    isCurrentMonth = true;
                    if (today.get(Calendar.YEAR) == currentYear && today.get(Calendar.MONTH) == currentMonth &&
                            today.get(Calendar.DAY_OF_MONTH) == dayNumber) {
                        tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.red));
                    } else {
                        tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
                    }
                    currentDayNum++;
                }

                tvDayNumber.setText(String.valueOf(dayNumber));

                if (isCurrentMonth) {
                    String dateKey = displayYear + "-" + displayMonth + "-" + dayNumber;
                    List<CalendarItem> items = getCalendarItemsForDate(displayYear, displayMonth, dayNumber);
                    if (items != null) {
                        int itemCount = 0;
                        for (CalendarItem item : items) {
                            if (itemCount >= 2) {
                                TextView moreView = new TextView(this);
                                moreView.setText("+ еще " + (items.size() - 2));
                                moreView.setTextSize(10);
                                moreView.setTextColor(ContextCompat.getColor(this, R.color.light_text));
                                moreView.setPadding(0, dpToPx(2), 0, dpToPx(2));
                                tasksContainer.addView(moreView);
                                break;
                            }
                            LinearLayout itemContainer = createCompactItemView(item);
                            tasksContainer.addView(itemContainer);
                            itemCount++;
                        }
                    }
                }

                final int finalYear = displayYear;
                final int finalMonth = displayMonth;
                final int finalDay = dayNumber;
                final boolean finalIsCurrentMonth = isCurrentMonth;

                cellView.setOnClickListener(v -> {
                    if (finalIsCurrentMonth) {
                        currentYear = finalYear;
                        currentMonth = finalMonth;
                        currentDay = finalDay;
                        currentCalendar.set(finalYear, finalMonth, finalDay);
                        showDayCalendar();
                    }
                });

                weekRow.addView(cellView);
            }
            calendarGrid.addView(weekRow);
        }
    }

    private void updateMonthHeader() {
        if (tvMonthYear != null) {
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("ru"));
            String text = monthYearFormat.format(currentCalendar.getTime());
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
            tvMonthYear.setText(text);
        }
    }

    private void showViewTypeDialog() {
        final String[] viewTypes = {"День", "Неделя", "Месяц"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите вид")
                .setItems(viewTypes, (dialog, which) -> {
                    currentViewType = viewTypes[which];
                    if (tvViewValue != null) tvViewValue.setText(currentViewType);
                    switch (which) {
                        case 0: showDayCalendar(); break;
                        case 1: showWeekCalendar(); break;
                        case 2: showMonthCalendar(); break;
                    }
                });
        builder.create().show();
    }

    private void setupBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_calendar) {
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
                    startActivity(new Intent(this, SettingsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
            bottomNav.setSelectedItemId(R.id.nav_calendar);
        }
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

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("habit_name");
            Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onTaskCreated(String title, String startTime, String endTime, int year, int month, int day) {
        addCalendarItem(year, month, day, title, startTime, endTime);

        if (currentViewType.equals("День")) {
            drawDayView();
        } else if (currentViewType.equals("Неделя")) {
            drawWeekView();
        } else {
            drawMonthCalendar();
        }

        Toast.makeText(this, "Задача \"" + title + "\" добавлена в календарь", Toast.LENGTH_SHORT).show();
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
}