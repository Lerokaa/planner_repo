package com.example.planner;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddHabitActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView btnDone;
    private EditText etName;
    private EditText etDescription;
    private LinearLayout colorPickerContainer;
    private LinearLayout weekDaysContainer;
    private TextView tvTime;
    private TextView tvTargetDays;

    private TextView tvReminder;

    private int selectedColor = Color.parseColor("#FF6B6B");
    private Calendar selectedTime = Calendar.getInstance();
    private boolean hasTime = false;
    private int targetDays = 0; // 0 = бесконечная

    private boolean[] selectedDays = new boolean[7];
    private String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private View[] colorViews;
    private TextView[] dayButtons;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));

    private boolean hasReminder = false;
    private long reminderTimeMillis = 0;


    private ReminderDao reminderDao;

    private HabitDao habitDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        AppDatabase db = AppDatabase.getInstance(this);
        reminderDao = db.reminderDao();
        habitDao = db.habitDao();

        initViews();
        setupListeners();
        setupColorPicker();
        setupWeekDays();
        setupTargetDays();
        updateTimeDisplay();
        showKeyboard();
        updateColorTheme();
        updateReminderDisplay();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnDone = findViewById(R.id.btnDone);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        colorPickerContainer = findViewById(R.id.colorPickerContainer);
        weekDaysContainer = findViewById(R.id.weekDaysContainer);
        tvTime = findViewById(R.id.tvTime);
        tvTargetDays = findViewById(R.id.tvTargetDays);
        tvReminder = findViewById(R.id.tvReminder);

        colorViews = new View[8];
        colorViews[0] = findViewById(R.id.color0);
        colorViews[1] = findViewById(R.id.color1);
        colorViews[2] = findViewById(R.id.color2);
        colorViews[3] = findViewById(R.id.color3);
        colorViews[4] = findViewById(R.id.color4);
        colorViews[5] = findViewById(R.id.color5);
        colorViews[6] = findViewById(R.id.color6);
        colorViews[7] = findViewById(R.id.color7);

        dayButtons = new TextView[7];
    }

    private void setupColorPicker() {
        String[] colorArray = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD", "#FF8C42", "#A8E6CF"};

        for (int i = 0; i < colorViews.length; i++) {
            final int colorValue = Color.parseColor(colorArray[i]);
            final int index = i;

            GradientDrawable drawable = (GradientDrawable) colorViews[i].getBackground();
            drawable.setColor(colorValue);

            colorViews[i].setOnClickListener(v -> {
                selectedColor = colorValue;
                updateColorSelection(index);
                updateColorTheme();
            });
        }
    }

    private void updateColorSelection(int selectedIndex) {
        for (int i = 0; i < colorViews.length; i++) {
            GradientDrawable drawable = (GradientDrawable) colorViews[i].getBackground();
            if (i == selectedIndex) {
                drawable.setStroke(dpToPx(4), ContextCompat.getColor(this, R.color.dark_text));
            } else {
                drawable.setStroke(0, Color.TRANSPARENT);
            }
        }
    }

    private void updateColorTheme() {
        View titleContainer = findViewById(R.id.titleContainer);
        View colorBarTop = findViewById(R.id.colorBarTop);
        View colorBarLeft = findViewById(R.id.colorBarLeft);

        if (colorBarTop != null) {
            colorBarTop.setBackgroundColor(selectedColor);
        }
        if (colorBarLeft != null) {
            colorBarLeft.setBackgroundColor(selectedColor);
        }
        if (titleContainer != null) {
            String hexColor = String.format("#%06X", (0xFFFFFF & selectedColor));
            String transparentColor = "#1A" + hexColor.substring(1);
            titleContainer.setBackgroundColor(Color.parseColor(transparentColor));
        }
    }

    private void setupWeekDays() {
        weekDaysContainer.removeAllViews();

        for (int i = 0; i < dayNames.length; i++) {
            final int dayIndex = i;
            TextView dayButton = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, dpToPx(44), 1);
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dayButton.setLayoutParams(params);
            dayButton.setText(dayNames[i]);
            dayButton.setTextSize(14);
            dayButton.setGravity(android.view.Gravity.CENTER);
            dayButton.setBackgroundResource(R.drawable.day_button_unselected);
            dayButton.setTextColor(ContextCompat.getColor(this, R.color.light_text));
            dayButton.setPadding(0, dpToPx(8), 0, dpToPx(8));

            dayButton.setOnClickListener(v -> {
                selectedDays[dayIndex] = !selectedDays[dayIndex];
                updateDayButtonStyle(dayButton, selectedDays[dayIndex]);
            });

            weekDaysContainer.addView(dayButton);
            dayButtons[i] = dayButton;
        }
    }

    private void setupTargetDays() {
        tvTargetDays.setOnClickListener(v -> {
            String[] options = {"Бесконечная", "7 дней", "14 дней", "30 дней", "66 дней", "100 дней"};
            int[] values = {0, 7, 14, 30, 66, 100};

            new AlertDialog.Builder(this)
                    .setTitle("Цель привычки")
                    .setItems(options, (dialog, which) -> {
                        targetDays = values[which];
                        if (targetDays == 0) {
                            tvTargetDays.setText("Бесконечная");
                        } else {
                            tvTargetDays.setText(targetDays + " дней");
                        }
                    })
                    .show();
        });
    }

    private void updateDayButtonStyle(TextView button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundResource(R.drawable.day_button_selected);
            button.setTextColor(Color.WHITE);
        } else {
            button.setBackgroundResource(R.drawable.day_button_unselected);
            button.setTextColor(ContextCompat.getColor(this, R.color.light_text));
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> saveAndClose());
        tvTime.setOnClickListener(v -> showTimePicker());
        tvReminder.setOnClickListener(v -> showReminderDialog());
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    hasTime = true;
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    updateTimeDisplay();
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE), true);
        timePicker.show();
    }

    private void updateTimeDisplay() {
        if (hasTime) {
            tvTime.setText(timeFormat.format(selectedTime.getTime()));
        } else {
            tvTime.setText("Не указано");
        }
    }

    private void showKeyboard() {
        etName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(etName, InputMethodManager.SHOW_IMPLICIT);
    }

    private void showReminderDialog() {
        String[] options = {"Без напоминания", "В выбранное время", "Выбрать другое время"};

        new AlertDialog.Builder(this)
                .setTitle("Напоминание")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        hasReminder = false;
                    } else if (which == 1) { // В выбранное время
                        if (hasTime) {
                            hasReminder = true;
                            reminderTimeMillis = selectedTime.getTimeInMillis();
                        } else {
                            Toast.makeText(this, "Сначала укажите время привычки", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else if (which == 2) { // Выбрать другое время
                        showCustomTimePickerForReminder();
                    }
                    updateReminderDisplay();
                })
                .show();
    }

    private void showCustomTimePickerForReminder() {

        Calendar calendar = Calendar.getInstance();

        new TimePickerDialog(this, (view, hourOfDay, minute) -> {

            hasReminder = true;

            Calendar reminderCal = Calendar.getInstance();

            reminderCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            reminderCal.set(Calendar.MINUTE, minute);
            reminderCal.set(Calendar.SECOND, 0);

            if (reminderCal.before(Calendar.getInstance())) {
                reminderCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            reminderTimeMillis = reminderCal.getTimeInMillis();

            updateReminderDisplay();

        },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void updateReminderDisplay() {
        if (hasReminder) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(reminderTimeMillis);
            tvReminder.setText("Напоминание в " + timeFormat.format(cal.getTime()));
        } else {
            tvReminder.setText("Без напоминания");
        }
    }

    private void saveAndClose() {

        String name = etName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this,
                    "Введите название привычки",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText().toString().trim();

        StringBuilder daysStr = new StringBuilder();

        for (int i = 0; i < selectedDays.length; i++) {

            if (selectedDays[i]) {

                if (daysStr.length() > 0) {
                    daysStr.append(", ");
                }

                daysStr.append(dayNames[i]);
            }
        }

        String schedule;

        if (daysStr.length() > 0) {
            schedule = daysStr.toString();
        } else {
            schedule = "Не выбрано";
        }

        if (hasTime) {
            schedule += " в " +
                    timeFormat.format(selectedTime.getTime());
        }

        Habit habit = new Habit();

        habit.name = name;
        habit.description = description;
        habit.color = selectedColor;
        habit.schedule = schedule;
        habit.targetDays = targetDays;

        habit.completedDays = 0;

        habit.streak = 0;

        habit.bestStreak = 0;

        habit.completedToday = false;

        if (hasTime) {
            habit.time = selectedTime.getTimeInMillis();
        }

        habit.hasReminder = hasReminder;
        habit.reminderTime = reminderTimeMillis;

        Intent resultIntent = new Intent();

        resultIntent.putExtra("habit_name", name);
        resultIntent.putExtra("habit_description", description);
        resultIntent.putExtra("habit_color", selectedColor);
        resultIntent.putExtra("habit_schedule", schedule);
        resultIntent.putExtra("habit_days", selectedDays);
        resultIntent.putExtra("habit_has_time", hasTime);
        resultIntent.putExtra("habit_target_days", targetDays);

        if (hasTime) {
            resultIntent.putExtra(
                    "habit_time",
                    selectedTime.getTimeInMillis()
            );
        }

        resultIntent.putExtra("has_reminder", hasReminder);

        Executors.newSingleThreadExecutor().execute(() -> {

            long habitId = habitDao.insert(habit);

            if (hasReminder) {

                Reminder reminder = new Reminder();

                reminder.taskId = habitId;
                reminder.type = "time";
                reminder.triggerTime = reminderTimeMillis;
                reminder.isTriggered = false;

                long reminderId = reminderDao.insert(reminder);

                ReminderScheduler.scheduleReminder(
                        AddHabitActivity.this,
                        reminderId,
                        reminderTimeMillis,
                        name
                );
            }
        });

        setResult(RESULT_OK, resultIntent);

        finish();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}