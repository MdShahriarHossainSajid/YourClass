package com.example.yourclass;
import com.google.android.material.button.MaterialButton;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class StudentDashboard extends AppCompatActivity {

    TextView noticeTextView, noteTextView, examDateTextView;

    FirebaseFirestore db;
    FirebaseUser currentUser;
    private String semester;  // Declare semester as a field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize views
        noticeTextView = findViewById(R.id.noticeTextView);
        noteTextView = findViewById(R.id.noteTextView);
        examDateTextView = findViewById(R.id.examDateTextView);
        Button viewRoutineBtn = findViewById(R.id.viewRoutineBtn);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button toolButton = findViewById(R.id.toolButton);
        toolButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboard.this, ToolActivity.class);
            startActivity(intent);
        });

        MaterialButton btnCheckAttendance = findViewById(R.id.btnCheckAttendance);
        btnCheckAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboard.this, StudentAttendanceActivity.class);
            startActivity(intent);
        });



        // Disable button initially until semester is loaded
        viewRoutineBtn.setEnabled(false);

        // Get user's semester
        db.collection("Users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && snapshot.contains("semester")) {
                        semester = snapshot.getString("semester");
                        if (semester != null && !semester.isEmpty()) {
                            // Load notices, notes, exam dates for this semester
                            loadContent("Semester_" + semester + "/Notices/Items", noticeTextView, "No notices available.");
                            loadContent("Semester_" + semester + "/Notes/Items", noteTextView, "No notes available.");
                            loadContent("Semester_" + semester + "/ExamDates/Items", examDateTextView, "No exam dates available.");

                            // Enable the button after semester is loaded
                            viewRoutineBtn.setEnabled(true);
                        } else {
                            Toast.makeText(this, "Semester not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User info not available.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        viewRoutineBtn.setOnClickListener(v -> {
            if (semester != null && !semester.isEmpty()) {
                Intent intent = new Intent(StudentDashboard.this, RoutineActivity.class);
                intent.putExtra("semester", semester);
                intent.putExtra("canEdit", false); // Students cannot edit
                startActivity(intent);
            } else {
                Toast.makeText(this, "Semester data is not loaded yet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadContent(String path, TextView textView, String emptyMessage) {
        CollectionReference ref = db.collection(path);
        ref.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        StringBuilder builder = new StringBuilder();
                        for (DocumentSnapshot doc : snapshot) {
                            String content = doc.getString("content");
                            if (content != null) {
                                builder.append("• ").append(content).append("\n\n");
                            }
                        }
                        textView.setText(builder.toString().trim());
                    } else {
                        textView.setText(emptyMessage);
                    }
                })
                .addOnFailureListener(e -> {
                    textView.setText(emptyMessage);
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
