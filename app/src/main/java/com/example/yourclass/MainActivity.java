package com.example.yourclass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button loginBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginBtn.setOnClickListener(v -> loginUser());
        findViewById(R.id.goToRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    void loginUser() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = mAuth.getCurrentUser().getUid();
                    db.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
                        String role = doc.getString("role");
                        switch (role) {
                            case "student":
                                startActivity(new Intent(this, StudentDashboard.class)); break;
                            case "cr":
                                startActivity(new Intent(this, CRDashboard.class)); break;
                            case "ca":
                                startActivity(new Intent(this, CADashboard.class)); break;
                        }
                        finish();
                    });
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                );
    }
}