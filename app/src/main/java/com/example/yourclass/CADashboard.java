package com.example.yourclass;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CADashboard extends AppCompatActivity {

    Spinner semesterSpinner, studentSpinner;
    Button assignCrBtn;
    TextView currentCrText;

    EditText noticeInput, examDateInput, noteLinkInput;
    Button postNoticeBtn, postExamBtn, postNoteBtn;

    FirebaseFirestore db;

    ArrayList<String> studentList = new ArrayList<>();
    ArrayList<String> studentIDs = new ArrayList<>();
    ArrayAdapter<String> studentAdapter;

    String selectedSemester = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadashboard);

        db = FirebaseFirestore.getInstance();

        semesterSpinner = findViewById(R.id.semesterSpinner);
        studentSpinner = findViewById(R.id.studentSpinner);
        assignCrBtn = findViewById(R.id.assignCrBtn);
        currentCrText = findViewById(R.id.currentCrText);

        String[] semesters = {"1", "2", "3", "4", "5", "6", "7", "8"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters);
        semesterSpinner.setAdapter(adapter);

        studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentList);
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentSpinner.setAdapter(studentAdapter);

        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSemester = semesters[position];
                loadStudents(selectedSemester);
                showCurrentCR(selectedSemester);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        assignCrBtn.setOnClickListener(v -> {
            int selectedIndex = studentSpinner.getSelectedItemPosition();

            if (selectedIndex < 0 || studentIDs.size() == 0) {
                Toast.makeText(this, "No student selected", Toast.LENGTH_SHORT).show();
                return;
            }

            String crUserId = studentIDs.get(selectedIndex);
            String crDisplayText = studentList.get(selectedIndex);

            db.collection("Users").document(crUserId)
                    .update("role", "cr")
                    .addOnSuccessListener(unused -> {
                        Map<String, Object> crData = new HashMap<>();
                        crData.put("crId", crDisplayText);
                        db.collection("CRs").document("Semester_" + selectedSemester)
                                .set(crData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "CR Assigned for Semester " + selectedSemester, Toast.LENGTH_SHORT).show();
                                    showCurrentCR(selectedSemester);
                                    loadStudents(selectedSemester);
                                });
                    });
        });

        noticeInput = findViewById(R.id.noticeInput);
        examDateInput = findViewById(R.id.examDateInput);
        noteLinkInput = findViewById(R.id.noteLinkInput);

        postNoticeBtn = findViewById(R.id.postNoticeBtn);
        postExamBtn = findViewById(R.id.postExamBtn);
        postNoteBtn = findViewById(R.id.postNoteBtn);

        postNoticeBtn.setOnClickListener(v -> postData("Notices"));
        postExamBtn.setOnClickListener(v -> postData("ExamDates"));
        postNoteBtn.setOnClickListener(v -> postData("Notes"));
    }

    void loadStudents(String semester) {
        studentList.clear();
        studentIDs.clear();

        db.collection("Users")
                .whereEqualTo("semester", semester)
                .whereEqualTo("role", "student")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        String studentID = doc.getString("studentID");
                        studentList.add(name + " (" + studentID + ")");
                        studentIDs.add(doc.getId());
                    }
                    studentAdapter.notifyDataSetChanged();
                });
    }

    void showCurrentCR(String semester) {
        db.collection("CRs").document("Semester_" + semester).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String crId = documentSnapshot.getString("crId");
                        currentCrText.setText("Current CR for Semester " + semester + ": " + crId);
                    } else {
                        currentCrText.setText("No CR assigned for Semester " + semester);
                    }
                });
    }

    void postData(String type) {
        String content = "";
        if (type.equals("Notices")) content = noticeInput.getText().toString().trim();
        else if (type.equals("ExamDates")) content = examDateInput.getText().toString().trim();
        else if (type.equals("Notes")) content = noteLinkInput.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Field is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Semester_" + selectedSemester)
                .document(type)
                .collection("Items")
                .add(data)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Posted to " + type + " of Semester " + selectedSemester, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to post", Toast.LENGTH_SHORT).show());
    }
}
