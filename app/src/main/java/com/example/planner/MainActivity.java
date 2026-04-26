package com.example.planner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000;

    private View splashView;
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
    private Bundle savedState;

    private enum Screen { SPLASH, ONBOARDING, CALENDAR, TASKS }
    private Screen currentScreen = Screen.SPLASH;

    private String currentViewType = "День";

    private Map<String, List<String>> tasksMap;
    private Map<String, List<Event>> eventsMap;
    private Calendar currentCalendar;
    private int currentYear, currentMonth, currentDay;

    private GestureDetector monthGestureDetector;
    private BottomNavigationView bottomNav;

    private TasksFragment tasksFragment;

    private static class Event {
        String title;
        String startTime;
        String endTime;
        Event(String title, String startTime, String endTime) {
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavigationSelection();
    }

    private void updateBottomNavigationSelection() {
        if (bottomNav != null && bottomNav.getMenu() != null) {
            if (currentScreen == Screen.TASKS) {
                bottomNav.setSelectedItemId(R.id.nav_tasks);
            } else if (currentScreen == Screen.CALENDAR) {
                bottomNav.setSelectedItemId(R.id.nav_calendar);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedState = savedInstanceState;

        currentCalendar = Calendar.getInstance();
        currentYear = currentCalendar.get(Calendar.YEAR);
        currentMonth = currentCalendar.get(Calendar.MONTH);
        currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

        initTestTasks();
        initTestEvents();
        tasksFragment = new TasksFragment();

        showSplashScreen();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });

        // ✅ Обработка навигации из SettingsActivity
        handleNavigationIntent(getIntent());
    }

    // ✅ Обрабатывает повторный вызов активности (когда возвращаемся из Настроек)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNavigationIntent(intent);
    }

    // ✅ Центральная обработка параметра навигации
    private void handleNavigationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("navigate_to")) {
            String navigateTo = intent.getStringExtra("navigate_to");
            if ("tasks".equals(navigateTo)) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (currentScreen != Screen.TASKS) {
                        showTasks();
                    }
                }, 100);
            }
        }
    }

    // ✅ Публичный метод для перехода из TasksFragment
    public void navigateToCalendar() {
        showCalendar();
    }

    private void initTestTasks() {
        tasksMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        addTask(year, month, day, "Купить продукты");
        addTask(year, month, day, "Созвониться с клиентом");
        addTask(year, month, day, "Прогуляться в парке");

        cal.add(Calendar.DAY_OF_MONTH, 1);
        addTask(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), "Сходить в магазин");
        addTask(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), "Выложить товар");

        cal.add(Calendar.DAY_OF_MONTH, 2);
        addTask(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), "Приготовить ужин");
        addTask(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), "Сделать уборку");

        cal.add(Calendar.DAY_OF_MONTH, 3);
        addTask(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), "Купить подарок");
        addTask(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), "Подготовиться к учебе");
    }

    private void initTestEvents() {
        eventsMap = new HashMap<>();
        addEvent(currentYear, currentMonth, currentDay, "7:00", "8:30", "Завтрак");
        addEvent(currentYear, currentMonth, currentDay, "9:00", "13:00", "Учеба");
        addEvent(currentYear, currentMonth, currentDay, "12:00", "13:00", "Обед");
        addEvent(currentYear, currentMonth, currentDay, "14:00", "18:00", "Работа");
        addEvent(currentYear, currentMonth, currentDay, "15:30", "18:00", "Подготовить отчет");
        addEvent(currentYear, currentMonth, currentDay, "17:40", "18:30", "Ужин");

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        addEvent(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH),
                "8:00", "10:00", "Встреча с партнерами");
        addEvent(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH),
                "10:00", "12:00", "Презентация проекта");
    }

    private void addTask(int year, int month, int day, String task) {
        String key = year + "-" + month + "-" + day;
        if (!tasksMap.containsKey(key)) tasksMap.put(key, new ArrayList<>());
        tasksMap.get(key).add(task);
    }

    private void addEvent(int year, int month, int day, String startTime, String endTime, String title) {
        String key = year + "-" + month + "-" + day;
        if (!eventsMap.containsKey(key)) eventsMap.put(key, new ArrayList<>());
        eventsMap.get(key).add(new Event(title, startTime, endTime));
    }

    public List<String> getTasksForDate(int year, int month, int day) {
        return tasksMap.get(year + "-" + month + "-" + day);
    }

    public List<Event> getEventsForDate(int year, int month, int day) {
        return eventsMap.get(year + "-" + month + "-" + day);
    }

    private void handleBackPress() {
        if (currentScreen == Screen.CALENDAR) {
            if (modalPopup != null && modalPopup.getVisibility() == View.VISIBLE) {
                hideModal();
                return;
            }
        }
        if (currentScreen == Screen.TASKS) {
            showCalendar();
            return;
        }
        finish();
    }

    private void showSplashScreen() {
        currentScreen = Screen.SPLASH;
        setContentView(R.layout.activity_splash);
        splashView = findViewById(R.id.splash_root);
        if (splashView != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(500);
            splashView.startAnimation(fadeIn);
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::showOnboarding, SPLASH_DURATION);
    }

    private void showOnboarding() {
        currentScreen = Screen.ONBOARDING;
        setContentView(R.layout.activity_onboarding);
        if (savedState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.onboarding_container, new OnboardingFragment())
                    .commit();
        }
    }

    private void animateTransition(Runnable transition) {
        View root = findViewById(android.R.id.content);
        if (root == null) {
            transition.run();
            return;
        }
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(200);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {
                transition.run();
                AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(200);
                root.startAnimation(fadeIn);
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        root.startAnimation(fadeOut);
    }

    // ==================== КАЛЕНДАРЬ: ДЕНЬ ====================
    private void showCalendar() {
        animateTransition(() -> {
            currentScreen = Screen.CALENDAR;
            currentViewType = "День";
            setContentView(R.layout.calendar_day);
            initDayViews();
            drawDayView();
            setupBottomNavigation();
            setupModalButtons();
        });
    }

    private void initDayViews() {
        View root = findViewById(R.id.coordinatorLayout);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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

    private void drawDayView() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("ru"));
        if (tvMonth != null) {
            String month = monthFormat.format(currentCalendar.getTime());
            month = month.substring(0, 1).toUpperCase() + month.substring(1);
            tvMonth.setText(month);
        }
        if (dateStrip != null) {
            dateStrip.removeAllViews();
            Calendar startCal = (Calendar) currentCalendar.clone();
            startCal.add(Calendar.DAY_OF_MONTH, -7);
            String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            int selectedIndex = -1;
            Calendar today = Calendar.getInstance();
            int todayYear = today.get(Calendar.YEAR);
            int todayMonth = today.get(Calendar.MONTH);
            int todayDay = today.get(Calendar.DAY_OF_MONTH);

            for (int i = 0; i < 14; i++) {
                int year = startCal.get(Calendar.YEAR);
                int month = startCal.get(Calendar.MONTH);
                int day = startCal.get(Calendar.DAY_OF_MONTH);
                int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
                int dayOfWeekIndex = (dayOfWeek - 2 + 7) % 7;

                LinearLayout dayLayout = new LinearLayout(this);
                dayLayout.setOrientation(LinearLayout.VERTICAL);
                dayLayout.setGravity(Gravity.CENTER);
                dayLayout.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(60), LinearLayout.LayoutParams.WRAP_CONTENT));
                dayLayout.setPadding(0, dpToPx(8), 0, dpToPx(8));
                dayLayout.setBackgroundResource(android.R.color.transparent);

                TextView tvWeekday = new TextView(this);
                tvWeekday.setText(dayNames[dayOfWeekIndex]);
                tvWeekday.setTextColor(ContextCompat.getColor(this, R.color.gray));
                tvWeekday.setTextSize(12);
                tvWeekday.setGravity(Gravity.CENTER);

                TextView tvDayNum = new TextView(this);
                tvDayNum.setText(String.valueOf(day));
                tvDayNum.setTextSize(18);
                tvDayNum.setGravity(Gravity.CENTER);
                tvDayNum.setTypeface(tvDayNum.getTypeface(), android.graphics.Typeface.BOLD);

                if (todayYear == year && todayMonth == month && todayDay == day) {
                    tvDayNum.setTextColor(ContextCompat.getColor(this, R.color.red));
                } else {
                    tvDayNum.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
                }
                if (currentYear == year && currentMonth == month && currentDay == day) {
                    selectedIndex = i;
                    dayLayout.setBackgroundResource(R.drawable.selected_day_background);
                }

                dayLayout.addView(tvWeekday);
                dayLayout.addView(tvDayNum);

                final int finalYear = year;
                final int finalMonth = month;
                final int finalDay = day;
                dayLayout.setOnClickListener(v -> {
                    currentYear = finalYear;
                    currentMonth = finalMonth;
                    currentDay = finalDay;
                    currentCalendar.set(finalYear, finalMonth, finalDay);
                    drawDayView();
                });
                dateStrip.addView(dayLayout);
                startCal.add(Calendar.DAY_OF_MONTH, 1);
            }
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
        drawHourlySchedule();
    }

    private void drawHourlySchedule() {
        LinearLayout hourlySchedule = findViewById(R.id.hourlySchedule);
        if (hourlySchedule == null) return;
        hourlySchedule.removeAllViews();
        List<Event> events = getEventsForDate(currentYear, currentMonth, currentDay);
        Map<String, List<Event>> eventsByStartHour = new HashMap<>();
        if (events != null) {
            for (Event event : events) {
                String startHour = event.startTime.split(":")[0] + ":00";
                if (!eventsByStartHour.containsKey(startHour)) {
                    eventsByStartHour.put(startHour, new ArrayList<>());
                }
                eventsByStartHour.get(startHour).add(event);
            }
        }
        for (int hour = 6; hour <= 23; hour++) {
            LinearLayout timeRow = new LinearLayout(this);
            timeRow.setOrientation(LinearLayout.HORIZONTAL);
            timeRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            timeRow.setPadding(0, dpToPx(8), 0, dpToPx(8));

            TextView tvTime = new TextView(this);
            tvTime.setText(hour + ":00");
            tvTime.setTextColor(ContextCompat.getColor(this, R.color.gray));
            tvTime.setTextSize(12);
            tvTime.setGravity(Gravity.START);
            tvTime.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(50), LinearLayout.LayoutParams.WRAP_CONTENT));

            LinearLayout eventsLayout = new LinearLayout(this);
            eventsLayout.setOrientation(LinearLayout.VERTICAL);
            eventsLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            eventsLayout.setPadding(dpToPx(16), 0, 0, 0);

            String timeKey = hour + ":00";
            List<Event> hourEvents = eventsByStartHour.get(timeKey);
            if (hourEvents != null && !hourEvents.isEmpty()) {
                for (Event event : hourEvents) {
                    LinearLayout eventContainer = new LinearLayout(this);
                    eventContainer.setOrientation(LinearLayout.VERTICAL);
                    eventContainer.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
                    LinearLayout.LayoutParams eventParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    eventParams.setMargins(0, 0, 0, dpToPx(8));
                    eventContainer.setLayoutParams(eventParams);

                    TextView tvEventTitle = new TextView(this);
                    tvEventTitle.setText(event.title);
                    tvEventTitle.setTextSize(14);
                    tvEventTitle.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
                    tvEventTitle.setTypeface(tvEventTitle.getTypeface(), android.graphics.Typeface.BOLD);

                    TextView tvEventTime = new TextView(this);
                    tvEventTime.setText("🕐 " + event.startTime + " - " + event.endTime);
                    tvEventTime.setTextSize(12);
                    tvEventTime.setTextColor(ContextCompat.getColor(this, R.color.gray));
                    tvEventTime.setPadding(0, dpToPx(4), 0, 0);

                    eventContainer.addView(tvEventTitle);
                    eventContainer.addView(tvEventTime);
                    eventsLayout.addView(eventContainer);
                }
            }
            timeRow.addView(tvTime);
            timeRow.addView(eventsLayout);
            hourlySchedule.addView(timeRow);
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
            hourlySchedule.addView(divider);
        }
    }

    // ==================== КАЛЕНДАРЬ: НЕДЕЛЯ ====================
    private void showWeekCalendar() {
        animateTransition(() -> {
            currentScreen = Screen.CALENDAR;
            currentViewType = "Неделя";
            setContentView(R.layout.calendar_week);
            initWeekViews();
            drawWeekView();
            setupBottomNavigation();
            setupModalButtons();
        });
    }

    private void initWeekViews() {
        View root = findViewById(R.id.coordinatorLayout);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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

    private void drawWeekView() {
        if (weekGrid == null) return;
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("ru"));
        if (tvMonth != null) {
            String month = monthFormat.format(currentCalendar.getTime());
            month = month.substring(0, 1).toUpperCase() + month.substring(1);
            tvMonth.setText(month);
        }
        if (dateStrip != null) {
            dateStrip.removeAllViews();
            Calendar startCal = (Calendar) currentCalendar.clone();
            startCal.add(Calendar.DAY_OF_MONTH, -7);
            String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            int selectedIndex = -1;
            Calendar today = Calendar.getInstance();
            int todayYear = today.get(Calendar.YEAR);
            int todayMonth = today.get(Calendar.MONTH);
            int todayDay = today.get(Calendar.DAY_OF_MONTH);

            for (int i = 0; i < 14; i++) {
                int year = startCal.get(Calendar.YEAR);
                int month = startCal.get(Calendar.MONTH);
                int day = startCal.get(Calendar.DAY_OF_MONTH);
                int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
                int dayOfWeekIndex = (dayOfWeek - 2 + 7) % 7;

                LinearLayout dayLayout = new LinearLayout(this);
                dayLayout.setOrientation(LinearLayout.VERTICAL);
                dayLayout.setGravity(Gravity.CENTER);
                dayLayout.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(60), LinearLayout.LayoutParams.WRAP_CONTENT));
                dayLayout.setPadding(0, dpToPx(8), 0, dpToPx(8));

                TextView tvWeekday = new TextView(this);
                tvWeekday.setText(dayNames[dayOfWeekIndex]);
                tvWeekday.setTextColor(ContextCompat.getColor(this, R.color.gray));
                tvWeekday.setTextSize(12);
                tvWeekday.setGravity(Gravity.CENTER);

                TextView tvDayNum = new TextView(this);
                tvDayNum.setText(String.valueOf(day));
                tvDayNum.setTextSize(18);
                tvDayNum.setGravity(Gravity.CENTER);
                tvDayNum.setTypeface(tvDayNum.getTypeface(), android.graphics.Typeface.BOLD);

                if (todayYear == year && todayMonth == month && todayDay == day) {
                    tvDayNum.setTextColor(ContextCompat.getColor(this, R.color.red));
                } else {
                    tvDayNum.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
                }
                if (currentYear == year && currentMonth == month && currentDay == day) {
                    selectedIndex = i;
                    dayLayout.setBackgroundResource(R.drawable.selected_day_background);
                }

                dayLayout.addView(tvWeekday);
                dayLayout.addView(tvDayNum);

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
        for (int i = 0; i < 3; i++) row1.addView(createWeekDayCell(weekStart, i, dayNames[i]));
        weekGrid.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        row2.setPadding(0, 0, 0, dpToPx(16));
        for (int i = 3; i < 6; i++) row2.addView(createWeekDayCell(weekStart, i, dayNames[i]));
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
        if (index == 6) params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, dpToPx(12), 0);
        if (index == 2 || index == 5) params.setMargins(0, 0, 0, 0);
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
        if (realToday.get(Calendar.YEAR) == year && realToday.get(Calendar.MONTH) == month && realToday.get(Calendar.DAY_OF_MONTH) == day) {
            tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {
            tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
        }

        LinearLayout tasksLayout = new LinearLayout(this);
        tasksLayout.setOrientation(LinearLayout.VERTICAL);
        tasksLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        List<String> tasks = getTasksForDate(year, month, day);
        if (tasks != null) {
            for (String task : tasks) {
                TextView taskView = new TextView(this);
                taskView.setText("• " + task);
                taskView.setTextSize(12);
                taskView.setTextColor(ContextCompat.getColor(this, R.color.light_text));
                taskView.setMaxLines(1);
                taskView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                taskView.setPadding(0, dpToPx(4), 0, 0);
                tasksLayout.addView(taskView);
            }
        }

        cell.addView(tvDayHeader);
        cell.addView(tvDayNumber);
        cell.addView(tasksLayout);

        final int finalYear = year;
        final int finalMonth = month;
        final int finalDay = day;
        cell.setOnClickListener(v -> {
            currentYear = finalYear;
            currentMonth = finalMonth;
            currentDay = finalDay;
            currentCalendar.set(finalYear, finalMonth, finalDay);
            showCalendar();
        });

        return cell;
    }

    // ==================== КАЛЕНДАРЬ: МЕСЯЦ ====================
    private void showMonthCalendar() {
        animateTransition(() -> {
            currentScreen = Screen.CALENDAR;
            currentViewType = "Месяц";
            setContentView(R.layout.calendar_month);
            initMonthViews();
            drawMonthCalendar();
            setupBottomNavigation();
            setupModalButtons();
            setupMonthGestures();
        });
    }

    private void initMonthViews() {
        View root = findViewById(R.id.coordinatorLayout);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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
        bottomNav = findViewById(R.id.bottomNavigationView);
        if (tvViewValue != null) tvViewValue.setText("Месяц");
        if (fabAdd != null) fabAdd.setOnClickListener(v -> showModal());
        if (dimOverlay != null) dimOverlay.setOnClickListener(v -> hideModal());
        if (layoutViewSelector != null) layoutViewSelector.setOnClickListener(v -> showViewTypeDialog());
        updateMonthHeader();
    }

    private void setupMonthGestures() {
        View root = findViewById(R.id.coordinatorLayout);
        if (root == null) return;
        monthGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) previousMonth(); else nextMonth();
                    return true;
                }
                return false;
            }
        });
        root.setOnTouchListener((v, event) -> monthGestureDetector != null && monthGestureDetector.onTouchEvent(event));
    }

    private void previousMonth() {
        currentCalendar.add(Calendar.MONTH, -1);
        currentYear = currentCalendar.get(Calendar.YEAR);
        currentMonth = currentCalendar.get(Calendar.MONTH);
        updateMonthHeader();
        drawMonthCalendar();
    }

    private void nextMonth() {
        currentCalendar.add(Calendar.MONTH, 1);
        currentYear = currentCalendar.get(Calendar.YEAR);
        currentMonth = currentCalendar.get(Calendar.MONTH);
        updateMonthHeader();
        drawMonthCalendar();
    }

    private void updateMonthHeader() {
        if (tvMonthYear != null) {
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("ru"));
            String text = monthYearFormat.format(currentCalendar.getTime());
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
            tvMonthYear.setText(text);
        }
    }

    private void drawMonthCalendar() {
        if (calendarGrid == null) return;
        calendarGrid.removeAllViews();

        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek - 2 + 7) % 7;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int currentDayNum = 1;
        boolean nextMonthStarted = false;

        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        for (int row = 0; row < 6; row++) {
            LinearLayout weekRow = new LinearLayout(this);
            weekRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            weekRow.setOrientation(LinearLayout.HORIZONTAL);

            for (int col = 0; col < 7; col++) {
                View cellView = LayoutInflater.from(this).inflate(R.layout.month_cell_layout, weekRow, false);
                TextView tvDayNumber = cellView.findViewById(R.id.tvDayNumber);
                LinearLayout tasksContainer = cellView.findViewById(R.id.tasksContainer);

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
                    if (todayYear == currentYear && todayMonth == currentMonth && todayDay == dayNumber) {
                        tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.red));
                    } else {
                        tvDayNumber.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
                    }
                    currentDayNum++;
                }

                tvDayNumber.setText(String.valueOf(dayNumber));

                if (isCurrentMonth) {
                    List<String> tasks = getTasksForDate(displayYear, displayMonth, dayNumber);
                    if (tasks != null) {
                        int taskCount = 0;
                        for (String task : tasks) {
                            if (taskCount >= 3) {
                                TextView moreView = new TextView(this);
                                moreView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                moreView.setText("+ еще " + (tasks.size() - 3));
                                moreView.setTextSize(11);
                                moreView.setTextColor(ContextCompat.getColor(this, R.color.light_text));
                                moreView.setPadding(0, dpToPx(2), 0, dpToPx(2));
                                tasksContainer.addView(moreView);
                                break;
                            }
                            TextView taskView = new TextView(this);
                            taskView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            taskView.setText("• " + task);
                            taskView.setTextSize(11);
                            taskView.setTextColor(ContextCompat.getColor(this, R.color.light_text));
                            taskView.setMaxLines(1);
                            taskView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                            taskView.setPadding(0, dpToPx(2), 0, dpToPx(2));
                            tasksContainer.addView(taskView);
                            taskCount++;
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
                        showCalendar();
                    }
                });
                weekRow.addView(cellView);
            }
            calendarGrid.addView(weekRow);
        }
    }

    // ==================== ОБЩИЕ МЕТОДЫ ====================
    private void showViewTypeDialog() {
        final String[] viewTypes = {"День", "Неделя", "Месяц"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите вид")
                .setItems(viewTypes, (dialog, which) -> {
                    currentViewType = viewTypes[which];
                    if (tvViewValue != null) tvViewValue.setText(currentViewType);
                    switch (which) {
                        case 0: showCalendar(); break;
                        case 1: showWeekCalendar(); break;
                        case 2: showMonthCalendar(); break;
                    }
                });
        builder.create().show();
    }

    @SuppressLint("MissingInflatedId")
    private void showTasks() {
        animateTransition(() -> {
            currentScreen = Screen.TASKS;
            setContentView(R.layout.fragment_tasks); // Загружаем оболочку

            dimOverlay = findViewById(R.id.dimOverlay);
            modalPopup = findViewById(R.id.modalPopup);
            fabAdd = findViewById(R.id.fabAdd);
            bottomNav = findViewById(R.id.bottomNavigationView);

            if (fabAdd != null) fabAdd.setVisibility(View.GONE); // Скрываем "+"
            if (dimOverlay != null) dimOverlay.setOnClickListener(v -> hideModal());
            if (fabAdd != null) fabAdd.setOnClickListener(v -> showModal());

            setupBottomNavigation();
            setupModalButtons();

            TasksFragment fragment = new TasksFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        });
    }

    private void setupBottomNavigation() {
        if (bottomNav == null) bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_calendar) {
                    if (currentScreen != Screen.CALENDAR) showCalendar();
                    return true;
                } else if (itemId == R.id.nav_tasks) {
                    if (currentScreen != Screen.TASKS) showTasks();
                    return true;
                } else if (itemId == R.id.nav_notes) {
                    Toast.makeText(this, "Заметки (в разработке)", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_habits) {
                    Toast.makeText(this, "Привычки (в разработке)", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
            updateBottomNavigationSelection();
        }
    }

    private void setupModalButtons() {
        if (modalPopup == null) return;
        TextView btnAddTask = modalPopup.findViewById(R.id.btnAddTask);
        TextView btnAddNote = modalPopup.findViewById(R.id.btnAddNote);
        TextView btnAddHabit = modalPopup.findViewById(R.id.btnAddHabit);
        View.OnClickListener hideListener = v -> {
            String message = "";
            if (v == btnAddTask) message = "Добавление задачи (в разработке)";
            else if (v == btnAddNote) message = "Добавление заметки (в разработке)";
            else if (v == btnAddHabit) message = "Добавление привычки (в разработке)";
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            hideModal();
        };
        if (btnAddTask != null) btnAddTask.setOnClickListener(hideListener);
        if (btnAddNote != null) btnAddNote.setOnClickListener(hideListener);
        if (btnAddHabit != null) btnAddHabit.setOnClickListener(hideListener);
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
}