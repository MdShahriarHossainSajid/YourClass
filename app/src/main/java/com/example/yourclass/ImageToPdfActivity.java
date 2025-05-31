package com.example.yourclass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ImageToPdfActivity extends AppCompatActivity {

    private static final int CREATE_FILE_REQUEST_CODE = 101;

    private ArrayList<Bitmap> selectedBitmaps = new ArrayList<>();
    private ImageAdapter adapter;

    private Button selectImagesBtn, convertToPdfBtn;
    private RecyclerView recyclerView;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<Uri> uris = new ArrayList<>();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            uris.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        uris.add(result.getData().getData());
                    }

                    for (Uri uri : uris) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            selectedBitmaps.add(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_pdf);

        selectImagesBtn = findViewById(R.id.selectImagesBtn);
        convertToPdfBtn = findViewById(R.id.convertToPdfBtn);
        recyclerView = findViewById(R.id.imageRecyclerView);

        adapter = new ImageAdapter(selectedBitmaps);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        selectImagesBtn.setOnClickListener(v -> selectImages());
        convertToPdfBtn.setOnClickListener(v -> {
            if (selectedBitmaps.isEmpty()) {
                Toast.makeText(this, "Please select at least one image!", Toast.LENGTH_SHORT).show();
            } else {
                promptUserToSavePdf();
            }
        });
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    private void promptUserToSavePdf() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "my_document.pdf");
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri pdfUri = data.getData();
                generatePdfToUri(pdfUri);
            }
        }
    }

    private void generatePdfToUri(Uri uri) {
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
            if (pfd == null) return;

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
            PdfDocument document = new PdfDocument();

            for (Bitmap bitmap : selectedBitmaps) {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                canvas.drawBitmap(bitmap, 0f, 0f, null);
                document.finishPage(page);
            }

            document.writeTo(out);
            document.close();
            out.close();
            pfd.close();

            Toast.makeText(this, "PDF saved successfully!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
