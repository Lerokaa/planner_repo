package com.example.planner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView btnDone;
    private TextView tvDate;
    private EditText etTitle;
    private EditText etContent;

    private NoteViewModel viewModel;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Инициализация ViewModel
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                checkAndClose();
            }
        });

        initViews();
        setupListeners();
        updateDate();
        showKeyboard();

        btnDone.setVisibility(View.VISIBLE);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnDone = findViewById(R.id.btnDone);
        tvDate = findViewById(R.id.tvDate);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> checkAndClose());
        btnDone.setOnClickListener(v -> saveAndClose());
    }

    private void showKeyboard() {
        etTitle.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(etTitle, InputMethodManager.SHOW_IMPLICIT);
    }

    private void updateDate() {
        Date date = new Date();
        String formattedDate = "Сегодня " + timeFormat.format(date);
        tvDate.setText(formattedDate);
    }

    private void checkAndClose() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (!title.isEmpty() || !content.isEmpty()) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Сохранить заметку?")
                    .setMessage("Вы хотите сохранить изменения?")
                    .setPositiveButton("Сохранить", (dialog, which) -> saveAndClose())
                    .setNegativeButton("Отмена", (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void saveAndClose() {
        String newTitle = etTitle.getText().toString().trim();
        String newContent = etContent.getText().toString().trim();

        if (newTitle.isEmpty() && !newContent.isEmpty()) {
            if (newContent.length() > 50) {
                newTitle = newContent.substring(0, 50) + "...";
            } else {
                newTitle = newContent;
            }
        }

        if (newTitle.isEmpty() && newContent.isEmpty()) {
            finish();
            return;
        }

        // 🔥 СОЗДАЕМ И СОХРАНЯЕМ ЗАМЕТКУ В БАЗУ
        Note note = new Note();
        note.title = newTitle;
        note.content = newContent;
        note.lastModified = System.currentTimeMillis();

        viewModel.insert(note);

        Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
        finish();
    }
}