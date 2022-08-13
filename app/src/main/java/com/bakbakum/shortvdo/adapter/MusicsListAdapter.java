package com.bakbakum.shortvdo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ItemMusicBinding;
import com.bakbakum.shortvdo.model.music.Musics;
import com.bakbakum.shortvdo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MusicsListAdapter extends RecyclerView.Adapter<MusicsListAdapter.MusicViewHolder> {
    public boolean isChild = false;
    public MusicsCategoryAdapter.OnItemClickListener onMusicClick;
    ArrayList<Musics.SoundList> mList = new ArrayList<>();

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        holder.setModel(position);
    }

    @Override
    public int getItemCount() {
        if (isChild) {
            return Math.min(mList.size(), 9);
        }
        return mList.size();
    }

    public void updateData(List<Musics.SoundList> soundList) {

        mList = (ArrayList<Musics.SoundList>) soundList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mList.remove(mList.get(position));
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mList.size());
    }


    public class MusicViewHolder extends RecyclerView.ViewHolder {
        private ItemMusicBinding binding;
        private SessionManager sessionManager;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            if (binding != null) {
                sessionManager = new SessionManager(binding.getRoot().getContext());
            }
        }

        public void setModel(int position) {
            Musics.SoundList model = mList.get(position);
            if (sessionManager != null && sessionManager.getFavouriteMusic() != null) {
                binding.setIsFav(sessionManager.getFavouriteMusic().contains(model.getSoundId()));
            }
            binding.setModel(model);
            binding.getRoot().setOnClickListener(v -> onMusicClick.onItemClick(binding, position, model, 0));
            binding.icFavourite.setOnClickListener(v -> onMusicClick.onItemClick(binding, position, model, 1));
            binding.btnSelect.setOnClickListener(v -> onMusicClick.onItemClick(binding, position, model, 2));
        }
    }
}
