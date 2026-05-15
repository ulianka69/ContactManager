package com.example.contactmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ContactAdapter.ActionListener {

    private static final String TAG = "ContactManager";

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private FirebaseContactRepository repo;
    private ListenerRegistration listener;
    private TextView tvContactCount; // <-- Элемент для счетчика

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Инициализация RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(this);
        recyclerView.setAdapter(adapter);

        // 2. Инициализация счетчика контактов
        tvContactCount = findViewById(R.id.tvContactCount);
        tvContactCount.setText("Загрузка...");

        // 3. Кнопка добавления
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> openForm(null));
        }

        // 4. Авторизация и инициализация репозитория
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(task -> {
                        if (task.getUser() != null) {
                            setupRepo(task.getUser().getUid());
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Ошибка входа: " + e.getMessage()));
        } else {
            setupRepo(auth.getCurrentUser().getUid());
        }
    }

    private void setupRepo(String uid) {
        Log.d(TAG, "Репозиторий создан для UID: " + uid);
        repo = new FirebaseContactRepository(uid);
    }

    // 🔑 ИСПРАВЛЕНИЕ: Слушатель запускается в onStart
    @Override
    protected void onStart() {
        super.onStart();
        if (repo != null && listener == null) {
            listener = repo.listen(new FirebaseContactRepository.ContactCallback() {
                @Override
                public void onSuccess(List<Contact> contacts) {
                    // Обновляем интерфейс только в главном потоке
                    runOnUiThread(() -> {
                        adapter.setData(contacts);
                        // Обновляем строку со счетчиком
                        tvContactCount.setText("Всего контактов: " + contacts.size());
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Ошибка загрузки: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_LONG).show());
                }
            });
        }
    }

    //  Отключаем слушатель при уходе с экрана
    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    @Override
    public void onEdit(Contact c) {
        openForm(c);
    }

    @Override
    public void onDelete(Contact c) {
        if (repo != null) {
            repo.delete(c.getId(),
                    aVoid -> Toast.makeText(this, "Контакт удален", Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void openForm(Contact c) {
        Intent intent = new Intent(this, ContactFormActivity.class);
        if (c != null) {
            intent.putExtra("ID", c.getId());
            intent.putExtra("NAME", c.getName());
            intent.putExtra("PHONE", c.getPhone());
            intent.putExtra("EMAIL", c.getEmail());
        }
        startActivity(intent);
    }
}