package com.example.planner;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        insertTestNote();
        loadNotes();
    }

    private void insertTestNote() {
        new Thread(() -> {
            Note note = new Note();
            note.title = "Первая заметка";
            note.content = "# Заголовок\n- пункт 1\n- пункт 2";
            note.lastModified = System.currentTimeMillis();

            db.noteDao().insert(note);
        }).start();
    }

    private void loadNotes() {
        new Thread(() -> {
            List<Note> notes = db.noteDao().getAllNotes();

            runOnUiThread(() -> {
                for (Note n : notes) {
                    System.out.println(n.title + " | " + n.content);
                }
            });
        }).start();
    }
}