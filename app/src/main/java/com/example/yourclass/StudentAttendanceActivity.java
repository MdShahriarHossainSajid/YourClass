package com.example.yourclass;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class StudentAttendanceActivity extends AppCompatActivity {

    private ListView attendanceListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> attendanceInfoList = new ArrayList<>();

    private FirebaseFirestore db;
    private String studentID, semester;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        attendanceListView = findViewById(R.id.attendanceListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceInfoList);
        attendanceListView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchStudentInfo();
    }

    private void fetchStudentInfo() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                studentID = doc.getString("studentID");
                semester = doc.getString("semester");
                loadCourses();
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourses() {
        db.collection("Courses").document("Semester_" + semester).get().addOnSuccessListener(courseDoc -> {
            if (courseDoc.exists()) {
                Map<String, Object> data = courseDoc.getData();
                if (data != null) {
                    for (String courseCode : data.keySet()) {
                        loadAttendanceSummary(courseCode, (String) data.get(courseCode));
                    }
                }
            } else {
                Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAttendanceSummary(String courseCode, String courseName) {
        db.collection("AttendanceSummary")
                .document("Semester_" + semester)
                .collection("Course_" + courseCode)
                .document("summary")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains(studentID)) {
                        Map<String, Object> studentData = (Map<String, Object>) doc.get(studentID);
                        long present = (long) studentData.getOrDefault("present", 0L);
                        long total = (long) studentData.getOrDefault("total", 0L);
                        long percent = total > 0 ? (present * 100) / total : 0;

                        attendanceInfoList.add(courseName + " (" + courseCode + ")\n" +
                                "Present: " + present + ", Total: " + total + ", Percentage: " + percent + "%");
                    } else {
                        attendanceInfoList.add(courseName + " (" + courseCode + ")\nNo attendance data.");
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
