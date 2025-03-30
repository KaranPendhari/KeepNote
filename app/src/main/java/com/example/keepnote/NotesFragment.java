package com.example.keepnote;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keepnote.models.GeminiRequest;
import com.example.keepnote.models.GeminiResponse;
import com.example.keepnote.network.GeminiApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotesFragment extends Fragment {

    private RecyclerView recyclerViewNotes;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;
    private FloatingActionButton fabAddNote;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference notesRef;
    private GeminiApiService geminiApiService;

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
        geminiApiService = GeminiApiService.create();

        String userId = auth.getCurrentUser().getUid();
        notesRef = db.collection("Notes").document(userId).collection("UserNotes");

        FirebaseFirestore.getInstance().addSnapshotsInSyncListener(() -> {
            if (auth.getCurrentUser() != null) {
                loadNotesFromFirestore();
            }
        });

        // Load Notes from Firestore
        loadNotesFromFirestore();

        // Show popup menu on Floating Action Button click
        fabAddNote.setOnClickListener(v -> showPopupMenu(v));

        return view;
    }

    private void loadNotesFromFirestore() {
        notesRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get(Source.CACHE) // Load from cache first
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    noteList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Note note = doc.toObject(Note.class);
                        noteList.add(note);
                    }
                    noteAdapter = new NoteAdapter(getContext(), noteList);
                    recyclerViewNotes.setAdapter(noteAdapter);

                    // Fetch missing summaries if not available
                    fetchMissingSummaries();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load notes from cache.", Toast.LENGTH_SHORT).show();
                    loadNotesFromServer(); // Fetch from server if cache fails
                });
    }

    private void loadNotesFromServer() {
        notesRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    noteList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Note note = doc.toObject(Note.class);
                        noteList.add(note);
                    }
                    noteAdapter = new NoteAdapter(getContext(), noteList);
                    recyclerViewNotes.setAdapter(noteAdapter);

                    // Fetch missing summaries if not available
                    fetchMissingSummaries();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load notes from server.", Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchMissingSummaries() {
        for (Note note : noteList) {
            if (note.getGeminiSummary() == null || note.getGeminiSummary().isEmpty()) {
                generateGeminiSummary(note);
            }
        }
    }

    private void generateGeminiSummary(Note note) {
        if (note.getContent() == null || note.getContent().trim().isEmpty()) return;

        GeminiRequest request = new GeminiRequest(
                "Summarize this note in one simple sentence: " + note.getContent()
        );

        geminiApiService.getSummary(GeminiApiService.API_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GeminiResponse> call,
                                           @NonNull Response<GeminiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String summary = response.body().getSummary();
                            note.setGeminiSummary(summary);

                            // Update Firestore
                            notesRef.document(note.getId())
                                    .update("geminiSummary", summary)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(getContext(), "Summary updated", Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed to update summary", Toast.LENGTH_SHORT).show()
                                    );

                            noteAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GeminiResponse> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to connect to AI", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v, Gravity.END);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_add_note, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_canvas) {
                startActivity(new Intent(getContext(), CanvasActivity.class));
                return true;
            } else if (id == R.id.menu_new_note) {
                showAddNoteDialog();
                return true;
            } else if (id == R.id.menu_voice_note) {
                startActivity(new Intent(getContext(), VoiceNoteActivity.class));
                return true;
            }
            return false;
        });

        popup.show();
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
        Note note = new Note(noteId, title, content, System.currentTimeMillis(), "");

        notesRef.document(noteId).set(note)
                .addOnSuccessListener(aVoid -> {
                    noteList.add(0, note);
                    noteAdapter.notifyItemInserted(0);
                    recyclerViewNotes.scrollToPosition(0);
                    generateGeminiSummary(note);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Offline mode: Note will sync when online", Toast.LENGTH_SHORT).show()
                );
    }
}
