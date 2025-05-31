package com.example.yourclass;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MarkAttendanceActivity extends AppCompatActivity {

    private ListView studentListView;
    private Button submitButton;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> studentIDs = new ArrayList<>();
    private HashMap<String, String> attendanceMap = new HashMap<>();

    private FirebaseFirestore db;
    private String courseCode, semester;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        studentListView = findViewById(R.id.studentListView);
        submitButton = findViewById(R.id.submitAttendanceButton);

        db = FirebaseFirestore.getInstance();

        courseCode = getIntent().getStringExtra("courseCode");
        semester = getIntent().getStringExtra("semester");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, studentIDs);
        studentListView.setAdapter(adapter);
        studentListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        loadStudents();

        submitButton.setOnClickListener(v -> saveAttendance());
    }

    private void loadStudents() {
        db.collection("Users")
                .whereEqualTo("role", "student")
                .whereEqualTo("semester", semester)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studentIDs.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String id = doc.getString("studentID");
                        studentIDs.add(id);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show());
    }

    private void saveAttendance() {
        attendanceMap.clear();
        SparseBooleanArray checked = studentListView.getCheckedItemPositions();

        for (int i = 0; i < studentIDs.size(); i++) {
            String id = studentIDs.get(i);
            boolean isPresent = checked.get(i);
            attendanceMap.put(id, isPresent ? "Present" : "Absent");
        }

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 1. Save daily attendance
        DocumentReference dateRef = db.collection("Attendance")
                .document("Semester_" + semester)
                .collection("Course_" + courseCode)
                .document(date);

        dateRef.set(attendanceMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Attendance Saved!", Toast.LENGTH_SHORT).show();
                    updateAttendanceSummary();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateAttendanceSummary() {
        final DocumentReference summaryRef = db.collection("AttendanceSummary")
                .document("Semester_" + semester)
                .collection("Course_" + courseCode)
                .document("summary");

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(summaryRef);
            Map<String, Object> updatedMap = new HashMap<>();

            for (Map.Entry<String, String> entry : attendanceMap.entrySet()) {
                String studentId = entry.getKey();
                boolean isPresent = "Present".equalsIgnoreCase(entry.getValue());

                Map<String, Long> existing = snapshot.contains(studentId)
                        ? (Map<String, Long>) snapshot.get(studentId)
                        : new HashMap<>();

                long present = existing.getOrDefault("present", 0L);
                long total = existing.getOrDefault("total", 0L);

                if (isPresent) present++;
                total++;

                Map<String, Object> updatedCounts = new HashMap<>();
                updatedCounts.put("present", present);
                updatedCounts.put("total", total);

                updatedMap.put(studentId, updatedCounts);
            }

            transaction.set(summaryRef, updatedMap, SetOptions.merge());
            return null;
        }).addOnSuccessListener(unused ->
                Toast.makeText(this, "Summary Updated", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Summary Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
