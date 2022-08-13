package com.bakbakum.shortvdo.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.view.home.VideoFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ViewPagerVideosAdapter extends FragmentStateAdapter {

    ArrayList<Video.Data> videos = new ArrayList<>();
    VideoFullAdapter.OnRecyclerViewItemClick onRecyclerViewItemClick;
    public WeakReference<Fragment> currentFragment;
    public boolean loading;
    public boolean finished;
    ArrayList<SimpleExoPlayer> players;
    SimpleExoPlayer player;
    private Context context;
    private Player.EventListener playerListener;

    public void setPlayerListener(Player.EventListener playerListener) {
        this.playerListener = playerListener;
    }

    public ArrayList<Video.Data> getVideos() {
        return videos;
    }

    public void updateData(List<Video.Data> list) {
        videos = (ArrayList<Video.Data>) list;
        //videos.add(null);
        notifyDataSetChanged();
    }

    public void loadMore(List<Video.Data> data) {
        videos.addAll(data);
        //videos.add(null);
        notifyDataSetChanged();
    }

    public void setListener(Player p) {
        if (playerListener != null)
            p.addListener(playerListener);
    }

    public ViewPagerVideosAdapter(@NonNull FragmentActivity fragmentActivity,
                                  VideoFullAdapter.OnRecyclerViewItemClick onRecyclerViewItemClick) {
        super(fragmentActivity);
        this.onRecyclerViewItemClick = onRecyclerViewItemClick;
    }

    public ViewPagerVideosAdapter(@NonNull Fragment fragment,
                                  VideoFullAdapter.OnRecyclerViewItemClick onRecyclerViewItemClick) {
        super(fragment);
        this.onRecyclerViewItemClick = onRecyclerViewItemClick;
        this.context = fragment.getContext();
    }

    @Override
    public Fragment createFragment(int position) {
        return VideoFragment.newInstance(videos.get(position), onRecyclerViewItemClick, this);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(videos.get(position).getPostId());
    }

    public void play() {
        if (currentFragment != null &&
                currentFragment.get() instanceof VideoFragment) {
            ((VideoFragment) currentFragment.get()).play();
        }
    }

    public void pause() {
        if (currentFragment != null &&
                currentFragment.get() instanceof VideoFragment) {
            ((VideoFragment) currentFragment.get()).pause();
        }
    }
}
