package com.bakbakum.shortvdo.view.profile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.adapter.FollowersPagerAdapter;
import com.bakbakum.shortvdo.databinding.ActivityFollowerFollowingBinding;
import com.bakbakum.shortvdo.model.user.User;
import com.bakbakum.shortvdo.viewmodel.FollowerFollowingViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class FollowerFollowingActivity extends AppCompatActivity {

    ActivityFollowerFollowingBinding binding;
    FollowerFollowingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_follower_following);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new FollowerFollowingViewModel()).createFor()).get(FollowerFollowingViewModel.class);
        initView();
        initListeners();
        binding.setViewmodel(viewModel);
    }

    private void initView() {

        FollowersPagerAdapter adapter = new FollowersPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewModel.itemType.set(getIntent().getIntExtra("itemtype", 0));
        viewModel.user = new Gson().fromJson(getIntent().getStringExtra("user"), User.class);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(viewModel.itemType.get());

    }

    private void initListeners() {
        binding.tvVids.setOnClickListener(v -> binding.viewPager.setCurrentItem(0));
        binding.tvUsers.setOnClickListener(v -> binding.viewPager.setCurrentItem(1));
        binding.imgBack.setOnClickListener(v -> onBackPressed());
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {
                viewModel.itemType.set(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}