package com.example.keepnote;

public class CommunityNote {
    private String noteId;
    private String title;
    private String content;
    private String authorId;  // ID of the user who created the note
    private String authorName; // Name of the user who created the note

    // Empty constructor for Firebase
    public CommunityNote() { }

    // Constructor
    public CommunityNote(String noteId, String title, String content, String authorId, String authorName) {
        this.noteId = noteId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
    }

    // Getters
    public String getNoteId() {
        return noteId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    // Setters (if needed)
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
