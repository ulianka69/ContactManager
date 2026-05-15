package com.example.contactmanager;

import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FirebaseContactRepository {
    private static final String TAG = "APP_DEBUG";
    private final FirebaseFirestore db;
    private final String userId;

    public FirebaseContactRepository(String userId) {
        this.userId = userId;
        this.db = FirebaseFirestore.getInstance();
    }

    private CollectionReference getContactsRef() {
        return db.collection("users").document(userId).collection("contacts");
    }

    public void add(Contact contact, OnSuccessListener<Void> ok, OnFailureListener err) {
        String newId = UUID.randomUUID().toString();
        contact.setId(newId);
        Log.d(TAG, " Попытка добавить: " + contact.getName());

        getContactsRef().document(newId).set(contact)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Успешно записано в Firestore");
                    ok.onSuccess(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Ошибка записи: " + e.getMessage());
                    err.onFailure(e);
                });
    }

    public void update(Contact contact, OnSuccessListener<Void> ok, OnFailureListener err) {
        getContactsRef().document(contact.getId()).set(contact)
                .addOnSuccessListener(ok).addOnFailureListener(err);
    }

    public void delete(String id, OnSuccessListener<Void> ok, OnFailureListener err) {
        getContactsRef().document(id).delete()
                .addOnSuccessListener(ok).addOnFailureListener(err);
    }

    public ListenerRegistration listen(ContactCallback callback) {
        return getContactsRef().addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e(TAG, " Ошибка слушателя: " + e.getMessage());
                callback.onError(e);
                return;
            }
            List<Contact> list = new ArrayList<>();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots) {
                    Contact c = doc.toObject(Contact.class);
                    if (c != null) { c.setId(doc.getId()); list.add(c); }
                }
            }
            Log.d(TAG, "📡 Snapshot получен. Контактов: " + list.size());
            callback.onSuccess(list);
        });
    }

    public interface ContactCallback {
        void onSuccess(List<Contact> data);
        void onError(Exception e);
    }
}