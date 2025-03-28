package com.example.keepnote;

import java.util.List;

public class Community {
    private String communityId; // Firestore Document ID
    private String name;
    private String code;
    private String creatorId;
    private List<String> members;

    // 🔹 Required empty constructor for Firestore
    public Community() {}

    // 🔹 Full constructor
    public Community(String communityId, String name, String code, String creatorId, List<String> members) {
        this.communityId = communityId;
        this.name = name;
        this.code = code;
        this.creatorId = creatorId;
        this.members = members;
    }

    // 🔹 Getters
    public String getCommunityId() { return communityId; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getCreatorId() { return creatorId; }
    public List<String> getMembers() { return members; }

    // 🔹 Setters (Needed for Firestore updates)
    public void setCommunityId(String communityId) { this.communityId = communityId; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public void setMembers(List<String> members) { this.members = members; }
}
