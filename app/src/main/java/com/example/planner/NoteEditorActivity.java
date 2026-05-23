package com.example.planner;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteEditorActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvDate;
    private EditText etTitle;
    private EditText etContent;
    private LinearLayout formatBar;

    private TextView btnFormatAa, btnFormatBold, btnFormatItalic;
    private TextView btnFormatUnderline, btnFormatStrike, btnFormatColor;

    private String noteTitle;
    private String noteContent;
    private long noteTimestamp;
    private int notePosition;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        // Получаем данные
        if (getIntent().hasExtra("note_title")) {
            noteTitle = getIntent().getStringExtra("note_title");
            noteContent = getIntent().getStringExtra("note_content");
            noteTimestamp = getIntent().getLongExtra("note_timestamp", System.currentTimeMillis());
            notePosition = getIntent().getIntExtra("note_position", -1);
        } else {
            noteTitle = "";
            noteContent = "";
            noteTimestamp = System.currentTimeMillis();
            notePosition = -1;
        }

        initViews();
        setupListeners();
        updateDateDisplay();
        setupFormatBar();
        showKeyboardIfNeeded();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvDate = findViewById(R.id.tvDate);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        formatBar = findViewById(R.id.formatBar);

        btnFormatAa = findViewById(R.id.btnFormatAa);
        btnFormatBold = findViewById(R.id.btnFormatBold);
        btnFormatItalic = findViewById(R.id.btnFormatItalic);
        btnFormatUnderline = findViewById(R.id.btnFormatUnderline);
        btnFormatStrike = findViewById(R.id.btnFormatStrike);
        btnFormatColor = findViewById(R.id.btnFormatColor);

        etTitle.setText(noteTitle);
        etContent.setText(noteContent);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> saveAndClose());

        // Показываем панель форматирования при фокусе на поле ввода
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                formatBar.setVisibility(View.VISIBLE);
            }
        };

        etTitle.setOnFocusChangeListener(focusListener);
        etContent.setOnFocusChangeListener(focusListener);

        // Скрываем панель при скролле (опционально)
        findViewById(R.id.scrollView).setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Можно скрывать панель при скролле
        });
    }

    private void setupFormatBar() {
        // Aa - настройки шрифта (показать диалог выбора размера)
        btnFormatAa.setOnClickListener(v -> showFontSizeDialog());

        // B - жирный
        btnFormatBold.setOnClickListener(v -> applyStyle(new StyleSpan(android.graphics.Typeface.BOLD)));

        // I - курсив
        btnFormatItalic.setOnClickListener(v -> applyStyle(new StyleSpan(android.graphics.Typeface.ITALIC)));

        // U - подчеркнутый
        btnFormatUnderline.setOnClickListener(v -> applyStyle(new UnderlineSpan()));

        // S - зачеркнутый
        btnFormatStrike.setOnClickListener(v -> applyStyle(new StrikethroughSpan()));

        // ● - цвет текста
        btnFormatColor.setOnClickListener(v -> showColorPickerDialog());
    }

    private void applyStyle(Object span) {
        EditText editText = getCurrentFocusEditText();
        if (editText == null) return;

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start != end) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(editText.getText());
            spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            editText.setText(spannable);
            editText.setSelection(start, end);
        } else {
            Toast.makeText(this, "Выделите текст для форматирования", Toast.LENGTH_SHORT).show();
        }
    }

    private EditText getCurrentFocusEditText() {
        if (etTitle.hasFocus()) return etTitle;
        if (etContent.hasFocus()) return etContent;
        return null;
    }

    private void showFontSizeDialog() {
        final String[] sizes = {"Маленький", "Средний", "Большой", "Огромный"};
        final int[] sizeValues = {12, 16, 20, 28};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Размер шрифта")
                .setItems(sizes, (dialog, which) -> {
                    EditText editText = getCurrentFocusEditText();
                    if (editText != null) {
                        editText.setTextSize(sizeValues[which]);
                    }
                })
                .show();
    }

    private void showColorPickerDialog() {
        final String[] colors = {"Черный", "Красный", "Синий", "Зеленый", "Оранжевый"};
        final int[] colorValues = {
                android.graphics.Color.BLACK,
                android.graphics.Color.RED,
                android.graphics.Color.BLUE,
                android.graphics.Color.GREEN,
                android.graphics.Color.rgb(255, 165, 0)
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle("Цвет текста")
                .setItems(colors, (dialog, which) -> {
                    EditText editText = getCurrentFocusEditText();
                    if (editText != null) {
                        int start = editText.getSelectionStart();
                        int end = editText.getSelectionEnd();
                        if (start != end) {
                            SpannableStringBuilder spannable = new SpannableStringBuilder(editText.getText());
                            spannable.setSpan(new ForegroundColorSpan(colorValues[which]), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            editText.setText(spannable);
                            editText.setSelection(start, end);
                        } else {
                            Toast.makeText(this, "Выделите текст для изменения цвета", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void showKeyboardIfNeeded() {
        if (noteTitle.isEmpty() && noteContent.isEmpty()) {
            etTitle.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(etTitle, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void updateDateDisplay() {
        Date date = new Date(noteTimestamp);
        String formattedDate;

        java.util.Calendar noteCal = java.util.Calendar.getInstance();
        noteCal.setTimeInMillis(noteTimestamp);
        java.util.Calendar today = java.util.Calendar.getInstance();

        if (noteCal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                noteCal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)) {
            formattedDate = "Сегодня " + timeFormat.format(date);
        } else {
            formattedDate = fullDateFormat.format(date);
        }

        tvDate.setText(formattedDate);
    }

    private void saveAndClose() {
        String newTitle = etTitle.getText().toString().trim();
        String newContent = etContent.getText().toString().trim();

        if (newTitle.isEmpty() && newContent.isEmpty()) {
            finish();
            return;
        }

        if (newTitle.isEmpty() && !newContent.isEmpty()) {
            if (newContent.length() > 50) {
                newTitle = newContent.substring(0, 50) + "...";
            } else {
                newTitle = newContent;
            }
        }

        android.content.Intent resultIntent = new Intent();
        resultIntent.putExtra("note_title", newTitle);
        resultIntent.putExtra("note_content", newContent);
        resultIntent.putExtra("note_timestamp", System.currentTimeMillis());
        resultIntent.putExtra("note_position", notePosition);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

}