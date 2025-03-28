package com.example.keepnote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommunityNotesAdapter extends RecyclerView.Adapter<CommunityNotesAdapter.NoteViewHolder> {
    private Context context;
    private List<CommunityNote> noteList; // Changed Note → CommunityNote
    private String currentUserId;
    private OnNoteDeleteListener onNoteDeleteListener;

    public CommunityNotesAdapter(Context context, List<CommunityNote> noteList, String currentUserId, OnNoteDeleteListener onNoteDeleteListener) {
        this.context = context;
        this.noteList = noteList;
        this.currentUserId = currentUserId;
        this.onNoteDeleteListener = onNoteDeleteListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.community_item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        CommunityNote note = noteList.get(position); // Changed Note → CommunityNote
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());
        holder.tvAuthor.setText("By: " + note.getAuthorName()); // Display author's name

        // Show delete button only if the note belongs to the current user
        if (note.getAuthorId().equals(currentUserId)) {
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.ivDelete.setOnClickListener(v -> onNoteDeleteListener.onDelete(note.getNoteId()));
        } else {
            holder.ivDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvAuthor;
        ImageView ivDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvAuthor = itemView.findViewById(R.id.tvAuthor); // Add this to display author name
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }

    public interface OnNoteDeleteListener {
        void onDelete(String noteId);
    }
}
