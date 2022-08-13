package com.bakbakum.shortvdo.view.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.FragmentMusicMainBinding;
import com.bakbakum.shortvdo.view.base.BaseFragment;
import com.bakbakum.shortvdo.viewmodel.MusicMainViewModel;
import com.bakbakum.shortvdo.viewmodel.MusicViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

public class MusicMainFragment extends BaseFragment {


    private FragmentMusicMainBinding binding;
    private MusicViewModel viewModel;
    private MusicMainViewModel parentViewModel;


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_main, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
            parentViewModel = ViewModelProviders.of(getActivity()).get(MusicMainViewModel.class);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new MusicViewModel()).createFor()).get(MusicViewModel.class);
        initListener();
        binding.setViewModel(viewModel);
    }

    private void initListener() {
        openFragment(0);
        viewModel.music = parentViewModel.music;
        viewModel.isMore = parentViewModel.isMore;
        viewModel.searchMusicAdapter = parentViewModel.searchMusicAdapter;
        viewModel.stopMusic = parentViewModel.stopMusic;
        binding.tvDiscover.setOnClickListener(v -> openFragment(0));
        binding.tvFavourite.setOnClickListener(v -> openFragment(1));

    }

    private void openFragment(int position) {
        viewModel.selectPosition.set(position);
        parentViewModel.stopMusic.setValue(true);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.frame, MusicChildFragment.newInstance(position))
                .commit();
    }
}