package com.example.yourclass;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutineActivity extends AppCompatActivity {

    private RecyclerView routineRecyclerView;
    private RoutineAdapter routineAdapter;
    private List<RoutineItem> routineList;

    private EditText routineDayInput, routineTimeInput, routineSubjectInput;
    private Button postRoutineBtn;
    private LinearLayout editRoutineLayout;

    private FirebaseFirestore db;
    private String semester;
    private boolean canEdit; // passed via intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        db = FirebaseFirestore.getInstance();

        routineRecyclerView = findViewById(R.id.routineRecyclerView);
        routineDayInput = findViewById(R.id.routineDayInput);
        routineTimeInput = findViewById(R.id.routineTimeInput);
        routineSubjectInput = findViewById(R.id.routineSubjectInput);
        postRoutineBtn = findViewById(R.id.postRoutineBtn);
        editRoutineLayout = findViewById(R.id.editRoutineLayout);

        routineList = new ArrayList<>();
        routineAdapter = new RoutineAdapter(routineList);
        routineRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        routineRecyclerView.setAdapter(routineAdapter);

        // Get extras
        semester = getIntent().getStringExtra("semester");
        canEdit = getIntent().getBooleanExtra("canEdit", false);

        if (canEdit) {
            editRoutineLayout.setVisibility(View.VISIBLE);
            postRoutineBtn.setOnClickListener(v -> postRoutine());
        } else {
            editRoutineLayout.setVisibility(View.GONE);
        }

        loadRoutines();
    }

    private void loadRoutines() {
        db.collection("Semester_" + semester)
                .document("Routines")
                .collection("Items")
                .orderBy("day")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    routineList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        RoutineItem item = doc.toObject(RoutineItem.class);
                        routineList.add(item);
                    }
                    routineAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load routines", Toast.LENGTH_SHORT).show());
    }

    private void postRoutine() {
        String day = routineDayInput.getText().toString().trim();
        String time = routineTimeInput.getText().toString().trim();
        String subject = routineSubjectInput.getText().toString().trim();

        if (day.isEmpty() || time.isEmpty() || subject.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> routineData = new HashMap<>();
        routineData.put("day", day);
        routineData.put("time", time);
        routineData.put("subject", subject);

        db.collection("Semester_" + semester)
                .document("Routines")
                .collection("Items")
                .add(routineData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Routine posted", Toast.LENGTH_SHORT).show();
                    routineDayInput.setText("");
                    routineTimeInput.setText("");
                    routineSubjectInput.setText("");
                    loadRoutines();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to post routine", Toast.LENGTH_SHORT).show());
    }
}
