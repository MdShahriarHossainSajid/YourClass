package com.example.yourclass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ToolActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool);

        Button ocrButton = findViewById(R.id.ocrButton);
        ocrButton.setOnClickListener(v -> {
            startActivity(new Intent(ToolActivity.this, OCRActivity.class));
        });

        Button imageToPdfButton = findViewById(R.id.imageToPdfButton);
        imageToPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ToolActivity.this, ImageToPdfActivity.class);
                startActivity(intent);
            }
        });


    }
}
