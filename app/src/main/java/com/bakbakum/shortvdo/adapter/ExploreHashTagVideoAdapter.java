package com.bakbakum.shortvdo.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ItemHashtagVideoBinding;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.view.video.PlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class ExploreHashTagVideoAdapter extends RecyclerView.Adapter<ExploreHashTagVideoAdapter.ExploreHashTagVideoViewHolder> {
    public ArrayList<Video.Data> mList = new ArrayList<>();
    public String hashTag = "";
    public boolean isChild = false;


    @NonNull
    @Override
    public ExploreHashTagVideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hashtag_video, parent, false);
        return new ExploreHashTagVideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreHashTagVideoViewHolder holder, int position) {
        holder.setModel(position);

    }

    @Override
    public int getItemCount() {
        if (isChild) {
            return Math.min(mList.size(), 10);
        }
        return mList.size();
    }

    public void updateData(List<Video.Data> list) {
        mList = (ArrayList<Video.Data>) list;
        notifyDataSetChanged();

    }

    public void loadMore(List<Video.Data> data) {
        for (int i = 0; i < data.size(); i++) {
            mList.add(data.get(i));
            notifyItemInserted(mList.size() - 1);
        }

    }


    class ExploreHashTagVideoViewHolder extends RecyclerView.ViewHolder {
        ItemHashtagVideoBinding binding;

        ExploreHashTagVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            if (binding != null) {
                binding.executePendingBindings();
            }
        }

        public void setModel(int position) {
            binding.setModel(mList.get(position));
            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(binding.getRoot().getContext(), PlayerActivity.class);
                intent.putExtra("video_list", new Gson().toJson(mList));
                intent.putExtra("position", position);
                intent.putExtra("type", 2);
                intent.putExtra("hash_tag", hashTag);
                binding.getRoot().getContext().startActivity(intent);
            });
        }
    }
}
