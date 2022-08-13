package com.bakbakum.shortvdo.view.home;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.adapter.HomeViewPagerAdapter;
import com.bakbakum.shortvdo.databinding.FragmentMainBinding;
import com.bakbakum.shortvdo.view.base.BaseFragment;
import com.bakbakum.shortvdo.viewmodel.HomeViewModel;
import com.bakbakum.shortvdo.viewmodel.MainViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends BaseFragment {


    private FragmentMainBinding binding;
    private MainViewModel parentViewModel;
    private HomeViewModel viewModel;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            parentViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        }
        viewModel = ViewModelProviders.of(requireActivity(), new ViewModelFactory(new HomeViewModel()).createFor()).get(HomeViewModel.class);
        initView();
        initViewPager();
        initObserve();
        initListener();
    }

    private void initView() {
        binding.swipeRefresh.setProgressViewOffset(true, 0, 250);
        binding.swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.napier_green),
                ContextCompat.getColor(getActivity(), R.color.india_red),
                ContextCompat.getColor(getActivity(), R.color.kellygreen),
                ContextCompat.getColor(getActivity(), R.color.tufs_blue),
                ContextCompat.getColor(getActivity(), R.color.tiffanyblue),
                ContextCompat.getColor(getActivity(), R.color.Sanddtorm),
                ContextCompat.getColor(getActivity(), R.color.salmonpink_1)
        );
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.onRefresh.setValue(true);
        });
        binding.tvFollowing.setTextColor(getResources().getColor(R.color.grey));
    }

    private void initViewPager() {
        HomeViewPagerAdapter homeViewPagerAdapter = new HomeViewPagerAdapter(getChildFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        binding.viewPager.setAdapter(homeViewPagerAdapter);
        binding.viewPager.setCurrentItem(1);
        viewModel.onPageSelect.postValue(1);
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewModel.onPageSelect.postValue(position);
                viewModel.onStop.setValue(true);
                binding.swipeRefresh.setEnabled(position == 1);
                if (position == 0) {
                    ((MainActivity) getActivity()).enableScroll(false);
                    binding.tvForu.setTextColor(getResources().getColor(R.color.grey));
                    binding.tvFollowing.setTextColor(getResources().getColor(R.color.white));
                } else {
                    ((MainActivity) getActivity()).enableScroll(true);
                    binding.tvFollowing.setTextColor(getResources().getColor(R.color.grey));
                    binding.tvForu.setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initObserve() {
        parentViewModel.onStop.observe(getViewLifecycleOwner(), onStop -> {
            viewModel.onPageSelect.setValue(binding.viewPager.getCurrentItem());
            viewModel.onStop.postValue(onStop);
        });
        viewModel.loadingVisibility = parentViewModel.loadingVisibility;
        viewModel.onRefresh.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null && !aBoolean) {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void initListener() {
        binding.tvFollowing.setOnClickListener(v -> {
            binding.tvForu.setTextColor(getResources().getColor(R.color.grey));
            binding.tvFollowing.setTextColor(getResources().getColor(R.color.white));
            binding.viewPager.setCurrentItem(0);
        });
        binding.tvForu.setOnClickListener(v -> {
            binding.tvFollowing.setTextColor(getResources().getColor(R.color.grey));
            binding.tvForu.setTextColor(getResources().getColor(R.color.white));
            binding.viewPager.setCurrentItem(1);
        });
    }

}
