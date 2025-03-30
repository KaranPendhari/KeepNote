package com.example.keepnote;

public class Note {
    private String id;
    private String title;
    private String content;
    private long timestamp;
    private String geminiSummary; // New field for caching Gemini summaries

    public Note() {} // Required for Firebase

    public Note(String id, String title, String content, long timestamp, String geminiSummary) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.geminiSummary = geminiSummary;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public String getGeminiSummary() { return geminiSummary; } // New getter

    // Setters (added for editing functionality)
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setGeminiSummary(String geminiSummary) { this.geminiSummary = geminiSummary; } // New setter
}
