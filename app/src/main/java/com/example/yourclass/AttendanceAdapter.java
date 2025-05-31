package com.example.yourclass;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private final List<AttendanceModel> list;

    public AttendanceAdapter(List<AttendanceModel> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseCode, attendanceInfo, percentage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCode = itemView.findViewById(R.id.courseCode);
            attendanceInfo = itemView.findViewById(R.id.attendanceInfo);
            percentage = itemView.findViewById(R.id.percentage);
        }
    }

    @NonNull
    @Override
    public AttendanceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceAdapter.ViewHolder holder, int position) {
        AttendanceModel model = list.get(position);
        holder.courseCode.setText("Course: " + model.getCourseCode());
        holder.attendanceInfo.setText("Present: " + model.getPresent() + " / " + model.getTotal());
        holder.percentage.setText(String.format("Percentage: %.2f%%", model.getPercentage()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
