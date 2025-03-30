package com.example.keepnote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keepnote.models.GeminiRequest;
import com.example.keepnote.models.GeminiResponse;
import com.example.keepnote.network.GeminiApiService;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private Context context;
    private GeminiApiService geminiApiService;
    private FirebaseFirestore db;

    public NoteAdapter(Context context, List<Note> noteList) {
        this.context = context;
        this.noteList = noteList;
        this.geminiApiService = GeminiApiService.create();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());

        // ✅ If summary already exists, display it and SKIP API call
        if (note.getGeminiSummary() != null && !note.getGeminiSummary().isEmpty()) {
            holder.tvContent.setText(note.getGeminiSummary());
        } else {
            // ✅ Only request summary if it is NOT already available
            holder.tvContent.setText("🔄 Generating summary...");
            generateSummary(note, holder);
        }

        // Click listener to open EditNoteActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditNoteActivity.class);
            intent.putExtra("noteId", note.getId());
            intent.putExtra("title", note.getTitle());
            intent.putExtra("content", note.getContent());
            intent.putExtra("summary", note.getGeminiSummary()); // Pass summary
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;

        NoteViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
        }
    }

    /**
     * Fetch AI-generated summary for a given note, only if needed.
     */
    private void generateSummary(Note note, NoteViewHolder holder) {
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            holder.tvContent.setText("❌ No content to summarize");
            return;
        }

        GeminiRequest request = new GeminiRequest(
                "Summarize this note in one simple sentence: " + note.getContent()
        );

        Log.d("NoteAdapter", "📤 Sending request for note: " + note.getTitle());

        geminiApiService.getSummary(GeminiApiService.API_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GeminiResponse> call,
                                           @NonNull Response<GeminiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String summary = response.body().getSummary();
                            Log.d("NoteAdapter", "✅ Summary received: " + summary);
                            holder.tvContent.setText(summary);

                            // Update the note object and Firestore
                            note.setGeminiSummary(summary);
                            db.collection("Notes")
                                    .document(note.getId())
                                    .update("geminiSummary", summary)
                                    .addOnSuccessListener(aVoid -> Log.d("NoteAdapter", "📌 Summary saved in Firestore"))
                                    .addOnFailureListener(e -> Log.e("NoteAdapter", "🔥 Failed to save summary", e));
                        } else {
                            handleApiError(response, holder);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GeminiResponse> call,
                                          @NonNull Throwable t) {
                        Log.e("NoteAdapter", "🚨 Network failure: " + t.getMessage());
                        holder.tvContent.setText("⚠️ Failed to connect to AI");
                    }
                });
    }

    private void handleApiError(Response<GeminiResponse> response, NoteViewHolder holder) {
        try {
            String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e("NoteAdapter", "❌ API Error: " + error);
            holder.tvContent.setText("⚠️ Error generating summary");
        } catch (IOException e) {
            Log.e("NoteAdapter", "🚨 Error parsing API response", e);
            holder.tvContent.setText("⚠️ Summary unavailable");
        }
    }
}
