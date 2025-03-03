package com.example.keepnote;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment {

    RecyclerView recyclerViewNotes;
    NoteAdapter noteAdapter;
    List<Note> noteList;
    FloatingActionButton fabAddNote;
    FirebaseAuth auth;
    FirebaseFirestore db;
    CollectionReference notesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerViewNotes = view.findViewById(R.id.recyclerViewNotes);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAddNote = view.findViewById(R.id.fabAddNote);

        // Firebase Setup
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        String userId = auth.getCurrentUser().getUid();
        notesRef = db.collection("Notes").document(userId).collection("UserNotes");

        // Load Notes from Firestore
        loadNotesFromFirestore();

        // Add Note Dialog
        fabAddNote.setOnClickListener(v -> showAddNoteDialog());

        return view;
    }

    private void loadNotesFromFirestore() {
        notesRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    noteList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Note note = doc.toObject(Note.class);
                        noteList.add(note);
                    }
                    noteAdapter = new NoteAdapter(getContext(), noteList);
                    recyclerViewNotes.setAdapter(noteAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load notes.", Toast.LENGTH_SHORT).show());
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_note, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (!title.isEmpty() && !content.isEmpty()) {
                saveNoteToFirestore(title, content);
                dialog.dismiss();
            } else {
                etTitle.setError("Title required");
                etContent.setError("Content required");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveNoteToFirestore(String title, String content) {
        String noteId = notesRef.document().getId();
        Note note = new Note(noteId, title, content, System.currentTimeMillis());

        notesRef.document(noteId).set(note)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Note added", Toast.LENGTH_SHORT).show();
                    noteList.add(0, note);
                    noteAdapter.notifyItemInserted(0);
                    recyclerViewNotes.scrollToPosition(0);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to add note", Toast.LENGTH_SHORT).show());
    }
}
