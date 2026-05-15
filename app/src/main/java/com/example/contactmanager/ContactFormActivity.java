package com.example.contactmanager;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ContactFormActivity extends AppCompatActivity {
    private static final String TAG = "APP_DEBUG";
    private EditText etName, etPhone, etEmail;
    private Button btnSave;
    private FirebaseContactRepository repo;
    private String editId;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_form);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repo = new FirebaseContactRepository(uid);

        if (getIntent().hasExtra("ID")) {
            isEdit = true;
            editId = getIntent().getStringExtra("ID");
            etName.setText(getIntent().getStringExtra("NAME"));
            etPhone.setText(getIntent().getStringExtra("PHONE"));
            etEmail.setText(getIntent().getStringExtra("EMAIL"));
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (name.isEmpty()) { etName.setError("Введите имя"); return; }

            Contact contact = new Contact(editId, name, phone, email);
            Log.d(TAG, "🖱️ Кнопка 'Сохранить' нажата. isEdit=" + isEdit);

            if (isEdit) {
                repo.update(contact,
                        aVoid -> { Log.d(TAG, "✅ Обновление завершено"); finish(); },
                        e -> { Log.e(TAG, "❌ Ошибка обновления: " + e.getMessage()); }
                );
            } else {
                repo.add(contact,
                        aVoid -> {
                            Log.d(TAG, "✅ Добавление завершено. Закрываю форму.");
                            Toast.makeText(this, "Добавлено", Toast.LENGTH_SHORT).show();
                            finish();
                        },
                        e -> Log.e(TAG, "❌ Ошибка добавления: " + e.getMessage())
                );
            }
        });
    }
}