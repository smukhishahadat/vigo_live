package com.bakbakum.shortvdo.view.search;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityHashtagBinding;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.viewmodel.HashTagViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

public class HashTagActivity extends BaseActivity {

    ActivityHashtagBinding binding;

    HashTagViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_hashtag);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new HashTagViewModel()).createFor()).get(HashTagViewModel.class);

        initView();
        initObserve();
        initListeners();

        binding.setViewmodel(viewModel);

    }

    private void initListeners() {
        binding.refreshlout.setOnLoadMoreListener(refreshLayout -> viewModel.onLoadMore());
        binding.imgBack.setOnClickListener(v -> onBackPressed());
    }

    private void initView() {
        if (getIntent().getStringExtra("hashtag") != null) {
            viewModel.hashtag = getIntent().getStringExtra("hashtag");
            //binding.tvVideoCount.setText(Global.prettyCount(viewModel.explore.getHashTagVideosCountl()) + " Videos");
        }
        binding.refreshlout.setEnableRefresh(false);
        viewModel.adapter.isHashTag = true;
        viewModel.adapter.word = viewModel.hashtag;
        viewModel.fetchHashTagVideos(false);


    }

    private void initObserve() {
        viewModel.onLoadMoreComplete.observe(this, onLoadMore -> binding.refreshlout.finishLoadMore());
        viewModel.video.observe(this, video -> binding.tvVideoCount.setText(Global.prettyCount(video.getPost_count()).concat(" Videos")));
    }
}