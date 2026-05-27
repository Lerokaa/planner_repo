package com.example.planner;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private NoteViewModel viewModel;
    private long noteId = -1;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteId = getIntent().getLongExtra("note_id", -1);

        initViews();
        setupListeners();
        setupFormatBar();

        if (noteId > 0) {
            // 🔥 Наблюдаем за заметкой из БД
            viewModel.getNoteById(noteId).observe(this, note -> {
                if (note != null) {
                    etTitle.setText(note.title);
                    etContent.setText(note.content);
                    updateDateDisplay(note.lastModified);
                }
            });
        } else {
            updateDateDisplay(System.currentTimeMillis());
            showKeyboardIfNeeded();
        }
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
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> saveAndClose());

        View.OnFocusChangeListener focusListener = (v, hasFocus) ->
                formatBar.setVisibility(hasFocus ? View.VISIBLE : View.GONE);

        etTitle.setOnFocusChangeListener(focusListener);
        etContent.setOnFocusChangeListener(focusListener);
    }

    private void setupFormatBar() {
        btnFormatAa.setOnClickListener(v -> showFontSizeDialog());
        btnFormatBold.setOnClickListener(v -> applyStyle(new StyleSpan(android.graphics.Typeface.BOLD)));
        btnFormatItalic.setOnClickListener(v -> applyStyle(new StyleSpan(android.graphics.Typeface.ITALIC)));
        btnFormatUnderline.setOnClickListener(v -> applyStyle(new UnderlineSpan()));
        btnFormatStrike.setOnClickListener(v -> applyStyle(new StrikethroughSpan()));
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
        final float[] sizeValues = {12, 16, 20, 28};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Размер шрифта")
                .setItems(sizes, (dialog, which) -> {
                    EditText editText = getCurrentFocusEditText();
                    if (editText != null) editText.setTextSize(sizeValues[which]);
                }).show();
    }

    private void showColorPickerDialog() {
        final String[] colors = {"Черный", "Красный", "Синий", "Зеленый", "Оранжевый"};
        final int[] colorValues = {
                android.graphics.Color.BLACK, android.graphics.Color.RED,
                android.graphics.Color.BLUE, android.graphics.Color.GREEN,
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
                        }
                    }
                }).show();
    }

    private void showKeyboardIfNeeded() {
        if (etTitle.getText().toString().trim().isEmpty()) {
            etTitle.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(etTitle, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void updateDateDisplay(long timestamp) {
        Date date = new Date(timestamp);
        Calendar noteCal = Calendar.getInstance();
        noteCal.setTimeInMillis(timestamp);
        Calendar today = Calendar.getInstance();

        String formattedDate = (noteCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                noteCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
                ? "Сегодня " + timeFormat.format(date)
                : fullDateFormat.format(date);

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
            newTitle = newContent.length() > 50 ? newContent.substring(0, 50) + "..." : newContent;
        }

        Note note = new Note();
        note.id = noteId;
        note.title = newTitle;
        note.content = newContent;
        note.lastModified = System.currentTimeMillis();

        if (noteId > 0) {
            viewModel.update(note);
        } else {
            viewModel.insert(note);
        }

        finish();
    }
}