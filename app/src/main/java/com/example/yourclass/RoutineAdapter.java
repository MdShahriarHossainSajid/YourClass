package com.example.yourclass;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    private List<RoutineItem> routineList;

    public RoutineAdapter(List<RoutineItem> routineList) {
        this.routineList = routineList;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        RoutineItem item = routineList.get(position);
        holder.subjectTextView.setText(item.getSubject());
        holder.dayTextView.setText(item.getDay());
        holder.timeTextView.setText(item.getTime());
        holder.roomTextView.setText(item.getRoom());
    }

    @Override
    public int getItemCount() {
        return routineList.size();
    }

    public static class RoutineViewHolder extends RecyclerView.ViewHolder {

        TextView subjectTextView, dayTextView, timeTextView, roomTextView;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            roomTextView = itemView.findViewById(R.id.roomTextView);
        }
    }
}
