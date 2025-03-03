package com.example.keepnote;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditNoteActivity extends AppCompatActivity {

    private EditText etEditTitle, etEditContent;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private String noteId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        etEditTitle = findViewById(R.id.etEditTitle);
        etEditContent = findViewById(R.id.etEditContent);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get Data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            noteId = intent.getStringExtra("noteId");
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");

            etEditTitle.setText(title);
            etEditContent.setText(content);
        }

        // Save Button: Update Note
        btnSave.setOnClickListener(v -> updateNote());

        // Cancel Button
        btnCancel.setOnClickListener(v -> finish());
    }

    private void updateNote() {
        String updatedTitle = etEditTitle.getText().toString().trim();
        String updatedContent = etEditContent.getText().toString().trim();

        if (TextUtils.isEmpty(updatedTitle) || TextUtils.isEmpty(updatedContent)) {
            Toast.makeText(this, "Title and Content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedNote = new HashMap<>();
        updatedNote.put("title", updatedTitle);
        updatedNote.put("content", updatedContent);
        updatedNote.put("timestamp", System.currentTimeMillis());

        db.collection("Notes").document(userId)
                .collection("UserNotes").document(noteId)
                .update(updatedNote)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show()
                );
    }
}
