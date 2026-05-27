package com.example.planner;

import android.app.AlertDialog;
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

public class HabitEditorActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView btnSave;
    private EditText etName;
    private EditText etDescription;
    private LinearLayout colorPickerContainer;
    private LinearLayout weekDaysContainer;
    private TextView tvTime;
    private TextView tvTargetDays;

    private int selectedColor = Color.parseColor("#FF6B6B");
    private Calendar selectedTime = Calendar.getInstance();
    private boolean hasTime = false;
    private int targetDays = 0;
    private ImageButton btnDelete;

    private boolean[] selectedDays = new boolean[7];
    private String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private View[] colorViews;
    private TextView[] dayButtons;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));
    private boolean isEditMode = false;
    private long habitId = -1;

    private HabitDao habitDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        AppDatabase db = AppDatabase.getInstance(this);

        habitDao = db.habitDao();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        initViews();
        setupListeners();
        setupColorPicker();
        setupWeekDays();
        setupTargetDays();
        loadHabitData();
        updateTimeDisplay();
        updateColorTheme();
    }

    private void deleteHabit() {
        if (habitId <= 0) return;

        new AlertDialog.Builder(this)
                .setTitle("Удаление привычки")
                .setMessage("Вы уверены, что хотите удалить эту привычку?")
                .setPositiveButton("Удалить", (dialog, which) -> {

                    Executors.newSingleThreadExecutor().execute(() -> {

                        Habit habit = habitDao.getHabitById(habitId);

                        if (habit != null) {
                            habitDao.delete(habit);

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Привычка удалена", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                        }
                    });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnDone);
        btnSave.setText("Сохранить");
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        colorPickerContainer = findViewById(R.id.colorPickerContainer);
        weekDaysContainer = findViewById(R.id.weekDaysContainer);
        tvTime = findViewById(R.id.tvTime);
        tvTargetDays = findViewById(R.id.tvTargetDays);
        btnDelete = findViewById(R.id.btnDelete);

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
        btnSave.setOnClickListener(v -> saveHabit());
        tvTime.setOnClickListener(v -> showTimePicker());
        btnDelete.setOnClickListener(v -> deleteHabit());
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

    private void loadHabitData() {

        habitId = getIntent().getLongExtra("habit_id", -1);

        if (habitId == -1) {
            return;
        }

        isEditMode = true;

        Executors.newSingleThreadExecutor().execute(() -> {

            Habit habit = habitDao.getHabitById(habitId);

            if (habit == null) {
                return;
            }

            runOnUiThread(() -> {

                // 🔥 показываем кнопку ТОЛЬКО если реально есть привычка
                btnDelete.setVisibility(View.VISIBLE);

                etName.setText(habit.name);
                etDescription.setText(habit.description);

                selectedColor = habit.color;

                if (habit.time > 0) {
                    hasTime = true;
                    selectedTime.setTimeInMillis(habit.time);
                    updateTimeDisplay();
                }

                if (habit.targetDays == 0) {
                    tvTargetDays.setText("Бесконечная");
                } else {
                    tvTargetDays.setText(habit.targetDays + " дней");
                }

                targetDays = habit.targetDays;

                String schedule = habit.schedule;

                for (int i = 0; i < dayNames.length; i++) {
                    if (schedule.contains(dayNames[i])) {
                        selectedDays[i] = true;
                        updateDayButtonStyle(dayButtons[i], true);
                    }
                }

                String[] colorArray = {
                        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
                        "#FFEAA7", "#DDA0DD", "#FF8C42", "#A8E6CF"
                };

                for (int i = 0; i < colorArray.length; i++) {
                    if (Color.parseColor(colorArray[i]) == selectedColor) {
                        updateColorSelection(i);
                        break;
                    }
                }

                updateColorTheme();
            });
        });
    }

    private void saveHabit() {

        String name = etName.getText().toString().trim();

        if (name.isEmpty()) {

            Toast.makeText(
                    this,
                    "Введите название привычки",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String description =
                etDescription.getText().toString().trim();

        StringBuilder daysStr = new StringBuilder();

        for (int i = 0; i < selectedDays.length; i++) {

            if (selectedDays[i]) {

                if (daysStr.length() > 0) {

                    daysStr.append(", ");
                }

                daysStr.append(dayNames[i]);
            }
        }

        String tempSchedule =
                daysStr.length() > 0
                        ? daysStr.toString()
                        : "Не выбрано";

        if (hasTime) {

            tempSchedule += " в " +
                    timeFormat.format(selectedTime.getTime());
        }

        final String schedule = tempSchedule;

        Executors.newSingleThreadExecutor().execute(() -> {

            Habit habit =
                    habitDao.getHabitById(habitId);

            if (habit == null) {
                return;
            }

            habit.name = name;

            habit.description = description;

            habit.color = selectedColor;

            habit.schedule = schedule;

            habit.targetDays = targetDays;

            if (hasTime) {

                habit.time =
                        selectedTime.getTimeInMillis();
            }

            habitDao.update(habit);

            runOnUiThread(() -> {

                Toast.makeText(
                        this,
                        "Изменения сохранены",
                        Toast.LENGTH_SHORT
                ).show();

                setResult(RESULT_OK);

                finish();
            });
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}