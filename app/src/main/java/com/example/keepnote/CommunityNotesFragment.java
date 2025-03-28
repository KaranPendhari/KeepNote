package com.example.keepnote;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommunityNotesFragment extends Fragment {
    private static final String ARG_COMMUNITY_ID = "communityId";

    private RecyclerView recyclerViewNotes;
    private CommunityNotesAdapter notesAdapter;
    private List<CommunityNote> noteList = new ArrayList<>();
    private Button btnAddNote;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference notesRef;
    private String communityId;
    private String currentUserId;
    private String communityCreatorId;

    public CommunityNotesFragment() {
        // Required empty public constructor
    }

    public static CommunityNotesFragment newInstance(String communityId) {
        CommunityNotesFragment fragment = new CommunityNotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMMUNITY_ID, communityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        if (getArguments() != null) {
            communityId = getArguments().getString(ARG_COMMUNITY_ID);
        }

        notesRef = db.collection("Communities").document(communityId).collection("Notes");

        // ✅ Fetch the community creator ID
        fetchCommunityCreator();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community_notes, container, false);

        recyclerViewNotes = view.findViewById(R.id.recyclerViewNotes);
        btnAddNote = view.findViewById(R.id.btnAddNote);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(getContext()));

        notesAdapter = new CommunityNotesAdapter(getContext(), noteList, currentUserId, this::deleteNote);
        recyclerViewNotes.setAdapter(notesAdapter);

        loadNotes();

        btnAddNote.setOnClickListener(v -> {
            if (currentUserId.equals(communityCreatorId)) {
                showAddNoteDialog();
            }
            else if (!currentUserId.equals(communityCreatorId)) {
                    Toast.makeText(getContext(), "Only the creator can add notes", Toast.LENGTH_SHORT).show();
                    return; // Exit the function if not the creator
            }

            else {
                Toast.makeText(getContext(), "Only the community creator can add notes.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchCommunityCreator() {
        db.collection("Communities").document(communityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        communityCreatorId = documentSnapshot.getString("creatorId");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch community creator.", Toast.LENGTH_SHORT).show());
    }

    private void loadNotes() {
        notesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    noteList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CommunityNote note = doc.toObject(CommunityNote.class);
                        noteList.add(note);
                    }
                    notesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load notes.", Toast.LENGTH_SHORT).show());
    }

    private void showAddNoteDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_note, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (!title.isEmpty() && !content.isEmpty()) {
                fetchUserNameAndAddNote(title, content);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Enter title and content", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchUserNameAndAddNote(String title, String content) {
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String authorName = "Unknown User";
                    if (documentSnapshot.exists()) {
                        authorName = documentSnapshot.getString("name");
                    }
                    addNote(title, content, authorName);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch user name.", Toast.LENGTH_SHORT).show());
    }

    private void addNote(String title, String content, String authorName) {
        String noteId = UUID.randomUUID().toString();

        CommunityNote note = new CommunityNote(noteId, title, content, currentUserId, authorName);

        notesRef.document(noteId).set(note)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Note added!", Toast.LENGTH_SHORT).show();
                    loadNotes();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add note.", Toast.LENGTH_SHORT).show());
    }

    private void deleteNote(String noteId) {
        notesRef.document(noteId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Note deleted!", Toast.LENGTH_SHORT).show();
                    loadNotes();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete note.", Toast.LENGTH_SHORT).show());
    }
}
