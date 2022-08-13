package com.bakbakum.shortvdo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ItemMusicBinding;
import com.bakbakum.shortvdo.databinding.ItemMusicCategoryBinding;
import com.bakbakum.shortvdo.model.music.Musics;

import java.util.ArrayList;
import java.util.List;

public class MusicsCategoryAdapter extends RecyclerView.Adapter<MusicsCategoryAdapter.MusicViewHolder> {
    public OnItemClickListener onItemClickListener;
    public OnItemMoreClickListener onItemMoreClickListener;
    ArrayList<Musics.Category> mList = new ArrayList<>();

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_category, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        holder.setModel(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void updateData(List<Musics.Category> data) {
        mList = (ArrayList<Musics.Category>) data;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(ItemMusicBinding view, int position, Musics.SoundList musics, int type);
    }

    public interface OnItemMoreClickListener {
        void onMoreClick(ArrayList<Musics.SoundList> lists);
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        private ItemMusicCategoryBinding binding;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setModel(int parentPosition) {

            Musics.Category model = mList.get(parentPosition);
            if (model.getSoundList() != null && !model.getSoundList().isEmpty()) {
                ((MusicsListAdapter) mList.get(parentPosition).getAdapter()).isChild = true;
                ((MusicsListAdapter) mList.get(parentPosition).getAdapter()).updateData(model.getSoundList());
                ((MusicsListAdapter) mList.get(parentPosition).getAdapter()).onMusicClick = onItemClickListener;
            }
            SnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.findSnapView(binding.rvMusics.getLayoutManager());
            snapHelper.attachToRecyclerView(binding.rvMusics);
            binding.setOnMoreClick(v -> onItemMoreClickListener.onMoreClick((ArrayList<Musics.SoundList>) model.getSoundList()));
            binding.setModel(model);
        }
    }
}
