package com.example.contactmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> {
    private List<Contact> data = new ArrayList<>();
    private final ActionListener listener;

    public interface ActionListener {
        void onEdit(Contact c);
        void onDelete(Contact c);
    }

    public ContactAdapter(ActionListener listener) {
        this.listener = listener;
    }



    public void setData(List<Contact> newList) {
        if (newList == null) return;

        data.clear();
        data.addAll(newList);
        notifyDataSetChanged();
    }


    public void addContact(Contact contact) {
        data.add(0, contact);
        notifyItemInserted(0);
    }

    public void removeContact(String contactId) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId().equals(contactId)) {
                data.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Contact c = data.get(position);
        holder.tvName.setText(c.getName());
        holder.tvPhone.setText(c.getPhone());
        holder.tvEmail.setText(c.getEmail());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(c));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvEmail;
        Button btnEdit, btnDelete;
        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvPhone = v.findViewById(R.id.tvPhone);
            tvEmail = v.findViewById(R.id.tvEmail);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}