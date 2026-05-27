package com.example.planner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotesActivity extends BaseActivity {

    private LinearLayout notesContainer;
    private TextView tvNotesCount;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAdd;
    private View dimOverlay;
    private LinearLayout modalPopup;

    private NoteViewModel viewModel;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ru"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        View rootView = findViewById(R.id.coordinatorLayout);
        bottomNav = findViewById(R.id.bottomNavigationView);
        setupAllInsets(rootView, bottomNav);

        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        viewModel.getAllNotes().observe(this, this::displayNotesFromDb);

        initViews();
        setupBottomNavigation();
        setupModal();
    }

    private void displayNotesFromDb(List<Note> notes) {
        notesContainer.removeAllViews();

        if (notes.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Нет заметок");
            emptyView.setTextSize(16);
            emptyView.setTextColor(getColor(R.color.gray));
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(0, dpToPx(100), 0, 0);
            notesContainer.addView(emptyView);
            updateNotesCount(0);
            return;
        }

        Map<String, List<Note>> sections = new HashMap<>();
        List<String> sectionOrder = new ArrayList<>();
        Calendar today = Calendar.getInstance();

        for (Note note : notes) {
            Calendar noteCal = Calendar.getInstance();
            noteCal.setTimeInMillis(note.lastModified);

            String sectionName;
            if (noteCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    noteCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                sectionName = "Сегодня";
            } else {
                sectionName = String.valueOf(noteCal.get(Calendar.YEAR));
            }

            if (!sections.containsKey(sectionName)) {
                sections.put(sectionName, new ArrayList<>());
                sectionOrder.add(sectionName);
            }
            sections.get(sectionName).add(note);
        }

        sectionOrder.sort((a, b) -> {
            if (a.equals("Сегодня")) return -1;
            if (b.equals("Сегодня")) return 1;
            return Integer.parseInt(b) - Integer.parseInt(a);
        });

        for (String section : sectionOrder) {
            View header = getLayoutInflater().inflate(R.layout.section_header, notesContainer, false);
            TextView tvSectionTitle = header.findViewById(R.id.tvSectionTitle);
            tvSectionTitle.setText(section);
            notesContainer.addView(header);

            for (Note note : sections.get(section)) {
                View noteView = getLayoutInflater().inflate(R.layout.item_note, notesContainer, false);

                TextView tvTitle = noteView.findViewById(R.id.tvNoteTitle);
                TextView tvDate = noteView.findViewById(R.id.tvNoteDate);
                TextView tvPreview = noteView.findViewById(R.id.tvNotePreview);

                tvTitle.setText(note.title);

                Calendar noteCal = Calendar.getInstance();
                noteCal.setTimeInMillis(note.lastModified);
                if (noteCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        noteCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    tvDate.setText(timeFormat.format(new Date(note.lastModified)));
                } else {
                    tvDate.setText(dateFormat.format(new Date(note.lastModified)));
                }

                String preview = note.content.length() > 80 ?
                        note.content.substring(0, 80) + "..." : note.content;
                tvPreview.setText(preview);

                noteView.setOnClickListener(v -> {
                    Intent intent = new Intent(NotesActivity.this, NoteEditorActivity.class);
                    intent.putExtra("note_id", note.id);
                    startActivity(intent);
                });

                notesContainer.addView(noteView);
            }
        }
        updateNotesCount(notes.size());
    }

    private void updateNotesCount(int count) {
        if (tvNotesCount != null) {
            tvNotesCount.setText(count + " заметок");
        }
    }

    private void initViews() {
        notesContainer = findViewById(R.id.notesContainer);
        tvNotesCount = findViewById(R.id.tvNotesCount);
        fabAdd = findViewById(R.id.fabAdd);
        dimOverlay = findViewById(R.id.dimOverlay);
        modalPopup = findViewById(R.id.modalPopup);
    }

    private void setupModal() {
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showModal());
        }
        if (dimOverlay != null) {
            dimOverlay.setOnClickListener(v -> hideModal());
        }
        setupModalButtons();
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

    private void setupBottomNavigation() {
        if (bottomNav == null) return;

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_tasks) {
                startActivity(new Intent(this, TasksActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_notes) {
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

        bottomNav.setSelectedItemId(R.id.nav_notes);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onNoteCreated(String title, String content) {
        Note note = new Note();
        note.title = title;
        note.content = content;
        note.lastModified = System.currentTimeMillis();
        viewModel.insert(note);
        Toast.makeText(this, "Заметка \"" + title + "\" создана", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onTaskCreated(String title, String startTime, String endTime, int year, int month, int day) {
        Toast.makeText(this, "Задача \"" + title + "\" создана. Перейдите в календарь или задачи", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onHabitCreated(String name, String schedule) {
        Toast.makeText(this, "Привычка \"" + name + "\" создана. Перейдите в привычки", Toast.LENGTH_SHORT).show();
    }
}