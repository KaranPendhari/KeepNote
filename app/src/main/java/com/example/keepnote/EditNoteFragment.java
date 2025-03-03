package com.example.keepnote;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditNoteFragment extends Fragment {

    private EditText etNoteTitle, etNoteContent;
    private Button btnUpdateNote;
    private String noteId, currentUserId;
    private FirebaseFirestore db;

    public EditNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Find views
        etNoteTitle = view.findViewById(R.id.etNoteTitle);
        etNoteContent = view.findViewById(R.id.etNoteContent);
        btnUpdateNote = view.findViewById(R.id.btnUpdateNote);

        // Get note details passed from NotesFragment
        if (getArguments() != null) {
            noteId = getArguments().getString("noteId");
            String noteTitle = getArguments().getString("noteTitle");
            String noteContent = getArguments().getString("noteContent");

            etNoteTitle.setText(noteTitle);
            etNoteContent.setText(noteContent);
        }

        // Update note on button click
        btnUpdateNote.setOnClickListener(v -> updateNote());
    }

    private void updateNote() {
        String updatedTitle = etNoteTitle.getText().toString().trim();
        String updatedContent = etNoteContent.getText().toString().trim();

        if (TextUtils.isEmpty(updatedTitle) || TextUtils.isEmpty(updatedContent)) {
            Toast.makeText(getContext(), "Both fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .collection("notes")
                .document(noteId)
                .update("title", updatedTitle, "content", updatedContent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Note updated", Toast.LENGTH_SHORT).show();
                    // Return to NotesFragment
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
