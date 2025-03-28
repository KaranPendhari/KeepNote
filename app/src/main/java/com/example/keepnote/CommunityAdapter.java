package com.example.keepnote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {
    private Context context;
    private List<Community> communityList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Community community);
    }

    public CommunityAdapter(Context context, List<Community> communityList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.communityList = communityList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_community, parent, false);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        Community community = communityList.get(position);
        holder.tvCommunityName.setText(community.getName());
        holder.tvCommunityCode.setText("Code: " + community.getCode());

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(community);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (communityList != null) ? communityList.size() : 0;
    }

    public static class CommunityViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommunityName, tvCommunityCode;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommunityName = itemView.findViewById(R.id.tvCommunityName);
            tvCommunityCode = itemView.findViewById(R.id.tvCommunityCode);
        }
    }
}
