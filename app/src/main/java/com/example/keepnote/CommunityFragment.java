package com.example.keepnote;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommunityFragment extends Fragment {

    RecyclerView recyclerViewCommunities;
    CommunityAdapter communityAdapter;
    List<Community> communityList = new ArrayList<>();
    FloatingActionButton fabCommunityOptions;
    FirebaseAuth auth;
    FirebaseFirestore db;
    CollectionReference communitiesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        recyclerViewCommunities = view.findViewById(R.id.recyclerViewCommunities);
        recyclerViewCommunities.setLayoutManager(new LinearLayoutManager(getContext()));

        fabCommunityOptions = view.findViewById(R.id.fabCommunityOptions);
        fabCommunityOptions.setOnClickListener(this::showCommunityOptionsMenu);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        communitiesRef = db.collection("Communities");

        communityAdapter = new CommunityAdapter(getContext(), communityList, this::openCommunityNotes);
        recyclerViewCommunities.setAdapter(communityAdapter);

        loadCommunities();
        return view;
    }

    // 🔹 Show Popup Menu on Floating Button Click
    private void showCommunityOptionsMenu(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_community_options, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_create_community) {
                showCreateCommunityDialog();
                return true;
            } else if (item.getItemId() == R.id.menu_join_community) {
                showJoinCommunityDialog();
                return true;
            }
            return false;
        });

        popup.show();
    }

    // 🔹 Load Communities the User is Part Of
    private void loadCommunities() {
        String userId = auth.getCurrentUser().getUid();
        communitiesRef.whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    communityList.clear(); // Prevent duplicates
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Community community = doc.toObject(Community.class);
                        community.setCommunityId(doc.getId()); // Ensure the ID is set
                        communityList.add(community);
                        Log.d("FirestoreData", "Loaded Community: " + community.getName());
                    }
                    communityAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to load communities", e);
                    Toast.makeText(getContext(), "Failed to load communities.", Toast.LENGTH_SHORT).show();
                });
    }

    // 🔹 Show Dialog to Create a Community
    private void showCreateCommunityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_community, null);
        builder.setView(dialogView);

        EditText etCommunityName = dialogView.findViewById(R.id.etCommunityName);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnCreate.setOnClickListener(v -> {
            String name = etCommunityName.getText().toString().trim();
            if (!name.isEmpty()) {
                createCommunity(name);
                dialog.dismiss();
            } else {
                etCommunityName.setError("Name required");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // 🔹 Create Community and Save to Firestore
    private void createCommunity(String name) {
        String communityId = UUID.randomUUID().toString();
        String code = UUID.randomUUID().toString().substring(0, 6);
        String creatorId = auth.getCurrentUser().getUid();

        Map<String, Object> communityData = new HashMap<>();
        communityData.put("name", name);
        communityData.put("code", code);
        communityData.put("creatorId", creatorId);
        communityData.put("members", new ArrayList<String>() {{ add(creatorId); }});

        communitiesRef.document(communityId).set(communityData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Community created! Code: " + code, Toast.LENGTH_LONG).show();
                    loadCommunities();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create community.", Toast.LENGTH_SHORT).show());
    }

    // 🔹 Show Dialog to Join a Community
    private void showJoinCommunityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_join_community, null);
        builder.setView(dialogView);

        EditText etCommunityCode = dialogView.findViewById(R.id.etCommunityCode);
        Button btnJoin = dialogView.findViewById(R.id.btnJoin);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnJoin.setOnClickListener(v -> {
            String code = etCommunityCode.getText().toString().trim();
            if (!code.isEmpty()) {
                joinCommunity(code);
                dialog.dismiss();
            } else {
                etCommunityCode.setError("Enter a valid code");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // 🔹 Join an Existing Community
    private void joinCommunity(String code) {
        String userId = auth.getCurrentUser().getUid();

        communitiesRef.whereEqualTo("code", code)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String communityId = doc.getId();
                            List<String> members = (List<String>) doc.get("members");

                            if (members != null && !members.contains(userId)) {
                                members.add(userId);
                                communitiesRef.document(communityId)
                                        .update("members", members)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Joined community!", Toast.LENGTH_SHORT).show();
                                            loadCommunities();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to join.", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(getContext(), "You are already a member.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Invalid community code.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error finding community.", Toast.LENGTH_SHORT).show());
    }

    // 🔹 Open Selected Community Notes
    private void openCommunityNotes(Community community) {
        CommunityNotesFragment fragment = CommunityNotesFragment.newInstance(community.getCommunityId());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCommunities(); // Reload data when fragment resumes
    }
}
