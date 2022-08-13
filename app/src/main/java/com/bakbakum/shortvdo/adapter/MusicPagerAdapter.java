package com.bakbakum.shortvdo.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.bakbakum.shortvdo.view.music.MusicChildFragment;

public class MusicPagerAdapter extends FragmentPagerAdapter {


    public MusicPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return MusicChildFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
