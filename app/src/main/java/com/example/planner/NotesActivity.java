package com.example.planner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private List<NoteItem> allNotes = new ArrayList<>();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ru"));

    public static class NoteItem {
        String title;
        String content;
        long timestamp;

        public NoteItem(String title, String content, long timestamp) {
            this.title = title;
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    @Override
    protected void onNoteCreated(String title, String content) {
        addNewNote(title, content);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        View rootView = findViewById(R.id.coordinatorLayout);
        bottomNav = findViewById(R.id.bottomNavigationView);

        setupAllInsets(rootView, bottomNav);

        initViews();
        setupBottomNavigation();
        setupModal();
        loadDemoNotes();
        displayNotes();
        updateNotesCount();
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



    private void addNewNote(String title, String content) {
        NoteItem newNote = new NoteItem(title, content, System.currentTimeMillis());
        allNotes.add(0, newNote);
        displayNotes();
        updateNotesCount();
        Toast.makeText(this, "Заметка создана", Toast.LENGTH_SHORT).show();
    }

    private void loadDemoNotes() {
        Calendar cal = Calendar.getInstance();

        // Сегодня
        allNotes.add(new NoteItem("Закат за окном", "Небо окрасилось в неоново-розовый и фиолетовый. Город замер на мгновение. Красота, доступная каждому, стоит только поднять голову.", cal.getTimeInMillis()));
        allNotes.add(new NoteItem("Страница 23", "Читаю новую книгу по психологии. Наткнулся на мысль, которая перевернула мое понимание.", cal.getTimeInMillis() - 3600000));

        // 2026
        cal.set(2026, 1, 8);
        allNotes.add(new NoteItem("Зарядка для мозга", "Решил выучить 5 новых иностранных слов в день. Пока получается, но требует дисциплины.", cal.getTimeInMillis()));

        cal.set(2026, 1, 10);
        allNotes.add(new NoteItem("Идеальный эспрессо", "Наконец-то поймал нужный помол. Кофе выдал густую крема и шоколадный вкус.", cal.getTimeInMillis()));

        cal.set(2026, 1, 11);
        allNotes.add(new NoteItem("Мысли в пробке", "Стою в очередной девятибалльной пробке. В который раз думаю о переезде ближе к работе.", cal.getTimeInMillis()));

        cal.set(2026, 1, 12);
        allNotes.add(new NoteItem("Кот-менеджер", "Весь день Барсик просидел на моих документах. Видимо, решил, что без его контроля я ничего не сделаю.", cal.getTimeInMillis()));

        cal.set(2026, 1, 13);
        allNotes.add(new NoteItem("Вечерний список", "Составил список дел на завтра. Выглядит устрашающе. Начну с самого сложного.", cal.getTimeInMillis()));

        cal.set(2026, 1, 7);
        allNotes.add(new NoteItem("Вдохновение в деталях", "Заметил, как бариста в кофейне рисует молоком сердечки. Маленькие радости.", cal.getTimeInMillis()));

        cal.set(2026, 1, 6);
        allNotes.add(new NoteItem("Цифровой детокс", "Вечер без телефона. Показалось, что время замедлилось. Надо повторять.", cal.getTimeInMillis()));

        cal.set(2026, 1, 5);
        allNotes.add(new NoteItem("План \"Б\"", "Поездку пришлось отменить из-за погоды. Не расстраиваюсь — будет повод устроить домашний уют.", cal.getTimeInMillis()));

        // 2025
        cal.set(2025, 11, 15);
        allNotes.add(new NoteItem("План \"А\"", "Бабка гренька затеяла ремонт. Это будет долгая история.", cal.getTimeInMillis()));
    }

    private void displayNotes() {
        notesContainer.removeAllViews();

        if (allNotes.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Нет заметок");
            emptyView.setTextSize(16);
            emptyView.setTextColor(getColor(R.color.gray));
            emptyView.setGravity(android.view.Gravity.CENTER);
            emptyView.setPadding(0, dpToPx(100), 0, 0);
            notesContainer.addView(emptyView);
            return;
        }

        // Группировка по секциям
        Map<String, List<NoteItem>> sections = new HashMap<>();
        List<String> sectionOrder = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        Calendar thisYear = Calendar.getInstance();
        thisYear.set(Calendar.MONTH, 0);
        thisYear.set(Calendar.DAY_OF_MONTH, 1);

        for (NoteItem note : allNotes) {
            Calendar noteCal = Calendar.getInstance();
            noteCal.setTimeInMillis(note.timestamp);

            String sectionName;
            if (noteCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    noteCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                sectionName = "Сегодня";
            } else if (noteCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                sectionName = String.valueOf(noteCal.get(Calendar.YEAR));
            } else {
                sectionName = String.valueOf(noteCal.get(Calendar.YEAR));
            }

            if (!sections.containsKey(sectionName)) {
                sections.put(sectionName, new ArrayList<>());
                sectionOrder.add(sectionName);
            }
            sections.get(sectionName).add(note);
        }

        // Сортируем секции: Сегодня, потом по убыванию года
        sectionOrder.sort((a, b) -> {
            if (a.equals("Сегодня")) return -1;
            if (b.equals("Сегодня")) return 1;
            return Integer.parseInt(b) - Integer.parseInt(a);
        });

        // Отображаем
        for (String section : sectionOrder) {
            // Заголовок секции
            View header = getLayoutInflater().inflate(R.layout.section_header, notesContainer, false);
            TextView tvSectionTitle = header.findViewById(R.id.tvSectionTitle);
            tvSectionTitle.setText(section);
            notesContainer.addView(header);

            // Заметки в секции
            List<NoteItem> notesInSection = sections.get(section);
            for (NoteItem note : notesInSection) {
                View noteView = getLayoutInflater().inflate(R.layout.item_note, notesContainer, false);

                TextView tvTitle = noteView.findViewById(R.id.tvNoteTitle);
                TextView tvDate = noteView.findViewById(R.id.tvNoteDate);
                TextView tvPreview = noteView.findViewById(R.id.tvNotePreview);

                tvTitle.setText(note.title);

                // Формат даты
                Calendar noteCal = Calendar.getInstance();
                noteCal.setTimeInMillis(note.timestamp);
                Calendar todayCal = Calendar.getInstance();

                if (noteCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                        noteCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
                    tvDate.setText(timeFormat.format(new Date(note.timestamp)));
                } else {
                    tvDate.setText(dateFormat.format(new Date(note.timestamp)));
                }

                // Превью (первые 80 символов)
                String preview = note.content.length() > 80 ? note.content.substring(0, 80) + "..." : note.content;
                tvPreview.setText(preview);

                final int position = notesInSection.indexOf(note);
                final NoteItem currentNote = note;

                noteView.setOnClickListener(v -> {
                    Intent intent = new Intent(NotesActivity.this, NoteEditorActivity.class);
                    intent.putExtra("note_title", currentNote.title);
                    intent.putExtra("note_content", currentNote.content);
                    intent.putExtra("note_timestamp", currentNote.timestamp);
                    intent.putExtra("note_position", position);
                    startActivityForResult(intent, 1);
                });

                notesContainer.addView(noteView);
            }
        }
    }

    private void updateNotesCount() {
        if (tvNotesCount != null) {
            tvNotesCount.setText(allNotes.size() + " заметок");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("habit_name");
            Toast.makeText(this, "Привычка \"" + name + "\" создана", Toast.LENGTH_SHORT).show();
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
}