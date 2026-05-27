package com.example.planner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TaskEditorActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView btnSave;
    private EditText etTitle;
    private EditText etDescription;
    private TextView tvDate;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private LinearLayout titleContainer;
    private View colorBarTop;
    private View colorBarLeft;

    private TaskViewModel viewModel;

    // Данные задачи
    private int selectedColor = Color.parseColor("#FF6B6B");
    private Calendar selectedDate = Calendar.getInstance();
    private int startTime = -1;   // Минуты от полуночи (например, 9:30 = 570)
    private int endTime = -1;     // Минуты от полуночи, -1 если не указано
    private boolean hasEndTime = false;

    private View[] colorViews;
    private long taskId = -1;

    private TextView btnDelete;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru"));
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        //  Получаем ID задачи (убраны пробелы в ключе "task_id")
        taskId = getIntent().getLongExtra("task_id", -1);

        initViews();
        setupListeners();
        setupColorPicker();

        if (taskId > 0) {
            btnDelete.setVisibility(View.VISIBLE);
            loadTaskFromDb();
        } else {
            btnDelete.setVisibility(View.GONE);

            selectedColor = Color.parseColor("#FF6B6B");
            updateDateTimeDisplay();
            showKeyboardIfNeeded();
        }

        updateColorTheme();
        updateColorSelection(0);
    }

    // 🔥 Загрузка из БД в фоновом потоке
    private void loadTaskFromDb() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Task task = AppDatabase.getInstance(this).taskDao().getTaskById(taskId);
            if (task != null) {
                runOnUiThread(() -> {
                    etTitle.setText(task.title);
                    etDescription.setText(task.description);
                    selectedColor = task.color;
                    selectedDate.setTimeInMillis(task.date);
                    startTime = task.startTime;
                    endTime = task.endTime;
                    hasEndTime = endTime >= 0;

                    updateDateTimeDisplay();
                    updateColorTheme();
                    updateColorSelection(getColorIndex(task.color));
                });
            }
        });
    }

    private int getColorIndex(int color) {
        int[] colors = {
                Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"),
                Color.parseColor("#45B7D1"), Color.parseColor("#96CEB4"),
                Color.parseColor("#FFEAA7"), Color.parseColor("#DDA0DD"),
                Color.parseColor("#FF8C42"), Color.parseColor("#A8E6CF")
        };
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == color) return i;
        }
        return 0;
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        tvDate = findViewById(R.id.tvDate);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        titleContainer = findViewById(R.id.titleContainer);
        colorBarTop = findViewById(R.id.colorBarTop);
        colorBarLeft = findViewById(R.id.colorBarLeft);
        btnDelete = findViewById(R.id.btnDelete);

        if (btnDelete != null) {
            btnDelete.setVisibility(View.GONE);
        }

        colorViews = new View[8];
        colorViews[0] = findViewById(R.id.color0);
        colorViews[1] = findViewById(R.id.color1);
        colorViews[2] = findViewById(R.id.color2);
        colorViews[3] = findViewById(R.id.color3);
        colorViews[4] = findViewById(R.id.color4);
        colorViews[5] = findViewById(R.id.color5);
        colorViews[6] = findViewById(R.id.color6);
        colorViews[7] = findViewById(R.id.color7);
    }

    private void deleteTask() {
        if (taskId <= 0) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Удаление задачи")
                .setMessage("Вы уверены, что хотите удалить эту задачу?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        Task task = AppDatabase.getInstance(this)
                                .taskDao()
                                .getTaskById(taskId);

                        if (task != null) {
                            AppDatabase.getInstance(this)
                                    .taskDao()
                                    .delete(task);

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Задача удалена", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void setupListeners() {
        // 🔥 Кнопка НАЗАД теперь просто закрывает экран (отмена изменений)
        btnBack.setOnClickListener(v -> finish());

        btnDelete.setOnClickListener(v -> deleteTask());

        // Кнопка СОХРАНИТЬ выполняет сохранение
        btnSave.setOnClickListener(v -> saveAndClose());

        tvDate.setOnClickListener(v -> showDatePicker());
        tvStartTime.setOnClickListener(v -> showStartTimePicker());
        tvEndTime.setOnClickListener(v -> showEndTimePicker());
    }

    private void setupColorPicker() {
        String[] colorArray = {
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
                "#FFEAA7", "#DDA0DD", "#FF8C42", "#A8E6CF"
        };
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
        if (colorBarTop != null) colorBarTop.setBackgroundColor(selectedColor);
        if (colorBarLeft != null) colorBarLeft.setBackgroundColor(selectedColor);
        if (titleContainer != null) {
            String hex = String.format("#%06X", (0xFFFFFF & selectedColor));
            titleContainer.setBackgroundColor(Color.parseColor("#1A" + hex.substring(1)));
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    updateDateTimeDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showStartTimePicker() {
        int hour = startTime >= 0 ? startTime / 60 : Calendar.HOUR_OF_DAY;
        int minute = startTime >= 0 ? startTime % 60 : Calendar.MINUTE;
        new TimePickerDialog(this,
                (view, h, m) -> {
                    startTime = h * 60 + m;
                    updateDateTimeDisplay();
                }, hour, minute, true).show();
    }

    private void showEndTimePicker() {
        int hour = endTime >= 0 ? endTime / 60 : Calendar.HOUR_OF_DAY;
        int minute = endTime >= 0 ? endTime % 60 : Calendar.MINUTE;
        new TimePickerDialog(this,
                (view, h, m) -> {
                    hasEndTime = true;
                    endTime = h * 60 + m;
                    updateDateTimeDisplay();
                }, hour, minute, true).show();
    }

    private void updateDateTimeDisplay() {
        tvDate.setText(dateFormat.format(selectedDate.getTime()));
        tvStartTime.setText(formatMinutes(startTime));
        tvEndTime.setText(hasEndTime && endTime >= 0 ? formatMinutes(endTime) : "Не указано");
        tvEndTime.setTextColor(hasEndTime ?
                ContextCompat.getColor(this, R.color.light_text) :
                ContextCompat.getColor(this, R.color.gray));
    }

    private String formatMinutes(int minutes) {
        if (minutes < 0) return "Не указано";
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private void showKeyboardIfNeeded() {
        if (etTitle.getText().toString().trim().isEmpty()) {
            etTitle.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(etTitle, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void saveAndClose() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTime < 0) {
            Toast.makeText(this, "Выберите время начала", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText().toString().trim();

        Task task = new Task();

        // 🔥 ИСПРАВЛЕНИЕ: присваиваем ID ТОЛЬКО при редактировании существующей задачи
        if (taskId > 0) {
            task.id = taskId;
        }
        // Для новой задачи task.id останется 0 (значение по умолчанию),
        // и Room автоматически сгенерирует уникальный ID.

        task.title = title;
        task.description = description;
        task.date = selectedDate.getTimeInMillis();
        task.startTime = startTime;
        task.endTime = hasEndTime ? endTime : -1;
        task.isCompleted = false;
        task.color = selectedColor;

        if (taskId > 0) {
            viewModel.update(task);
        } else {
            viewModel.insert(task);
        }

        finish();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}