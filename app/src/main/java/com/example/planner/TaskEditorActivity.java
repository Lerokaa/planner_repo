package com.example.planner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

    private int selectedColor = Color.parseColor("#4B5C78");
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private boolean hasEndTime = false;

    private View[] colorViews;

    private boolean isEditMode = false;
    private long taskId = -1;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru"));
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        initViews();
        setupListeners();
        loadTaskData();
        updateDateTimeDisplay();
        setupColorPicker();
        updateColorTheme();
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

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTask());

        tvDate.setOnClickListener(v -> showDatePicker());
        tvStartTime.setOnClickListener(v -> showStartTimePicker());
        tvEndTime.setOnClickListener(v -> showEndTimePicker());
    }

    private void loadTaskData() {
        if (getIntent().hasExtra("task_title")) {
            isEditMode = true;
            String originalTitle = getIntent().getStringExtra("task_title");
            String originalDescription = getIntent().getStringExtra("task_description");
            int originalColor = getIntent().getIntExtra("task_color", Color.parseColor("#4B5C78"));
            long originalDate = getIntent().getLongExtra("task_date", System.currentTimeMillis());
            long originalStartTime = getIntent().getLongExtra("task_start_time", System.currentTimeMillis());
            long originalEndTime = getIntent().getLongExtra("task_end_time", -1);
            taskId = getIntent().getLongExtra("task_id", -1);

            etTitle.setText(originalTitle);
            etDescription.setText(originalDescription);
            selectedColor = originalColor;
            selectedDate.setTimeInMillis(originalDate);
            startTime.setTimeInMillis(originalStartTime);
            if (originalEndTime != -1) {
                hasEndTime = true;
                endTime.setTimeInMillis(originalEndTime);
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateTimeDisplay();
                    updateColorTheme();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showStartTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    startTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE), true);
        timePicker.show();
    }

    private void showEndTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    hasEndTime = true;
                    endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    endTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE), true);
        timePicker.show();
    }

    private void updateDateTimeDisplay() {
        tvDate.setText(dateFormat.format(selectedDate.getTime()));
        tvStartTime.setText(timeFormat.format(startTime.getTime()));
        if (hasEndTime) {
            tvEndTime.setText(timeFormat.format(endTime.getTime()));
            tvEndTime.setTextColor(ContextCompat.getColor(this, R.color.light_text));
        } else {
            tvEndTime.setText("Не указано");
            tvEndTime.setTextColor(ContextCompat.getColor(this, R.color.gray));
        }
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText().toString().trim();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("task_title", title);
        resultIntent.putExtra("task_description", description);
        resultIntent.putExtra("task_color", selectedColor);
        resultIntent.putExtra("task_date", selectedDate.getTimeInMillis());
        resultIntent.putExtra("task_start_time", startTime.getTimeInMillis());
        resultIntent.putExtra("task_end_time", hasEndTime ? endTime.getTimeInMillis() : -1);
        resultIntent.putExtra("task_id", taskId);
        resultIntent.putExtra("is_edit", isEditMode);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}