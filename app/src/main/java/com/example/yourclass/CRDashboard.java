package com.example.yourclass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CRDashboard extends AppCompatActivity {

    EditText noticeInput, examDateInput, noteLinkInput;
    Button postNoticeBtn, postExamBtn, postNoteBtn;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String userId, semester, senderName;  // ✅ Added senderName

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crdashboard);

        noticeInput = findViewById(R.id.noticeInput);
        examDateInput = findViewById(R.id.examDateInput);
        noteLinkInput = findViewById(R.id.noteLinkInput);
        postNoticeBtn = findViewById(R.id.postNoticeBtn);
        postExamBtn = findViewById(R.id.postExamBtn);
        postNoteBtn = findViewById(R.id.postNoteBtn);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        Button manageRoutineBtn = findViewById(R.id.manageRoutineBtn);
        manageRoutineBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, RoutineActivity.class);
            intent.putExtra("semester", semester);
            intent.putExtra("canEdit", true); // Allow editing
            startActivity(intent);
        });
        Button attendanceBtn = findViewById(R.id.attendanceBtn);
        attendanceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CRDashboard.this, AttendanceActivity.class);
            startActivity(intent);
        });



        // ✅ Fetch semester and name from Firestore
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    semester = documentSnapshot.getString("semester");
                    senderName = documentSnapshot.getString("name"); // ✅

                    postNoticeBtn.setOnClickListener(v -> addData("Notices", noticeInput.getText().toString()));
                    postExamBtn.setOnClickListener(v -> addData("ExamDates", examDateInput.getText().toString()));
                    postNoteBtn.setOnClickListener(v -> addData("Notes", noteLinkInput.getText().toString()));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load CR info", Toast.LENGTH_SHORT).show()
                );
    }

    private void addData(String type, String content) {
        if (semester == null || content.trim().isEmpty()) {
            Toast.makeText(this, "Please enter data properly", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Get current timestamp
        String timestamp = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("createdBy", userId);
        data.put("senderName", senderName);     // ✅ Save name
        data.put("timestamp", timestamp);       // ✅ Save time

        db.collection("Semester_" + semester)
                .document(type)
                .collection("Items")
                .add(data)
                .addOnSuccessListener(doc ->
                        Toast.makeText(this, type + " posted successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to post " + type, Toast.LENGTH_SHORT).show()
                );
    }
}
