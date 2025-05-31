package com.example.yourclass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity {

    private ListView courseListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> courseList = new ArrayList<>();

    private EditText courseCodeInput, courseNameInput;
    private Button addCourseButton;

    private FirebaseFirestore db;
    private String crSemester;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        courseListView = findViewById(R.id.courseListView);
        courseCodeInput = findViewById(R.id.courseCodeInput);
        courseNameInput = findViewById(R.id.courseNameInput);
        addCourseButton = findViewById(R.id.addCourseButton);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
        courseListView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        getCRSemester();

        addCourseButton.setOnClickListener(v -> {
            String courseCode = courseCodeInput.getText().toString().trim();
            String courseName = courseNameInput.getText().toString().trim();

            if (courseCode.isEmpty() || courseName.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (crSemester == null) {
                Toast.makeText(this, "Semester not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            DocumentReference docRef = db.collection("Courses").document("Semester_" + crSemester);
            Map<String, Object> courseData = new HashMap<>();
            courseData.put(courseCode, courseName);

            docRef.set(courseData, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
                        courseCodeInput.setText("");
                        courseNameInput.setText("");
                        loadCourses();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void getCRSemester() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                crSemester = doc.getString("semester");
                loadCourses();
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourses() {
        if (crSemester == null) return;

        db.collection("Courses").document("Semester_" + crSemester)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    courseList.clear();
                    if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                        for (String key : documentSnapshot.getData().keySet()) {
                            String courseName = documentSnapshot.getString(key);
                            courseList.add(courseName + " (" + key + ")");
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
                    }
                });

        courseListView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = courseList.get(position);
            String courseCode = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")"));

            Intent intent = new Intent(AttendanceActivity.this, MarkAttendanceActivity.class);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("semester", crSemester);
            startActivity(intent);
        });
    }
}
