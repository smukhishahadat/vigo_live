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
import com.bakbakum.shortvdo.databinding.ItemSearchVideosBinding;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.view.video.PlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.HashTagVideoViewHolder> {
    public ArrayList<Video.Data> mList = new ArrayList<>();
    public boolean isHashTag = false;
    public String word = "";


    @NonNull
    @Override
    public HashTagVideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_videos, parent, false);
        return new HashTagVideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HashTagVideoViewHolder holder, int position) {
        holder.setModel(position);

    }

    @Override
    public int getItemCount() {
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


    class HashTagVideoViewHolder extends RecyclerView.ViewHolder {
        ItemSearchVideosBinding binding;

        HashTagVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            if (binding != null) {
                binding.executePendingBindings();
            }
        }

        public void setModel(int position) {
            binding.setModel(mList.get(position));
            binding.tvLikeCount.setText(Global.prettyCount(Integer.parseInt(mList.get(position).getPostLikesCount())));
            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(binding.getRoot().getContext(), PlayerActivity.class);
                intent.putExtra("video_list", new Gson().toJson(mList));
                intent.putExtra("position", position);
                if (isHashTag) {
                    intent.putExtra("type", 2);
                    intent.putExtra("hash_tag", word);
                } else {
                    intent.putExtra("type", 3);
                    intent.putExtra("keyword", word);
                }
                binding.getRoot().getContext().startActivity(intent);
            });
        }

    }
}
